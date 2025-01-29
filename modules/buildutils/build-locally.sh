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
underline() { printf "${underline}${bold}%s${reset}\n" "$@" ; }
h1() { printf "\n${underline}${bold}${blue}%s${reset}\n" "$@" ; }
h2() { printf "\n${underline}${bold}${white}%s${reset}\n" "$@" ; }
debug() { printf "${white}[.] %s${reset}\n" "$@" ; }
info()  { printf "${white}[➜] %s${reset}\n" "$@" ; }
success() { printf "${white}[${green}✔${white}] ${green}%s${reset}\n" "$@" ; }
error() { printf "${white}[${red}✖${white}] ${red}%s${reset}\n" "$@" ; }
warn() { printf "${white}[${tan}➜${white}] ${tan}%s${reset}\n" "$@" ; }
bold() { printf "${bold}%s${reset}\n" "$@" ; }
note() { printf "\n${underline}${bold}${blue}Note:${reset} ${blue}%s${reset}\n" "$@" ; }

#-----------------------------------------------------------------------------------------                   
# Functions
#-----------------------------------------------------------------------------------------                   
function usage {
    info "Syntax: build-locally.sh [OPTIONS]"
    cat << EOF
Options are:
-h | --help : Display this help text
-s | --detectsecrets true|false : Do we want to detect secrets in the entire repo codebase ? Default is 'true'. Valid values are 'true' or 'false'

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
    detect-secrets scan --exclude-files "go.sum|openapi2beans/go.sum" --update .secrets.baseline
    rc=$? 
    check_exit_code $rc "Failed to run detect-secrets.  Check it is installed properly" 
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
    sed -i '' '/[ ]*"generated_at": ".*",/d' .secrets.baseline
    success "secrets audit complete"
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
exportbuild_type=""
detectsecrets="true"
while [ "$1" != "" ]; do
    case $1 in
        -h | --help )           usage
                                exit
                                ;;
        -s | --detectsecrets )  detectsecrets="$2"
                                shift
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

function clean_temp_folder() {
    rm -fr $BASEDIR/temp
    mkdir -p $BASEDIR/temp
    LOGS_DIR=$BASEDIR/temp
}

function build_tools() {

    project=$(basename ${BASEDIR})
    h1 "Building ${project}"

    info "Using source code at ${source_dir}"
    cd ${BASEDIR}/${source_dir}

    log_file=${LOGS_DIR}/${project}.txt
    info "Log will be placed at ${log_file}"
    date > ${log_file}

    cmd="make all"
    info "Command is '$cmd'"

    cd ${BASEDIR}
    $cmd 2>&1 >> ${log_file}

    rc=$? 
    check_exit_code $rc "Failed to build the ${project}" 
    success "${project} built ok - log is at ${log_file}"
}



function build_openapi2beans() {
    h2 "Building openapi2beans."
    ./openapi2beans/build-locally.sh
}


clean_temp_folder
build_tools
build_openapi2beans

$BASEDIR/test-locally.sh
rc=$? ; check_exit_code $rc "Failed to test galasabld" 

if [[ "$detectsecrets" == "true" ]]; then
    $REPO_ROOT/tools/detect-secrets.sh 
    check_exit_code $? "Failed to detect secrets"
fi