#!/bin/sh

export TERM=${TERM:-dumb}
cd  cloudwatch-lambda
gradle buildZip