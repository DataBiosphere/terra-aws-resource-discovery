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

BASE64_EXE="$(which base64)"

# Handle differences in OSX and GNU base64 implementations
if [[ "$OSTYPE" == "darwin"* ]]; then
  # Mac OSX
  BASE64_ENCODE_COMMAND="$BASE64_EXE -i"
else
  # GNU/Linux
  BASE64_ENCODE_COMMAND="$BASE64_EXE --wrap=0"
fi

jq -n -M \
  --arg schema "$($BASE64_ENCODE_COMMAND "$1")" \
  --arg payload "$($BASE64_ENCODE_COMMAND "$2")" \
  '{schema: $schema, payload: $payload}'
