#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
echo "Running script gherkin-runs-tests.sh"
# This script can be ran locally or executed in a pipeline to test the various built binaries of galasactl
# This script tests the 'galasactl runs submit local --gherkin' command against a test that is generated
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

function SubmitLocalSimpleGherkinTest {
    h1 "Submitting the sample gherkin feature"

    gherkin_feature_filename="simple.feature"

    feature_file_path="$ORIGINAL_DIR/temp/${gherkin_feature_filename}"

    cat << EOF > $feature_file_path 
Feature: GherkinSubmitTest
  Scenario: Log Example Statement
    
    THEN Write to log "This is a log statement"
    
  Scenario: Log Statement Test
    
    THEN Write to log "This is a second scenario"
EOF
    success "OK"

    log_output_file=$ORIGINAL_DIR/temp/$gherkin_feature_filename.log

    cmd="$ORIGINAL_DIR/bin/${binary} runs submit local \
    --remoteMaven https://development.galasa.dev/main/maven-repo/obr \
    --gherkin file://$feature_file_path \
    --log $log_output_file"

    info "Command is: $cmd"

    $cmd
    rc=$?

    if [[ "${rc}" != "0" ]]; then 
        error "Failed to run gherkin test"
        exit 1
    fi

    log_occurrances=$(cat $log_output_file | grep "CoreStatementOwner - This is a log statement" | wc -l | xargs)
    if [[ "$log_occurrances" != "1" ]]; then 
        error "The log statement 1 we tried to log is not visible in the log. It appeared $log_occurrances times."
        exit 1
    fi
    success "Log statement 1 appeared in the log OK"

    log_occurrances=$(cat $log_output_file | grep "CoreStatementOwner - This is a second scenario" | wc -l | xargs)
    if [[ "$log_occurrances" != "1" ]]; then 
        error "The log statement 2 we tried to log is not visible in the log. It appeared $log_occurrances times."
        exit 1
    fi
    success "Log statement 2 appeared in the log OK"

    success "OK"
}

function SubmitTestWhichUsesACPSVariable {
  h1 "Submitting the gherkin which uses a CPS variable."

    gherkin_feature_filename="scenario-cps-prop-use.feature"

    feature_file_path="$ORIGINAL_DIR/temp/${gherkin_feature_filename}"

    echo "test.fruit.name=peach" > $ORIGINAL_DIR/temp/home/cps.properties

    cat << EOF > $feature_file_path 
Feature: GherkinSubmitTest
  Scenario: Log A fruit
    GIVEN <fruit> is test property fruit.name
    THEN Write to log "my favourite fruit is <fruit>"
EOF
    success "OK"

    log_output_file=$ORIGINAL_DIR/temp/$gherkin_feature_filename.log

    cmd="$ORIGINAL_DIR/bin/${binary} runs submit local \
    --remoteMaven https://development.galasa.dev/main/maven-repo/obr \
    --gherkin file://$feature_file_path \
    --log $log_output_file"

    info "Command is: $cmd"

    $cmd
    rc=$?

    if [[ "${rc}" != "0" ]]; then 
        error "Failed to run gherkin test"
        exit 1
    fi

    info "output is:"
    cat $log_output_file | grep "d.g.c.m.i.g.CoreStatementOwner - my favourite fruit is"

    log_occurrances=$(cat $log_output_file | grep "my favourite fruit is peach" | wc -l | xargs)
    if [[ "$log_occurrances" != "1" ]]; then 
        error "The log statement 1 we tried to log is not visible in the log. It appeared $log_occurrances times."
        exit 1
    fi
    success "Log statement 1 appeared in the log OK"

    success "OK"
}

function SubmitLocalGherkinScenarioOutlineTest {
    h1 "Submitting the gherkin feature with a scenario outline."

    gherkin_feature_filename="scenario-outline.feature"

    feature_file_path="$ORIGINAL_DIR/temp/${gherkin_feature_filename}"

    cat << EOF > $feature_file_path 
Feature: GherkinSubmitTest
  Scenario Outline: Log A fruit
    
    # This is a comment. Should be ignored.
    # We want to write out one piece of fruit to the log, for each 
    # scenario described by this outline.

    THEN Write to log "<fruit>"
    
    Examples:
    | fruit  |
    | apple  |
    | banana |
EOF
    success "OK"

    log_output_file=$ORIGINAL_DIR/temp/$gherkin_feature_filename.log

    cmd="$ORIGINAL_DIR/bin/${binary} runs submit local \
    --remoteMaven https://development.galasa.dev/main/maven-repo/obr \
    --gherkin file://$feature_file_path \
    --log $log_output_file"

    info "Command is: $cmd"

    $cmd
    rc=$?

    if [[ "${rc}" != "0" ]]; then 
        error "Failed to run gherkin test"
        exit 1
    fi

    log_occurrances=$(cat $log_output_file | grep "CoreStatementOwner - apple" | wc -l | xargs)
    if [[ "$log_occurrances" != "1" ]]; then 
        error "The log statement 1 we tried to log is not visible in the log. It appeared $log_occurrances times."
        exit 1
    fi
    success "Log statement 1 appeared in the log OK"

    log_occurrances=$(cat $log_output_file | grep "CoreStatementOwner - banana" | wc -l | xargs)
    if [[ "$log_occurrances" != "1" ]]; then 
        error "The log statement 2 we tried to log is not visible in the log. It appeared $log_occurrances times."
        exit 1
    fi
    success "Log statement 2 appeared in the log OK"

    success "OK"
}

function SubmittingBadPrefixLocalGherkinTest {
    h1 "Submitting bad prefix gherkin feature"

    cmd="$ORIGINAL_DIR/bin/${binary} runs submit local \
    --remoteMaven https://development.galasa.dev/main/maven-repo/obr \
    --gherkin $input_file \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?

    if [[ "${rc}" != "1" ]]; then 
        error "Gherkin test did not fail as expected"
        exit 1
    fi

    success "OK"
}

function SubmittingBadSuffixLocalGherkinTest {
    h1 "Submitting bad suffix gherkin feature"

    cmd="$ORIGINAL_DIR/bin/${binary} runs submit local \
    --remoteMaven https://development.galasa.dev/main/maven-repo/obr \
    --gherkin file:///gherkin \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?

    if [[ "${rc}" != "1" ]]; then 
        error "Gherkin test did not fail as expected"
        exit 1
    fi

    success "OK"
}

function test_gherkin_commands {
    SubmitTestWhichUsesACPSVariable

    SubmittingBadPrefixLocalGherkinTest
    SubmittingBadSuffixLocalGherkinTest

    SubmitLocalSimpleGherkinTest
    SubmitLocalGherkinScenarioOutlineTest
}

# checks if it's been called by main, set this variable if it is
if [[ "$CALLED_BY_MAIN" == "" ]]; then
    source $BASEDIR/calculate-galasactl-executables.sh
    calculate_galasactl_executable
    test_gherkin_commands
fi