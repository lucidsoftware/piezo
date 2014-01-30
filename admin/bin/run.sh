#!/bin/bash

SOURCE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DEPS_DIR="$SOURCE_DIR/../target/staged"
QUARTZ_PROPS_FILE="$SOURCE_DIR/../conf/quartz.properties"

java -Dorg.quartz.properties=$QUARTZ_PROPS_FILE -Dpidfile.path=/tmp/pid -Dnetworkaddress.cache.ttl=10 -Dnetworkaddress.cache.negative.ttl=10 -Dhttp.port=8001 -cp "${DEPS_DIR}/*" play.core.server.NettyServer
