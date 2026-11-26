#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#


#-----------------------------------------------------------------------------------------                   
#
# Objectives: Get all modules that have been changed in a Pull Request, plus every module
# that depends on them, directly or transitively.
#
# The module dependency graph below (moduleDownstream) must be kept in sync with the 'needs:'
# ordering in .github/workflows/pull-requests.yaml. If a build job is added, removed or
# re-ordered there, update the graph here, and the test cases in
# get-changed-modules-pull-request-test.sh.
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

# An associative array of modules to the space-separated list of modules that directly depend on them.
# When a module changes, it and every module reachable from it through this graph are flagged as changed.
declare -A moduleDownstream
moduleDownstream["platform"]="wrapping gradle"
moduleDownstream["gradle"]="maven"
moduleDownstream["buildutils"]="framework"
moduleDownstream["wrapping"]="framework"
moduleDownstream["maven"]="framework"
moduleDownstream["framework"]="extensions managers docs"
moduleDownstream["extensions"]="obr"
moduleDownstream["managers"]="obr"
moduleDownstream["obr"]="ivts cli"
moduleDownstream["cli"]="docs"
moduleDownstream["ivts"]=""
moduleDownstream["docs"]=""

# The modules which have already been flagged as changed, so each is only walked once.
declare -A visitedModules

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

# Marks a module as changed, along with every module that depends on it, directly or transitively.
function module_changed() {
    local module=$1

    if [[ -n "${visitedModules[$module]}" ]]; then
        return
    fi
    visitedModules["$module"]="true"

    local flagName="${module^^}_CHANGED"
    info "Marking that module $module changed. Setting flag $flagName to be true"
    flagToValue["$flagName"]="true"

    local downstreamModule
    for downstreamModule in ${moduleDownstream[$module]}; do
        module_changed "$downstreamModule"
    done
}

# Marks every module as changed.
function all_modules_changed() {
    local module
    for module in "${!moduleDownstream[@]}"; do
        module_changed "$module"
    done
}

# Works out which modules a changed file under .github/ affects.
# Files are checked in order: files which need no build, then workflow files
# belonging to one module, then CI files shared by every module.
# Anything else under .github/ does not affect any module.
function process_github_path() {
    local changed_file=$1

    case "$changed_file" in
        .github/ISSUE_TEMPLATE/* | .github/SECURITY.md | .github/dependabot.yml | .github/pull_request_template.md )
            info "$changed_file does not need any module to be built"
            return
            ;;
    esac

    local workflow_name
    workflow_name=$(basename "$changed_file" .yaml)
    workflow_name=${workflow_name#pr-}
    if [[ "$changed_file" == .github/workflows/*.yaml && -n "${moduleToPrefix[$workflow_name]}" ]]; then
        info "$changed_file is the workflow for module $workflow_name"
        module_changed "$workflow_name"
        return
    fi

    case "$changed_file" in
        .github/workflows/pull-requests.yaml | \
        .github/workflows/pushes.yaml | \
        .github/workflows/codeql-*.yml | \
        .github/workflows/test-creds-store.yaml | \
        .github/workflows/snyk-security-scan.yaml | \
        .github/workflows/check-*.yaml | \
        .github/workflows/releases.yaml | \
        .github/workflows/release-cli.yaml | \
        .github/scripts/* | \
        .github/common-files/* )
            info "$changed_file is shared by every module"
            all_modules_changed
            ;;
    esac
}

function process_paths() {
    h1 "Processing list of changed files"

    for changed_file in "${changed_files_in_pr[@]}"; do
        info "Processing $changed_file"

        if [[ "$changed_file" == .github/* ]]; then
            process_github_path "$changed_file"
            continue
        fi

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
