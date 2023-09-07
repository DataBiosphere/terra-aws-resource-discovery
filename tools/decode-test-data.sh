#!/bin/bash

PARSE_PAYLOAD_SCRIPT=$(dirname "${0}")/parse-payload.sh

# Parse test data payloads into a directory out of the source tree for editing.  These payloads can then
# be written back to the tree (along with any schema updates) using the encode-test-data.sh script.

if [[ "$#" -ne 2 ]]; then
  echo "Usage: $0 <path_to_src_test_data_dir> <path_to_dest_test_data_dir>" >&2
  exit 1
fi

if [[ ! -d "$1" ]]; then
  echo "Source directory '$1' does not exist."
  exit 2
fi

if [[ ! -d "$2" ]]; then
  echo "Destination directory '$2' does not exist."
  exit 3
fi

for subdir in ${1}/*; do
  TEST_CFG=$(basename "${subdir}")
  SRC_ENV_DIR="${subdir}/v0/environment"

  if [[ ! -d "${SRC_ENV_DIR}" ]]; then
    continue
  fi

  if [[ ! -f "${SRC_ENV_DIR}/config.json" ]]; then
    continue
  fi

  DST_ENV_DIR="${2}/${TEST_CFG}/v0/environment"
  mkdir -p "${DST_ENV_DIR}"
  ${PARSE_PAYLOAD_SCRIPT} "${SRC_ENV_DIR}/config.json" > "${DST_ENV_DIR}/payload.json"

  SRC_LZ_DIR="${subdir}/v0/landingzones"

  if [[ ! -d "${SRC_LZ_DIR}" ]]; then
    continue
  fi

  for lzdir in ${SRC_LZ_DIR}/*; do
      LZ_REGION="$(basename "${lzdir}")"
      DST_LZ_DIR="${2}/${TEST_CFG}/v0/landingzones/${LZ_REGION}"

        if [[ ! -f "${lzdir}/config.json" ]]; then
          continue
        fi

      mkdir -p "${DST_LZ_DIR}"
      ${PARSE_PAYLOAD_SCRIPT} "${lzdir}/config.json" > "${DST_LZ_DIR}/payload.json"
  done

done