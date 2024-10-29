#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

nohup ++JAVA_CMD++ -jar ++SIMPLATFORM_JAR++ > ++SIMPLATFORM_CONSOLE++ &

echo PROCESS=$!

sleep 2