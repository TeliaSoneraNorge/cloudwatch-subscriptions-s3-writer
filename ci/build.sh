#!/bin/sh
gradle -p cloudwatch-lambda test buildZip
cp build/distributions/*.zip dist