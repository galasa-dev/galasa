#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
echo "Running script test-galasactl-local.sh"

# This script can be ran locally or executed in a pipeline to test the various built binaries of galasactl
# This script tests the 'galasactl project create' and 'galasactl runs submit local' commands
# Pre-requesite: the CLI must have been built first so the binaries are present in the ./bin directory


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
underline() { printf "${underline}${bold}%s${reset}\n" "$@"
}
h1() { printf "\n${underline}${bold}${blue}%s${reset}\n" "$@"
}
h2() { printf "\n${underline}${bold}${white}%s${reset}\n" "$@"
}
debug() { printf "${white}%s${reset}\n" "$@"
}
info() { printf "${white}➜ %s${reset}\n" "$@"
}
success() { printf "${green}✔ %s${reset}\n" "$@"
}
error() { printf "${red}✖ %s${reset}\n" "$@"
}
warn() { printf "${tan}➜ %s${reset}\n" "$@"
}
bold() { printf "${bold}%s${reset}\n" "$@"
}
note() { printf "\n${underline}${bold}${blue}Note:${reset} ${blue}%s${reset}\n" "$@"
}


#-----------------------------------------------------------------------------------------                   
# Functions
#-----------------------------------------------------------------------------------------                   
function usage {
    info "Syntax: test-galasactl-local.sh [flags]"
    cat << EOF
Optional flags are:
--buildTool maven: Use Maven to build the generated project.
--buildTool gradle: Use Gradle to build the generated project.

If neither are specified, defaults to Maven.
EOF
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------              
buildTool=""

while [ "$1" != "" ]; do
    case $1 in
        --buildTool )                     shift
                                          buildTool="$1"
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


if [[ "${buildTool}" != "" ]]; then
    case ${buildTool} in
        maven  )            echo "Using Maven"
                            ;;
        gradle )            echo "Using Gradle"
                            ;;
        * )                 error "Unrecognised build tool ${buildTool}"
                            usage
                            exit 1
    esac
else
    export buildTool="maven"
    info "No build tool specified so defaulting to Maven." 
fi

#--------------------------------------------------------------------------
function calculate_galasactl_executable {
    h2 "Calculate the name of the galasactl executable for this machine/os"

    raw_os=$(uname -s) # eg: "Darwin"
    os=""
    case $raw_os in
        Darwin*) 
            os="darwin" 
            ;;
        Windows*)
            os="windows"
            ;;
        Linux*)
            os="linux"
            ;;
        *) 
            error "Failed to recognise which operating system is in use. $raw_os"
            exit 1
    esac

    architecture=$(uname -m)

    export binary="galasactl-${os}-${architecture}"
    info "galasactl binary is ${binary}"
    success "OK"
}

#--------------------------------------------------------------------------
# Initialise Galasa home
function galasa_home_init {
    h2 "Initialising galasa home directory"

    rm -rf ${BASEDIR}/temp
    mkdir -p ${BASEDIR}/temp
    cd ${BASEDIR}/temp

    export GALASA_HOME=${BASEDIR}/temp/home

    cmd="${BASEDIR}/bin/${binary} local init --development \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to initialise galasa home"
        exit 1
    fi
    success "Galasa home initialised"
}

#--------------------------------------------------------------------------
# Invoke the galasactl command to create a project.
function generate_sample_code {
    h2 "Invoke the tool to create a sample project."

    cd ${BASEDIR}/temp

    export PACKAGE_NAME="dev.galasa.example.banking"

    if [[ "${buildTool}" == "maven" ]]; then
        ${BASEDIR}/bin/${binary} project create --package ${PACKAGE_NAME} --features payee --obr --maven --force --development --log -
    elif [[ "${buildTool}" == "gradle" ]]; then
        ${BASEDIR}/bin/${binary} project create --package ${PACKAGE_NAME} --features payee --obr --gradle --force --development --log -
    fi

    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error " Failed to create the galasa test project using galasactl command. rc=${rc}"
        exit 1
    fi
    success "OK"
}

