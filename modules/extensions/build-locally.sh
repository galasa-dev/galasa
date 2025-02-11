#! /usr/bin/env bash 

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
#-----------------------------------------------------------------------------------------                   
#
# Objectives: Build this repository code locally.
#
# Environment variable over-rides:
# LOGS_DIR - Optional. Where logs are placed. Defaults to creating a temporary directory.
# SOURCE_MAVEN - Optional. Where a maven repository is from which the build will draw artifacts.
# DEBUG - Optional. Defaults to 0 (off)
# 
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)


cd "${BASEDIR}/.."
WORKSPACE_DIR=$(pwd)

cd "${BASEDIR}/../.."
REPO_ROOT=$(pwd)

cd "${BASEDIR}"

#-----------------------------------------------------------------------------------------                   
#
# Set Colors
#
#-----------------------------------------------------------------------------------------                   
bold=$(tput bold)
underline=$(tput sgr 0 1)
reset=$(tput sgr0)
red=$(tput setaf 1)
green=$(tput setaf 76)
white=$(tput setaf 7)
tan=$(tput setaf 202)
blue=$(tput setaf 25)

#-----------------------------------------------------------------------------------------                   
#
# Headers and Logging
#
#-----------------------------------------------------------------------------------------                   
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
# Functions
#-----------------------------------------------------------------------------------------                   
function usage {
    info "Syntax: build-locally.sh [OPTIONS]"
    cat << EOF
Options are:
-c | --clean : Do a clean build. One of the --clean or --delta flags are mandatory.
-d | --delta : Do a delta build. One of the --clean or --delta flags are mandatory.
-s | --detectsecrets true|false : Do we want to detect secrets in the entire repo codebase ? Default is 'true'. Valid values are 'true' or 'false'

Environment variables used:
DEBUG - Optional. Valid values "1" (on) or "0" (off). Defaults to "0" (off).
SOURCE_MAVEN - Optional. Where maven/gradle can look for pre-built development levels of things.
    Defaults to https://development.galasa.dev/main/maven-repo/obr/

EOF
}

function check_exit_code () {
    # This function takes 3 parameters in the form:
    # $1 an integer value of the expected exit code
    # $2 an error message to display if $1 is not equal to 0
    if [[ "$1" != "0" ]]; then 
        error "$2" 
        exit 1  
    fi
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   
build_type=""
detectsecrets="true"
while [ "$1" != "" ]; do
    case $1 in
        -c | --clean )          build_type="clean"
                                ;;
        -d | --delta )          build_type="delta"
                                ;;
        -s | --detectsecrets )  detectsecrets="$2"
                                shift
                                ;;

        -h | --help )           usage
                                exit
                                ;;
        * )                     error "Unexpected argument $1"
                                usage
                                exit 1
    esac
    shift
done

if [[ "${build_type}" == "" ]]; then
    error "Need to use either the --clean or --delta parameter."
    usage
    exit 1  
fi

if [[ "${detectsecrets}" != "true" ]] && [[ "${detectsecrets}" != "false" ]]; then
    error "--detectsecrets flag must be 'true' or 'false'. Was $detectesecrets"
    exit 1
fi

#-----------------------------------------------------------------------------------------                   
# Main logic.
#-----------------------------------------------------------------------------------------                   

source_dir="galasa-extensions-parent"

project=$(basename ${BASEDIR})
h1 "Building ${project}"

# Debug or not debug ? Override using the DEBUG flag.
if [[ -z ${DEBUG} ]]; then
    export DEBUG=0
    # export DEBUG=1
    info "DEBUG defaulting to ${DEBUG}."
    info "Over-ride this variable if you wish. Valid values are 0 and 1."
else
    info "DEBUG set to ${DEBUG} by caller."
fi

# Over-rode SOURCE_MAVEN if you want to build from a different maven repo...
if [[ -z ${SOURCE_MAVEN} ]]; then
    export SOURCE_MAVEN=https://development.galasa.dev/main/maven-repo/obr/
    info "SOURCE_MAVEN repo defaulting to ${SOURCE_MAVEN}."
    info "Set this environment variable if you want to over-ride this value."
else
    info "SOURCE_MAVEN set to ${SOURCE_MAVEN} by caller."
fi

export LOGS_DIR=$BASEDIR/temp
mkdir -p $LOGS_DIR

info "Using source code at ${source_dir}"
cd ${BASEDIR}/${source_dir}
if [[ "${DEBUG}" == "1" ]]; then
    OPTIONAL_DEBUG_FLAG="-debug"
else
    OPTIONAL_DEBUG_FLAG="-info"
fi

# auto plain rich or verbose
CONSOLE_FLAG=--console=plain

log_file=${LOGS_DIR}/${project}.txt
info "Log will be placed at ${log_file}"

