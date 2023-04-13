package bio.terra.aws.resource.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.util.Assert;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.utils.Validate;

/** Represents all the Regional Support Resources in a Terra AWS Landing Zone. */
public class LandingZone {
  private final Metadata metadata;
  private final StorageBucket storageBucket;
  private final KmsKey kmsKey;
  private final List<NotebookLifecycleConfiguration> notebookLifecycleConfigurations;

  private LandingZone(Builder builder) {
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
    private Metadata metadata;
    private StorageBucket storageBucket;
    private KmsKey kmsKey;
    private List<NotebookLifecycleConfiguration> notebookLifecycleConfigurations;

    private Builder() {
      notebookLifecycleConfigurations = new ArrayList<>();
    }

    /** Set the metadata describing the LandingZone */
    Builder metadata(Metadata metadata) {
      this.metadata = metadata;
      return this;
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
   * @return a list of {@link NotebookLifecycleConfiguration} representing all of the Landing Zone's
   *     AWS Sagemaker Notebook Lifecycle Configurations
   */
  public List<NotebookLifecycleConfiguration> getNotebookLifecycleConfigurations() {
    return new ArrayList<>(notebookLifecycleConfigurations);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LandingZone that)) return false;
    return Objects.equals(metadata, that.metadata)
        && Objects.equals(storageBucket, that.storageBucket)
        && kmsKey.equals(that.kmsKey)
        && notebookLifecycleConfigurations.equals(that.notebookLifecycleConfigurations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        metadata, storageBucket, kmsKey, notebookLifecycleConfigurations.hashCode());
  }

  /**
   * Validates AWS landing zone
   *
   * @param landingZone AWS landing zone
   * @param prefix Error message prefix
   * @throws IllegalArgumentException environment error
   */
  public static void validate(LandingZone landingZone, String prefix)
      throws IllegalArgumentException {
    if (!prefix.endsWith(".")) {
      prefix += ".";
    }
    Assert.notNull(landingZone, prefix + "landingZone null");
    String lzPrefix = prefix + "landingZone.";

    Metadata.validate(landingZone.getMetadata(), lzPrefix);
    validate(landingZone.getStorageBucket(), lzPrefix);
    validate(landingZone.getKmsKey(), lzPrefix);

    Assert.notEmpty(
        landingZone.getNotebookLifecycleConfigurations(),
        lzPrefix + "notebookLifecycleConfigurations empty");
    landingZone
        .getNotebookLifecycleConfigurations()
        .forEach(lcConfig -> validate(lcConfig, lzPrefix));
  }

  // StorageBucket, KmsKey, NotebookLifecycleConfiguration: records used in LandingZone
  /**
   * Validates AWS storage bucket
   *
   * @param storageBucket AWS storage bucket
   * @param prefix Error message prefix
   * @throws IllegalArgumentException environment error
   */
  public static void validate(StorageBucket storageBucket, String prefix)
      throws IllegalArgumentException {
    if (!prefix.endsWith(".")) {
      prefix += ".";
    }
    Assert.notNull(storageBucket, prefix + "storageBucket null");
    String sbPrefix = prefix + "storageBucket.";

    Assert.notNull(storageBucket.arn(), sbPrefix + "arn null");
    Assert.hasLength(storageBucket.name(), sbPrefix + "name empty");
  }

  /**
   * Validates AWS kms key
   *
   * @param kmsKey AWS kms key
   * @param prefix Error message prefix
   * @throws IllegalArgumentException environment error
   */
  public static void validate(KmsKey kmsKey, String prefix) throws IllegalArgumentException {
    if (!prefix.endsWith(".")) {
      prefix += ".";
    }
    Assert.notNull(kmsKey, prefix + "kmsKey null");
    String kmsPrefix = prefix + "kmsKey.";

    Assert.notNull(kmsKey.arn(), kmsPrefix + "arn null");
    Assert.notNull(kmsKey.id(), kmsPrefix + "id null");
  }

  /**
   * Validates AWS notebook lifecycle configuration
   *
   * @param notebookLifecycleConfiguration AWS notebook lifecycle configuration
   * @param prefix Error message prefix
   * @throws IllegalArgumentException environment error
   */
  public static void validate(
      NotebookLifecycleConfiguration notebookLifecycleConfiguration, String prefix)
      throws IllegalArgumentException {
    if (!prefix.endsWith(".")) {
      prefix += ".";
    }
    Assert.notNull(notebookLifecycleConfiguration, prefix + "notebookLifecycleConfiguration null");
    String lcConfigPrefix = prefix + "kmsKey.";

    Assert.notNull(notebookLifecycleConfiguration.arn(), lcConfigPrefix + "arn null");
    Assert.hasLength(notebookLifecycleConfiguration.name(), lcConfigPrefix + "name empty");
  }
}
