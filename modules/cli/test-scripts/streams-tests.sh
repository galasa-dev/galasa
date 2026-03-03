#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
echo "Running script streams-tests.sh"
set -o pipefail

# This script can be ran locally or executed in a pipeline to test the various built binaries of galasactl
# This script tests the 'galasactl streams' commands to create, read, update, and delete streams in a Galasa service
# Pre-requisite: the CLI must have been built first so the binaries are present in the /bin directory

function usage {
    info "Syntax: streams-tests.sh [OPTIONS]"
    cat << EOF
Options are:
-h | --help : Display this help text
--bootstrap : The bootstrap URL of the Galasa service to run tests against
EOF
}

if [[ "$CALLED_BY_MAIN" == "" ]]; then
    # Where is this script executing from ?
    BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
    export ORIGINAL_DIR=$(pwd)
    cd "${BASEDIR}"
    mkdir -p ${BASEDIR}/temp

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
            --bootstrap )                   shift
                                            bootstrap="$1"
                                            ;;
            -h | --help )                   usage
                                            exit
                                            ;;
            * )                             error "Unexpected argument $1"
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

    info "Running tests against service bootstrap ${bootstrap}"
fi

#-----------------------------------------------------------------------------------------
# Helper Functions
#-----------------------------------------------------------------------------------------
function set_stream {
    stream_name="$1"
    description="$2"
    maven_repo_url="$3"
    testcatalog_url="$4"
    obr="$5"

    cmd="${BINARY_LOCATION} streams set \
    --name ${stream_name} \
    --description ${description} \
    --bootstrap ${bootstrap} \
    --log -"

    if [[ -n "${maven_repo_url}" ]]; then
        cmd="${cmd} --maven-repo-url ${maven_repo_url}"
    fi

    if [[ -n "${testcatalog_url}" ]]; then
        cmd="${cmd} --testcatalog-url ${testcatalog_url}"
    fi

    if [[ -n "${obr}" ]]; then
        cmd="${cmd} --obr ${obr}"
    fi

    info "Creating stream with command: $cmd"
    ${BINARY_LOCATION} streams set \
    --name "${stream_name}" \
    --description "${description}" \
    ${maven_repo_url:+--maven-repo-url "${maven_repo_url}"} \
    ${testcatalog_url:+--testcatalog-url "${testcatalog_url}"} \
    ${obr:+--obr "${obr}"} \
    --bootstrap "${bootstrap}" \
    --log -
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to set stream."; exit 1; fi
}

function delete_stream {
    stream_name="$1"

    cmd="${BINARY_LOCATION} streams delete \
    --name ${stream_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Deleting stream with command: $cmd"
    $cmd
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to delete stream."; exit 1; fi
}

