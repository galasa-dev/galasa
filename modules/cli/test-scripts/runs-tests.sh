#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
echo "Running script runs-tests.sh"
# This script can be ran locally or executed in a pipeline to test the various built binaries of galasactl
# This script tests the 'galasactl runs submit' command against a test that is in our ecosystem's testcatalog already
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

    #-----------------------------------------------------------------------------------------
    # Constants
    #-----------------------------------------------------------------------------------------
    export GALASA_TEST_NAME_SHORT="core.CoreManagerIVT"   
    export GALASA_TEST_NAME_LONG="dev.galasa.ivts.${GALASA_TEST_NAME_SHORT}" 
    export GALASA_TEST_RUN_GET_EXPECTED_SUMMARY_LINE_COUNT="4"
    export GALASA_TEST_RUN_GET_EXPECTED_DETAILS_LINE_COUNT="14"
    export GALASA_TEST_RUN_GET_EXPECTED_RAW_PIPE_COUNT="11"
    export GALASA_TEST_RUN_GET_EXPECTED_NUMBER_ARTIFACT_RUNNING_COUNT="10"

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
function launch_test_on_ecosystem_with_portfolio {

    group_name=$1
    h2 "Building a portfolio..."

    mkdir -p ${BASEDIR}/temp
    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs prepare \
    --bootstrap $bootstrap \
    --stream ivts \
    --portfolio portfolio.yaml \
    --test ${GALASA_TEST_NAME_SHORT} \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '0' because this test is in the ecosystem's testcatalog.
    if [[ "${rc}" != "0" ]]; then
        error "Failed to create a portfolio with a known test from the ecosystem's testcatalog."
        exit 1
    fi
    success "Creating portfolio.yaml worked OK"

    h2 "Launching test on an ecosystem from a portfolio..."

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs submit \
    --bootstrap ${bootstrap} \
    --portfolio portfolio.yaml \
    --throttle 1 \
    --poll 10 \
    --progress 1 \
    --noexitcodeontestfailures \
    --group ${group_name} \
    --log -"

    info "Command is: $cmd"

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.
    $cmd | tee runs-submit-output.txt # Store the output of galasactl runs submit to use later

    rc=$?
    # We expect a return code of '0' because the ecosystem should be able to run this test.
    # We have specified the flag --noexitcodeontestfailures so that we still receive a return code '0' even if the test fails,
    # as we are testing galasactl here, not the test itself
    if [[ "${rc}" != "0" ]]; then
        error "Failed to submit a test to a remote ecosystem."
        exit 1
    fi
    success "Submitting test to ecosystem worked OK"
}

