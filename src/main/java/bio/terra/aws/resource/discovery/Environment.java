package bio.terra.aws.resource.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
  private final Arn workspaceManagerRoleArn;
  private final Arn userRoleArn;
  private final Arn notebookRoleArn;
  private final Map<Region, LandingZone> landingZoneMap;

  private Environment(Builder builder) {
    workspaceManagerRoleArn = builder.workspaceManagerRoleArn;
    userRoleArn = builder.userRoleArn;
    notebookRoleArn = builder.notebookRoleArn;
    landingZoneMap = builder.landingZoneMap;
  }

  /** Builder for class @{link Environment} */
  public static class Builder {
    private Arn workspaceManagerRoleArn;
    private Arn userRoleArn;
    private Arn notebookRoleArn;
    private Map<Region, LandingZone> landingZoneMap;

    private Builder() {
      landingZoneMap = new HashMap<>();
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Environment)) return false;
    Environment that = (Environment) o;
    return Objects.equals(workspaceManagerRoleArn, that.workspaceManagerRoleArn)
        && Objects.equals(userRoleArn, that.userRoleArn)
        && Objects.equals(notebookRoleArn, that.notebookRoleArn)
        && landingZoneMap.equals(that.landingZoneMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(workspaceManagerRoleArn, userRoleArn, notebookRoleArn, landingZoneMap);
  }
}
