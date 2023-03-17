#!/bin/bash
set -e
# This script builds a new GitHub release for the Terra AWS Resource Discovery module.
# The GitHub release includes the avro schema files.
# Note that a pre-release does not affect the "Latest release" tag, but a regular release does.
#
# Dependencies: gh
#
# Inputs: releaseVersion (arg, required) determines the git tag to use for creating the release
#         isRegularRelease (arg, optional) 'false' for a pre-release (default), 'true' for a regular release
#
# Usage: ./create-release.sh  0.0.0        --> publishes version 0.0.0 as a pre-release
# Usage: ./create-release.sh  0.0.0 true   --> publishes version 0.0.0 as a regular release

## The script assumes that it is being run from the top-level directory "terra-aws-resource-discovery/".
if [[ $(basename "$PWD") != 'terra-aws-resource-discovery' ]]; then
  >&2 echo "ERROR: Script must be run from top-level directory 'terra-aws-resource-discovery/'"
  exit 1
fi

releaseVersion=$1
isRegularRelease=$2

if [[ -z "$releaseVersion" ]]; then
    >&2 echo "ERROR: Usage: tools/publish-release.sh [releaseVersion] [isRegularRelease]"
    exit 1
fi

echo "-- Checking if there is a tag that matches provided releaseVersion"
if [[ -z $(git tag -l "$releaseVersion") ]]; then
  >&2 echo "ERROR: No tag found matching this version"
  exit 1
else
  echo "Found tag matching this version"
fi
releaseTag="v$releaseVersion"

echo "-- Creating a new GitHub release with the avro schema files"
if [[ "$isRegularRelease" == "true" ]]; then
  echo "Creating regular release"
  preReleaseFlag=""
else
  echo "Creating pre-release"
  preReleaseFlag="--prerelease"
fi

schemaDirectory="src/main/avro"
schemaFilesList=()
for filePath in "$schemaDirectory"/*.avsc
do
  schemaFilesList+=("$filePath#$(basename "$filePath")")
done

gh config set prompt disabled
gh release create "$releaseTag" $preReleaseFlag \
  --title "$releaseTag" \
  "${schemaFilesList[@]}"