#--------------------------------------------------------------------------
function runs_download_check_folder_names_during_test_run {
    # runs_download_check_folder_names_during_test_run performs multiple runs downloads on a test that is running in the ecosystem
    # checks the folder names are correct with timestamps where appropriate
    h2 "Performing runs download while test is running..."

    mkdir -p ${BASEDIR}/temp
    cd ${BASEDIR}/temp

    # Create the portfolio.
    cmd="${BINARY_LOCATION} runs prepare \
    --bootstrap $bootstrap \
    --stream ivts \
    --portfolio portfolio.yaml \
    --test ${GALASA_TEST_NAME_SHORT} \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '0' because this test is in the ecosystem's testcatalog.
    if [[ "${rc}" != "0" ]]; then
        error "Failed to create a portfolio with a known test from the ecosystem's testcatalog."
        exit 1
    fi
    success "Creating portfolio.yaml worked OK"

    h2 "Launching test on an ecosystem from a portfolio..."

    cd ${BASEDIR}/temp

    log_file="runs-submit-output-for-download.txt"

    cmd="${BINARY_LOCATION} runs submit \
    --bootstrap ${bootstrap} \
    --portfolio portfolio.yaml \
    --throttle 1 \
    --poll 1 \
    --progress 1 \
    --noexitcodeontestfailures \
    --log ${log_file}"

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.

    # Start the test running inside a background process... so we can try to download artifacts about that test while it's running
    $cmd &

    is_done="false"
    retries=0
    max=100
    target_line=""

    # Loop waiting until we can extract the name of the test run which is running in the background.
    while [[ "${is_done}" == "false" ]]; do
        if [[ -e $log_file ]]; then
            success "file exists"
            target_line=$(cat ${log_file} | grep "submitted")


            if [[ "$target_line" != "" ]]; then
                info "Target line is found."
                is_done="true"
            fi
        fi
        sleep 1
        ((retries++))
        if (( $retries > $max )); then
            error "Too many retries."
            exit 1
        fi
    done

    run_name=$(echo $target_line | cut -f4 -d' ')
    info "Run name is $run_name"

    # Now download the test results which are available from the test which is being submitted in the background process.
    cmd="${BINARY_LOCATION} runs download \
    --name ${run_name} \
    --bootstrap ${bootstrap} \
    --force"

    info "Command is: $cmd"

    output_file="runs-download-output.txt"

    is_test_finished="false"
    retries=0
    max=100
    target_line=""
    while [[ "${is_test_finished}" == "false" ]]; do
        sleep 5
        $cmd | tee $output_file
        # If the test run isn't finished, then we expect downloaded artifacts to appear in a folder with a timestamp - eg: U456-16:40:32
        # So we can look for ':' in the folder name to tell if the test is still running or not.

        target_line=$(cat ${log_file} | grep "has finished")
        # Test has finished so should not have a timestamp in the folder name
        if [[ "$target_line" != "" ]]; then
            success "Target line is found."
            is_test_finished="true"

            folder_name=$(cat $output_file| cut -d' ' -f 7)

            echo $folder_name | grep ":"
            rc=$?
            if [[ "${rc}" != "1" ]]; then
                error "Folder named incorrectly. Has timestamp when it should not."
                exit 1
            fi

        else

            test_building_line=$(cat ${log_file} | grep "now 'building'")
            if [[ "$test_building_line" != "" ]]; then
                cat ${log_file} | grep "now 'running'" -q #if now running is there, dont look further -
                rc=$?
                if [[ "${rc}" != "0" ]]; then
                    # Check to see of the folder created has a ":" in the folder name... indicating that the test is running.
                    folder_name=$(cat $output_file| cut -d' ' -f 7)
                    no_artifacts=$(cat $output_file| cut -d' ' -f 3)
                    no_artifacts=$(($no_artifacts+0))
                    expected_artifact_count=$GALASA_TEST_RUN_GET_EXPECTED_NUMBER_ARTIFACT_RUNNING_COUNT
                    expected_artifact_count=$(($expected_artifact_count+0))
                    echo $folder_name | grep ":"
                    rc=$?
                    if [[ "${rc}" != "0" ]]; then
                        if [[ "${no_artifacts}" -lt "${expected_artifact_count}" ]]; then
                            error "Folder named incorrectly. Has no timestamp when it should, because downloading from running tests should create a folder with a time in, such as U456-16:50:32."
                            exit 1
                        fi
                    fi
                fi
            fi
        fi



        # Give up if we've been waiting for the test to finish for too long. Test could be stuck.
        ((retries++))
        if (( $retries > $max )); then
            error "Too many retries."
            exit 1
        fi
    done

    success "Downloading artifacts from a running test results in folder names with a timestamp. OK"
}

