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
-p | --passphrase - Optional. A GPG passphrase to use to sign the artifacts created.
    This can be the same GPG passphrase as you use to sign github commits if you wish, 
    provided that the artifacts are never released or deployed without being re-built
    with the official verification GPG key.
    The passphrase will default to the value of the GPG_PASSPHRASE environment variable
    if available, otherwise the command will fail until some sort of passphrase is 
    supplied.
-h | --help - See this help.

Environment variables
LOGS_DIR - Optional. Where logs are placed. Defaults to creating a temporary directory.
GPG_PASSPHRASE - The passphrase. Over-ridden if the --passphrase parameter is used.
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

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   
build_type=""

gpg_passphrase=""

while [ "$1" != "" ]; do
    case $1 in
        -p | --passphrase )
                                shift
                                export gpg_passphrase="$1"
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

if [[ "${gpg_passphrase}" == "" ]]; then

    if [[ "${GPG_PASSPHRASE}" != "" ]]; then
        info "Environment variable GPG_PASSPHRASE is being used to sign the built artifacts."
        export gpg_passphrase="${GPG_PASSPHRASE}"
    else
        error "Need to use the --passphrase parameter to supply a GPG key to be used for signing artifacts. Or supply a value with the GPG_PASSPHRASE environment variable."
        usage
        exit 1  
    fi
fi

info "gpg passphrase is $gpg_passphrase"


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

cmd="mvn clean install -Dgpg.passphrase=${gpg_passphrase}"


info "Using command: $cmd"
$cmd 2>&1 >> ${log_file}
rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to build ${project}" ; info "See log file at ${log_file}" ; exit 1 ; fi
success "Built OK"

h2 "Listing dependencies..."
mvn dependency:tree > $BASEDIR/temp/dependencies.txt
rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to list dependencies ${project}" ; info "See log file at ${log_file}" ; exit 1 ; fi
success "Dependencies listed here: $BASEDIR/temp/dependencies.txt"

check_secrets

success "Project ${project} built - OK - log is at ${log_file}"