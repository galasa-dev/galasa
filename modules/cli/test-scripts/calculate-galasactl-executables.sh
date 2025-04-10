#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

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

    # Determine if the /bin directory exists i.e. if the script is testing
    # a built binary or if it is testing a published Docker image in GHCR
    # If testing a built binary, it will use it from the /bin, otherwise 
    # `galasactl` will be used as it is installed on the path within the image.
    path_to_bin="${BASEDIR}/bin"
    echo $path_to_bin

    # Check if the /bin directory exists
    if [ -d "$path_to_bin" ]; then
        echo "The /bin directory exists so assume the script is testing a locally built binary."

        export BINARY_LOCATION="${ORIGINAL_DIR}/bin/${binary}"
        info "binary location is ${BINARY_LOCATION}"
    else
        echo "The /bin directory does not exist so assume the script is testing a published image in GHCR."

        export BINARY_LOCATION="galasactl"
    fi

    success "OK"
}