function runs_reset_check_retry_present {

    h2 "Performing runs reset on an active test run..."

    run_name=$1

    h2 "First, launching test on an ecosystem without a portfolio in a background process, so it can be reset."

    mkdir -p ${BASEDIR}/temp
    cd ${BASEDIR}/temp

    runs_submit_log_file="runs-submit-output-for-reset.txt"

    cmd="${BINARY_LOCATION} runs submit \
    --bootstrap $bootstrap \
    --class dev.galasa.ivts/dev.galasa.ivts.core.TestSleep \
    --stream ivts
    --throttle 1 \
    --poll 10 \
    --progress 1 \
    --noexitcodeontestfailures \
    --log ${runs_submit_log_file}"

    info "Command is: $cmd"

    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.

    # Start the test running inside a background process... so we can try to reset it while it's running
    $cmd &

    run_name_found="false"
    retries=0
    max=100
    target_line=""

    # Loop waiting until we can extract the name of the test run which is running in the background.
    while [[ "${run_name_found}" == "false" ]]; do
        if [[ -e $runs_submit_log_file ]]; then
            success "file exists"
            # Check the run has been submitted before attempting to reset
            target_line=$(cat ${runs_submit_log_file} | grep "submitted")

            if [[ "$target_line" != "" ]]; then
                info "Target line is found - the test has been submitted."
                run_name_found="true"
            fi
        fi
        sleep 3
        ((retries++))
        if (( $retries > $max )); then
            error "Too many retries."
            exit 1
        fi
    done

    # sleep for 10 seconds to allow the test to reach an active stage
    sleep 10

    run_name=$(echo $target_line | cut -f4 -d' ')
    info "Run name is $run_name"

    h2 "Now attempting to reset the run while it's running in the background process."

    cmd="${BINARY_LOCATION} runs reset \
    --name ${run_name} \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"
    $cmd

    h2 "Now using runs get to check that two different runs show up in the runs get output."

    runs_get_log_file="runs-get-output-for-reset.txt"

    # Now poll runs get to check when the tests are finished
    cmd="${BINARY_LOCATION} runs get \
    --name ${run_name} \
    --bootstrap ${bootstrap}"

    two_tests_found="false"
    retries=0
    max=100
    target_line=""
    while [[ "${two_tests_found}" == "false" ]]; do
        sleep 5

        # Run the runs get command
        $cmd | tee $runs_get_log_file
        # Check for line in the runs get output to signify that there are 2 tests
        target_line=$(cat ${runs_get_log_file} | grep "Total:2")
        if [[ "$target_line" != "" ]]; then
            success "Target line is found - two runs were found."
            two_tests_found="true"
        fi

        # Give up if we've been waiting for the test to finish for too long. Test could be stuck.
        ((retries++))
        if (( $retries > $max )); then
            error "Too many retries."
            exit 1
        fi
    done

}

#--------------------------------------------------------------------------
function get_result_with_runname {
    h2 "Querying the result of the test we just ran..."

    cd ${BASEDIR}/temp

    # Get the RunName from the output of galasactl runs submit
    # The output of runs submit should look like:
    # submitted-time(UTC) name  requestor status   result test-name
    # 2024-09-05 12:45:33 C9955 galasa    building Passed ivts/dev.galasa.ivts/dev.galasa.ivts.core.CoreManagerIVT \
    #
    # Total:1 Passed:1

    # Gets the run name from the second line of the runs submit output (after the headers).
    # The run name should be the third field, after the date and time fields.
    runname=$(cat runs-submit-output.txt | sed -n "2{p;q;}" | cut -f3 -d' ')

    if [[ "$runname" == "" ]]; then
        error "Run name not captured from previous run launch."
        exit 1
    fi

    info "Run name is: ${runname}"

    cmd="${BINARY_LOCATION} runs get \
    --name ${runname} \
    --bootstrap ${bootstrap} \
    --log -"

    info "Command is: $cmd"

    $cmd | grep "${runname}" # Checks the RunName can be found in the output from galasactl runs get
    rc=$?
    # We expect a return code of '0' because the ecosystem should be able to find this test as we just ran it.
    if [[ "${rc}" != "0" ]]; then
        error "Failed to query the result of run ${runname} in the remote ecosystem."
        exit 1
    fi
    success "Querying the result of a run in the ecosystem worked OK"

    # The test above just checks that some output was found from galasactl runs get.
    # TO DO - Get the Result of the run from the output of galasactl runs submit as well as the RunName, and make sure the Result is correct too
    export RUN_NAME=${runname}
}

