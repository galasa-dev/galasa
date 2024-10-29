#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#


#-----------------------------------------------------------------------------------------                   
#
# Objectives: Get all modules that have been changed in a Push (Merge of a Pull Request).
# 
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
# cd "${BASEDIR}"

cd "${BASEDIR}/.."
PROJECT_DIR=$(pwd)

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
    info "Syntax: get-changed-modules-push.sh [OPTIONS]"
    cat << EOF
Options are:
-h | --help : Display this help text
--repo The repository for this GitHub Actions workflow
--base The base commit level (the commit before the push)
--head The head commit level (the commit that triggered the push)
EOF
}

module_names=(\
    "buildutils" \
    "wrapping" \
    "gradle" \
    "maven" \
    "framework" \
    "extensions" \
    "managers" \
    "obr" \
)

#-----------------------------------------------------------------------------------------
# Process parameters
#-----------------------------------------------------------------------------------------
repo=""
base=""
head=""
while [ "$1" != "" ]; do
    case $1 in
        -h | --help )   usage
                        exit
                        ;;

        --repo )        repo="$2"
                        shift
                        ;;

        --base )        base="$2"
                        shift
                        ;;

        --head )        head="$2"
                        shift
                        ;;

        * )             error "Unexpected argument $1"
                        usage
                        exit 1
    esac
    shift
done

#-----------------------------------------------------------------------------------------
# Functions
#-----------------------------------------------------------------------------------------

function get_files_changed_in_push() {

    h1 "Getting the files changed in Push event triggered by commit ${head}" 

    h2 "Files changed:"
    gh api repos/${repo}/compare/${base}...${head} --jq '.files[].filename'

    # Extract changed module names from changed files from GitHub CLI output
    mapfile -t changed_files_in_push < <(gh api repos/${repo}/compare/${base}...${head} --jq '.files[].filename')

    modules_changed_in_push=()
    for changed_file in "${changed_files_in_push[@]}"; do
        echo "$changed_file"
        module=$(echo "$changed_file" | cut -d'/' -f2)
        modules_changed_in_push+=("$module")
    done

    # Remove possible duplicates from array of changed modules
    declare -A unique_module_map

    unique_modules_found_in_push=()
    for module in "${modules_changed_in_push[@]}"; do
    if [[ -z "${unique_module_map[$module]}" ]]; then
        unique_modules_found_in_push+=("$module")
        unique_module_map[$module]=1
    fi
    done

    h2 "Modules changed:"
    echo "${unique_modules_found_in_push[@]}"

}

function get_changed_modules_and_set_in_environment() {

    h1 "Finding changed modules and setting environment variables that can be used in the GitHub Actions workflows..."

    for module in "${unique_modules_found_in_push[@]}"; do
        if [[ "$module" == "buildutils" ]]; then
            echo "BUILDUTILS_CHANGED=true" >> $GITHUB_OUTPUT
            continue
        fi
        if [[ "$module" == "wrapping" ]]; then
            echo "WRAPPING_CHANGED=true" >> $GITHUB_OUTPUT
            continue
        fi
        if [[ "$module" == "gradle" ]]; then
            echo "GRADLE_CHANGED=true" >> $GITHUB_OUTPUT
            continue
        fi
        if [[ "$module" == "maven" ]]; then
            echo "MAVEN_CHANGED=true" >> $GITHUB_OUTPUT
            continue
        fi
        if [[ "$module" == "framework" ]]; then
            echo "FRAMEWORK_CHANGED=true" >> $GITHUB_OUTPUT
            continue
        fi
        if [[ "$module" == "extensions" ]]; then
            echo "EXTENSIONS_CHANGED=true" >> $GITHUB_OUTPUT
            continue
        fi
        if [[ "$module" == "managers" ]]; then
            echo "MANAGERS_CHANGED=true" >> $GITHUB_OUTPUT
            continue
        fi
        if [[ "$module" == "obr" ]]; then
            echo "OBR_CHANGED=true" >> $GITHUB_OUTPUT
            continue
        fi
    done
}

# Set outputs to false as default value.
echo "BUILDUTILS_CHANGED=false" >> $GITHUB_OUTPUT
echo "WRAPPING_CHANGED=false" >> $GITHUB_OUTPUT
echo "GRADLE_CHANGED=false" >> $GITHUB_OUTPUT
echo "MAVEN_CHANGED=false" >> $GITHUB_OUTPUT
echo "FRAMEWORK_CHANGED=false" >> $GITHUB_OUTPUT
echo "EXTENSIONS_CHANGED=false" >> $GITHUB_OUTPUT
echo "MANAGERS_CHANGED=false" >> $GITHUB_OUTPUT
echo "OBR_CHANGED=false" >> $GITHUB_OUTPUT

get_files_changed_in_push
get_changed_modules_and_set_in_environment