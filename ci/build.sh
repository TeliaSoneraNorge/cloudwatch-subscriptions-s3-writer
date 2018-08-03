#!/bin/sh

export TERM=${TERM:-dumb}
cd cloudwatch-lambda
sudo gradle buildZip