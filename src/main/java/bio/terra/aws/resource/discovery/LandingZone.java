package bio.terra.aws.resource.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.utils.Validate;

/** Represents all the Regional Support Resources in a Terra AWS Landing Zone. */
public class LandingZone {
  private final StorageBucket storageBucket;
  private final KmsKey kmsKey;
  private final List<NotebookLifecycleConfiguration> notebookLifecycleConfigurations;

  private LandingZone(Builder builder) {
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
    private StorageBucket storageBucket;
    private KmsKey kmsKey;
    private List<NotebookLifecycleConfiguration> notebookLifecycleConfigurations;

    private Builder() {
      notebookLifecycleConfigurations = new ArrayList<>();
    }

    /** Set the Landing Zone's AWS S3 Storage Bucket's ARN and Name */
    Builder storageBucket(Arn arn, String name) {
      storageBucket = new StorageBucket(arn, name);
      return this;
    }

    /** Set the Landing Zone's KNS Key's ARN and UUID */
    Builder kmsKey(Arn arn, UUID id) {
      kmsKey = new KmsKey(arn, id);
      return this;
    }

    /** Add a Sagemaker Notebook Lifecycle Configuration to the Landing Zone */
    Builder addNotebookLifecycleConfiguration(Arn arn, String name) {
      notebookLifecycleConfigurations.add(new NotebookLifecycleConfiguration(arn, name));
      return this;
    }

    /** Build the {@link LandingZone} instance */
    LandingZone build() {
      return new LandingZone(this);
    }
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
   * @return a list of {@link NotebookLifecycleConfiguration} representing all of the Landing Zone's
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
    return Objects.equals(storageBucket, that.storageBucket)
        && kmsKey.equals(that.kmsKey)
        && notebookLifecycleConfigurations.equals(that.notebookLifecycleConfigurations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(storageBucket, kmsKey, notebookLifecycleConfigurations.hashCode());
  }
}
