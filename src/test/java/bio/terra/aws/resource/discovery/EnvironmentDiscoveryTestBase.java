package bio.terra.aws.resource.discovery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.regions.Region;

/**
 * Please see the "Static Test Data" section of this repo's <a
 * href="https://github.com/DataBiosphere/terra-aws-resource-discovery/blob/main/README.md##static-test-data">README
 * file</a> for info on test files used in this class.
 */
public class EnvironmentDiscoveryTestBase {

  private static final String TEST_DATA_RESOURCE_PATH = "test_discovery_data";

  private static final String METADATA_ACCOUNT_ID = "111111111111";
  private static final Map<String, String> METADATA_TAGS = Map.of("Version", "v0");

  private static final String NOTEBOOK_ROLE_ARN =
      "arn:aws:iam::111111111111:role/develwest-TerraNotebookExecution";
  private static final String USER_ROLE_ARN = "arn:aws:iam::111111111111:role/develwest-TerraUser";
  private static final String WSM_ROLE_ARN =
      "arn:aws:iam::111111111111:role/develwest-TerraNWorkspaceManager";

  private static final String US_EAST_BUCKET_ARN = "arn:aws:s3:::develeastverily-terra-workspace";
  private static final String US_EAST_BUCKET_NAME = "develeastverily-terra-workspace";
  private static final String US_EAST_KMS_KEY_ARN =
      "arn:aws:kms:us-east-1:111111111111:key/538feabb-eba0-4696-b485-caddc2bb5344";
  private static final String US_EAST_KMS_KEY_ID = "538feabb-eba0-4696-b485-caddc2bb5344";
  private static final String US_EAST_NOTEBOOK_LIFECYCLE_ARN =
      "arn:aws:sagemaker:us-east-1:111111111111:notebook-instance-lifecycle-config/develeastterranotebooklifecycleconfigv1";
  private static final String US_EAST_NOTEBOOK_LIFECYCLE_NAME =
      "develeastTerraNotebookLifecycleConfigV1";

  private static final String US_WEST_BUCKET_ARN = "arn:aws:s3:::develwestverily-terra-workspace";
  private static final String US_WEST_BUCKET_NAME = "develwestverily-terra-workspace";
  private static final String US_WEST_KMS_KEY_ARN =
      "arn:aws:kms:us-west-1:111111111111:key/60cad918-8212-41cf-8c1e-22fea5661b42";
  private static final String US_WEST_KMS_KEY_ID = "60cad918-8212-41cf-8c1e-22fea5661b42";
  private static final String US_WEST_NOTEBOOK_LIFECYCLE_ARN =
      "arn:aws:sagemaker:us-west-1:111111111111:notebook-instance-lifecycle-config/develwestterranotebooklifecycleconfigv1";
  private static final String US_WEST_NOTEBOOK_LIFECYCLE_NAME =
      "develwestTerraNotebookLifecycleConfigV1";

  private final Path basePath;
  private final Environment expectedEnvironment;

  public EnvironmentDiscoveryTestBase() {

    this.basePath =
        Path.of(getClass().getClassLoader().getResource(TEST_DATA_RESOURCE_PATH).getPath());

    Assertions.assertTrue(
        Files.exists(basePath), String.format("Test data path '%s' does not exist.", basePath));

    expectedEnvironment =
        Environment.builder()
            .metadata(new Metadata(METADATA_ACCOUNT_ID, Region.US_EAST_1, METADATA_TAGS))
            .notebookRoleArn(Arn.fromString(NOTEBOOK_ROLE_ARN))
            .workspaceManagerRoleArn(Arn.fromString(WSM_ROLE_ARN))
            .userRoleArn(Arn.fromString(USER_ROLE_ARN))
            .addLandingZone(
                Region.US_EAST_1,
                LandingZone.builder()
                    .metadata(new Metadata(METADATA_ACCOUNT_ID, Region.US_EAST_1, METADATA_TAGS))
                    .storageBucket(Arn.fromString(US_EAST_BUCKET_ARN), US_EAST_BUCKET_NAME)
                    .kmsKey(
                        Arn.fromString(US_EAST_KMS_KEY_ARN), UUID.fromString(US_EAST_KMS_KEY_ID))
                    .addNotebookLifecycleConfiguration(
                        Arn.fromString(US_EAST_NOTEBOOK_LIFECYCLE_ARN),
                        US_EAST_NOTEBOOK_LIFECYCLE_NAME)
                    .build())
            .addLandingZone(
                Region.US_WEST_1,
                LandingZone.builder()
                    .metadata(new Metadata(METADATA_ACCOUNT_ID, Region.US_WEST_1, METADATA_TAGS))
                    .storageBucket(Arn.fromString(US_WEST_BUCKET_ARN), US_WEST_BUCKET_NAME)
                    .kmsKey(
                        Arn.fromString(US_WEST_KMS_KEY_ARN), UUID.fromString(US_WEST_KMS_KEY_ID))
                    .addNotebookLifecycleConfiguration(
                        Arn.fromString(US_WEST_NOTEBOOK_LIFECYCLE_ARN),
                        US_WEST_NOTEBOOK_LIFECYCLE_NAME)
                    .build())
            .build();
  }

