#!/usr/bin/env bash

set -euxo pipefail
DEFAULT_ES_VERSION=8.2.3
ES_VERSION=${ES_VERSION:-$DEFAULT_ES_VERSION}
SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]:-$0}"; )" &> /dev/null && pwd 2> /dev/null; )";
ES_URL="https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-${ES_VERSION}-linux-x86_64.tar.gz"
ES_FILE="elasticsearch-${ES_VERSION}-linux-x86_64.tar.gz"
ES_DIR=elasticsearch-${ES_VERSION}

if [[ -z "$PLUGIN_VERSION" ]]; then
  echo "PLUGIN_VERSION variable is not set"
  exit 1
fi

PLUGIN_PATH="$SCRIPT_DIR/../build/libs/analysis-sudachi-$PLUGIN_VERSION.jar"

if ! [[ -f "$PLUGIN_PATH" ]]; then
  echo "Plugin is not built, run ./gradlew build"
  exit 1
fi

# make an absolute path to plugin
PLUGIN_PATH=$(readlink -f "$PLUGIN_PATH")

if ! [[ -f $ES_FILE ]]; then
  wget "$ES_URL" -o "$ES_FILE"
fi

PLUGIN="file://$PLUGIN_PATH"

tar xf "$ES_FILE"
"$ES_DIR/bin/elasticsearch-plugin" install "$PLUGIN"