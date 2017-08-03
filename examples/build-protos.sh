#!/usr/bin/env bash

protoc --proto_path=src/main/proto -I=../proto-actor/src/main/proto --java_out=src/main/java/ src/main/proto/actor/proto/examples/remotebenchmark/*.proto
