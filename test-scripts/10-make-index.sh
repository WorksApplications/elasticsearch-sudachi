#!/usr/bin/env bash

SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]:-$0}"; )" &> /dev/null && pwd 2> /dev/null; )";
ES_PORT=${ES_PORT:-9200}
INDEX_NAME=${INDEX_NAME:-test_sudachi}

curl -X PUT "localhost:$ES_PORT/$INDEX_NAME" \
  -H 'Content-Type: application/json' \
  -d "@$SCRIPT_DIR/10-init-index.json"