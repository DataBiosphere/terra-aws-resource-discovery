package bio.terra.discovery;

import software.amazon.awssdk.arns.Arn;

/**
 * A {@link Record} representing an AWS Sagemaker Notebook Lifecycle Configuration
 *
 * @param arn ARN of the Sagemaker Notebook Lifecycle Configuration
 * @param name name of the Sagemaker Notebook Lifecycle Configuration
 */
public record NotebookLifecycleConfiguration(Arn arn, String name) {}
