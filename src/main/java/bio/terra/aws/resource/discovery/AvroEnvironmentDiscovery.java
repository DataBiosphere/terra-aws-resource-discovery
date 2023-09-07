package bio.terra.aws.resource.discovery;

import bio.terra.aws.resource.discovery.avro.EnvironmentMetadataModel;
import bio.terra.aws.resource.discovery.avro.EnvironmentModel;
import bio.terra.aws.resource.discovery.avro.LandingZoneMetadataModel;
import bio.terra.aws.resource.discovery.avro.LandingZoneModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.regions.Region;

/**
 * Abstract class used to discover AWS Support Resources in a single Terra AWS Environment and its
 * associated Landing Zones.
 *
 * <p>This class is responsible for parsing {@link AvroConfiguration} files/S3 objects into Java
 * objects representing AWS Support Resources, usable by other components such as the Terra
 * Workspace Manager service.
 *
 * <p>This base class implements the reusable logic for parsing Avro records into generated model
 * classes using the Avro SDK, and marshalling these (private) model class instances into the
 * library's public object model; subclasses will be responsible for discovering the Avro records
 * stored hierarchically in different storage media types.
 */
abstract class AvroEnvironmentDiscovery implements EnvironmentDiscovery {

  public static final Integer SCHEMA_MAJOR_VERSION = 0;

  /**
   * Object mapper used for JSON parsing of {@link AvroConfiguration} objects from data records
   * stored in files or S3 objects.
   */
  private final ObjectMapper mapper;

  protected AvroEnvironmentDiscovery() {
    mapper = new ObjectMapper();
  }

  /**
   * Subclasses extending {@link AvroEnvironmentDiscovery} must implement this method to discover
   * Environment configurations in storage media (such as a filesystem folder hierarchy or an S3
   * prefix hierarchy).
   *
   * @param mapper Jackson ObjectMapper used to map stored JSON into {@link AvroConfiguration} class
   *     instances.
   * @return An {@link AvroConfiguration} object describing the Global Support Resources in the
   *     Terra Environment.
   * @throws IOException IOException
   */
  protected abstract AvroConfiguration getEnvironmentConfiguration(ObjectMapper mapper)
      throws IOException;

  /**
   * Subclasses extending {@link AvroEnvironmentDiscovery} must implement this method to discover
   * Landing Zone configurations in storage media (such as a filesystem folder hierarchy or an S3
   * prefix hierarchy).
   *
   * @param mapper Jackson ObjectMapper used to map stored JSON into {@link AvroConfiguration} class
   *     instances.
   * @return A populated {@link Optional<AvroConfiguration>} object describing Regional Support
   *     Resources if a Terra Landing Zone exists in the Environment for the passed AWS Region, an
   *     empty {@link Optional<AvroConfiguration>} otherwise.
   * @throws IOException IOException
   */
  protected abstract Map<Region, AvroConfiguration> getLandingZoneConfigurations(
      ObjectMapper mapper) throws IOException;

