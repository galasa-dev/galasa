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


h2 "Making sure the plantuml tool is available"
if [[ -e plantuml.jar ]]; then
    info "Plantuml jar is already downloaded. No need to download it again"
else 
    info "Downloading the plantuml tool..."
    url=https://github.com/plantuml/plantuml/releases/download/v1.2024.3/plantuml-epl-1.2024.3.jar
    curl -O $url
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to download the plantuml tool jar." ; exit 1 ; fi
    mv plantuml-*.jar plantuml.jar
fi
success "OK"

h2 "Building using the make file."
make all
rc=$? ; if [[ "${rc}" != "0" ]]; then error "Make build failed." ; exit 1 ; fi
success "OK"

h2 "Running Java Checker."
./JavaChecker/build-locally.sh