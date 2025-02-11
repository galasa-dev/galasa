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

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)

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

#-------------------------------------------------------------
function check_exit_code () {
    # This function takes 2 parameters in the form:
    # $1 an integer value of the returned exit code
    # $2 an error message to display if $1 is not equal to 0
    if [[ "$1" != "0" ]]; then 
        error "$2" 
        exit 1  
    fi
}

function usage {
    info "Syntax: build-locally.sh [OPTIONS]"
    cat << EOF
Options are:
-s | --detectsecrets true|false : Do we want to detect secrets in the entire repo codebase ? Default is 'true'. Valid values are 'true' or 'false'

Environment variables used:
SOURCE_MAVEN - Optional. Where gradle can look for pre-built development levels of things.
    Defaults to https://development.galasa.dev/main/maven-repo/obr/
EOF
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   

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
h1 "Building the ivts module"

# Override SOURCE_MAVEN if you want to build from a different maven repo...
if [[ -z ${SOURCE_MAVEN} ]]; then
    export SOURCE_MAVEN=https://development.galasa.dev/main/maven-repo/obr/
    info "SOURCE_MAVEN repo defaulting to ${SOURCE_MAVEN}."
    info "Set this environment variable if you want to override this value."
else
    info "SOURCE_MAVEN set to ${SOURCE_MAVEN} by caller."
fi

function clean_ivts() {
    TARGET_MAVEN_FOLDER=~/.m2/repository
    h2 "Cleaning the existing built ivts in the ${TARGET_MAVEN_FOLDER} repository"
    rm -fr ${TARGET_MAVEN_FOLDER}/dev/galasa/ivts/dev.galasa.ivts*
    success "Cleaned up ${TARGET_MAVEN_FOLDER} repository"
}

function build_ivts() {
    h2 "Building the ivts source using gradle."
    cd $BASEDIR/dev.galasa.ivts
    rc=$?
    check_exit_code $rc "Failed to cd to the ivts source folder"
    gradle build check publish -PtargetMaven=${TARGET_MAVEN_FOLDER} -PsourceMaven=${SOURCE_MAVEN}
    rc=$?
    check_exit_code $rc "Failed to build the ivts module source"
    success "OK"
}

clean_ivts
build_ivts

if [[ "$detectsecrets" == "true" ]]; then
    $REPO_ROOT/tools/detect-secrets.sh 
    check_exit_code $? "Failed to detect secrets"
fi