  public Environment getExpectedEnvironment() {
    return expectedEnvironment;
  }

  public Path getBasePath() {
    return basePath;
  }

  public String getValidationTestDataBucketName() {
    return "validation";
  }

  public Path getValidationTestDataPath() {
    return basePath.resolve(getValidationTestDataBucketName());
  }

  /**
   * Test data contains a known good Environment with two Landing Zones which match the
   * expectedEnvironment configuration manually created at ctor time. Test ensures that the parsed
   * configuration results in an equivalent object.
   *
   * <p>Note that there are files/directories other than the two legitimate landing zone
   * configuration files ('us-east-1/config.json' and 'us-west-1/config.json'), which test various
   * corner cases in path parsing.
   */
  public void validationTestLogic(EnvironmentDiscovery environmentDiscovery) throws IOException {
    Environment environment = environmentDiscovery.discoverEnvironment();
    Assertions.assertEquals(expectedEnvironment, environment);

    Set<Region> regionSet = environment.getSupportedRegions();

    for (Region region : Region.regions()) {
      if (region.equals(Region.US_EAST_1) || region.equals(Region.US_WEST_1)) {
        Assertions.assertTrue(regionSet.contains(region));
        Optional<LandingZone> expectedLandingZone = environment.getLandingZone(region);
        Optional<LandingZone> landingZone = environment.getLandingZone(region);
        Assertions.assertFalse(expectedLandingZone.isEmpty());
        Assertions.assertFalse(landingZone.isEmpty());
        Assertions.assertEquals(expectedLandingZone.get(), landingZone.get());
      } else {
        Assertions.assertFalse(regionSet.contains(region));
        Assertions.assertTrue(expectedEnvironment.getLandingZone(region).isEmpty());
        Assertions.assertTrue(environment.getLandingZone(region).isEmpty());
      }
    }
  }

  public String getDoesNotExistTestDataBucketName() {
    return "does_not_exist";
  }

  public Path getDoesNotExistTestDataPath() {
    return basePath.resolve(getDoesNotExistTestDataBucketName());
  }

  // Non-existent bucket/dir throws NoSuchElementException at subclass ctor time, test logic must be
  // implemented in subclass test.

  public String getNoLandingZonesTestDataBucketName() {
    return "no_landing_zones";
  }

  public Path getNoLandingZonesTestDataPath() {
    return basePath.resolve(getNoLandingZonesTestDataBucketName());
  }

  /** Test data contains a valid Environment configuration, but no Landing Zones. */
  public void noLandingZonesTestLogic(EnvironmentDiscovery environmentDiscovery)
      throws IOException {
    Environment environment = environmentDiscovery.discoverEnvironment();

    // Global Support Resources should match
    Assertions.assertEquals(
        expectedEnvironment.getNotebookRoleArn(), environment.getNotebookRoleArn());
    Assertions.assertEquals(expectedEnvironment.getUserRoleArn(), environment.getUserRoleArn());
    Assertions.assertEquals(
        expectedEnvironment.getWorkspaceManagerRoleArn(), environment.getWorkspaceManagerRoleArn());

    // Make sure there are no LZ's
    for (Region region : Region.regions()) {
      Assertions.assertTrue(environment.getLandingZone(region).isEmpty());
    }
  }

  public String getMissingEnvironmentConfigTestDataBucketName() {
    return "missing_test_data";
  }

  public Path getMissingEnvironmentConfigTestDataPath() {
    return basePath.resolve(getMissingEnvironmentConfigTestDataBucketName());
  }

  /**
   * Test data contains no Environment configuration; this should be detected as an error and raised
   * as a NoSuchElementException.
   */
  public void missingEnvironmentConfigTestLogic(EnvironmentDiscovery environmentDiscovery) {

    // Test data contains no Environment configuration; this should be detected as an error and
    // raised as a NoSuchElementException.

    Assertions.assertThrows(
        NoSuchElementException.class,
        () -> {
          environmentDiscovery.discoverEnvironment();
        });
  }

  public String getNotebookLifecycleMismatchTestDataBucketName() {
    return "notebook_lifecycle_mismatch";
  }

  public Path getNotebookLifecycleMismatchTestDataPath() {
    return basePath.resolve(getNotebookLifecycleMismatchTestDataBucketName());
  }

  /**
   * Test data contains a Landing Zone where the number of notebook lifecycle configuration ARN's
   * does not match the number of notebook lifecycle names; this should be detected as an error and
   * raised as an InputMismatchException.
   */
  public void notebookLifecycleMismatchTestLogic(EnvironmentDiscovery environmentDiscovery)
      throws IOException {
    Assertions.assertThrows(
        InputMismatchException.class,
        () -> {
          environmentDiscovery.discoverEnvironment();
        });
  }
}
