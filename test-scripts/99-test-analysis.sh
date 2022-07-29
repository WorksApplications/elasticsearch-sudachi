#!/usr/bin/env bash

ES_PORT=${ES_PORT:-9200}
INDEX_NAME=${INDEX_NAME:-test_sudachi}
ANALYZER_NAME=${ANALYZER_NAME:-sudachi_analyzer}
curl -X GET "localhost:9200/$INDEX_NAME/_analyze?pretty" \
 -H 'Content-Type: application/json' \
 -d @- <<EOF
{"analyzer":"sudachi_baseform_analyzer", "text" : "$1"}
EOF