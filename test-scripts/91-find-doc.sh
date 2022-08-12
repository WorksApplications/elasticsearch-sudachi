#!/usr/bin/env bash

ES_PORT=${ES_PORT:-9200}
INDEX_NAME=${INDEX_NAME:-test_sudachi}
QUERY=${1:-天皇}
curl -X GET "http://localhost:9200/$INDEX_NAME/_search" \
 -H 'Content-Type: application/json' \
 -d @- <<EOF
{
    "query": {
        "multi_match": {
            "query": "$QUERY",
            "fields": ["text*"],
            "type": "most_fields"
        }
    }
}
EOF