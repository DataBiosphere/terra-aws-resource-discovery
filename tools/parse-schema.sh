#!/bin/bash
#
# Parse and decode the schema from a config.json file
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <path_to_discovery_config_json_file>" >&2
  exit 1
fi

cat $1 | jq -r '.schema' | base64 -d