#--------------------------------------------------------------------------
function runs_get_check_summary_format_output {
    h2 "Performing runs get with summary format..."

    run_name=$1

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs get \
    --name ${run_name} \
    --format summary \
    --bootstrap ${bootstrap} "

    info "Command is: $cmd"

    output_file="runs-get-output.txt"
    $cmd | tee $output_file

    # Check that the full test name is output
    grep "${GALASA_TEST_NAME_LONG}" $output_file -q
    rc=$?
    # We expect a return code of '0' because the test name should be output.
    if [[ "${rc}" != "0" ]]; then
        error "Did not find ${GALASA_TEST_NAME_LONG} in summary output"
        exit 1
    fi

    # Check headers
    headers=("submitted-time(UTC)" "name" "status" "result" "test-name" "group")

    for header in "${headers[@]}"
    do
        grep "${header}" $output_file -q
        rc=$?
        # We expect a return code of '0' because the header name should be output.
        if [[ "${rc}" != "0" ]]; then
            error "Did not find header $header in summary output"
            exit 1
        fi
    done

    # Check that we got 4 lines - headers, result data, empty line, totals count
    line_count=$(cat $output_file | wc -l | xargs)
    expected_line_count=$GALASA_TEST_RUN_GET_EXPECTED_SUMMARY_LINE_COUNT
    if [[ "${line_count}" != "${expected_line_count}" ]]; then
        error "line count is wrong. expected ${expected_line_count} got ${line_count}"
        exit 1
    fi

    success "galasactl runs get --format summary seemed to work"
}

#--------------------------------------------------------------------------
function runs_get_check_details_format_output {
    h2 "Performing runs get with details format..."

    run_name=$1

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs get \
    --name ${run_name} \
    --format details \
    --bootstrap ${bootstrap} "

    info "Command is: $cmd"

    output_file="runs-get-output.txt"
    $cmd | tee $output_file


    # Check that the full test name is output and formatted
    grep "test-name[[:space:]]*:[[:space:]]*${GALASA_TEST_NAME_LONG}" $output_file -q
    rc=$?
    # We expect a return code of '0' because the ecosystem should be able to find this test as we just ran it.
    if [[ "${rc}" != "0" ]]; then
        error "Did not find ${GALASA_TEST_NAME_LONG} in details output"
        exit 1
    fi

    # Check method headers
    headers=("method" "type" "status" "result" "start-time(UTC)" "end-time(UTC)" "duration(ms)")

    for header in "${headers[@]}"
    do
        grep "${header}" $output_file -q
        rc=$?
        # We expect a return code of '0' because the header name should be output.
        if [[ "${rc}" != "0" ]]; then
            error "Did not find header $header in details output"
            exit 1
        fi
    done

    #check methods start on line 14 - implies other test details have outputted
    line_count=$(grep -n "method[[:space:]]*type[[:space:]]*status[[:space:]]*result[[:space:]]*start-time(UTC)[[:space:]]*end-time(UTC)[[:space:]]*duration(ms)" $output_file | head -n1 | sed 's/:.*//')
    expected_line_count=$GALASA_TEST_RUN_GET_EXPECTED_DETAILS_LINE_COUNT
    if [[ "${line_count}" != "${expected_line_count}" ]]; then
        # We expect a return code of '0' because the method header should be output on line 13.
        error "line count is wrong. expected methods to start on ${expected_line_count} got ${line_count}"
        exit 1
    fi

    success "galasactl runs get --format details seemed to work"
}

#--------------------------------------------------------------------------
function runs_get_check_raw_format_output {
    h2 "Performing runs get with raw format..."

    run_name=$1

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs get \
    --name ${run_name} \
    --format raw \
    --bootstrap ${bootstrap} "

    info "Command is: $cmd"

    output_file="runs-get-output.txt"
    $cmd | tee $output_file

    # Check that the full test name is output
    grep "${GALASA_TEST_NAME_LONG}" $output_file -q
    rc=$?
    # We expect a return code of '0' because the test name should be output.
    if [[ "${rc}" != "0" ]]; then
        error "Did not find ${GALASA_TEST_NAME_LONG} in raw output"
        exit 1
    fi

    # Check that we got 11 pipes
    pipe_count=$(grep -o "|" $output_file | wc -l | xargs)
    expected_pipe_count=$GALASA_TEST_RUN_GET_EXPECTED_RAW_PIPE_COUNT
    if [[ "${pipe_count}" != "${expected_pipe_count}" ]]; then
        error "pipe count is wrong. expected ${expected_pipe_count} got ${pipe_count}"
        exit 1
    fi

    success "galasactl runs get --format raw seemed to work"
}

