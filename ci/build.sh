#!/bin/sh
gradle -p cloudwatch-lambda test buildZip
cp cloudwatch-lambda/build/distributions/*.zip dist