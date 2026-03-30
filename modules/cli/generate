#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Where is this script executing from ? Lets find out...
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
export ORIGINAL_DIR=$(pwd)
cd "${BASEDIR}"

# Allow an environment variable to over-ride the location of the openapi generator cli tool
# otherwise set a default.
if [[ -z $OPENAPI_GENERATOR_CLI_JAR ]]; then
    export OPENAPI_GENERATOR_CLI_JAR=${BASEDIR}/build/dependencies/openapi-generator-cli.jar
    echo "The location of the open generator jar is not specified. Defaulting it to ${OPENAPI_GENERATOR_CLI_JAR}"
    echo "Set the OPENAPI_GENERATOR_CLI_JAR environment variable to over-ride this setting."
    exit 1
fi

# OpenAPigen should be 6.0.1

# Make sure that the openapi generator cli tool is present where we expect.
if [[ ! -e ${OPENAPI_GENERATOR_CLI_JAR} ]]; then
    echo "The openapi generator is not found at ${OPENAPI_GENERATOR_CLI_JAR}."
    echo "Download it and set the OPENAPI_GENERATOR_CLI_JAR environment variable to point to it."
    exit 1
fi

export OPENAPI_YAML_FILE="${BASEDIR}/build/dependencies/openapi.yaml"

if [[ ! -e ${OPENAPI_YAML_FILE} ]]; then  
    echo "This build requires that the galasa.dev/framework repository is checked-out to ${BASEDIR}"
    exit 1
fi

java -jar ${OPENAPI_GENERATOR_CLI_JAR} generate \
-i ${OPENAPI_YAML_FILE} \
-g go \
-o pkg/galasaapi \
--additional-properties=packageName=galasaapi \
--global-property=apiTests=false

rm pkg/galasaapi/go.mod
rm pkg/galasaapi/go.sum