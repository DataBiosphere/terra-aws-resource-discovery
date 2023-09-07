package bio.terra.aws.resource.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.utils.Validate;

/** Represents all the Regional Support Resources in a Terra AWS Landing Zone. */
public class LandingZone {
  private final String applicationVpcId;
  private final String applicationVpcPrivateSubnetId;
  private final Metadata metadata;
  private final StorageBucket storageBucket;
  private final KmsKey kmsKey;
  private final List<NotebookLifecycleConfiguration> notebookLifecycleConfigurations;

  private LandingZone(Builder builder) {
    Validate.notNull(builder.applicationVpcId, "Application VPC ID may not be null.");
    this.applicationVpcId = builder.applicationVpcId;

    Validate.notNull(
        builder.applicationVpcPrivateSubnetId, "Application VPC Subnet ID may not be null.");
    this.applicationVpcPrivateSubnetId = builder.applicationVpcPrivateSubnetId;

    Validate.notNull(builder.metadata, "Metadata may not be null.");
    this.metadata = builder.metadata;

    Validate.notNull(builder.storageBucket, "Storage bucket may not be null");
    storageBucket = builder.storageBucket;

    Validate.notNull(builder.storageBucket, "KMS Key may not be null");
    kmsKey = builder.kmsKey;

    Validate.notNull(
        builder.notebookLifecycleConfigurations,
        "Notebook Lifecycle Configs list may be empty, but not be null");
    notebookLifecycleConfigurations = builder.notebookLifecycleConfigurations;
  }

  /** Get a {@link Builder} for {@link LandingZone} */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for class @{link LandingZone} */
  public static class Builder {
    private String applicationVpcId;
    private String applicationVpcPrivateSubnetId;
    private Metadata metadata;
    private StorageBucket storageBucket;
    private KmsKey kmsKey;
    private List<NotebookLifecycleConfiguration> notebookLifecycleConfigurations;

    private Builder() {
      notebookLifecycleConfigurations = new ArrayList<>();
    }

    /** Set the ID of the dedicated VPC to place Application EC2 instances into. */
    public Builder applicationVpcId(String applicationVpcId) {
      this.applicationVpcId = applicationVpcId;
      return this;
    }

    /** Set the ID of the dedicated private VPC subnet to place Application EC2 instances into. */
    public Builder applicationVpcPrivateSubnetId(String applicationVpcPrivateSubnetId) {
      this.applicationVpcPrivateSubnetId = applicationVpcPrivateSubnetId;
      return this;
    }

    /** Set the metadata describing the LandingZone */
    public Builder metadata(Metadata metadata) {
      this.metadata = metadata;
      return this;
    }

    /** Set the Landing Zone's AWS S3 Storage Bucket's ARN and Name */
    public Builder storageBucket(Arn arn, String name) {
      storageBucket = new StorageBucket(arn, name);
      return this;
    }

    /** Set the Landing Zone's KNS Key's ARN and UUID */
    public Builder kmsKey(Arn arn, UUID id) {
      kmsKey = new KmsKey(arn, id);
      return this;
    }

    /** Add a Sagemaker Notebook Lifecycle Configuration to the Landing Zone */
    public Builder addNotebookLifecycleConfiguration(Arn arn, String name) {
      notebookLifecycleConfigurations.add(new NotebookLifecycleConfiguration(arn, name));
      return this;
    }

    /** Build the {@link LandingZone} instance */
    public LandingZone build() {
      return new LandingZone(this);
    }
  }

  /** Get the ID of the dedicated VPC to place Application EC2 instances into. */
  public String getApplicationVpcId() {
    return applicationVpcId;
  }

  /** Get the ID of the dedicated private VPC subnet to place Application EC2 instances into. */
  public String getApplicationVpcPrivateSubnetId() {
    return applicationVpcPrivateSubnetId;
  }

  /** Get the {@link Metadata} describing the {@link Environment} */
  public Metadata getMetadata() {
    return metadata;
  }

  /** Gets the Landing Zone's AWS S3 Storage Bucket */
  public StorageBucket getStorageBucket() {
    return storageBucket;
  }

  /** Gets the Landing Zone's AWS KMS Key */
  public KmsKey getKmsKey() {
    return kmsKey;
  }

  /**
   * Gets the Landing Zone's AWS Sagemaker Notebook Lifecycle Configurations
   *
   * @return a list of {@link NotebookLifecycleConfiguration} representing all the Landing Zone's
   *     AWS Sagemaker Notebook Lifecycle Configurations
   */
  public List<NotebookLifecycleConfiguration> getNotebookLifecycleConfigurations() {
    return new ArrayList<>(notebookLifecycleConfigurations);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LandingZone)) return false;
    LandingZone that = (LandingZone) o;
    return Objects.equals(applicationVpcId, that.applicationVpcId)
        && Objects.equals(applicationVpcPrivateSubnetId, that.applicationVpcPrivateSubnetId)
        && Objects.equals(metadata, that.metadata)
        && Objects.equals(storageBucket, that.storageBucket)
        && kmsKey.equals(that.kmsKey)
        && notebookLifecycleConfigurations.equals(that.notebookLifecycleConfigurations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        applicationVpcId,
        applicationVpcPrivateSubnetId,
        metadata,
        storageBucket,
        kmsKey,
        notebookLifecycleConfigurations.hashCode());
  }
}
