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
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
# cd "${BASEDIR}"

cd "${BASEDIR}/.."
WORKSPACE_DIR=$(pwd)

cd "${BASEDIR}/../.."
REPO_ROOT=$(pwd)

cd $BASEDIR


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
-s | --detectsecrets true|false : Do we want to detect secrets in the entire repo codebase ? Default is 'true'. Valid values are 'true' or 'false'
-h | --help - See this help.

Environment variables
LOGS_DIR - Optional. Where logs are placed. Defaults to creating a temporary directory.
EOF
}

function check_exit_code () {
    # This function takes 2 parameters in the form:
    # $1 an integer value of the returned exit code
    # $2 an error message to display if $1 is not equal to 0
    if [[ "$1" != "0" ]]; then 
        error "$2" 
        exit 1  
    fi
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

function check_secrets_unless_supressed() {
    # Check if the script is being called directly or from another script
    if [[ -z "${IN_CHAIN_MODE}" ]]; then
        info "Script invoked directly, running detect-secrets.sh script"

        # Run the detect-secrets.sh in root
        cd "${WORKSPACE_DIR}/.."
        TOOL_DIR=$(pwd)
        $TOOL_DIR/tools/detect-secrets.sh
    fi
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   
build_type=""
detectsecrets="true"
while [ "$1" != "" ]; do
    case $1 in
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

if [[ "${detectsecrets}" != "true" ]] && [[ "${detectsecrets}" != "false" ]]; then
    error "--detectsecrets flag must be 'true' or 'false'. Was $detectesecrets"
    exit 1
fi

#-----------------------------------------------------------------------------------------                   
# Main logic.
#-----------------------------------------------------------------------------------------                   

source_dir="."

project=$(basename ${BASEDIR})
h1 "Building ${project}"

# Create a temporary dir.
# Note: This bash 'spell' works in OSX and Linux.
if [[ -z ${LOGS_DIR} ]]; then
    export LOGS_DIR=$(mktemp -d 2>/dev/null || mktemp -d -t "galasa-logs")
    info "Logs are stored in the ${LOGS_DIR} folder."
    info "Over-ride this setting using the LOGS_DIR environment variable."
else
    mkdir -p ${LOGS_DIR} 2>&1 > /dev/null # Don't show output. We don't care if it already existed.
    info "Logs are stored in the ${LOGS_DIR} folder."
    info "Over-ridden by caller using the LOGS_DIR variable."
fi

export LOGS_DIR=$BASEDIR/temp
rm -fr $BASEDIR/temp
mkdir -p $BASEDIR/temp

info "Using source code at ${source_dir}"
cd ${BASEDIR}/${source_dir}

log_file=${LOGS_DIR}/${project}.txt
info "Log will be placed at ${log_file}"

h2 "Building..."

cmd="mvn clean install -Dgpg.skip=true"


info "Using command: $cmd"
$cmd 2>&1 >> ${log_file}
rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to build ${project}" ; info "See log file at ${log_file}" ; exit 1 ; fi
success "Built OK"

h2 "Listing dependencies..."
mvn dependency:tree > $BASEDIR/temp/dependencies.txt
rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to list dependencies ${project}" ; info "See log file at ${log_file}" ; exit 1 ; fi
success "Dependencies listed here: $BASEDIR/temp/dependencies.txt"

if [[ "$detectsecrets" == "true" ]]; then
    $REPO_ROOT/tools/detect-secrets.sh 
    check_exit_code $? "Failed to detect secrets"
fi

success "Project ${project} built - OK - log is at ${log_file}"