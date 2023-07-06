package bio.terra.aws.resource.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Discovers a Single Terra Environment represented by an AWS S3 Bucket with the following layout:
 *
 * <pre>{@code
 * vM
 * ├── vM/environment
 * │   ├── vM/environment/config.json
 * └── vM/landingzones
 *     ├── vM/landingzones/eu-central-1
 *     │   └── vM/landingzones/eu-central-1/config.json
 *     └── vM/landingzones/us-east-1
 *         └── vM/landingzones/us-east-1/config.json
 * }</pre>
 *
 * <p>Where the {@code M} in {@code vM/} corresponds to the current major version supported by the
 * library ({@link AvroEnvironmentDiscovery#SCHEMA_MAJOR_VERSION}).
 *
 * <p>This example supports Landing Zones in two AWS regions: {@code eu-central-1} and {@code
 * us-east-1}; any number of AWS regions could be provided under the {@code landingzone} folder.
 */
public class S3EnvironmentDiscovery extends AvroEnvironmentDiscovery {

  private static final String ENVIRONMENT_FOLDER_NAME = "environment";
  private static final String LANDING_ZONE_FOLDER_NAME = "landingzones";
  private static final String CONFIGURATION_OBJECT_KEY = "config.json";
  private static final String REGION_REGEX_CAPTURE = "([a-z0-9-]*)";

  private final S3Client s3Client;
  private final String bucketName;
  private final Pattern regexPattern;

  /**
   * Construct a {@link S3EnvironmentDiscovery} class from an S3 Bucket
   *
   * @param bucketName the name of the AWS S3 bucket that contains the configuration corresponding
   *     to a single Terra AWS Environment, which contains Avro configuration objects matching the
   *     layout described in the {@link S3EnvironmentDiscovery} class documentation.
   * @param s3Client an {@link S3Client} instance credentialed to read {@param bucketName}
   */
  public S3EnvironmentDiscovery(String bucketName, S3Client s3Client) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
    regexPattern = Pattern.compile(getLandingZoneConfigurationObjectKeyRegex());

    HeadBucketRequest request = HeadBucketRequest.builder().bucket(bucketName).build();
    try {
      s3Client.headBucket(request);
    } catch (NoSuchBucketException exception) {
      throw new NoSuchElementException(String.format("Bucket '%s' does not exist.", bucketName));
    }
  }

  private static String getVersionPrefix() {
    return String.format("v%d", SCHEMA_MAJOR_VERSION);
  }

  private static String getEnvironmentConfigurationObjectKey() {
    return String.join("/", getVersionPrefix(), ENVIRONMENT_FOLDER_NAME, CONFIGURATION_OBJECT_KEY);
  }

  private static String getLandingZoneBasePrefix() {
    return String.join("/", getVersionPrefix(), LANDING_ZONE_FOLDER_NAME, "");
  }

  private static String getLandingZoneConfigurationObjectKeyRegex() {
    return String.join(
        "\\/",
        getVersionPrefix(),
        LANDING_ZONE_FOLDER_NAME,
        REGION_REGEX_CAPTURE,
        CONFIGURATION_OBJECT_KEY);
  }

  /**
   * Check whether an Object key matches the regex for a Landing Zone configuration object,
   * validating that the AWS region component of the path is a valid AWS region.
   *
   * @param key object key to parse
   * @return a populated {@link Optional<Region>} if the passed key matches the configuration object
   *     regex key, AND the region component is known valid AWS region, otherwise an empty {@link
   *     Optional<Region>}
   */
  private Optional<Region> regionFromObjectKey(String key) {
    Matcher matcher = regexPattern.matcher(key);
    if (matcher.find()) {
      Region region = Region.of(matcher.group(matcher.groupCount()));

      // Regions are not validated in Region.of() ... only return a Region if the parsed region is a
      // known-good region.

      if (Region.regions().contains(region)) {
        return Optional.of(region);
      }
    }

    return Optional.empty();
  }

  private AvroConfiguration readIntoConfiguration(String key, ObjectMapper mapper)
      throws IOException {

    try {
      GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(key).build();

      ResponseBytes<GetObjectResponse> response =
          s3Client.getObject(request, ResponseTransformer.toBytes());

      return mapper.readValue(response.asString(StandardCharsets.UTF_8), AvroConfiguration.class);
    } catch (NoSuchKeyException exception) {
      throw new NoSuchElementException(
          String.format("Object with key '%s' not found in bucket '%s'.", key, bucketName));
    }
  }

  @Override
  protected AvroConfiguration getEnvironmentConfiguration(ObjectMapper mapper) throws IOException {
    return readIntoConfiguration(getEnvironmentConfigurationObjectKey(), mapper);
  }

  @Override
  protected Map<Region, AvroConfiguration> getLandingZoneConfigurations(ObjectMapper mapper)
      throws IOException {

    // List objects under the Landing Zone prefix.

    HashMap<Region, AvroConfiguration> retVal = new HashMap<>();

    ListObjectsV2Request request =
        ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(getLandingZoneBasePrefix())
            .build();

    ListObjectsV2Response response = s3Client.listObjectsV2(request);

    // Iterate over discovered S3 objects, looking for keys that the LandingZone config object
    // regex, and parsing the configurations into the return value map.

    for (S3Object s3Object : response.contents()) {
      String objectKey = s3Object.key();
      Optional<Region> region = regionFromObjectKey(objectKey);
      if (region.isPresent()) {
        retVal.put(region.get(), readIntoConfiguration(objectKey, mapper));
      }
    }

    return retVal;
  }
}
