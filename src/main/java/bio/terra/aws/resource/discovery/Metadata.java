package bio.terra.aws.resource.discovery;

import java.util.Map;
import software.amazon.awssdk.regions.Region;

/**
 * Metadata attached to an {@link Environment} or {@link LandingZone}
 *
 * @param accountId AWS Account ID
 * @param region AWS Region
 * @param tagMap Map of key-values to tag resources in the Environment/Landing Zone with
 */
public record Metadata(String accountId, Region region, Map<String, String> tagMap) {
  public Metadata(String accountId, Region region, Map<String, String> tagMap) {
    this.accountId = accountId;
    this.region = region;
    this.tagMap = Map.copyOf(tagMap);
  }

  @Override
  public Map<String, String> tagMap() {
    return Map.copyOf(tagMap);
  }
}
