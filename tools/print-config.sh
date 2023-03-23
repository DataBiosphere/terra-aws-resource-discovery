#!/bin/bash
#
# Generate and print discovery config.json content for a given schema file and JSON payload file
if [[ "$#" -ne 2 ]]; then
  echo "Usage: $0 <path_to_avro_schema_file> <path_to_json_payload_file>" >&2
  exit 1
fi

if [[ ! -f "$1" ]]; then
  echo "Schema file '$1' does not exist."
  exit 2
fi

if [[ ! -f "$2" ]]; then
  echo "Payload file '$2' does not exist."
  exit 3
fi

jq -n \
  --arg schema "$(cat "$1" | base64)" \
  --arg payload "$(cat "$2" | base64)" \
  '{schema: $schema, payload: $payload}'
