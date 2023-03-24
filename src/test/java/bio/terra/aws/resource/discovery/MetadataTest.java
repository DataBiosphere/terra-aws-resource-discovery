package bio.terra.aws.resource.discovery;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

// Extend EnvironmentDiscoveryTestBase to leverage Environment generation.
public class MetadataTest extends EnvironmentDiscoveryTestBase {

  @Test
  public void equality() {
    Metadata metadata = getExpectedEnvironment().getMetadata();

    // Self equality
    Assertions.assertEquals(metadata, metadata);

    // Deep copy
    Metadata metadataClone = new Metadata(metadata);
    Assertions.assertEquals(metadata, metadataClone);
    Assertions.assertEquals(metadata.hashCode(), metadataClone.hashCode());
  }

  private static void checkInequality(Metadata l, Metadata r) {
    // Check equals()/hashCode()
    Assertions.assertNotEquals(l, r);
    Assertions.assertNotEquals(l.hashCode(), r.hashCode());
  }

  @Test
  public void inequality() {
    Metadata metadata = getExpectedEnvironment().getMetadata();

    // Incompatible types
    String string = "string";
    Assertions.assertNotEquals((Object) metadata, (Object) string);

    // Null
    Assertions.assertNotEquals(metadata, null);

    String tenantAlias = metadata.getTenantAlias();
    String organizationId = metadata.getOrganizationId();
    String environmentAlias = metadata.getEnvironmentAlias();
    String accountId = metadata.getAccountId();
    Region region = metadata.getRegion();
    String majorVersion = metadata.getMajorVersion();
    Map<String, String> tagMap = metadata.getTagMap();

    checkInequality(
        metadata,
        Metadata.builder()
            .tenantAlias("junk")
            .organizationId(organizationId)
            .environmentAlias(environmentAlias)
            .accountId(accountId)
            .region(region)
            .majorVersion(majorVersion)
            .tagMap(tagMap)
            .build());

    checkInequality(
        metadata,
        Metadata.builder()
            .tenantAlias(tenantAlias)
            .organizationId("junk")
            .environmentAlias(environmentAlias)
            .accountId(accountId)
            .region(region)
            .majorVersion(majorVersion)
            .tagMap(tagMap)
            .build());

    checkInequality(
        metadata,
        Metadata.builder()
            .tenantAlias(tenantAlias)
            .organizationId(organizationId)
            .environmentAlias("junk")
            .accountId(accountId)
            .region(region)
            .majorVersion(majorVersion)
            .tagMap(tagMap)
            .build());

    checkInequality(
        metadata,
        Metadata.builder()
            .tenantAlias(tenantAlias)
            .organizationId(organizationId)
            .environmentAlias(environmentAlias)
            .accountId("junk")
            .region(region)
            .majorVersion(majorVersion)
            .tagMap(tagMap)
            .build());

    checkInequality(
        metadata,
        Metadata.builder()
            .tenantAlias(tenantAlias)
            .organizationId(organizationId)
            .environmentAlias(environmentAlias)
            .accountId(accountId)
            .region(Region.AWS_GLOBAL)
            .majorVersion(majorVersion)
            .tagMap(tagMap)
            .build());

    checkInequality(
        metadata,
        Metadata.builder()
            .tenantAlias(tenantAlias)
            .organizationId(organizationId)
            .environmentAlias(environmentAlias)
            .accountId(accountId)
            .region(region)
            .majorVersion("junk")
            .tagMap(tagMap)
            .build());

    checkInequality(
        metadata,
        Metadata.builder()
            .tenantAlias(tenantAlias)
            .organizationId(organizationId)
            .environmentAlias(environmentAlias)
            .accountId(accountId)
            .region(region)
            .majorVersion(majorVersion)
            .tagMap(Map.of())
            .build());
  }
}
