package bio.terra.aws.resource.discovery;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.regions.Region;

// Extend EnvironmentDiscoveryTestBase to leverage Environment generation.
public class LandingZoneTest extends EnvironmentDiscoveryTestBase {

  /** Build an ARN that will compare not equal */
  private static Arn junkArn() {
    return Arn.builder().partition("junk").service("junk").resource("junk").build();
  }

  @Test
  public void equality() {
    Optional<LandingZone> landingZoneOptional =
        getExpectedEnvironment().getLandingZone(Region.US_EAST_1);
    Assertions.assertTrue(landingZoneOptional.isPresent());
    LandingZone landingZone = landingZoneOptional.get();

    // Self equality
    Assertions.assertEquals(landingZone, landingZone);

    // Deep copy
    Metadata metadata = landingZone.getMetadata();
    KmsKey kmsKey = landingZone.getKmsKey();
    StorageBucket storageBucket = landingZone.getStorageBucket();

    LandingZone.Builder builder =
        LandingZone.builder()
            .applicationVpcId(landingZone.getApplicationVpcId())
            .applicationVpcPrivateSubnetId(landingZone.getApplicationVpcPrivateSubnetId())
            .metadata(new Metadata(metadata))
            .storageBucket(storageBucket.arn(), storageBucket.name())
            .kmsKey(kmsKey.arn(), kmsKey.id());

    for (NotebookLifecycleConfiguration config : landingZone.getNotebookLifecycleConfigurations()) {
      builder.addNotebookLifecycleConfiguration(config.arn(), config.name());
    }

    // Test equals()/hashCode()
    LandingZone builtLandingZone = builder.build();
    Assertions.assertEquals(landingZone, builtLandingZone);
    Assertions.assertEquals(landingZone.hashCode(), builtLandingZone.hashCode());
  }

  private static void checkInequality(LandingZone l, LandingZone r) {
    // Check equals()/hashCode()
    Assertions.assertNotEquals(l, r);
    Assertions.assertNotEquals(l.hashCode(), r.hashCode());
  }

  @Test
  public void inequality() {
    Optional<LandingZone> landingZoneOptional =
        getExpectedEnvironment().getLandingZone(Region.US_EAST_1);
    Assertions.assertTrue(landingZoneOptional.isPresent());
    LandingZone landingZone = landingZoneOptional.get();

    // Incompatible types
    String string = "string";
    Assertions.assertNotEquals((Object) landingZone, (Object) string);

    KmsKey kmsKey = landingZone.getKmsKey();
    StorageBucket storageBucket = landingZone.getStorageBucket();

    // Different App VPC ID
    checkInequality(
        landingZone,
        LandingZone.builder()
            .applicationVpcId("junk")
            .applicationVpcPrivateSubnetId(landingZone.getApplicationVpcPrivateSubnetId())
            .metadata(landingZone.getMetadata())
            .storageBucket(storageBucket.arn(), storageBucket.name())
            .kmsKey(kmsKey.arn(), kmsKey.id())
            .build());

    // Different App Private Subnet ID
    checkInequality(
        landingZone,
        LandingZone.builder()
            .applicationVpcId(landingZone.getApplicationVpcId())
            .applicationVpcPrivateSubnetId("junk")
            .metadata(landingZone.getMetadata())
            .storageBucket(storageBucket.arn(), storageBucket.name())
            .kmsKey(kmsKey.arn(), kmsKey.id())
            .build());

    // Different Metadata
    checkInequality(
        landingZone,
        LandingZone.builder()
            .applicationVpcId(landingZone.getApplicationVpcId())
            .applicationVpcPrivateSubnetId(landingZone.getApplicationVpcPrivateSubnetId())
            // Create a junk metadata
            .metadata(
                new Metadata(landingZone.getMetadata().toBuilder().majorVersion("junk").build()))
            .storageBucket(storageBucket.arn(), storageBucket.name())
            .kmsKey(kmsKey.arn(), kmsKey.id())
            .build());

    // Different Notebook Role Arn
    checkInequality(
        landingZone,
        LandingZone.builder()
            .applicationVpcId(landingZone.getApplicationVpcId())
            .applicationVpcPrivateSubnetId(landingZone.getApplicationVpcPrivateSubnetId())
            .metadata(landingZone.getMetadata())
            .storageBucket(junkArn(), "")
            .kmsKey(kmsKey.arn(), kmsKey.id())
            .build());

    // Different Storage Bucket
    checkInequality(
        landingZone,
        LandingZone.builder()
            .applicationVpcId(landingZone.getApplicationVpcId())
            .applicationVpcPrivateSubnetId(landingZone.getApplicationVpcPrivateSubnetId())
            .metadata(landingZone.getMetadata())
            .storageBucket(storageBucket.arn(), storageBucket.name())
            .kmsKey(junkArn(), UUID.randomUUID())
            .build());

    // Lifecycle Configs Not Equal
    checkInequality(
        landingZone,
        LandingZone.builder()
            .applicationVpcId(landingZone.getApplicationVpcId())
            .applicationVpcPrivateSubnetId(landingZone.getApplicationVpcPrivateSubnetId())
            .metadata(landingZone.getMetadata())
            .storageBucket(storageBucket.arn(), storageBucket.name())
            .kmsKey(kmsKey.arn(), kmsKey.id())
            .build());
  }
}
