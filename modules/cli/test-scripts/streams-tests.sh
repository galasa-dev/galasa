#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
echo "Running script streams-tests.sh"
# This script can be ran locally or executed in a pipeline to test the various built binaries of galasactl
# This script tests the 'galasactl streams' command in the framework namespace that is in our ecosystem's cps namespaces already
# Pre-requesite: the CLI must have been built first so the binaries are present in the /bin directory

if [[ "$CALLED_BY_MAIN" == "" ]]; then
    # Where is this script executing from ?
    BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
    export ORIGINAL_DIR=$(pwd)
    cd "${BASEDIR}"

    #--------------------------------------------------------------------------
    #
    # Set Colors
    #
    #--------------------------------------------------------------------------
    bold=$(tput bold)
    underline=$(tput sgr 0 1)
    reset=$(tput sgr0)

    red=$(tput setaf 1)
    green=$(tput setaf 76)
    white=$(tput setaf 7)
    tan=$(tput setaf 202)
    blue=$(tput setaf 25)

    #--------------------------------------------------------------------------
    #
    # Headers and Logging
    #
    #--------------------------------------------------------------------------
    underline() { printf "${underline}${bold}%s${reset}\n" "$@" ;}
    h1() { printf "\n${underline}${bold}${blue}%s${reset}\n" "$@" ;}
    h2() { printf "\n${underline}${bold}${white}%s${reset}\n" "$@" ;}
    debug() { printf "${white}%s${reset}\n" "$@" ;}
    info() { printf "${white}➜ %s${reset}\n" "$@" ;}
    success() { printf "${green}✔ %s${reset}\n" "$@" ;}
    error() { printf "${red}✖ %s${reset}\n" "$@" ;}
    warn() { printf "${tan}➜ %s${reset}\n" "$@" ;}
    bold() { printf "${bold}%s${reset}\n" "$@" ;}
    note() { printf "\n${underline}${bold}${blue}Note:${reset} ${blue}%s${reset}\n" "$@" ;}

    #-----------------------------------------------------------------------------------------                   
    # Process parameters
    #-----------------------------------------------------------------------------------------                   
    bootstrap=""

    while [ "$1" != "" ]; do
        case $1 in
            --bootstrap )                     shift
                                            bootstrap="$1"
                                            ;;
            -h | --help )                     usage
                                            exit
                                            ;;
            * )                               error "Unexpected argument $1"
                                            usage
                                            exit 1
        esac
        shift
    done

    # Can't really verify that the bootstrap provided is a valid one, but galasactl will pick this up later if not
    if [[ "${bootstrap}" == "" ]]; then
        export bootstrap="https://galasa-service1.galasa.dev/api/bootstrap"
        info "No bootstrap supplied. Defaulting the --bootstrap to be ${bootstrap}"
    fi

    info "Running tests against ecosystem bootstrap ${bootstrap}"
fi

#-----------------------------------------------------------------------------------------                   
    # Utility functions
#-----------------------------------------------------------------------------------------                   

function configure_stream {
    local stream_name=$1
    local bootstrap=$2
    
    local desc_prop_name="test.stream.${stream_name}.description"
    local maven_prop_name="test.stream.${stream_name}.repo"
    local testCatalog_prop_name="test.stream.${stream_name}.location"
    local obr_prop_name="test.stream.${stream_name}.obr"

    # Define property settings with their commands
    local cmd1="${BINARY_LOCATION} properties set --namespace framework \
    --name $desc_prop_name \
    --value Galasa \
    --bootstrap $bootstrap \
    --log -"

    local cmd2="${BINARY_LOCATION} properties set --namespace framework \
    --name $maven_prop_name \
    --value http://points-to-my-maven-repo.example.org \
    --bootstrap $bootstrap \
    --log -"

    local cmd3="${BINARY_LOCATION} properties set --namespace framework \
    --name $testCatalog_prop_name \
    --value http://points-to-my-test-catalog.example.org \
    --bootstrap $bootstrap \
    --log -"

    local cmd4="${BINARY_LOCATION} properties set --namespace framework \
    --name $obr_prop_name \
    --value mvn:myorg/myartifact/0.0.1/obr \
    --bootstrap $bootstrap \
    --log -"

    info "Adding description property: $cmd1"
    info "Adding maven repo url property: $cmd2"
    info "Adding test catalog url property: $cmd3"
    info "Adding obr property: $cmd4"

    for cmd_var in cmd1 cmd2 cmd3 cmd4; do
        # Use indirect reference to execute the command variable
        eval "\${$cmd_var}"
        rc=$?
        if [[ "${rc}" != "0" ]]; then 
            error "Failed to execute command: ${!cmd_var}"
            return 1
        fi
    done

}

function get_stream {
    local stream_name=$1
    local bootstrap=$2
    local output_file=$3
    local format=$4
    
    local cmd="${BINARY_LOCATION} streams get \
    --name $stream_name \
    --bootstrap $bootstrap \
    --format $format \
    --log -"

    info "Getting stream with command: $cmd"

    $cmd | tee "$output_file"
    local rc=$?
    
    return $rc
}

