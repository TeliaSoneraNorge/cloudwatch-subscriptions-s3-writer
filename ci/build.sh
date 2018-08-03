#!/bin/sh

export TERM=${TERM:-dumb}
cd  cloudwatch-lambda
./gradlew --no-daemon buildZip