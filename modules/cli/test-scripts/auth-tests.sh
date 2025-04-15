#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
echo "Running script auth-tests.sh"
# This script can be ran locally or executed in a pipeline to test the various built binaries of galasactl
# This script tests the 'galasactl auth tokens get' command against a test that is in our ecosystem's testcatalog already
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
        export bootstrap="https://galasa-ecosystem1.galasa.dev/api/bootstrap"
        info "No bootstrap supplied. Defaulting the --bootstrap to be ${bootstrap}"
    fi

    info "Running tests against ecosystem bootstrap ${bootstrap}"
fi

#-----------------------------------------------------------------------------------------
# Tests
#-----------------------------------------------------------------------------------------

function auth_tokens_get_all_tokens_without_loginId {

    h2 "Performing auth tokens get without loginId: get..."

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.

    cmd="${BINARY_LOCATION} auth tokens get \
    --bootstrap $bootstrap \
    --log -
    "

    info "Command is: $cmd"
    mkdir -p $ORIGINAL_DIR/temp
    output_file="$ORIGINAL_DIR/temp/auth-get-output.txt"
    $cmd | tee $output_file
    rc=$?

    # We expect a return code of 0 because this is a properly formed auth tokens get command.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get access tokens."
        exit 1
    fi

    # Checks that the tokens were fetched successfully
    cat $output_file | grep "Total: 1" -q

    success "All access tokens fetched from database successfully."

}


function auth_tokens_get_all_tokens_by_loginId {

    h2 "Performing auth tokens get with loginId: get..."
    loginId="galasa-team"

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.

    cmd="${BINARY_LOCATION} auth tokens get \
    --user $loginId \
    --bootstrap $bootstrap \
    --log -
    "

    info "Command is: $cmd"
    mkdir -p $ORIGINAL_DIR/temp
    output_file="$ORIGINAL_DIR/temp/auth-get-output.txt"

    $cmd | tee $output_file
    rc=$?
    
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to fetch access tokens by login ID"
        exit 1
    fi

    # Checks that the tokens were fetched successfully
    cat $output_file | grep "Total: 1" -q

    success "All access tokens by loginId fetched from database successfully."

}

#--------------------------------------------------------------------------

function auth_tests {
    auth_tokens_get_all_tokens_without_loginId
    auth_tokens_get_all_tokens_by_loginId
}

# checks if it's been called by main, set this variable if it is
if [[ "$CALLED_BY_MAIN" == "" ]]; then
    source $BASEDIR/calculate-galasactl-executables.sh
    calculate_galasactl_executable
    auth_tests
fi