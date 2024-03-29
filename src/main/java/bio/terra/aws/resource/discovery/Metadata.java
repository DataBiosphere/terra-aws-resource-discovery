package bio.terra.aws.resource.discovery;

import java.util.Map;
import java.util.Objects;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.Validate;

/** Metadata attached to an {@link Environment} or {@link LandingZone} */
public class Metadata {
  private final String tenantAlias;
  private final String organizationId;
  private final String environmentAlias;
  private final String accountId;
  private final Region region;
  private final String majorVersion;
  private final Map<String, String> tagMap;

  public Metadata(Metadata metadata) {
    this(metadata.toBuilder());
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getTenantAlias() {
    return tenantAlias;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public String getEnvironmentAlias() {
    return environmentAlias;
  }

  public String getAccountId() {
    return accountId;
  }

  public Region getRegion() {
    return region;
  }

  public String getMajorVersion() {
    return majorVersion;
  }

  public Map<String, String> getTagMap() {
    return Map.copyOf(tagMap);
  }

  public Builder toBuilder() {
    return builder()
        .tenantAlias(this.tenantAlias)
        .organizationId(this.organizationId)
        .environmentAlias(this.environmentAlias)
        .accountId(this.accountId)
        .region(this.region)
        .majorVersion(this.majorVersion)
        .tagMap(this.tagMap);
  }

  private Metadata(Builder builder) {
    Validate.notNull(builder.tenantAlias, "tenantAlias may not be null.");
    tenantAlias = builder.tenantAlias;

    Validate.notNull(builder.organizationId, "organizationId may not be null.");
    organizationId = builder.organizationId;

    Validate.notNull(builder.environmentAlias, "environmentAlias may not be null.");
    environmentAlias = builder.environmentAlias;

    Validate.notNull(builder.accountId, "accountId may not be null.");
    accountId = builder.accountId;

    Validate.notNull(builder.region, "region may not be null.");
    region = builder.region;

    Validate.notNull(builder.majorVersion, "majorVersion may not be null.");
    majorVersion = builder.majorVersion;

    Validate.notNull(builder.tagMap, "tagMap may not be null.");
    tagMap = builder.tagMap;
  }

  public static class Builder {
    private String tenantAlias;
    private String organizationId;
    private String environmentAlias;
    private String accountId;
    private Region region;
    private String majorVersion;
    private Map<String, String> tagMap;

    public Builder tenantAlias(String tenantAlias) {
      this.tenantAlias = tenantAlias;
      return this;
    }

    public Builder organizationId(String organizationId) {
      this.organizationId = organizationId;
      return this;
    }

    public Builder environmentAlias(String environmentAlias) {
      this.environmentAlias = environmentAlias;
      return this;
    }

    public Builder accountId(String accountId) {
      this.accountId = accountId;
      return this;
    }

    public Builder region(Region region) {
      this.region = region;
      return this;
    }

    public Builder majorVersion(String majorVersion) {
      this.majorVersion = majorVersion;
      return this;
    }

    public Builder tagMap(Map<String, String> tagMap) {
      this.tagMap = tagMap;
      return this;
    }

    public Metadata build() {
      return new Metadata(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Metadata metadata = (Metadata) o;
    return getTenantAlias().equals(metadata.getTenantAlias())
        && getOrganizationId().equals(metadata.getOrganizationId())
        && getEnvironmentAlias().equals(metadata.getEnvironmentAlias())
        && getAccountId().equals(metadata.getAccountId())
        && getRegion().equals(metadata.getRegion())
        && getMajorVersion().equals((metadata.getMajorVersion()))
        && getTagMap().equals(metadata.getTagMap());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getTenantAlias(),
        getOrganizationId(),
        getEnvironmentAlias(),
        getAccountId(),
        getRegion(),
        getMajorVersion(),
        getTagMap());
  }
}
