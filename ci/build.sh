#!/bin/sh

export TERM=${TERM:-dumb}
cd cloudwatch-lambda
gradle --stacktrace --no-daemon buildZip