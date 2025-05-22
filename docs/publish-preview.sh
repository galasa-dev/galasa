#! /usr/bin/env bash 

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
#-----------------------------------------------------------------------------------------                   
#
# Objectives: Build the Galasa documentation
#
# Environment variable over-rides:
# None.
# 
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
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
--help

Environment variables used:
None

EOF
}

function check_exit_code () {
    # This function takes 3 parameters in the form:
    # $1 an integer value of the returned exit code
    # $2 an error message to display if $1 is not equal to 0
    if [[ "$1" != "0" ]]; then 
        error "$2" 
        exit 1  
    fi
}
#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   

while [ "$1" != "" ]; do
    case $1 in
        -h | --help )           usage
                                exit
                                ;;
        * )                     error "Unexpected argument $1"
                                usage
                                exit 1
    esac
    shift
done

cd ${BASEDIR}
mkdir -p temp

export GALASA_PREVIEW_REPO_NAME="galasa-docs-preview"

cd temp
rm -fr ${GALASA_PREVIEW_REPO_NAME}
git clone git@github.com:${GITHUB_ORG}/${GALASA_PREVIEW_REPO_NAME}.git

cd ${GALASA_PREVIEW_REPO_NAME}

rm -fr docs

mkdir -p docs
cp -r ../../build/site/* docs

git config user.name $(GIT_NAME)
git config user.email $(GIT_EMAIL)
git config credential.username $(GIT_NAME)
git config user.email
git config user.name

git add .
git commit -m "$USER pushing changes from local system manually" --allow-empty
git push https://${GITHUB_ORG}:${GITHUB_TOKEN}@github.com/${GITHUB_ORG}/${GALASA_PREVIEW_REPO_NAME}.git main

success "OK. Documentation has been published to https://${GITHUB_ORG}.github.io/${GALASA_PREVIEW_REPO_NAME}/"