#--------------------------------------------------------------------------
function runs_get_check_raw_format_output_with_from_and_to {
    h2 "Performing runs get with raw format providing a from and to age..."

    run_name=$1

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs get \
    --age 1h:0h \
    --format raw \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    output_file="runs-get-output.txt"
    set -o pipefail
    $cmd | tee $output_file

    # Check that the run name we just ran is output as we are asking for all tests submitted from 1 hour ago until now.
    grep "${run_name}" $output_file -q
    rc=$?
    # We expect a return code of '0' because the run name should be output.
    if [[ "${rc}" != "0" ]]; then
        error "Did not find ${run_name} in raw output"
        exit 1
    fi

    success "galasactl runs get with age parameter returned results okay."
}

#--------------------------------------------------------------------------
function runs_get_check_raw_format_output_with_just_from {
    h2 "Performing runs get with raw format providing just a from age..."

    run_name=$1

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs get \
    --age 1d \
    --format raw \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    output_file="runs-get-output.txt"
    set -o pipefail
    $cmd | tee $output_file

    # Check that the run name we just ran is output as we are asking for all tests submitted from 1 hour ago until now.
    grep "${run_name}" $output_file -q
    rc=$?
    # We expect a return code of '0' because the run name should be output.
    if [[ "${rc}" != "0" ]]; then
        error "Did not find ${run_name} in raw output"
        exit 1
    fi

    success "galasactl runs get with age parameter with just from value returned results okay."
}

#--------------------------------------------------------------------------
function runs_get_check_raw_format_output_with_no_runname_and_no_age_param {
    h2 "Performing runs get with raw format providing no run name and no age..."

    cmd="${BINARY_LOCATION} runs get \
    --format raw \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '1' because this should return the error GAL1079E.
    if [[ "${rc}" != "1" ]]; then
        error "Failed to return an error."
        exit 1
    fi

    success "galasactl runs get with no run name and no age returned an error okay."
}

#--------------------------------------------------------------------------
function runs_get_check_raw_format_output_with_invalid_age_param {
    h2 "Performing runs get with raw format providing an age parameter with an invalid value..."


    cmd="${BINARY_LOCATION} runs get \
    --age 1y:1m \
    --format raw \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '1' because this should return the error GAL1078E.
    if [[ "${rc}" != "1" ]]; then
        error "Failed to return an error."
        exit 1
    fi

    success "galasactl runs get with invalid age values returned an error okay."
}

#--------------------------------------------------------------------------
function runs_get_check_raw_format_output_with_older_to_than_from_age {
    h2 "Performing runs get with raw format providing an age parameter with an older to than from age..."


    cmd="${BINARY_LOCATION} runs get \
    --age 1h:1d \
    --format raw \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '1' because this should return the error GAL1077E.
    if [[ "${rc}" != "1" ]]; then
        error "Failed to return an error."
        exit 1
    fi

    success "galasactl runs get with older to age than from age returned an error okay."
}

#--------------------------------------------------------------------------
function runs_get_check_requestor_parameter {
    requestor="galasa-team"
    h2 "Performing runs get with details format providing a from age and requestor as $requestor..."

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs get \
    --age 1d \
    --requestor $requestor \
    --format details \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    output_file="runs-get-output.txt"
    $cmd | tee $output_file

    # Check that the run name we just ran is output as we are asking for all tests submitted from 1 hour ago until now.
    grep "requestor[ ]*:[ ]*${requestor}" $output_file -q
    rc=$?
    # We expect a return code of '0' because the run name should be output.
    if [[ "${rc}" != "0" ]]; then
        error "Did not find any runs with requestor '$requestor' in output"
        exit 1
    fi

    success "galasactl runs get with age parameter with just from value and requestor '$requestor' returned results okay."
}

