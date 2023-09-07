#!/bin/bash

PRINT_CONFIG_SCRIPT=$(dirname "${0}")/print-config.sh

# Generate config files from the payload data in the source test data directory tree
# and the schema files in the schema directory, placing them in corresponding locations
# in the destination test data directory tree.  This is meant to be used in conjunction
# with the decode-test-data.sh tool to update test data payloads.

if [[ "$#" -ne 3 ]]; then
  echo "Usage: $0 <path_to_avro_schema_dir> <path_to_src_test_data_dir> <path_to_dest_test_data_dir>" >&2
  exit 1
fi

ENV_AVRO="${1}/Environment.avsc"

if [[ ! -f ${ENV_AVRO} ]]; then
  echo "Expected Environment schema file ${ENV_AVRO} not found."
  exit 2
fi

LZ_AVRO="${1}/LandingZone.avsc"

if [[ ! -f ${LZ_AVRO} ]]; then
  echo "Expected Environment schema file ${LZ_AVRO} not found."
  exit 2
fi

if [[ ! -d "$2" ]]; then
  echo "Source directory '$2' does not exist."
  exit 4
fi

if [[ ! -d "$2" ]]; then
  echo "Source directory '$2' does not exist."
  exit 4
fi

if [[ ! -d "$3" ]]; then
  echo "Destination directory '$3' does not exist."
  exit 5
fi

for subdir in ${2}/*; do
  TEST_CFG=$(basename "${subdir}")
  SRC_ENV_DIR="${subdir}/v0/environment"

  if [[ ! -d "${SRC_ENV_DIR}" ]]; then
    continue
  fi

  if [[ ! -f "${SRC_ENV_DIR}/payload.json" ]]; then
    continue
  fi

  DST_ENV_DIR="${3}/${TEST_CFG}/v0/environment"
  mkdir -p "${DST_ENV_DIR}"
  ${PRINT_CONFIG_SCRIPT} "${ENV_AVRO}" "${SRC_ENV_DIR}/payload.json" > "${DST_ENV_DIR}/config.json"
  #echo "Writing config from schema '${ENV_AVRO}' and payload '${SRC_ENV_DIR}/payload.json' to '${DST_ENV_DIR/config.json}'"

  SRC_LZ_DIR="${subdir}/v0/landingzones"

  if [[ ! -d "${SRC_LZ_DIR}" ]]; then
    continue
  fi

  for lzdir in ${SRC_LZ_DIR}/*; do
      LZ_REGION="$(basename "${lzdir}")"
      DST_LZ_DIR="${3}/${TEST_CFG}/v0/landingzones/${LZ_REGION}"

        if [[ ! -f "${lzdir}/payload.json" ]]; then
          continue
        fi

      mkdir -p "${DST_LZ_DIR}"
      ${PRINT_CONFIG_SCRIPT} "${LZ_AVRO}" "${lzdir}/payload.json" > "${DST_LZ_DIR}/config.json"
      #echo "Writing config from schema '${LZ_AVRO}' and payload '${lzdir}/payload.json' to '${DST_LZ_DIR}/config.json'"
  done

done