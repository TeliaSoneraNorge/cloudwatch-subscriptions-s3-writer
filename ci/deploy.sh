#!/bin/sh
printenv
aws s3 cp dist/*.zip s3://telia-common-logs-prod-lambda