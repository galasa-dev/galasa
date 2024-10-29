#! /usr/bin/env bash 

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
#-----------------------------------------------------------------------------------------                   
#
# Objectives: Tests the cps rest layer as much as we can from a real testcase.
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
cd "${BASEDIR}"

cd "${BASEDIR}/.."
WORKSPACE_DIR=$(pwd)
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
    info "Syntax: test.sh [OPTIONS]"
    cat << EOF
Options are:
-u | --galasaApiUrl : The url of the galasa api server. Mandatory. For example: https://my.server/api
-h | --help         : displays this help.
EOF
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   
galasaApiUrl=""

while [ "$1" != "" ]; do
    case $1 in
        -u | --galasaApiUrl )   shift
                                galasaApiUrl=$1
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

if [[ "${galasaApiUrl}" == "" ]]; then
    error "Need to use the --galasaApiUrl parameter."
    usage
    exit 1  
fi

#-----------------------------------------------------------------------------------------                   
# More Functions...
#-----------------------------------------------------------------------------------------


#-----------------------------------------------------------------------------------------
function run_tests {
    h2 "Running the test code locally"
    # Add the "--log -" flag if you want to see more detailed output.
    baseName="dev.galasa.cps.rest.test"
    cmd="galasactl runs submit local --obr mvn:${baseName}/${baseName}.obr/0.0.1-SNAPSHOT/obr \
        --class ${baseName}.http/${baseName}.http.TestHttp \
        --bootstrap file://$BASEDIR/temp/home/bootstrap.properties \
        --log -
       "
    $cmd
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to run the test code. Return code: ${rc}" ; exit 1 ; fi
    success "OK"
}

function build_galasa_home {
    h2 "Building galasa home"

    rm -fr $BASEDIR/temp
    export GALASA_HOME=$BASEDIR/temp/home
    mkdir $BASEDIR/temp
    cd $BASEDIR/temp

    galasactl local init 
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to build galasa home. Return code: ${rc}" ; exit 1 ; fi

    galasaConfigStoreRestUrl=$(echo -n "${galasaApiUrl}" | sed "s/https:/galasacps:/g")
    galasactl auth login 
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to login to the galasa server. Return code: ${rc}" ; exit 1 ; fi

    cat << EOF >> $BASEDIR/temp/home/bootstrap.properties

# These properties were added on the fly by the test script.

# Target the CPS on the ecosystem
framework.config.store=${galasaConfigStoreRestUrl}
framework.extra.bundles=dev.galasa.cps.rest
EOF

    success "OK"
}


function generating_galasa_test_project {
    h2 "Generating galasa test project code..."
    cd ${BASEDIR}/temp
    cmd="galasactl project create --package dev.galasa.cps.rest.test --features http --obr --gradle --force --development "
    info "Command is $cmd"
    $cmd
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to generate galasa test project. Return code: ${rc}" ; exit 1 ; fi
    success "OK"
}


function build_test_project {
    h2 "Building the generated code..."
    cd ${BASEDIR}/temp/dev.galasa.cps.rest.test
    gradle clean build publishToMavenLocal
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to build the generated test project code. Return code: ${rc}" ; exit 1 ; fi
    success "OK"
}


build_galasa_home
generating_galasa_test_project
build_test_project
run_tests