#--------------------------------------------------------------------------
function runs_get_check_result_parameter {
    result="Passed"
    h2 "Performing runs get with details format providing a from age and result as $result..."

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs get \
    --age 1d \
    --result ${result} \
    --format details \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    output_file="runs-get-output.txt"
    $cmd | tee $output_file

    grep -q "result[ ]*:[ ]*${result}" $output_file

    rc=$?

    if [[ "${rc}" != "0" ]]; then
        error "Did not find any runs with result '$result' in output"
        exit 1
    fi

    success "galasactl runs get with age parameter with just from value and result '$result' returned results okay."
}

#--------------------------------------------------------------------------
function runs_get_test_can_get_runs_by_group {

    group_name=$1
    h2 "Performing runs get with details format providing group name '${group_name}'..."

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs get \
    --group ${group_name} \
    --format details \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    output_file="runs-get-output.txt"
    $cmd | tee $output_file

    grep -q "group[ ]*:[ ]*${group_name}" $output_file

    rc=$?

    if [[ "${rc}" != "0" ]]; then
        error "Did not find any runs with group name '${group_name}' in output"
        exit 1
    fi

    success "galasactl runs get with group name '${group_name}' returned results OK."
}

#--------------------------------------------------------------------------
function runs_get_test_non_existant_group_returns_zero {

    group_name="non-existant-group"
    h2 "Attempting to get runs by a non-existant group name '${group_name}'..."

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs get \
    --group ${group_name} \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    output_file="runs-get-output.txt"
    $cmd | tee $output_file

    grep -q "Total:0" $output_file

    rc=$?

    if [[ "${rc}" != "0" ]]; then
        error "Found runs with group name '${group_name}' in output when there should not have been any results"
        exit 1
    fi

    success "galasactl runs get with non-existant group name '${group_name}' returned no results as expected."
}

#--------------------------------------------------------------------------
function launch_test_on_ecosystem_without_portfolio {
    h2 "Launching test on an ecosystem... directly... without a portfolio."

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs submit \
    --bootstrap $bootstrap \
    --class dev.galasa.ivts/dev.galasa.ivts.core.CoreManagerIVT \
    --stream ivts
    --throttle 1 \
    --poll 10 \
    --progress 1 \
    --noexitcodeontestfailures \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '0' because the ecosystem should be able to run this test.
    # We have specified the flag --noexitcodeontestfailures so that we still receive a return code '0' even if the test fails,
    # as we are testing galasactl here, not the test itself
    if [[ "${rc}" != "0" ]]; then
        error "Failed to submit a test to a remote ecosystem."
        exit 1
    fi
    success "Submitting test to ecosystem worked OK"
}

#--------------------------------------------------------------------------
function create_portfolio_with_unknown_test {
    h2 "Building a portfolio with an unknown test..."

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs prepare \
    --bootstrap $bootstrap \
    --stream ivts \
    --portfolio unknown-portfolio.yaml \
    --test local.UnknownTest \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '1' because the ecosystem doesn't know about this testcase.
    if [[ "${rc}" != "1" ]]; then
        error "Failed to recognise an Unknown testcase."
        exit 1
    fi
    success "Unknown test was recognised and no tests were selected"
}

#--------------------------------------------------------------------------
function launch_test_from_unknown_portfolio {
    h2 "Launching a test from an unknown portfolio..."

    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs submit \
    --bootstrap $bootstrap \
    --portfolio unknown-portfolio.yaml \
    --throttle 1 \
    --poll 10 \
    --progress 1 \
    --noexitcodeontestfailures \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '1' because the galasactl shouldn't be able to read this portfolio.
    if [[ "${rc}" != "1" ]]; then
        error "Failed to recognise a non-existent portfolio."
        exit 1
    fi
    success "Unknown portfolio could not be read. galasactl reported this error correctly."
}

