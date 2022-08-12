#!/usr/bin/env bash

set -euxo pipefail
DEFAULT_ES_VERSION=8.2.3
ES_VERSION=${ES_VERSION:-$DEFAULT_ES_VERSION}
SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]:-$0}"; )" &> /dev/null && pwd 2> /dev/null; )";
ES_URL="https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-${ES_VERSION}-linux-x86_64.tar.gz"
WORK_DIR="${WORK_DIR:-$(readlink -f "$SCRIPT_DIR/../build/integration")}"
ES_DIR="$WORK_DIR/elasticsearch-${ES_VERSION}"
ES_FILE="$WORK_DIR/elasticsearch-${ES_VERSION}-linux-x86_64.tar.gz"
DIC_VERSION=${DIC_VERSION:-latest}
unset JAVA_HOME

PLUGIN_PATH="$SCRIPT_DIR/../build/distributions/analysis-sudachi-$ES_VERSION-$PLUGIN_VERSION.zip"

if [[ ! -f "$PLUGIN_PATH" ]]; then
  echo "Plugin is not built, run ./gradlew build"
  exit 1
fi

mkdir -p "$WORK_DIR"
pushd "$PWD"

cd "$WORK_DIR"

if [[ ! -f $ES_FILE ]]; then
  wget "$ES_URL"
fi

PLUGIN_PATH="$(readlink -f "$PLUGIN_PATH")"
PLUGIN="file://$PLUGIN_PATH"

if [[ ! -d "$ES_DIR" ]]; then
  tar xf "$ES_FILE"
fi

if [[ -d "$ES_DIR/plugins/analysis-sudachi" ]]; then
  "$ES_DIR/bin/elasticsearch-plugin" remove analysis-sudachi
fi

"$ES_DIR/bin/elasticsearch-plugin" install "$PLUGIN"

cp "$SCRIPT_DIR/elasticsearch.yml" "$ES_DIR/config/elasticsearch.yml"

DIC_ZIP_PATH="$WORK_DIR/sudachi-dictionary-$DIC_VERSION-small.zip"

if [[ ! -f "$DIC_ZIP_PATH" ]]; then
  wget "http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-$DIC_VERSION-small.zip"
fi

if [[ "$ES_DIR/config/sudachi/system_core.dic" -ot "$DIC_ZIP_PATH" ]]; then
  unzip -p "$DIC_ZIP_PATH" "*/system_small.dic" > "$ES_DIR/config/sudachi/system_core.dic"
fi


# log sudachi plugin messages with debug level
if ! grep -qF com.worskap "$ES_DIR/config/log4j2.properties"; then
cat >> "$ES_DIR/config/log4j2.properties" <<'EOF'
# sudachi debugging
logger.com.worksap.nlp.level = debug
EOF
fi

popd