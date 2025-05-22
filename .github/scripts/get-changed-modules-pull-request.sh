#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#


#-----------------------------------------------------------------------------------------                   
#
# Objectives: Get all modules that have been changed in a Pull Request.
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
    info "Syntax: get-changed-modules-pull-request.sh [OPTIONS]"
    cat << EOF
Options are:
-h | --help : Display this help text
--github-repo The GitHub repository the Pull Request was opened on
--pr-number The number of the Pull Request
--test : (no parameters) Used when the caller is testing the script.
--test-input-file <file> : Specifies an input file to draw data from instead of github. Only used in test mode.
--test-output-file <file> : Specifies an output file to write results to for checking. Only used in test mode.
EOF
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------
github_repo=""
pr_number=""
is_test="false"
test_input_file=""
test_output_file=""
while [ "$1" != "" ]; do
    case $1 in
        -h | --help )       usage
                            exit
                            ;;

        --github-repo )     github_repo="$2"
                            shift
                            ;;

        --pr-number )       pr_number="$2"
                            shift
                            ;;

        --test )            is_test="true"
                            ;;

        --test-input-file ) test_input_file="$2"
                            shift
                            ;;

        --test-output-file ) test_output_file="$2"
                            shift
                            ;;

        * )                 error "Unexpected argument $1"
                            usage
                            exit 1
    esac
    shift
done


#-----------------------------------------------------------------------------------------                   
# Set up some controlling data structures with data.
#-----------------------------------------------------------------------------------------                   
declare -A someAssociativeMap
rc=$?
if [[ "$rc" != "0" ]]; then 
    error "The version of bash you are using does not support associative arrays. This could mean your bash is down-level, or you are running on a mac."
    exit 1
fi

# An associative array of modules, to the path prefix the file uses.
declare -A moduleToPrefix
moduleToPrefix["buildutils"]="modules/buildutils"
moduleToPrefix["platform"]="modules/platform"
moduleToPrefix["wrapping"]="modules/wrapping"
moduleToPrefix["gradle"]="modules/gradle"
moduleToPrefix["maven"]="modules/maven"
moduleToPrefix["framework"]="modules/framework"
moduleToPrefix["extensions"]="modules/extensions"
moduleToPrefix["managers"]="modules/managers"
moduleToPrefix["obr"]="modules/obr"
moduleToPrefix["ivts"]="modules/ivts"
moduleToPrefix["cli"]="modules/cli"
moduleToPrefix["docs"]="docs/"
moduleToPrefix["workflows"]=".github/"
moduleToPrefix["restApi"]="modules/framework/galasa-parent/dev.galasa.framework.api.openapi/src/main/resources/openapi.yaml"

# An associative array of modules to the comma-separated list of flags they should set to true if the module has changed.
declare -A moduleToFlagList
moduleToFlagList["buildutils"]="BUILDUTILS_CHANGED"
moduleToFlagList["platform"]="PLATFORM_CHANGED,WRAPPING_CHANGED,GRADLE_CHANGED,MAVEN_CHANGED,FRAMEWORK_CHANGED,EXTENSIONS_CHANGED,MANAGERS_CHANGED,OBR_CHANGED"
moduleToFlagList["wrapping"]="WRAPPING_CHANGED"
moduleToFlagList["gradle"]="GRADLE_CHANGED"
moduleToFlagList["maven"]="MAVEN_CHANGED"
moduleToFlagList["framework"]="FRAMEWORK_CHANGED"
moduleToFlagList["extensions"]="EXTENSIONS_CHANGED"
moduleToFlagList["managers"]="MANAGERS_CHANGED"
moduleToFlagList["obr"]="OBR_CHANGED"
moduleToFlagList["ivts"]="IVTS_CHANGED"
moduleToFlagList["cli"]="CLI_CHANGED,DOCS_CHANGED"
moduleToFlagList["docs"]="DOCS_CHANGED"
moduleToFlagList["workflows"]="DOCS_CHANGED"
moduleToFlagList["restApi"]="FRAMEWORK_CHANGED,CLI_CHANGED,DOCS_CHANGED"

# An associative array of flags we want to output a value for
declare -A flagToValue
flagToValue["BUILDUTILS_CHANGED"]="false"
flagToValue["PLATFORM_CHANGED"]="false"
flagToValue["WRAPPING_CHANGED"]="false"
flagToValue["GRADLE_CHANGED"]="false"
flagToValue["MAVEN_CHANGED"]="false"
flagToValue["FRAMEWORK_CHANGED"]="false"
flagToValue["EXTENSIONS_CHANGED"]="false"
flagToValue["MANAGERS_CHANGED"]="false"
flagToValue["OBR_CHANGED"]="false"
flagToValue["IVTS_CHANGED"]="false"
flagToValue["CLI_CHANGED"]="false"
flagToValue["DOCS_CHANGED"]="false"

changed_files_in_pr=()


#-----------------------------------------------------------------------------------------                   
# Functions
#-----------------------------------------------------------------------------------------  

function get_paths_changed_in_pr() {
    h1 "Getting the file paths changed in Pull Request number ${pr_number} for GitHub repo ${github_repo}" 

    # Extract changed module names from changed files from GitHub CLI output
    mapfile -t changed_files_in_pr < <(gh pr diff --repo ${github_repo} ${pr_number} --name-only)

    export changed_files_in_pr
}

function module_changed() {
    module=$1
    info "Marking that module $module changed."
    
    flagList=${moduleToFlagList[$module]}
    for flagName in ${flagList//,/ }; do
        info "    Setting flag $flagName to be true"
        flagToValue["$flagName"]="true"
    done
}

function process_paths() {
    h1 "Processing list of changed files"

    for changed_file in "${changed_files_in_pr[@]}"; do
        info "Processing $changed_file"

        for module in "${!moduleToPrefix[@]}"
        do
            
            pathPrefix=${moduleToPrefix[$module]}

            # info "Checking against module $module 's path prefix of $pathPrefix"
            if [[ "$changed_file" == $pathPrefix* ]]; then 
                module_changed "$module"
            fi
        done
    done
}

function output_flags() {
    h1 "Writing out flag values to the workflow..."   
    target=$1   

    for flag in "${!flagToValue[@]}"
    do
        flagValue=${flagToValue["$flag"]}
        info "Flag: $flag = $flagValue"
        echo "$flag=$flagValue" >> $target
    done
}

function get_paths_from_test_input_file() {
    input_file=$1
    h2 "Reading test input data from $input_file"
    while read line; do 
        echo "Test input line: $line"
        changed_files_in_pr+=("$line")
    done < $input_file
}

if [[ "$is_test" == "true" ]]; then 
    # The caller is trying to test the script.
    get_paths_from_test_input_file $test_input_file
    output_target=$test_output_file

else
    output_target="$GITHUB_OUTPUT"
    get_paths_changed_in_pr
fi

process_paths
output_flags $output_target
