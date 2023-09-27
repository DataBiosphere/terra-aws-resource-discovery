package bio.terra.aws.resource.discovery;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilesystemEnvironmentDiscoveryTest extends EnvironmentDiscoveryTestBase {

  @Test
  public void validation() throws IOException {
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getValidationTestDataPath());
    validationTestLogic(discovery);
  }

  @Test
  public void doesNotExist() throws IOException {
    // Base directory does not exist... this should throw NoSuchElementException at ctor time.
    Assertions.assertThrows(
        NoSuchElementException.class,
        () -> {
          new FilesystemEnvironmentDiscovery(getDoesNotExistTestDataPath());
        });
  }

  @Test
  public void noLandingZones() throws IOException {
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getNoLandingZonesTestDataPath());
    noLandingZonesTestLogic(discovery);
  }

  @Test
  public void missingEnvironmentConfig() throws IOException {
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getMissingEnvironmentConfigTestDataPath());
    missingEnvironmentConfigTestLogic(discovery);
  }

  @Test
  public void notebookLifecycleMismatch() throws IOException {
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getNotebookLifecycleMismatchTestDataPath());
    notebookLifecycleMismatchTestLogic(discovery);
  }

  @Test
  public void addFieldBeforeSchemaUpdate() throws IOException {
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getAddFieldBeforeSchemaUpdateTestDataPath());
    addFieldBeforeSchemaUpdateTestLogic(discovery);
  }

  @Test
  public void appsDisabled() throws IOException {
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getAppsDisabledTestDataPath());
    appsDisabledTTestLogic(discovery);
  }

  @Test
  public void v0_5Backward() throws IOException {
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getV0_5BackwardTestDataPath());
    v0_5BackwardTestLogic(discovery);
  }
}
