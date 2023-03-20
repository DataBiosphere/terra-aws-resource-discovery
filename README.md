# terra-aws-resource-discovery

Terra AWS Support Resource Discovery Client Library

# Introduction
In order for Terra services to manage and provide access to [Controlled Resources](https://github.com/DataBiosphere/terra-workspace-manager#overview) 
in Amazon Web Services (AWS), there must exist several AWS cloud resources used for this purpose.
These resources will be referred to **Support Resources**.

Support regions may be [**Global** or **Regional**](https://docs.aws.amazon.com/ram/latest/userguide/working-with-regional-vs-global.html):
* **Global Support Resources** are resources that do not exist in an AWS Region.  These are 
most commonly (but not limited to) IAM resources ([IAM Roles](https://aws.amazon.com/iam/features/manage-roles/), 
[IAM Policies](https://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies.html#policies_id-based)).
* **Regional Support Resources** are resources that exist in a specific AWS Region, such as [S3 
storage buckets](https://aws.amazon.com/s3/) and [KMS Keys](https://aws.amazon.com/kms/).

This library provides the ability to discover all of the Support Resources needed by Terra services and 
present in a given AWS environment, so that Terra services can make use of these Support Resources to 
create and provide access to Terra Controlled Resources in AWS.

# Support Resource Organization
## Environments
In Terra, an **Environment** corresponds to a single [AWS Account](https://docs.aws.amazon.com/accounts/latest/reference/accounts-welcome.html).
All Support Resources and Controlled Resources exist in a single Environment.

The [`Environment` class](src/main/java/bio/terra/aws/resource/discovery/Environment.java) provides
getters for all Global Support Resources usable by Terra Services.

## Landing Zones
In Terra, a Landing Zone corresponds to the nexus of a [Terra Environment](#Environments) and an AWS Region.
Each Regional Support Resource exists within a Landing Zone, as do all Terra Controlled Resources.

The [`LandingZone` class](src/main/java/bio/terra/aws/resource/discovery/LandingZone.java) provides
getters for all Regional Support Resources usable by Terra Services in a given AWS Region in a
Terra AWS Environment.

Instances of class `LandingZone` are obtained by calling method `getLandingZone()` on an `Environment`
instance, passing the corresponding AWS Region for the Landing Zone within the Environment.

The following diagram illustrates the relationship between Environments, Regions, and Terra 
Controlled Resources in AWS:
![](AWS%20Workspace%20Landing%20Zone.png)

# Discovery Implementation
## Support Resource Deployment
Deployment of Support Resources in an AWS Environment is out of scope for this document.  However,
producers of these resources are required to provide discovery using the conventions described
below in order to make them discoverable to Terra Services.
## Configuration Schemas
We have chosen to use [Apache Avro](https://avro.apache.org/) for specifying the schema for Support 
Resource discovery, for several reasons:
* Strong schema evolution support.
* Support for many programming languages, supporting both Java service development and
infrastructure-deployment-side testing/validation.
* Human readable JSON-based Schema IDL and JSON data files.

*This repository ([`terra-aws-resource-discovery`](https://github.com/DataBiosphere/terra-aws-resource-discovery))
will be the single source of truth for Resource Discovery Schemas.*

Two configuration schemas are specified in this repository:
* [`Environment.avsc`](src/main/avro/Environment.avsc) - the schema used to describe all Global
Support Resources available in a Terra AWS Environment.
* [`LandingZone.avsc`](src/main/avro/LandingZone.avsc) - the schema used to describe all Regional
  Support Resources available in a Landing Zone within a Terra AWS Environment.

## Schema Evolution and Versioning
Avro always requires two versions of schema when deserializing:

* The Writer's Schema (the current schema version used by the writer at the time the data was written)
* The Reader's Schema (the current schema version used by the reader of the data)

Thus, when a configuration  is written to storage, it will include both the version of the schema
used to write the data (as retrieved from the artifacts of this library, published to GitHub with
each release) as well as the data itself.

We will maintain Forward Transitive Compatibility as described in 
[this document](https://docs.confluent.io/platform/current/schema-registry/avro.html#forward-compatibility).

Specifically, these are the rules we will follow:
* Any change to either Avro Schema file *must* incur at least a minor version bump.
* Fields may be added to the schema without breaking forward transitive compatibility.
* Optional fields may be deleted without breaking forward transitive compatibility.
* Producers of configuration artifacts (backend infrastructure deployment) *must* be updated before 
consumers (Terra services).
* Breaking changes *must* be a major version bump, and should be avoided.  *We will not support
forward compatibility between major versions.*

## Configuration Storage Layout
The `terra-aws-resource-discovery` provides discovery of all Support Resources in a single 
Environment through interface [`EnvironmentDiscovery`](src/main/java/bio/terra/aws/resource/discovery/EnvironmentDiscovery.java).
Three implementations of this interface are provided:
* Class [`S3EnvironmentDiscovery`](src/main/java/bio/terra/aws/resource/discovery/S3EnvironmentDiscovery.java) 
discovers Support Resources by reading them from an S3 bucket that the caller has access to.
* Class [`FilesystemEnvironmentDiscovery`](src/main/java/bio/terra/aws/resource/discovery/FilesystemEnvironmentDiscovery.java)
discovers Support Resources by reading them from directories within an accessible file system path.
* Class [`S3EnvironmentDiscovery`](src/main/java/bio/terra/aws/resource/discovery/CachedEnvironmentDiscovery.java) 
is used in conjunction with one of the two above classes to cache discovery results between calls to 
`discoverEnvironment()`, in order to reduce the number of calls to storage API's.

Whether stored in an S3 Bucket or a local file system directory, the following layout is expected
by the discovery library (in this example, this is major version 1 of the library, and we are
discovering an Environment with two Landing Zones in AWS regions `eu-central-1` and `us-east-1`:
```
v1
├── v1/environment
│   ├── v1/environment/config.json
└── v1/landingzones
    ├── v1/landingzones/eu-central-1
    │   └── v1/landingzones/eu-central-1/config.json
    └── v1/landingzones/us-east-1
        └── v1/landingzones/eu-central-1/config.json
```
Each `config.json` file contains the following JSON content:
```json
{
  "schema": "<base64-encoded avro schema>",
  "payload": "<base64-encoded support resource data>"
}
```

File `v1/environment/config.json` uses schema [`Environment.avsc`](src/main/avro/Environment.avsc)
to describe the Global Support Resources in the Environment.

Files `v1/landingzones/eu-central-1/config.json` and `v1/landingzones/eu-central-1/config.json` use
schema [`LandingZone.avsc`](src/main/avro/LandingZone.avsc) to describe the Regional Support
Resources in the Environment's Landing Zones in regions `eu-central-1` and `us-east-1` 
respectively.