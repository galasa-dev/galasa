#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
echo "Running script resources-tests.sh"
# This script can be ran locally or executed in a pipeline to test the various built binaries of galasactl
# This script tests the 'galasactl resources' command against a namespace that is in our ecosystem's cps namespaces already
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

# generate a random number to append to test names to avoid multiple running at once overriding each other
function get_random_property_name_number {
    minimum=100
    maximum=999
    PROP_NUM=$(($minimum + $RANDOM % $maximum))
    echo $PROP_NUM
}

#-----------------------------------------------------------------------------------------
# Tests
#----------------------------------------------------------------------------------------- 
function properties_resources_create {
    h2 "Performing resources create, expecting ..."

    used -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.

    prop_name="properties.test.1"
    prop_value="value1"

    input_file="$ORIGINAL_DIR/temp/resources-create-input.yaml"

    cat << EOF > $input_file 
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: $prop_name
  namespace: ecosystemtest
data:
  value: $prop_value
EOF

    cmd="${BINARY_LOCATION} resources create \
    --bootstrap $bootstrap \
    -f $input_file \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to create resource"
        exit 1
    fi

    # check that property resource has been created
    cmd="${BINARY_LOCATION} properties get --namespace ecosystemtest \
    --name $prop_name \
    --bootstrap $bootstrap \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get property with name used, get command failed."
        exit 1
    fi

    output_file="$ORIGINAL_DIR/temp/resources-create-output.txt"
    $cmd | tee $output_file

    # Check that the value matches the property created
    cat $output_file | grep "$prop_name\s+$prop_value" -q -E

    rc=$?
    # We expect a return code of 0 because this is a properly formed properties get command.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to create property."
        exit 1
    fi

    rm $input_file    
    rm $output_file

    success "Resources create seems to be successful."
}

#--------------------------------------------------------------------------
function properties_resources_update {
    h2 "Performing resources update, expecting ..."

    used -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.

    prop_name="properties.test.1"
    prop_value="updated-value"

    input_file="$ORIGINAL_DIR/temp/resources-update-input.yaml"

    cat << EOF > $input_file 
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: $prop_name
  namespace: ecosystemtest
data:
  value: $prop_value
EOF

    cmd="${BINARY_LOCATION} resources update \
    --bootstrap $bootstrap \
    -f $input_file \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to update resources"
        exit 1
    fi

    # check that property resource has been updated
    cmd="${BINARY_LOCATION} properties get --namespace ecosystemtest \
    --name $prop_name \
    --bootstrap $bootstrap \
    --log -"

    info "Command is: $cmd"
    
    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get property with name used, get command failed."
        exit 1
    fi

    output_file="$ORIGINAL_DIR/temp/resources-update-output.txt"
    $cmd | tee $output_file

    # Check that the previous properties set updated the property value
    cat $output_file | grep "$prop_name\s+$prop_value" -q -E

    rc=$?
    # We expect a return code of 0 because this is a properly formed properties get command.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to update property, property not found in namespace."
        exit 1
    fi

    rm $input_file
    rm $output_file

    success "Resources update seems to be successful."
}

#--------------------------------------------------------------------------
function properties_resources_apply {
    h2 "Performing resources apply, expecting ..."

    used -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.

    prop_name_to_create="properties.test.create"
    prop_value_to_create="value-created"
    #this property needs to already exist
    prop_name_to_update="properties.test.1"
    prop_value_to_update="updated-value-2"

    input_file="$ORIGINAL_DIR/temp/resources-apply-input.yaml"

    cat << EOF > $input_file 
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: $prop_name_to_update
  namespace: ecosystemtest
data:
  value: $prop_value_to_update
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: $prop_name_to_create
  namespace: ecosystemtest
data:
  value: $prop_value_to_create
EOF

    cmd="${BINARY_LOCATION} resources apply \
    --bootstrap $bootstrap \
    -f $input_file \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to apply resources"
        exit 1
    fi

    # check that property resource has been applied
    cmd="${BINARY_LOCATION} properties get --namespace ecosystemtest \
    --infix properties.test \
    --bootstrap $bootstrap \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to get property with name used, get command failed."
        exit 1
    fi

    output_file="$ORIGINAL_DIR/temp/resources-apply-output.txt"
    $cmd | tee $output_file

    # Check that the previous properties applied have correct values
    grep "$prop_name_to_update\s+$prop_value_to_update | $prop_name_to_create\s+$prop_value_to_create" $output_file -q -E

    rc=$?
    # We expect a return code of 0 because this is a properly formed resources apply command.
    if [[ "${rc}" != "0" ]]; then 
        error "The properties to be applied were unable to be updated and/or created"
        exit 1
    fi

    rm $input_file
    rm $output_file

    success "Properties resources apply seems to be successful."
}

