#!/usr/bin/env bash

ES_PORT=${ES_PORT:-9200}
INDEX_NAME=${INDEX_NAME:-test_sudachi}
curl -X PUT "localhost:9200/$INDEX_NAME/_doc/test" \
 -H 'Content-Type: application/json' \
 -d @- <<EOF
{"text":"田中羽尾子である"}
EOF