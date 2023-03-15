package bio.terra.aws.resource.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import software.amazon.awssdk.regions.Region;

/**
 * Discovers a Single Terra Environment represented by a file system directory with the following
 * layout:
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
 * us-east-1}; any number of AWS regions could be provided under the {@code landingzone} directory.
 */
public class FilesystemEnvironmentDiscovery extends AvroEnvironmentDiscovery {

  private static final String ENVIRONMENT_SUBDIRECTORY_NAME = "environment";
  private static final String LANDING_ZONE_SUBDIRECTORY_NAME = "landingzones";
  private static final String CONFIGURATION_FILE_NAME = "config.json";

  private final Path basePath;

  /**
   * Construct a {@link FilesystemEnvironmentDiscovery} class from a local file system directory
   *
   * @param basePath the base path of a file system directory that contains the configuration
   *     corresponding to a single Terra AWS Environment, which contains Avro configuration files
   *     matching the layout described in the {@link FilesystemEnvironmentDiscovery} class
   *     documentation.
   */
  public FilesystemEnvironmentDiscovery(Path basePath) {
    if (!Files.exists(basePath)) {
      throw new NoSuchElementException(String.format("Base path '%s' does not exist!", basePath));
    }
    this.basePath = basePath;
  }

  private AvroConfiguration readIntoConfiguration(Path path, ObjectMapper mapper)
      throws IOException {
    return mapper.readValue(Files.readString(path), AvroConfiguration.class);
  }

  private Path getVersionSubdirectoryPath() {
    return basePath.resolve(String.format("v%d", SCHEMA_MAJOR_VERSION));
  }

  private Path getEnvironmentSubdirectoryPath() {
    return getVersionSubdirectoryPath().resolve(ENVIRONMENT_SUBDIRECTORY_NAME);
  }

  private Path getEnvironmentConfigurationFilePath() {
    return getEnvironmentSubdirectoryPath().resolve(CONFIGURATION_FILE_NAME);
  }

  private Path getLandingZoneSubdirectoryPath() {
    return getVersionSubdirectoryPath().resolve(LANDING_ZONE_SUBDIRECTORY_NAME);
  }

  @Override
  protected AvroConfiguration getEnvironmentConfiguration(ObjectMapper mapper) throws IOException {
    Path environmentConfigurationFilePath = getEnvironmentConfigurationFilePath();

    if (Files.notExists(environmentConfigurationFilePath)) {
      throw new NoSuchElementException(
          String.format(
              "Environment configuration file '%s' does not exist",
              environmentConfigurationFilePath));
    }

    return readIntoConfiguration(environmentConfigurationFilePath, mapper);
  }

  @Override
  protected Map<Region, AvroConfiguration> getLandingZoneConfigurations(ObjectMapper mapper)
      throws IOException {
    HashMap<Region, AvroConfiguration> retVal = new HashMap<>();

    File landingZoneBaseDirectory = getLandingZoneSubdirectoryPath().toFile();

    File[] regionFiles = landingZoneBaseDirectory.listFiles();

    // File.listFiles() returns NULL if the file does not exist OR is not a directory.
    if (regionFiles == null) {
      return retVal;
    }

    for (File regionDirectory : regionFiles) {
      if (!regionDirectory.isDirectory()) {
        continue;
      }

      Region currentRegion = Region.of(regionDirectory.getName());
      if (!Region.regions().contains(currentRegion)) {
        continue;
      }

      Path landingZoneConfigurationFilePath =
          regionDirectory.toPath().resolve(CONFIGURATION_FILE_NAME);

      if (Files.exists(landingZoneConfigurationFilePath)) {
        retVal.put(currentRegion, readIntoConfiguration(landingZoneConfigurationFilePath, mapper));
      }
    }

    return retVal;
  }
}