#--------------------------------------------------------------------------
# Now build the source it created
function build_generated_source {
    h2 "Building the sample project we just generated."
    cd ${BASEDIR}/temp/${PACKAGE_NAME}

    if [[ "${buildTool}" == "maven" ]]; then
        mvn clean test install
    elif [[ "${buildTool}" == "gradle" ]]; then
        gradle clean build publishToMavenLocal
    fi

    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error " Failed to build the generated source code which galasactl created."
        exit 1
    fi
    success "OK"
}

#--------------------------------------------------------------------------
# Run test using the galasactl locally in a JVM
function submit_local_test {

    h2 "Submitting a local test using galasactl in a local JVM"

    cd ${BASEDIR}/temp/*banking

    BUNDLE=$1
    JAVA_CLASS=$2
    OBR_GROUP_ID=$3
    OBR_ARTIFACT_ID=$4
    OBR_VERSION=$5

    export REMOTE_MAVEN=https://development.galasa.dev/main/maven-repo/obr/

    export GALASACTL="${BASEDIR}/bin/${binary}"

    ${GALASACTL} runs submit local \
    --obr mvn:${OBR_GROUP_ID}/${OBR_ARTIFACT_ID}/${OBR_VERSION}/obr \
    --remoteMaven ${REMOTE_MAVEN} \
    --class ${BUNDLE}/${JAVA_CLASS} \
    --throttle 1 \
    --requesttype automated-test \
    --poll 10 \
    --progress 1 \
    --log - \
    --reportjunit junitReport.xml \
    --reportjson jsonReport.json

    # Uncomment this if testing that a test that should fail, fails
    # --noexitcodeontestfailures \

    rc=$?
    if [[ "${rc}" != "0" ]]; then 
        error "Failed to run the test"
        exit 1
    fi
    success "Test ran OK"
}

function run_test_locally_using_galasactl {
    
    # Run the Payee tests.
    export TEST_BUNDLE=dev.galasa.example.banking.payee
    export TEST_JAVA_CLASS=dev.galasa.example.banking.payee.TestPayee
    export TEST_OBR_GROUP_ID=dev.galasa.example.banking
    export TEST_OBR_ARTIFACT_ID=dev.galasa.example.banking.obr
    export TEST_OBR_VERSION=0.0.1-SNAPSHOT

    export totalTests=1
    export failedTests=0
    export testName="simpleSampleTest"
    export testResult="Passed"

    submit_local_test $TEST_BUNDLE $TEST_JAVA_CLASS $TEST_OBR_GROUP_ID $TEST_OBR_ARTIFACT_ID $TEST_OBR_VERSION
    check_junit_report $totalTests $failedTests
    check_json_report $testName $testResult
}

function check_junit_report {

    cd ${BASEDIR}/temp/*banking

    totalTests=$1
    failedTests=$2
    
    junitReportFile="junitReport.xml"
    stringToFind="name=\"Galasa test run\" tests=\"${totalTests}\" failures=\"${failedTests}\""
    
    echo "string to find: "${stringToFind}

    if ! grep -q "${stringToFind}" ${junitReportFile}; then
        error "Junit report not created properly"
        exit 1
    fi
    success "Junit report was created successfully"  
}

function check_json_report {

    cd ${BASEDIR}/temp/*banking

    testName=$1
    testResult=$2
    
    jsonReportFile="jsonReport.json"
    stringToFind="      \"tests\": \\[
        {
          \"name\": \"${testName}\",
          \"result\": \"${testResult}\"
        }
      ]"
    
    echo "string to find: "${stringToFind}

    if ! grep -q "${stringToFind}" ${jsonReportFile}; then
        error "Json report not created properly"
        exit 1
    fi
    success "Json report was created successfully"  
}

function cleanup_local_maven_repo {
    rm -fr ~/.m2/repository/dev/galasa/example
}

calculate_galasactl_executable

# Initialise Galasa home ...
galasa_home_init

# Generate sample project ...
generate_sample_code

cleanup_local_maven_repo
build_generated_source

run_test_locally_using_galasactl

CALLED_BY_MAIN="true"
source ${BASEDIR}/test-scripts/gherkin-runs-tests.sh
test_gherkin_commands