#--------------------------------------------------------------------------
function runs_delete_check_run_can_be_deleted {
    run_name=$1

    h2 "Attempting to delete the run named '${run_name}' using runs delete..."

    mkdir -p ${BASEDIR}/temp
    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs delete \
    --name ${run_name} \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    # We expect a return code of '0' because the run should have been deleted successfully.
    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to delete run '${run_name}'"
        exit 1
    fi

    h2 "Checking that the run '${run_name}' no longer exists"

    cmd="${BINARY_LOCATION} runs get \
    --name ${run_name} \
    --bootstrap ${bootstrap}"

    output_file="runs-delete-output.txt"
    set -o pipefail
    $cmd | tee $output_file | grep -q "Total:0"

    # We expect a return code of '0' because there should be no runs with the given run name anymore.
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed when checking if run '${run_name}' has been deleted. The run still exists when it should not."
        exit 1
    fi

    success "galasactl runs delete was able to delete an existing run OK."
}

#--------------------------------------------------------------------------
function runs_delete_non_existant_run_returns_error {
    run_name="NonExistantRun123"

    h2 "Attempting to delete the non-existant run named '${run_name}' using runs delete..."

    mkdir -p ${BASEDIR}/temp
    cd ${BASEDIR}/temp

    cmd="${BINARY_LOCATION} runs delete \
    --name ${run_name} \
    --bootstrap ${bootstrap}"

    info "Command is: $cmd"

    output_file="runs-delete-output.txt"
    set -o pipefail
    $cmd | tee $output_file

    # We expect a return code of '1' because the run does not exist and an error should be reported.
    rc=$?
    if [[ "${rc}" != "1" ]]; then
        error "Failed to return an error when attempting to delete non-existant run '${run_name}'"
        exit 1
    fi

    success "galasactl runs delete correctly reported an error when attempting to delete a non-existant run."
}

#--------------------------------------------------------------------------
function test_runs_commands {
    # Launch test on ecosystem without a portfolio ...
    launch_test_on_ecosystem_without_portfolio

    # Launch test on ecosystem from a portfolio ...
    group_name="cli-ecosystem-tests"
    launch_test_on_ecosystem_with_portfolio ${group_name}

    # Query the result ... setting RUN_NAME to hold the one which galasa allocated
    get_result_with_runname
    runs_get_check_summary_format_output  $RUN_NAME
    runs_get_check_details_format_output  $RUN_NAME
    runs_get_check_raw_format_output  $RUN_NAME
    runs_get_test_can_get_runs_by_group ${group_name}
    runs_get_test_non_existant_group_returns_zero

    # Query the result with the age parameter
    runs_get_check_raw_format_output_with_from_and_to $RUN_NAME
    runs_get_check_raw_format_output_with_just_from $RUN_NAME

    # Check that the age parameter throws correct errors with invalid values
    runs_get_check_raw_format_output_with_no_runname_and_no_age_param
    runs_get_check_raw_format_output_with_invalid_age_param
    runs_get_check_raw_format_output_with_older_to_than_from_age
    runs_get_check_requestor_parameter
    runs_get_check_result_parameter
    # Unable to test 'to' age because the smallest time unit we support is Hours so would have to query a test that happened over an hour ago

    # Attempt to create a test portfolio with an unknown test ...
    create_portfolio_with_unknown_test

    # Attempt to launch a test from an unknown portfolio ...
    launch_test_from_unknown_portfolio

    runs_download_check_folder_names_during_test_run

    # Attempt to reset an active run...
    runs_reset_check_retry_present

    # Attempt to delete a run...
    runs_delete_check_run_can_be_deleted $RUN_NAME
    runs_delete_non_existant_run_returns_error
}


# checks if it's been called by main, set this variable if it is
if [[ "$CALLED_BY_MAIN" == "" ]]; then
    source $BASEDIR/calculate-galasactl-executables.sh
    calculate_galasactl_executable
    test_runs_commands
fi