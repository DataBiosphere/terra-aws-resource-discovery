package bio.terra.aws.resource.discovery;

import java.util.*;
import software.amazon.awssdk.arns.Arn;

/**
 * A {@link Record} representing an AWS KMS Key instance
 *
 * @param arn ARN of the KMS Key instance
 * @param id ID of the KMS Key instance
 */
public record KmsKey(Arn arn, UUID id) {}
