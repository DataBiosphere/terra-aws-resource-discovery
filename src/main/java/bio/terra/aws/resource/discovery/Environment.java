package bio.terra.aws.resource.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.util.Assert;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.regions.Region;

/**
 * Represents all the Support Resources in a Terra AWS Environment.
 *
 * <p>Global Support Resources can be obtained directly from the Environment class using getters.
 *
 * <p>Regional Support Resources can be obtained by calling method {@link
 * Environment#getLandingZone(Region)} with a given AWS region to obtain an instance of class {@link
 * LandingZone}.
 */
public class Environment {
  private final Metadata metadata;
  private final Arn workspaceManagerRoleArn;
  private final Arn userRoleArn;
  private final Arn notebookRoleArn;
  private final Map<Region, LandingZone> landingZoneMap;

  private Environment(Builder builder) {
    metadata = builder.metadata;
    workspaceManagerRoleArn = builder.workspaceManagerRoleArn;
    userRoleArn = builder.userRoleArn;
    notebookRoleArn = builder.notebookRoleArn;
    landingZoneMap = builder.landingZoneMap;
  }

  /** Builder for class @{link Environment} */
  public static class Builder {
    private Metadata metadata;
    private Arn workspaceManagerRoleArn;
    private Arn userRoleArn;
    private Arn notebookRoleArn;
    private Map<Region, LandingZone> landingZoneMap;

    private Builder() {
      landingZoneMap = new HashMap<>();
    }

    /** Set the metadata describing the Environment */
    Builder metadata(Metadata metadata) {
      this.metadata = metadata;
      return this;
    }

    /** Set the AWS ARN for the TerraWorkspaceManager IAM Role Global Shared Resource */
    Builder workspaceManagerRoleArn(Arn arn) {
      workspaceManagerRoleArn = arn;
      return this;
    }

    /** Set the AWS ARN for the TerraUser IAM Role Global Shared Resource */
    Builder userRoleArn(Arn arn) {
      userRoleArn = arn;
      return this;
    }

    /** Set the AWS ARN for the TerraNotebookExecution IAM Role Global Shared Resource */
    Builder notebookRoleArn(Arn arn) {
      notebookRoleArn = arn;
      return this;
    }

    /** Add a {@link LandingZone} to the {@link Environment} being built for a given AWS region */
    Builder addLandingZone(Region region, LandingZone landingZone) {
      landingZoneMap.put(region, landingZone);
      return this;
    }

    /** Build the {@link Environment} instance */
    Environment build() {
      return new Environment(this);
    }
  }

  /** Get a {@link Builder} for {@link Environment} */
  public static Builder builder() {
    return new Builder();
  }

  /** Get the {@link Metadata} describing the {@link Environment} */
  public Metadata getMetadata() {
    return metadata;
  }

  /** Get the AWS ARN for the TerraWorkspaceManager IAM Role Global Shared Resource */
  public Arn getWorkspaceManagerRoleArn() {
    return workspaceManagerRoleArn;
  }

  /** Get the AWS ARN for the TerraUser IAM Role Global Shared Resource */
  public Arn getUserRoleArn() {
    return userRoleArn;
  }

  /** Get the AWS ARN for the TerraNotebookExecution IAM Role Global Shared Resource */
  public Arn getNotebookRoleArn() {
    return notebookRoleArn;
  }

  /**
   * Gets an instance of class {@link LandingZone} representing the Regional Support Resources in
   * the Landing Zone if one exists in the Terra AWS Environment.
   *
   * @param region the AWS region to get the Regional Support Resources for
   * @return A populated {@link Optional<LandingZone>} if a Landing Zone exists in the Environment
   *     for the passed AWS region; an empty {@link Optional<LandingZone>} if a Landing Zone does
   *     not exist in the Environment for this AWS region.
   */
  public Optional<LandingZone> getLandingZone(Region region) {
    return Optional.ofNullable(landingZoneMap.get(region));
  }

  /**
   * Returns a {@link Set<Region>} containing all of the AWS Regions supported by an {@link
   * Environment}. For all regions contained in this set, a call to {@link
   * Environment#getLandingZone(Region)} should return a non-empty {@link Optional<LandingZone>}.
   *
   * @return a set containing all AWS regions supported by the region
   */
  public Set<Region> getSupportedRegions() {
    return Set.copyOf(landingZoneMap.keySet());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Environment that)) return false;
    return Objects.equals(metadata, that.metadata)
        && Objects.equals(workspaceManagerRoleArn, that.workspaceManagerRoleArn)
        && Objects.equals(userRoleArn, that.userRoleArn)
        && Objects.equals(notebookRoleArn, that.notebookRoleArn)
        && landingZoneMap.equals(that.landingZoneMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        metadata, workspaceManagerRoleArn, userRoleArn, notebookRoleArn, landingZoneMap);
  }

  /**
   * Validates AWS environment
   *
   * @param environment AWS environment
   * @param prefix Error message prefix
   * @throws IllegalArgumentException environment error
   */
  public static void validate(Environment environment, String prefix)
      throws IllegalArgumentException {
    if (!prefix.endsWith(".")) {
      prefix += ".";
    }
    Assert.notNull(environment, prefix + "environment null");
    String envPrefix = prefix + "environment.";

    Metadata.validate(environment.getMetadata(), envPrefix);
    Assert.notNull(
        environment.getWorkspaceManagerRoleArn(), envPrefix + "workspaceManagerRoleArn null");
    Assert.notNull(environment.getUserRoleArn(), envPrefix + "userRoleArn null");
    Assert.notNull(environment.getNotebookRoleArn(), envPrefix + "notebookRoleArn null");

    Assert.notEmpty(environment.getSupportedRegions(), envPrefix + "supportedRegions empty");
    environment
        .getSupportedRegions()
        .forEach(
            region ->
                LandingZone.validate(environment.getLandingZone(region).orElse(null), envPrefix));
  }
}
