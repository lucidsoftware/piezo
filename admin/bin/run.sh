#!/bin/bash

echo "Run 'make stage' before running this script."

SOURCE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR="$SOURCE_DIR/.."
QUARTZ_PROPS_FILE="$BASE_DIR/conf/quartz.properties"
START="$BASE_DIR/target/universal/stage/bin/admin"

$START -Dorg.quartz.properties=$QUARTZ_PROPS_FILE -Dpidfile.path=/tmp/pid -Dnetworkaddress.cache.ttl=10 -Dnetworkaddress.cache.negative.ttl=10 -Dhttp.port=8001