  @Override
  public Environment discoverEnvironment() throws IOException {

    // Call into subclassed getEnvironmentConfiguration() method to get a parsed Avro configuration
    // record describing the Terra AWS Environment's Global Support Resources.

    AvroConfiguration environmentConfiguration = getEnvironmentConfiguration(mapper);

    // Parse the Avro configuration record into generated deserialization object model Java class.

    EnvironmentModel environmentModel =
        parseModel(environmentConfiguration, EnvironmentModel.getClassSchema());

    // Start building the public Environment class to return to the caller with discovered Global
    // Support Resources.

    Environment.Builder environmentBuilder =
        Environment.builder()
            .applicationInstanceProfileName(environmentModel.getAppInstanceProfileName())
            .metadata(createMetadataFromEnvironmentModel(environmentModel))
            .workspaceManagerRoleArn(
                Arn.fromString(environmentModel.getRoleArnTerraWorkspaceManager()))
            .userRoleArn(Arn.fromString(environmentModel.getRoleArnTerraUser()))
            .notebookRoleArn(Arn.fromString(environmentModel.getRoleArnTerraNotebook()));

    // Call into subclassed getLandingZoneConfigurations() method to get parsed Avro configuration
    // records describing the Terra AWS Landing Zone Regional Support Resources for all supported
    // AWS Regions.
    Map<Region, AvroConfiguration> landingZoneConfigurations = getLandingZoneConfigurations(mapper);

    // Now iterate over every AWS region to check for configured Landing Zones.

    for (Map.Entry<Region, AvroConfiguration> entry : landingZoneConfigurations.entrySet()) {

      // Parse the Avro configuration record into generated deserialization object model Java class.

      LandingZoneModel landingZoneModel =
          parseModel(entry.getValue(), LandingZoneModel.getClassSchema());

      // Building a public LandingZone class to return to the caller with discovered Regional
      // Support Resources.

      LandingZone.Builder landingZoneBuilder =
          LandingZone.builder()
              .applicationVpcId(landingZoneModel.getAppFrameworkVpcId())
              .applicationVpcPrivateSubnetId(landingZoneModel.getAppFrameworkPrivateSubnetId())
              .metadata(createMetadataFromELandingZoneModel(landingZoneModel))
              .storageBucket(
                  Arn.fromString(landingZoneModel.getBucketArn()), landingZoneModel.getBucketId())
              .kmsKey(
                  Arn.fromString(landingZoneModel.getKmsKeyArn()),
                  UUID.fromString(landingZoneModel.getKmsKeyId()));

      List<String> notebookLifecycleConfigArns =
          landingZoneModel.getNotebookLifecycleConfigurationArns();
      List<String> notebookLifecycleConfigNames =
          landingZoneModel.getNotebookLifecycleConfigurationNames();

      if (notebookLifecycleConfigArns.size() != notebookLifecycleConfigNames.size()) {
        throw new InputMismatchException(
            "Mismatch between lifecycle configuration ARN and name counts.");
      }

      for (int i = 0; i < notebookLifecycleConfigArns.size(); i++) {
        landingZoneBuilder.addNotebookLifecycleConfiguration(
            Arn.fromString(landingZoneModel.getNotebookLifecycleConfigurationArns().get(i)),
            landingZoneModel.getNotebookLifecycleConfigurationNames().get(i));
      }

      // Build the LandingZone and push it into the in-flight Environment builder mapped to the
      // current AWS region.
      environmentBuilder.addLandingZone(entry.getKey(), landingZoneBuilder.build());
    }

    // Now that all the LandingZones have been discovered and added to the Environment builder,
    // build
    // the Environment instance and return to the caller.
    return environmentBuilder.build();
  }

  /**
   * Private helper method to parse the three pieces of data required to marshal an Avro record into
   * a Java object. The required data are:
   *
   * <ul>
   *   <li>Reader schema (matches schema in src/main/avro used for Java code generation)
   *   <li>Writer schema (version of schema used to write the record data)
   *   <li>Record data
   * </ul>
   *
   * @param configuration Parsed configuration; provides the configuration data and the writer
   *     schema used to write it.
   * @param readerSchema Provides the reader schema used for Java code generation.
   * @return The marshalled Java object of type T
   * @param <T> Type of generated Java object to create from passed Avro record data and schemas.
   * @throws IOException IOException
   */
  private static <T> T parseModel(AvroConfiguration configuration, Schema readerSchema)
      throws IOException {

    // First parse the schema that the data was written with and create a JSON decoder to parse it
    // with.
    Schema writerSchema = new Schema.Parser().parse(configuration.schema());
    Decoder decoder = DecoderFactory.get().jsonDecoder(writerSchema, configuration.payload());

    // Now create an Avro DatumReader, which will validate that the writer schema is compatible with
    // the reader schema, and use the schema to marshal the data into the Java type.
    DatumReader<T> reader = new SpecificDatumReader<>(writerSchema, readerSchema);
    return reader.read(null, decoder);
  }

  /** Private helper to create a {@link Metadata} from an Avro {@link EnvironmentModel} */
  private Metadata createMetadataFromEnvironmentModel(EnvironmentModel environmentModel) {
    EnvironmentMetadataModel metadataModel = environmentModel.getMetadata();
    return Metadata.builder()
        .tenantAlias(metadataModel.getTenantAlias())
        .organizationId(metadataModel.getOrganizationId())
        .environmentAlias(metadataModel.getEnvironmentAlias())
        .accountId(metadataModel.getAccountId())
        .region(Region.of(metadataModel.getRegion()))
        .majorVersion(metadataModel.getMajorVersion())
        .tagMap(metadataModel.getTags())
        .build();
  }

  /** Private helper to create a {@link Metadata} from an Avro {@link LandingZoneModel} */
  private Metadata createMetadataFromELandingZoneModel(LandingZoneModel landingZoneModel) {
    LandingZoneMetadataModel metadataModel = landingZoneModel.getMetadata();
    return Metadata.builder()
        .tenantAlias(metadataModel.getTenantAlias())
        .organizationId(metadataModel.getOrganizationId())
        .environmentAlias(metadataModel.getEnvironmentAlias())
        .accountId(metadataModel.getAccountId())
        .region(Region.of(metadataModel.getRegion()))
        .majorVersion(metadataModel.getMajorVersion())
        .tagMap(metadataModel.getTags())
        .build();
  }
}