function clean_maven_repo {
    h2 "Removing .m2 artifacts"
    rm -fr ~/.m2/repository/dev/galasa/dev.galasa.cps.etcd
    rm -fr ~/.m2/repository/dev/galasa/dev.galasa.ras.couchdb
    rm -fr ~/.m2/repository/dev/galasa/dev.galasa.cps.rest
    rm -fr ~/.m2/repository/dev/galasa/dev.galasa.events.kafka
    success "OK"
}

function build_with_gradle {
    h2 "Building with gradle"

    if [[ "${build_type}" == "clean" ]]; then
        goals="clean build check jacocoTestReport publishToMavenLocal --no-build-cache "
    else
        goals="build check jacocoTestReport publishToMavenLocal"
    fi

    cmd="gradle \
    ${CONSOLE_FLAG} \
    -Dorg.gradle.java.home=${JAVA_HOME} \
    -PsourceMaven=${SOURCE_MAVEN} ${OPTIONAL_DEBUG_FLAG} \
    ${goals}"
    info "Using command: $cmd"
    $cmd 2>&1 > ${log_file}
    rc=$? 
    check_exit_code $rc "Failed to build ${project} with gradle."
}

function displayCouchDbCodeCoverage {
    h2 "Calculating couchDb code coverage..."
    percent_code_complete=$(cat ${BASEDIR}/galasa-extensions-parent/dev.galasa.ras.couchdb/build/jacocoHtml/dev.galasa.ras.couchdb.internal/index.html \
    | sed "s/.*<td>Total<\/td>//1" \
    | cut -f1 -d'%' \
    | sed "s/.*>//g")
    info 
    info
    info "Statement code coverage is ${percent_code_complete}%"
    info
    info "See html report here: file://${BASEDIR}/galasa-extensions-parent/dev.galasa.ras.couchdb/build/jacocoHtml/index.html"
}

function displayKafkaCodeCoverage {
    h2 "Calculating Kafka code coverage..."
    percent_code_complete=$(cat ${BASEDIR}/galasa-extensions-parent/dev.galasa.events.kafka/build/jacocoHtml/dev.galasa.events.kafka.internal/index.html \
    | sed "s/.*<td>Total<\/td>//1" \
    | cut -f1 -d'%' \
    | sed "s/.*>//g")
    info 
    info
    info "Statement code coverage is ${percent_code_complete}%"
    info
    info "See html report here: file://${BASEDIR}/galasa-extensions-parent/dev.galasa.events.kafka/build/jacocoHtml/index.html"
}

function check_secrets {
    h2 "updating secrets baseline"
    cd ${BASEDIR}
    detect-secrets scan --update .secrets.baseline
    rc=$? 
    check_exit_code $rc "Failed to run detect-secrets. Please check it is installed properly" 
    success "updated secrets file"

    h2 "running audit for secrets"
    detect-secrets audit .secrets.baseline
    rc=$? 
    check_exit_code $rc "Failed to audit detect-secrets."
    
    #Check all secrets have been audited
    secrets=$(grep -c hashed_secret .secrets.baseline)
    audits=$(grep -c is_secret .secrets.baseline)
    if [[ "$secrets" != "$audits" ]]; then 
        error "Not all secrets found have been audited"
        exit 1  
    fi
    success "secrets audit complete"

    h2 "Removing the timestamp from the secrets baseline file so it doesn't always cause a git change."
    mkdir -p temp
    rc=$? 
    check_exit_code $rc "Failed to create a temporary folder"
    cat .secrets.baseline | grep -v "generated_at" > temp/.secrets.baseline.temp
    rc=$? 
    check_exit_code $rc "Failed to create a temporary file with no timestamp inside"
    mv temp/.secrets.baseline.temp .secrets.baseline
    rc=$? 
    check_exit_code $rc "Failed to overwrite the secrets baseline with one containing no timestamp inside."
    success "secrets baseline timestamp content has been removed ok"
}

function update_release_yaml {
    h2 "Updating release.yaml"

    # After running 'gradle build', a release.yaml file should have been automatically generated
    generated_release_yaml="${BASEDIR}/galasa-extensions-parent/build/release.yaml"
    current_release_yaml="${BASEDIR}/release.yaml"

    if [[ -f ${generated_release_yaml} ]]; then
        cp ${generated_release_yaml} ${current_release_yaml}
        success "Updated release.yaml OK"
    else
        warn "Failed to automatically generate release.yaml, please ensure any changed bundles have had their versions updated in ${current_release_yaml}"
    fi
}


clean_maven_repo
build_with_gradle
update_release_yaml
displayCouchDbCodeCoverage
displayKafkaCodeCoverage

if [[ "$detectsecrets" == "true" ]]; then
    $REPO_ROOT/tools/detect-secrets.sh 
    check_exit_code $? "Failed to detect secrets"
fi

success "Project ${project} built - OK - log is at ${log_file}"