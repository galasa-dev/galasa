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
    cd "Base Dir ${BASEDIR}"

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
        export bootstrap="https://galasa-ecosystem1.galasa.dev/api/bootstrap"
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

    info "Command is: $cmd1"
    info "Command is: $cmd2"
    info "Command is: $cmd3"
    info "Command is: $cmd4"

    $cmd1
    $cmd2
    $cmd3
    $cmd4
    local rc=$?

    # We expect a return code of 0 because this is a properly formed properties set command.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to create property with name and value used."
        return 1
    fi
    
    return 0
}

#-----------------------------------------------------------------------------------------
# Tests
#----------------------------------------------------------------------------------------- 
function streams_create {
    h2 "Performing properties set with name and value parameter used: create..."

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.
    
    stream_name="mystream"
    configure_stream "$stream_name" "$bootstrap"
    rc=$?
    
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to configure stream properties."
        exit 1
    fi

    # check that stream has been created
    cmd="${BINARY_LOCATION} streams get \
    --name $stream_name \
    --bootstrap $bootstrap \
    --log -"

    info "Command is: $cmd"

    output_file="$ORIGINAL_DIR/temp/streams-get-output.txt"
    $cmd | tee $output_file
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get stream with name used: command failed."
        exit 1
    fi

    # Check that the previous properties set created a stream property
    cat $output_file | grep "$stream_name" -q

    rc=$?
    # We expect a return code of 0 because this is a properly formed streams get command.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to create stream with name and value used."
        exit 1
    fi

    # Check that the previous properties set created a stream, with the correct values
    cat $output_file | grep "Galasa" -q -E
    cat $output_file | grep "mystream" -q -E
    cat $output_file | grep "enabled" -q -E

    rc=$?
    # We expect a return code of 0 because this is a properly formed properties get command.
    if [[ "${rc}" != "0" ]]; then 
        error "Stream successfully created, but value incorrect."
        exit 1
    fi

    success "Stream set with name and value used seems to have been created correctly."
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
    cmd="${BINARY_LOCATION} streams get \
    --name $stream_name \
    --bootstrap $bootstrap \
    --log -"

    info "Command is: $cmd"

    output_file="$ORIGINAL_DIR/temp/streams-get-output.txt"
    $cmd | tee $output_file

    # Check for successful deletion, it should return 404 response code
    cat $output_file | grep "GAL5420E: Unable to retrieve a stream with the given stream name. No such stream exists." -q -E
    

    success "Properties delete with name used seems to have been deleted correctly."
}

function streams_tests {
    streams_create
    streams_delete
}

if [[ "$CALLED_BY_MAIN" == "" ]]; then
    source $BASEDIR/calculate-galasactl-executables.sh
    calculate_galasactl_executable
    streams_tests
fi