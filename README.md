# terra-aws-resource-discovery

Terra AWS Support Resource Discovery Client Library

Table of Contents
=================
* [Introduction](#introduction)
* [Support Resource Organization](#support-resource-organization)
  * [Environments](#environments)
  * [Landing Zones](#landing-zones)
* [Discovery Implementation](#discovery-implementation)
  * [Support Resource Deployment](#support-resource-deployment)
  * [Configuration Schemas](#configuration-schemas)
  * [Schema Evolution and Versioning](#schema-evolution-and-versioning)
  * [Configuration Storage Layout](#configuration-storage-layout)
* [Library Development Notes](#library-development-notes)
  * [Static Test Data](#static-test-data)
    * [Updating an Existing Test Case Config File](#updating-an-existing-test-case-config-file)
    * [Writing a New Test Case Config File](#writing-a-new-test-case-config-file)

# Introduction
In order for Terra services to manage and provide access to [Controlled Resources](https://github.com/DataBiosphere/terra-workspace-manager#overview)
in Amazon Web Services (AWS), there must exist several AWS cloud resources used for this purpose.
These resources will be referred to as **Support Resources**.

Support resources may be [**Global** or **Regional**](https://docs.aws.amazon.com/ram/latest/userguide/working-with-regional-vs-global.html):
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
![](images/AWS%20Workspace%20Landing%20Zone.png)

The area in the dark purple dashed box represents the resources (Support and Controlled) that make
up a Terra AWS Landing Zone.

# Discovery Implementation
## Support Resource Deployment
Deployment of Support Resources in an AWS Environment is out of scope for this document.  However,
producers of these resources are required to provide discoverability of their Support Resources by
using the conventions described below.
## Configuration Schemas
We have chosen to use [Apache Avro](https://avro.apache.org/) for specifying the schema for Support
Resource discovery, for several reasons:
* Strong schema evolution support.
* Support for many programming languages, supporting both Java service development and
infrastructure-deployment-side testing/validation in other languages.
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
* Class [`CachedEnvironmentDiscovery`](src/main/java/bio/terra/aws/resource/discovery/CachedEnvironmentDiscovery.java)
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
        └── v1/landingzones/us-east-1/config.json
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

# Library Development Notes

## Dependency Locking
We use [Gradle's dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html) to ensure that builds use the same transitive dependencies, so they're reproducible. This means that adding or updating a dependency requires telling Gradle to save the change. Execute the below command when any dependency versions are updated.
```
./gradlew dependencies --write-locks
```

## Static Test Data
Class [`EnvironmentDiscoveryTestBase`](src/test/java/bio/terra/aws/resource/discovery/EnvironmentDiscoveryTestBase.java)
serves as a test fixture consuming static test data written in folder
[`src/test/resources/test_discovery_data`](src/test/resources/test_discovery_data)
to allow for testing of Avro parsing and schema validation.

In order to update the static test schema files as schemas evolve (as well as create new test data)
test authors can make use of the following scripts in the [`tools`](tools) directory:
* [`decode-test-data.sh`](tools/decode-test-data.sh) creates an out-of-tree directory (mirroring the
  structure of the `src/test/resources/test_discovery_data` directory tree), but with the payload
  parsed into un-encoded JSON.  Changes can be made in this out-of-tree location to the plaintext
  JSON and written back to the source tree using `encode-test-data.sh` script.
* [`encode-test-data.sh`](tools/encode-test-data.sh) can be used to write the payload changes made
  to an out-of-tree directory created with `decode-test-data.sh` back to the in-tree test
  configuration files in encoded form (along with the current in-tree schema versions).
* [`parse-schema.sh`](tools/parse-schema.sh) parses the Base64-encoded Avro schema from a
`config.json` Configuration file/object and prints it as plain JSON.
* [`parse-payload.sh`](tools/parse-payload.sh) parses the Base64-encoded payload from a
`config.json` Configuration file/object and prints it as plain JSON.
* [`print-config.sh`](tools/print-config.sh) takes an Avro schema and payload data file (both in
plain JSON) and Base64 encodes them into a configuration file format, printing the output to STDOUT

### Updating Multiple Test Case Config Files
``` shell
# Decode all test payloads into empty directory ~/DiscoveryTestData
$ ./tools/decode-test-data.sh src/test/resources/test_discovery_data /DiscoveryTestData

# The following unencoded JSON files mirror those in the src/test/resources/test_discovery_data
# directory.  These payload files can be edited in-place to update the test data payloads.
$ find /DiscoveryTestData/ -type file
/Users/jczerk/DiscoveryTestData//add_field_before_schema_update/v0/environment/payload.json
/DiscoveryTestData//notebook_lifecycle_mismatch/v0/environment/payload.json
/DiscoveryTestData//notebook_lifecycle_mismatch/v0/landingzones/us-east-1/payload.json
/DiscoveryTestData//no_landing_zones/v0/environment/payload.json
/DiscoveryTestData//validation/v0/environment/payload.json
/DiscoveryTestData//validation/v0/landingzones/us-west-1/payload.json
/DiscoveryTestData//validation/v0/landingzones/us-east-1/payload.json
/DiscoveryTestData//validation/v0/landingzones/fake-region/payload.json

# Now use the encode-test-data.sh script to encode the updated payloads (along with any
# in-tree schema updates from src/main/avro) into the in-tree test configuration files.
$ ./tools/encode-test-data.sh src/main/avro/ ~/DiscoveryTestData/ src/test/resources/test_discovery_data/
```
### Updating a Single Existing Test Case Config File
```shell
# Make any changes to the Avro schema, in this case src/main/avro/Environment.avsc

# Choose the file that you wish to update
TEST_FILE="src/test/resources/test_discovery_data/validation/v0/environment/config.json"

# Parse the payload from the existing test data file and write it to a scratch file for editing
./tools/parse-payload.sh ${TEST_FILE} > /tmp/scratch.json

# Make any changes to the test payload to the scratch file directly

# Now write the updated schema and test data back to the original file
./tools/print-config.sh src/main/avro/Environment.avsc /tmp/scratch.json > ${TEST_FILE}
```

### Writing a New Test Case Config File
```shell
# Write your test case payload to a new file somewhere outside of the terra-aws-resource-discovery
# filesystem tree (optionally making any required schema changes in src/main/avro)
NEW_TEST_DATA=/tmp/new_test.json

# Identify the new test data case location
NEW_TEST_CONFIG=src/test/resources/test_discovery_data/new_test_data/v0/environment/config.json

# Now write the schema and new test data to the new config file
./tools/print-config.sh src/main/avro/Environment.avsc ${NEW_TEST_DATA} > ${NEW_TEST_CONFIG}

```
