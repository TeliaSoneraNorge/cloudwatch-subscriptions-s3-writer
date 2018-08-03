#!/bin/sh

export TERM=${TERM:-dumb}
export GRADLE_USER_HOME=~/gradle_2_3_cache/.gradle
cd  cloudwatch-lambda
gradle buildZip