package bio.terra.aws.resource.discovery;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.findify.s3mock.S3Mock;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jdt.launching.SocketUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3EnvironmentDiscoveryTest extends EnvironmentDiscoveryTestBase {

  private final S3Mock s3Mock;
  private final S3Client s3Client;

  public S3EnvironmentDiscoveryTest() throws URISyntaxException {

    int s3MockPort = SocketUtil.findFreePort();
    assertNotEquals(-1, s3MockPort);

    s3Mock =
        new S3Mock.Builder().withFileBackend(getBasePath().toString()).withPort(s3MockPort).build();

    URI uri = new URIBuilder().setScheme("http").setHost("localhost").setPort(s3MockPort).build();

    s3Client =
        S3Client.builder()
            .region(Region.AWS_GLOBAL)
            .forcePathStyle(true)
            .endpointOverride(uri)
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .build();
  }

  @BeforeEach
  public void setUp() {
    s3Mock.start();
  }

  @AfterEach
  public void tearDown() {
    s3Mock.stop();
  }

  @Test
  public void validation() throws IOException {
    EnvironmentDiscovery discovery =
        new S3EnvironmentDiscovery(getValidationTestDataBucketName(), s3Client);
    validationTestLogic(discovery);
  }

  @Test
  public void doesNotExist() throws IOException {

    // Bucket does not exist... this should throw NoSuchElementException at ctor time.

    Assertions.assertThrows(
        NoSuchElementException.class,
        () -> {
          new S3EnvironmentDiscovery(getDoesNotExistTestDataBucketName(), s3Client);
        });
  }

  @Test
  public void noLandingZones() throws IOException {
    EnvironmentDiscovery discovery =
        new S3EnvironmentDiscovery(getNoLandingZonesTestDataBucketName(), s3Client);
    noLandingZonesTestLogic(discovery);
  }

  @Test
  public void missingEnvironmentConfig() throws IOException {
    EnvironmentDiscovery discovery =
        new S3EnvironmentDiscovery(getMissingEnvironmentConfigTestDataBucketName(), s3Client);
    missingEnvironmentConfigTestLogic(discovery);
  }

  @Test
  public void notebookLifecycleMismatch() throws IOException {
    EnvironmentDiscovery discovery =
        new S3EnvironmentDiscovery(getNotebookLifecycleMismatchTestDataBucketName(), s3Client);
    notebookLifecycleMismatchTestLogic(discovery);
  }

  @Test
  public void addFieldBeforeSchemaUpdate() throws IOException {
    EnvironmentDiscovery discovery =
        new S3EnvironmentDiscovery(getAddFieldBeforeSchemaUpdateTestDataBucketName(), s3Client);
    addFieldBeforeSchemaUpdateTestLogic(discovery);
  }

  @Test
  public void appsDisabled() throws IOException {
    EnvironmentDiscovery discovery =
        new S3EnvironmentDiscovery(getAppsDisabledTestDataBucketName(), s3Client);
    appsDisabledTestLogic(discovery);
  }

  @Test
  public void v0_5Backward() throws IOException {
    EnvironmentDiscovery discovery =
        new S3EnvironmentDiscovery(getV0_5BackwardTestDataBucketName(), s3Client);
    v0_5BackwardTestLogic(discovery);
  }
}
