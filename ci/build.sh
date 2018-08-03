#!/bin/sh

export TERM=${TERM:-dumb}
cd cloudwatch-lambda
gradle --scan --no-daemon buildZip