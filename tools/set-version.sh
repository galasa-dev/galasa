#! /usr/bin/env bash 

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------------                   
#
# Objectives: Sets the version number of this component.
#
# Environment variable over-rides:
# None
# 
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)

cd "${BASEDIR}/.."
REPO_ROOT_DIR=$(pwd)


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
    h1 "Syntax"
    cat << EOF
set-version.sh [OPTIONS]
Options are:
-v | --version xxx : Mandatory. Set the version number to something explicitly. 
    Re-builds the release.yaml based on the contents of sub-projects.
    For example '--version 0.40.0'
EOF
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   
component_version=""

while [ "$1" != "" ]; do
    case $1 in
        -v | --version )        shift
                                export component_version=$1
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

if [[ -z $component_version ]]; then 
    error "Missing mandatory '--version' argument."
    usage
    exit 1
fi

function check_for_error() {
    rc=$?
    message="$1"
    if [[ "${rc}" != "0" ]]; then 
        error "$message"
        exit 1
    fi
}

function set_version_in_all_modules() {
    h2 "Setting the version number in all modules to $component_version"
    for module_name in $(ls $REPO_ROOT_DIR/modules); do
        info "Looking at module $module_name"
        if [[ -f "$REPO_ROOT_DIR/modules/$module_name/set-version.sh" ]]; then
            info "Module $module_name contains a set-version.sh script. Calling it."
            ${REPO_ROOT_DIR}/modules/${module_name}/set-version.sh --version $component_version
            check_for_error "Failed to set the version for module $module_name"
        fi
    done
    success "OK - All modules have had their versions set to $component_version"
}

function set_version_in_docs() {
    h2 "Setting the version number in the docs to $component_version"

    ${REPO_ROOT_DIR}/docs/set-version.sh --version $component_version
    check_for_error "Failed to set the version for module $module_name"

    success "OK - docs version has been set to $component_version"
}

function set_version_in_readme() {
    h2 "Setting the version number in the README.md"
    mkdir -p ${REPO_ROOT_DIR}/temp
    cat ${REPO_ROOT_DIR}/README.md | sed "s/\`set-version.sh --version .*\`/\`set-version.sh --version ${component_version}\`/g" > ${REPO_ROOT_DIR}/temp/readme.md
    cp ${REPO_ROOT_DIR}/temp/readme.md ${REPO_ROOT_DIR}/README.md
    success "set version in README.md OK"
}

function set_version_in_build_properties() {
    h2 "Setting the version number in the build.properties"
    mkdir -p ${REPO_ROOT_DIR}/temp
    cat ${REPO_ROOT_DIR}/build.properties | sed "s/^GALASA_VERSION=.*$/GALASA_VERSION=${component_version}/g" > ${REPO_ROOT_DIR}/temp/build.properties
    cp ${REPO_ROOT_DIR}/temp/build.properties ${REPO_ROOT_DIR}/build.properties
    success "set version in build.properties file OK"
}

h1 "Setting version of this repository to $component_version"
set_version_in_all_modules
check_for_error "Failed to set version in all modules"

set_version_in_docs

set_version_in_readme
set_version_in_build_properties

success "OK"