#-----------------------------------------------------------------------------------------
# Tests
#----------------------------------------------------------------------------------------- 
function streams_get_all {
    h2 "Creating a test stream for get test"

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.
    
    stream_name="mystream"

    configure_stream "$stream_name" "$bootstrap"
    rc=$?
    
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to configure stream properties with name $stream_name."
        exit 1
    fi

    configure_stream "$stream_name_2" "$bootstrap"
    rc=$?

    if [[ "${rc}" != "0" ]]; then 
        error "Failed to configure stream properties with name $stream_name_2."
        exit 1
    fi

    # check that stream has been created
    output_file="$ORIGINAL_DIR/temp/streams-get-output.txt"
    local cmd="${BINARY_LOCATION} streams get \
    --bootstrap $bootstrap \
    --log -"

    info "Fetching all streams"
    $cmd
    rc=$?

    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get streams: command failed."
        exit 1
    fi

    success "Fetched all streams"
}

function streams_create {
    h2 "Creating a test stream to test successful stream creation"

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.
    
    stream_name="mystream"
    configure_stream "$stream_name" "$bootstrap"
    rc=$?
    
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to configure stream properties."
        exit 1
    fi

    # check that stream has been created
    output_file="$ORIGINAL_DIR/temp/streams-get-output.txt"
    get_stream "$stream_name" "$bootstrap" "$output_file" "summary"
    rc=$?

    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get stream with name used: command failed."
        exit 1
    fi

    # Check that the previous properties set created a stream property
    cat $output_file | grep "$stream_name" -q

    rc=$?
    # We expect a return code of 0 because this is a properly formed streams get command.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get stream with name $stream_name."
        exit 1
    fi

    # Check that the previous properties set created a stream, with the correct values
    grep -qF -- 'Galasa' "$output_file" \
    || { error "Missing 'Galasa' in stream output"; exit 1; }

    # 2) Check for the stream name
    grep -qF -- "$stream_name" "$output_file" \
    || { error "Missing '$stream_name' in stream output"; exit 1; }

    # 3) Check for “enabled”
    grep -qF -- 'enabled' "$output_file" \
    || { error "Missing 'enabled' in stream output"; exit 1; }

    success "Stream created successfully with all expected values."

}

function streams_get_yaml_format {
    h2 "Creating a test stream to get stream in YAML format"

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.
    
    stream_name="mystream"
    configure_stream "$stream_name" "$bootstrap"
    rc=$?
    
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to configure stream properties."
        exit 1
    fi

    # check that stream has been created
    output_file="$ORIGINAL_DIR/temp/streams-get-output.txt"
    get_stream "$stream_name" "$bootstrap" "$output_file" "yaml"
    rc=$?

    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get stream with name used: command failed."
        exit 1
    fi

    # Check that the previous properties set created a stream property
    cat $output_file | grep "$stream_name" -q

    rc=$?
    # We expect a return code of 0 because this is a properly formed streams get command.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get stream with name $stream_name."
        exit 1
    fi

    # Check that the previous properties set created a stream, with the correct values
    grep -qF -- 'Galasa' "$output_file" \
    || { error "Missing 'Galasa' in stream output"; exit 1; }

    # 2) Check for the stream name
    grep -qF -- "$stream_name" "$output_file" \
    || { error "Missing '$stream_name' in stream output"; exit 1; }

    # 3) Check for “enabled”
    grep -qF -- 'true' "$output_file" \
    || { error "Missing 'enabled' in stream output"; exit 1; }

    success "Stream set with name and value used seems to have been created correctly and formatted in YAML"
}

function streams_get_missing_name {
    h2 "Creating a test stream if stream is not present"

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.
    
    # check that stream has been created
    output_file="$ORIGINAL_DIR/temp/streams-get-output.txt"
    get_stream "some-unknown-stream" "$bootstrap" "$output_file" "summary"

    if grep -qF \
        'GAL5420E: Unable to retrieve a stream with the given stream name. No such stream exists.' \
        "$output_file"
    then
        success "Stream with the name was missing and threw an error as expected."
    else
        error "Expected 404 error message not found."
        exit 1
    fi

}

function streams_delete {
    h2 "Performing streams delete with name parameter used..."

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.
    
    stream_name="mystream"
    configure_stream "$stream_name" "$bootstrap"
    rc=$?
    
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to configure stream properties."
        exit 1
    fi

    cmd="${BINARY_LOCATION} streams delete \
    --name $stream_name \
    --bootstrap $bootstrap \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of 0 because this is a properly formed streams delete command.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to delete stream, command failed."
        exit 1
    fi

    # check that stream has been deleted
    output_file="$ORIGINAL_DIR/temp/streams-get-output.txt"
    get_stream "$stream_name" "$bootstrap" "$output_file" "summary"

    if grep -qF \
        'GAL5420E: Unable to retrieve a stream with the given stream name. No such stream exists.' \
        "$output_file"
    then
        success "Stream with the name was deleted correctly."
    else
        error "Deletion verification failed: expected 404 error message not found."
        exit 1
    fi
        
}

function streams_tests {
    streams_create
    streams_get_yaml_format
    streams_get_missing_name
    streams_get_all
    streams_delete
}

if [[ "$CALLED_BY_MAIN" == "" ]]; then
    source $BASEDIR/calculate-galasactl-executables.sh
    calculate_galasactl_executable
    streams_tests
fi