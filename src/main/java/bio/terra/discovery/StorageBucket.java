package bio.terra.discovery;

import software.amazon.awssdk.arns.Arn;

/**
 * A {@link Record} representing an AWS S3 Storage Bucket
 *
 * @param arn ARN of the S3 Storage Bucket
 * @param name name of the S3 Storage Bucket
 */
public record StorageBucket(Arn arn, String name) {}
