#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
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

function get_architecture() {
        raw_os=$(uname -s) # eg: "Darwin"
    os=""
    case $raw_os in
        Darwin*)
            os="darwin"
            ;;
        Linux*)
            os="linux"
            ;;
        *)
            error "Unsupported operating system is in use. $raw_os"
            exit 1
    esac

    architecture=$(uname -m)
    case $architecture in
        aarch64)
            architecture="arm64"
            ;;
        amd64)
            architecture="x86_64"
    esac
}

function generate_code() {
    h2 "Generating code..."



    cmd="${BASEDIR}/../bin/openapi2beans-${os}-${architecture} generate \
        --yaml ${BASEDIR}/src/main/resources/test-reference.yaml \
        --output ${BASEDIR}/src/main/java \
        --package dev.galasa.openapi2beans.example.generated \
        --force"

    $cmd
    rc=$?; if [[ "${rc}" != "0" ]]; then error "Failed to generate code" ; exit 1 ; fi

    success "OK"
}

function check_code() {
    h2 "Checking the generated code..."

    mvn clean test
    rc=$?; if [[ "${rc}" != "0" ]]; then error "Failed to build code" ; exit 1 ; fi
    success "OK"
}

get_architecture
generate_code
check_code