#--------------------------------------------------------------------------
function properties_resources_delete {
    h2 "Performing resources delete with valid fields..."

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.
    
    prop_name_to_create="properties.test.create"
    prop_value_to_create="value-created"
    #this property needs to already exist
    prop_name_to_update="properties.test.1"
    prop_value_to_update="updated-value-2"

    input_file="$ORIGINAL_DIR/temp/resources-apply-input.yaml"

    cat << EOF > $input_file 
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: $prop_name_to_update
  namespace: ecosystemtest
data:
  value: $prop_value_to_update
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: $prop_name_to_create
  namespace: ecosystemtest
data:
  value: $prop_value_to_create
EOF

    cmd="${BINARY_LOCATION} resources delete \
    --bootstrap $bootstrap \
    -f $input_file \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of 0 because this is a properly formed properties delete command.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to delete property resources, command failed."
        exit 1
    fi

    # check that property has been deleted
    cmd="${BINARY_LOCATION} properties get --namespace ecosystemtest \
    --name $prop_name \
    --bootstrap $bootstrap \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to delete property resource with name used."
        exit 1
    fi

    output_file="$ORIGINAL_DIR/temp/resources-delete-output.txt"
    $cmd | tee $output_file

    # Check that the previous properties set updated the property value
    cat $output_file | grep "Total:0" -q

    rc=$?
    # We expect a return code of 1 because this property should not exist anymore.
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to delete property resource, property remains in namespace."
        exit 1
    fi

    rm $input_file
    rm $output_file

    success "Resources delete with name used seems to have been deleted correctly."
}

#--------------------------------------------------------------------------
function properties_resources_delete_invalid_property {
    h2 "Performing resources properties delete with non-existing property..."

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.

    input_file="$ORIGINAL_DIR/temp/resources-delete-input.yaml"

    cat << EOF > $input_file 
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: prop.doesnt.exist
  namespace: ecosystemtest
data:
  value: inexistent
EOF

    cmd="${BINARY_LOCATION} resources delete \
    --bootstrap $bootstrap \
    -f $input_file \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of 0 because the api would return an OK status (200) 
    # as we want this property to not exist
    if [[ "${rc}" != "0" ]]; then 
        error "Command should not fail as we expect OK status."
        exit 1
    fi

    rm $input_file

    success "Resources delete with the name of a non existent property correctly throws an error."
}

#--------------------------------------------------------------------------
function properties_resources_create_with_name_without_value {
    h2 "Performing resources create with name parameter, but no value parameter..."

    prop_name="properties.test.name.$PROP_NUM"

    input_file="$ORIGINAL_DIR/temp/resources-create-input.yaml"

    cat << EOF > $input_file 
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: $prop_name
  namespace: ecosystemtest
data:
  value:
EOF

    cmd="${BINARY_LOCATION} resources create \
    --bootstrap $bootstrap \
    -f $input_file \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # we expect a return code of 1 as properties set should not be able to run without value used.
    if [[ "${rc}" != "1" ]]; then 
        error "Failed to recognise properties set without value should error."
        exit 1
    fi

    rm $input_file

    success "Resource create with no value correctly throws an error."
}

#--------------------------------------------------------------------------
function properties_resources_create_with_invalid_file {
    h2 "Performing resources create with non-existing file..."

    prop_name="properties.test.name.$PROP_NUM"

    input_file="non_existing_file.txt"

    cat << EOF > $input_file 
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: $prop_name
  namespace: ecosystemtest
data:
  value: value
EOF

    cmd="${BINARY_LOCATION} resources create \
    --bootstrap $bootstrap \
    -f $input_file \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # we expect a return code of 1 as properties set should not be able to run without value used.
    if [[ "${rc}" != "1" ]]; then 
        error "Failed to recognise properties set with non-existing file should error."
        exit 1
    fi

    rm $input_file

    success "Resource create with non-existing file correctly throws an error."
}
#--------------------------------------------------------------------------
function properties_resources_create_with_non_yaml_file {
    h2 "Performing resources create with non-yaml file..."

    prop_name="properties.test.name.$PROP_NUM"

    input_file="$ORIGINAL_DIR/temp/resources-create-input.txt"

    cat << EOF > $input_file apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: $prop_name
  namespace: ecosystemtest
data:
  value: value
EOF

    cmd="${BINARY_LOCATION} resources create \
    --bootstrap $bootstrap \
    -f $input_file \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # we expect a return code of 1 as properties set should not be able to run without value used.
    if [[ "${rc}" != "1" ]]; then 
        error "Failed to recognise properties set with non-yaml file should error."
        exit 1
    fi

    rm $input_file

    success "Resource create with non-yaml file correctly throws an error."
}


#-------------------------------------------------------------------------------------
function resources_tests {
    get_random_property_name_number
    properties_resources_create
    properties_resources_update
    properties_resources_apply
    properties_resources_delete
    properties_resources_delete_invalid_property
    properties_resources_create_with_name_without_value
    properties_resources_create_with_invalid_file
    properties_resources_create_with_non_yaml_file
}

# checks if it's been called by main, set this variable if it is
if [[ "$CALLED_BY_MAIN" == "" ]]; then
    source $BASEDIR/calculate-galasactl-executables.sh
    calculate_galasactl_executable
    resources_tests
fi