#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
cd "${BASEDIR}"

mkdir -p $BASEDIR/temp
rm -fr $BASEDIR/temp/*output.txt


if [[ "$(uname -o)" == "Darwin" ]]; then
    echo "You are trying to run this test script on Mac, which doesn't support associative arrays or the declare -A syntax."
    echo "Switch to linux, perhaps inside a docker container to test more"
    echo "like this:"
    echo "docker run -it -v $BASEDIR/mnt/test ubuntu:latest /mnt/test/get-changed-modules-pull-request-test.sh"
    exit 1
fi


function test1_cli() {
    cat << EOF > $BASEDIR/temp/test-1-input.txt
modules/cli/a
modules/cli/b
EOF

    $BASEDIR/get-changed-modules-pull-request.sh --test --test-input-file $BASEDIR/temp/test-1-input.txt --test-output-file $BASEDIR/temp/test-1-output.txt

    is_cli_changed_detected=$(cat $BASEDIR/temp/test-1-output.txt | grep "CLI_CHANGED=true" | wc -l | xargs)
    if [[ "$is_cli_changed_detected" != "1" ]]; then 
        echo  "Failed to detect CLI module change on test 1. is_cli_changed_detected: $is_cli_changed_detected"
        exit 1
    fi


    is_docs_changed_detected=$(cat $BASEDIR/temp/test-1-output.txt | grep "DOCS_CHANGED=true" | wc -l | xargs)
    if [[ "$is_docs_changed_detected" != "1" ]]; then 
        echo  "Failed to detect docs module should change if the CLI files change in test 1. is_docs_changed_detected: $is_docs_changed_detected"
        exit 1
    fi

    echo "Test 1 passed"

}

function test2_docs() {
    cat << EOF > $BASEDIR/temp/test-2-input.txt
docs/a
docs/b
EOF

    $BASEDIR/get-changed-modules-pull-request.sh --test --test-input-file $BASEDIR/temp/test-2-input.txt --test-output-file $BASEDIR/temp/test-2-output.txt

    is_docs_changed_detected=$(cat $BASEDIR/temp/test-2-output.txt | grep "DOCS_CHANGED=true" | wc -l | xargs)
    if [[ "$is_docs_changed_detected" != "1" ]]; then 
        echo  "Failed to detect docs module change on test 2. is_docs_changed_detected: $is_docs_changed_detected"
        exit 1
    fi

    is_cli_changed_detected=$(cat $BASEDIR/temp/test-2-output.txt | grep "CLI_CHANGED=true" | wc -l | xargs)
    if [[ "$is_cli_changed_detected" != "0" ]]; then 
        echo  "CLI module should not be marked as changed if only docs module changes. Test 2. is_cli_changed_detected: $is_cli_changed_detected"
        exit 1
    fi

    echo "Test 2 passed"
}

function test3_platform() {
    cat << EOF > $BASEDIR/temp/test-3-input.txt
modules/platform/a
modules/platform/b
EOF

    $BASEDIR/get-changed-modules-pull-request.sh --test --test-input-file $BASEDIR/temp/test-3-input.txt --test-output-file $BASEDIR/temp/test-3-output.txt

    is_docs_changed_detected=$(cat $BASEDIR/temp/test-3-output.txt | grep "DOCS_CHANGED=true" | wc -l | xargs)
    if [[ "$is_docs_changed_detected" != "0" ]]; then 
        echo  "Docs module should not change if platform changes. is_docs_changed_detected: $is_docs_changed_detected"
        exit 1
    fi

    is_cli_changed_detected=$(cat $BASEDIR/temp/test-3-output.txt | grep "CLI_CHANGED=true" | wc -l | xargs)
    if [[ "$is_cli_changed_detected" != "0" ]]; then 
        echo  "CLI module should not change if platform changes. is_cli_changed_detected: $is_cli_changed_detected"
        exit 1
    fi

    is_framework_changed_detected=$(cat $BASEDIR/temp/test-3-output.txt | grep "FRAMEWORK_CHANGED=true" | wc -l | xargs)
    if [[ "$is_framework_changed_detected" != "1" ]]; then 
        echo  "Framework module should change if platform changes. is_framework_changed_detected: $is_framework_changed_detected"
        exit 1
    fi

    echo "Test 3 passed"
}

test1_cli
test2_docs
test3_platform