#-----------------------------------------------------------------------------------------
# Tests
#-----------------------------------------------------------------------------------------
function test_can_create_stream {
    h2 "Creating a stream to test successful stream creation"

    stream_name="test-create-stream-$(date +%s)"
    description="testing that stream creation works"
    maven_repo_url="https://maven.example.com/repo"
    testcatalog_url="https://catalog.example.com/testcatalog.json"
    obr="mvn:dev.galasa/galasa-obr/0.1.0/obr"

    set_stream "${stream_name}" "${description}" "${maven_repo_url}" "${testcatalog_url}" "${obr}"

    # check that stream has been created
    output_file="$BASEDIR/temp/streams-set-check.txt"
    cmd="${BINARY_LOCATION} streams get \
    --name ${stream_name} \
    --log -"

    info "Checking the stream that was just created"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get the stream that was just created."; exit 1; fi

    # Check that the previous streams set command created a stream with the correct values
    cat ${output_file} | grep "${stream_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get stream with name ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "${description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${description}' in stream output."; exit 1; fi

    success "Stream created successfully with all expected values."
    delete_stream "${stream_name}"
}

function test_can_get_streams {
    h2 "Checking that we can get streams from the Galasa service"

    stream_name="test-get-streams-$(date +%s)"
    description="testing that stream retrieval works"
    maven_repo_url="https://maven.example.com/repo"
    testcatalog_url="https://maven.example.com/repo/testcatalog.json"
    obr="mvn:my-group/my-artifact/1.0.0/obr"

    set_stream "${stream_name}" "${description}" "${maven_repo_url}" "${testcatalog_url}" "${obr}"

    output_file="$BASEDIR/temp/streams-get-all-check.txt"
    cmd="${BINARY_LOCATION} streams get \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting streams with command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get streams"; exit 1; fi

    # Check that the previous streams set command created a stream with the given name
    cat ${output_file} | grep "${stream_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get stream with name ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "${description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${description}' in stream output."; exit 1; fi

    success "Streams retrieved successfully with expected values."
    delete_stream "${stream_name}"
}

function test_can_get_stream_by_name {
    h2 "Checking that we can get a stream by name from the Galasa service"

    stream_name="test-get-stream-by-name-$(date +%s)"
    description="testing that stream retrieval works"
    maven_repo_url="https://maven.example.com/repo"
    testcatalog_url="https://catalog.example.com/testcatalog.json"
    obr="mvn:my-group/my-artifact/1.0.0/obr"

    set_stream "${stream_name}" "${description}" "${maven_repo_url}" "${testcatalog_url}" "${obr}"

    output_file="$BASEDIR/temp/streams-get-by-name-check.txt"
    cmd="${BINARY_LOCATION} streams get \
    --name ${stream_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting stream with name '${stream_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get stream with name ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "${stream_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get stream with name ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "${description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${description}' in stream output."; exit 1; fi

    success "Stream retrieved successfully with expected values."
    delete_stream "${stream_name}"
}

function test_get_missing_stream_name_outputs_no_results {
    h2 "Checking that getting a non-existent stream returns a total of 0 results"

    stream_name="unknown-stream-$(date +%s)"

    output_file="$BASEDIR/temp/streams-get-unknown-name.txt"
    cmd="${BINARY_LOCATION} streams get \
    --name ${stream_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting stream with name '${stream_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get stream with name ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "Total:0" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Found a stream named '${stream_name}' when there should not have been any results"; exit 1; fi

    success "Streams get successfully returned no results."
}

function test_can_get_stream_in_yaml_format {
    h2 "Checking that we can get a stream in YAML format from the Galasa service"

    stream_name="test-get-stream-yaml-$(date +%s)"
    description="testing that stream YAML formatting works"
    maven_repo_url="https://maven.example.com/repo"
    testcatalog_url="https://catalog.example.com/testcatalog.json"
    obr="mvn:my-group/my-artifact/1.0.0/obr"

    set_stream "${stream_name}" "${description}" "${maven_repo_url}" "${testcatalog_url}" "${obr}"

    output_file="$BASEDIR/temp/streams-get-by-name-check.txt"
    cmd="${BINARY_LOCATION} streams get \
    --name ${stream_name} \
    --format yaml \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting stream with name '${stream_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get stream with name ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "name: ${stream_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get stream with name ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "description: ${description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${description}' in stream output."; exit 1; fi

    success "Stream retrieved successfully in YAML format."
    delete_stream "${stream_name}"
}

function test_can_update_stream {
    h2 "Checking that we can update an existing stream"

    stream_name="test-update-stream-$(date +%s)"
    old_description="testing that stream updates work"
    old_maven_repo_url="https://old-maven.example.com/repo"
    testcatalog_url="https://catalog.example.com/testcatalog.json"
    obr="mvn:my-group/my-artifact/1.0.0/obr"

    set_stream "${stream_name}" "${old_description}" "${old_maven_repo_url}" "${testcatalog_url}" "${obr}"

    # Now update the stream with new values
    new_description="updated description"
    new_maven_repo_url="https://new-maven.example.com/repo"
    new_testcatalog_url="https://new-catalog.example.com/testcatalog.json"
    set_stream "${stream_name}" "${new_description}" "${new_maven_repo_url}" "${new_testcatalog_url}" ""

    output_file="$BASEDIR/temp/streams-update-check.txt"
    cmd="${BINARY_LOCATION} streams get \
    --name ${stream_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting stream with name '${stream_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get a stream named ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "${stream_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get stream with name ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "${new_description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${new_description}' in stream output."; exit 1; fi

    cat ${output_file} | grep "${old_description}" -q
    rc=$? ; if [[ "${rc}" != "1" ]]; then error "Description for stream ${stream_name} wasn't updated"; exit 1; fi

    success "Stream retrieved successfully with updated values."
    delete_stream "${stream_name}"
}

function test_can_delete_stream {
    h2 "Checking that we can delete a stream"

    stream_name="test-delete-stream-$(date +%s)"
    description="testing that stream deletion works"
    maven_repo_url="https://maven.example.com/repo"
    testcatalog_url="https://catalog.example.com/testcatalog.json"
    obr="mvn:my-group/my-artifact/1.0.0/obr"

    set_stream "${stream_name}" "${description}" "${maven_repo_url}" "${testcatalog_url}" "${obr}"

    # Now delete the stream
    delete_stream "${stream_name}"

    output_file="$BASEDIR/temp/streams-delete-check.txt"
    cmd="${BINARY_LOCATION} streams get \
    --name ${stream_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting stream with name '${stream_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get stream named ${stream_name}."; exit 1; fi

    cat ${output_file} | grep "${stream_name}" -q
    rc=$? ; if [[ "${rc}" != "1" ]]; then error "A stream named ${stream_name} exists even though it should have been deleted"; exit 1; fi

    success "Stream deleted successfully."
}

function run_streams_tests {
    test_can_create_stream
    test_can_get_streams
    test_can_get_stream_by_name
    test_can_get_stream_in_yaml_format
    test_get_missing_stream_name_outputs_no_results
    test_can_update_stream
    test_can_delete_stream

    success "All streams tests completed successfully!"
}

if [[ "$CALLED_BY_MAIN" == "" ]]; then
    source $BASEDIR/calculate-galasactl-executables.sh --bootstrap "${bootstrap}"
    calculate_galasactl_executable
    run_streams_tests
fi