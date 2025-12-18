#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
echo "Running script tags-tests.sh"
set -o pipefail

# This script can be ran locally or executed in a pipeline to test the various built binaries of galasactl
# This script tests the 'galasactl tags' commands to create, read, update, and delete tags in a Galasa service
# Pre-requisite: the CLI must have been built first so the binaries are present in the /bin directory

function usage {
    info "Syntax: tags-tests.sh [OPTIONS]"
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
function set_tag {
    tag_name="$1"
    description="$2"
    priority="$3"

    cmd="${BINARY_LOCATION} tags set \
    --name ${tag_name} \
    --description ${description} \
    --priority ${priority} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Creating tag with command: $cmd"
    ${BINARY_LOCATION} tags set \
    --name "${tag_name}" \
    --description "${description}" \
    --priority "${priority}" \
    --bootstrap "${bootstrap}" \
    --log -
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to set tag."; exit 1; fi
}

function delete_tag {
    tag_name="$1"

    cmd="${BINARY_LOCATION} tags delete \
    --name ${tag_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Deleting tag with command: $cmd"
    $cmd
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to delete tag."; exit 1; fi
}

#-----------------------------------------------------------------------------------------
# Tests
#-----------------------------------------------------------------------------------------
function test_can_create_tag {
    h2 "Creating a tag to test successful tag creation"

    tag_name="test-create-tag-$(date +%s)"
    description="testing that tag creation works"
    priority="123"

    set_tag "${tag_name}" "${description}" "${priority}"

    # check that tag has been created
    output_file="$BASEDIR/temp/tags-set-check.txt"
    cmd="${BINARY_LOCATION} tags get \
    --name ${tag_name} \
    --log -"

    info "Checking the tag that was just created"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get the tag that was just created."; exit 1; fi

    # Check that the previous tags set command created a tag with the correct values
    cat ${output_file} | grep "${tag_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tag with name ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "${description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${description}' in tag output."; exit 1; fi

    cat ${output_file} | grep "${priority}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing priority '${priority}' in tag output"; exit 1; fi

    success "Tag created successfully with all expected values."
    delete_tag "${tag_name}"
}

function test_can_get_tags {
    h2 "Checking that we can get tags from the Galasa service"

    tag_name="test-get-tags-$(date +%s)"
    description="testing that tag retrieval works"
    priority="123"

    set_tag "${tag_name}" "${description}" "${priority}"

    output_file="$BASEDIR/temp/tags-get-all-check.txt"
    cmd="${BINARY_LOCATION} tags get \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting tags with command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tags"; exit 1; fi

    # Check that the previous tags set command created a tag with the given name
    cat ${output_file} | grep "${tag_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tag with name ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "${description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${description}' in tag output."; exit 1; fi

    cat ${output_file} | grep "${priority}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing priority '${priority}' in tag output"; exit 1; fi

    success "Tags retrieved successfully with expected values."
    delete_tag "${tag_name}"
}

function test_can_get_tag_by_name {
    h2 "Checking that we can get a tag by name from the Galasa service"

    tag_name="test-get-tag-by-name-$(date +%s)"
    description="testing that tag retrieval works"
    priority="123"

    set_tag "${tag_name}" "${description}" "${priority}"

    output_file="$BASEDIR/temp/tags-get-by-name-check.txt"
    cmd="${BINARY_LOCATION} tags get \
    --name ${tag_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting tag with name '${tag_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tag with name ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "${tag_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tag with name ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "${description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${description}' in tag output."; exit 1; fi

    cat ${output_file} | grep "${priority}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing priority '${priority}' in tag output"; exit 1; fi

    success "Tag retrieved successfully with expected values."
    delete_tag "${tag_name}"
}

function test_get_missing_tag_name_outputs_no_results {
    h2 "Checking that getting a non-existent tag returns a total of 0 results"

    tag_name="unknown tag $(date +%s)"

    output_file="$BASEDIR/temp/tags-get-unknown-name.txt"
    cmd="${BINARY_LOCATION} tags get \
    --name ${tag_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting tag with name '${tag_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tag with name ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "Total:0" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Found a tag named '${tag_name}' when there should not have been any results"; exit 1; fi

    success "Tags get successfully returned no results."
}

function test_can_get_tag_in_yaml_format {
    h2 "Checking that we can get a tag in YAML format from the Galasa service"

    tag_name="test-get-tag-yaml-$(date +%s)"
    description="testing that tag YAML formatting works"
    priority="123"

    set_tag "${tag_name}" "${description}" "${priority}"

    output_file="$BASEDIR/temp/tags-get-by-name-check.txt"
    cmd="${BINARY_LOCATION} tags get \
    --name ${tag_name} \
    --format yaml \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting tag with name '${tag_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tag with name ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "name: ${tag_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tag with name ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "description: ${description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${description}' in tag output."; exit 1; fi

    cat ${output_file} | grep "priority: ${priority}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing priority '${priority}' in tag output"; exit 1; fi

    success "Tag retrieved successfully in YAML format."
    delete_tag "${tag_name}"
}

function test_can_update_tag {
    h2 "Checking that we can update an existing tag"

    tag_name="test-update-tag-$(date +%s)"
    old_description="testing that tag updates work"
    old_priority="123"

    set_tag "${tag_name}" "${old_description}" "${old_priority}"

    # Now update the tag with new values
    new_description="updated description"
    new_priority="456"
    set_tag "${tag_name}" "${new_description}" "${new_priority}"

    output_file="$BASEDIR/temp/tags-update-check.txt"
    cmd="${BINARY_LOCATION} tags get \
    --name ${tag_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting tag with name '${tag_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get a tag named ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "${tag_name}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tag with name ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "${new_description}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing description '${new_description}' in tag output."; exit 1; fi

    cat ${output_file} | grep "${new_priority}" -q
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Missing priority '${new_priority}' in tag output"; exit 1; fi

    cat ${output_file} | grep "${old_priority}" -q
    rc=$? ; if [[ "${rc}" != "1" ]]; then error "Description for tag ${tag_name} wasn't updated"; exit 1; fi

    cat ${output_file} | grep "${old_priority}" -q
    rc=$? ; if [[ "${rc}" != "1" ]]; then error "Priority for tag ${tag_name} wasn't updated"; exit 1; fi

    success "Tag retrieved successfully with updated values."
    delete_tag "${tag_name}"
}

function test_can_delete_tag {
    h2 "Checking that we can delete a tag"

    tag_name="test-delete-tag-$(date +%s)"
    description="testing that tag deletion works"
    priority="123"

    set_tag "${tag_name}" "${description}" "${priority}"

    # Now delete the tag
    delete_tag "${tag_name}"

    output_file="$BASEDIR/temp/tags-delete-check.txt"
    cmd="${BINARY_LOCATION} tags get \
    --name ${tag_name} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Getting tag with name '${tag_name}' using command: $cmd"
    $cmd | tee "${output_file}"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to get tag named ${tag_name}."; exit 1; fi

    cat ${output_file} | grep "${tag_name}" -q
    rc=$? ; if [[ "${rc}" != "1" ]]; then error "A tag named ${tag_name} exists even though it should have been deleted"; exit 1; fi

    success "Tag deleted successfully."
}

function run_tags_tests {
    test_can_create_tag
    test_can_get_tags
    test_can_get_tag_by_name
    test_can_get_tag_in_yaml_format
    test_get_missing_tag_name_outputs_no_results
    test_can_update_tag
    test_can_delete_tag

    success "All tags tests completed successfully!"
}

if [[ "$CALLED_BY_MAIN" == "" ]]; then
    source $BASEDIR/calculate-galasactl-executables.sh --bootstrap "${bootstrap}"
    calculate_galasactl_executable
    run_tags_tests
fi