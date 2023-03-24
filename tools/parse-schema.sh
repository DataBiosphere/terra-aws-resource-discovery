#!/bin/bash
#
# Parse and decode the schema from a config.json file
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <path_to_discovery_config_json_file>" >&2
  exit 1
fi

jq -r '.schema' "$1" | base64 --decode
