package bio.terra.aws.resource.discovery;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.regions.Region;

// Extend EnvironmentDiscoveryTestBase to leverage Environment generation.
public class EnvironmentTest extends EnvironmentDiscoveryTestBase {

  /** Build an ARN that will compare not equal */
  private static Arn junkArn() {
    return Arn.builder().partition("junk").service("junk").resource("junk").build();
  }

  private static Metadata buildJunkMetadata() {
    return Metadata.builder()
        .tenantAlias("junk")
        .organizationId("junk")
        .environmentAlias("junk")
        .accountId("junk")
        .region(Region.AWS_GLOBAL)
        .majorVersion("junk")
        .tagMap(Map.of())
        .build();
  }

  @Test
  public void equality() {
    // Self equality
    Environment environment = getExpectedEnvironment();
    Assertions.assertEquals(environment, environment);

    // Deep copy
    Environment.Builder builder =
        Environment.builder()
            .metadata(environment.getMetadata())
            .notebookRoleArn(environment.getNotebookRoleArn())
            .userRoleArn(environment.getUserRoleArn())
            .workspaceManagerRoleArn(environment.getWorkspaceManagerRoleArn());

    for (Region region : Region.regions()) {
      environment
          .getLandingZone(region)
          .ifPresent(
              landingZone -> {
                builder.addLandingZone(region, landingZone);
              });
    }

    // Test equals()/hashCode()
    Environment builtEnvironment = builder.build();
    Assertions.assertEquals(environment, builtEnvironment);
    Assertions.assertEquals(environment.hashCode(), builtEnvironment.hashCode());
  }

  private static void checkInequality(Environment l, Environment r) {
    // Check equals()/hashCode()
    Assertions.assertNotEquals(l, r);
    Assertions.assertNotEquals(l.hashCode(), r.hashCode());
  }

  @Test
  public void inequality() {
    Environment environment = getExpectedEnvironment();

    // Incompatible types
    String string = "string";
    Assertions.assertNotEquals((Object) environment, (Object) string);

    // Different Metadata
    checkInequality(
        environment,
        Environment.builder()
            .metadata(buildJunkMetadata())
            .notebookRoleArn(environment.getNotebookRoleArn())
            .userRoleArn(environment.getUserRoleArn())
            .workspaceManagerRoleArn(environment.getWorkspaceManagerRoleArn())
            .build());

    // Different Notebook Role Arn
    checkInequality(
        environment,
        Environment.builder()
            .metadata(environment.getMetadata())
            .notebookRoleArn(junkArn())
            .userRoleArn(environment.getUserRoleArn())
            .workspaceManagerRoleArn(environment.getWorkspaceManagerRoleArn())
            .build());

    // Different User Role Arn
    checkInequality(
        environment,
        Environment.builder()
            .metadata(environment.getMetadata())
            .notebookRoleArn(environment.getNotebookRoleArn())
            .userRoleArn(junkArn())
            .workspaceManagerRoleArn(environment.getWorkspaceManagerRoleArn())
            .build());

    // Different WSM Role Arn
    checkInequality(
        environment,
        Environment.builder()
            .metadata(environment.getMetadata())
            .notebookRoleArn(environment.getNotebookRoleArn())
            .userRoleArn(environment.getUserRoleArn())
            .workspaceManagerRoleArn(junkArn())
            .build());

    // Landing Zones not Equal
    checkInequality(
        environment,
        Environment.builder()
            .metadata(environment.getMetadata())
            .notebookRoleArn(environment.getNotebookRoleArn())
            .userRoleArn(environment.getUserRoleArn())
            .workspaceManagerRoleArn(environment.getWorkspaceManagerRoleArn())
            .build());
  }
}
