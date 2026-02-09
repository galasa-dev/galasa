#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------------                   
#
# Objectives: Build this repository code locally.
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
    info "Syntax: build-locally.sh [OPTIONS]"
    cat << EOF
Options are:
-h | --help : Display this help text
--module <name> : The name of the module to start building from. Defaults to the first module in the build chain.
--chain true/false/yes/no/y/n : Enables/disables the chaining of builds. Defaults to true.
--docker : Enables the building of Docker images. If --docker is not provided, Docker images will not be built.
--minikube : Primes a Docker registry for minikube to pull images from and pushes built Docker images to it.
EOF
}

function check_chain_is_true_or_false() {
    chain_input=$1

    if [[ "$chain_input" == "yes" ]] || [[ "$chain_input" == "y" ]] || [[ "$chain_input" == "true" ]]; then 
        chain="true"
    else
        if [[ "$chain_input" == "no" ]] || [[ "$chain_input" == "n" ]] || [[ "$chain_input" == "false" ]]; then 
            chain="false"
        else 
            error "--chain parameter value '$chain_input' should be yes,y,true,no,n or false."
            exit 1
        fi
    fi
    success "--chain option is value. $chain"
}

function get_module_names() {
    # Dynamically discover modules from the modules/ directory
    local modules=()
    
    # Get all directories in modules/ folder
    if [[ -d "${PROJECT_DIR}/modules" ]]; then
        for dir in "${PROJECT_DIR}/modules"/*/ ; do
            if [[ -d "$dir" ]]; then
                module_name=$(basename "$dir")
                modules+=("$module_name")
            fi
        done
    fi
    
    # Add docs if it exists at the root level
    if [[ -d "${PROJECT_DIR}/docs" ]]; then
        modules+=("docs")
    fi
    
    echo "${modules[@]}"
}

function check_module_name_is_supported() {
    module_input=$1
    
    # Get the list of valid module names dynamically
    module_names=($(get_module_names))
    
    is_valid="false"
    # Loop through all the keys of our command
    if [[ " ${module_names[*]} " =~ " ${module_input} " ]]; then
        is_valid="true"
    fi

    if [[ "$is_valid" != "true" ]]; then
        msg="'$module_input' is an invalid module name. Valid module names are: [ ${module_names[*]} ]"
        error "$msg"
        exit 1
    fi

    success "--module '$module_input' is a valid module name."
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------
module_input="platform"
chain_input="true"
build_docker_flag=""
is_setup_minikube_requested=""
while [ "$1" != "" ]; do
    case $1 in
        -h | --help )   usage
                        exit
                        ;;

        --module )      module_input="$2"
                        shift
                        ;;

        --chain )       chain_input="$2"
                        shift
                        ;;

        --docker )      build_docker_flag="--docker"
                        ;;

        --minikube )    is_setup_minikube_requested="true"
                        build_docker_flag="--docker"
                        ;;

        * )             error "Unexpected argument $1"
                        usage
                        exit 1
    esac
    shift
done

check_chain_is_true_or_false $chain_input
# This gives us $chain holding "true" or "false"

check_module_name_is_supported $module_input


      
#-----------------------------------------------------------------------------------------                   
# Functions
#-----------------------------------------------------------------------------------------  

function clean_local_m2() {
    h2 "Cleaning up maven .m2 results"
    rm -fr ~/.m2/repository/dev/galasa/galasa*
    rm -fr ~/.m2/repository/dev/galasa/dev-galasa*
}

function run_minikube_setup() {
    h1 "Running minikube docker registry setup script..."

    ${BASEDIR}/setup-minikube-docker-registry.sh
    rc=$? ;  if [[ "${rc}" != "0" ]]; then error "Failed to set up minikube docker registry. rc=$rc" ; exit 1 ; fi

    success "Docker registry for minikube set up OK"
}

function get_next_module() {
    local current=$1
    case "$current" in
        platform   ) echo "buildutils" ;;
        buildutils ) echo "wrapping"   ;;
        wrapping   ) echo "gradle"     ;;
        gradle     ) echo "maven"      ;;
        maven      ) echo "framework"  ;;
        framework  ) echo "extensions" ;;
        extensions ) echo "managers"   ;;
        managers   ) echo "obr"        ;;
        obr        ) echo "ivts"       ;;
        ivts       ) echo "cli"        ;;
        cli        ) echo "docs"       ;;
        *          ) echo ""           ;;
    esac
}

function get_build_args() {
    local module=$1
    case "$module" in
        framework | extensions | managers | cli)
            echo "--clean --detectsecrets false"
            ;;
        obr)
            echo "--detectsecrets false ${build_docker_flag}"
            ;;
        docs)
            echo "${build_docker_flag}"
            ;;
        *)
            echo "--detectsecrets false"
            ;;
    esac
}

function get_module_path() {
    local module=$1
    if [[ "$module" == "docs" ]]; then
        echo "${PROJECT_DIR}/$module"
    else
        echo "${PROJECT_DIR}/modules/$module"
    fi
}

function build_single_module() {
    local module=$1
    local module_path=$(get_module_path "$module")
    local build_args=$(get_build_args "$module")
    
    h2 "Building $module"
    cd "$module_path"
    ${module_path}/build-locally.sh ${build_args}
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to build module $module. rc=$rc"
        exit 1
    fi
    success "Built module $module OK"
}

function build_module() {
    local module=$1
    local chain=$2
    h1 "Building... module:'$module' chain:'$chain'"

    while [[ -n "$module" ]]; do
        build_single_module "$module"
        
        if [[ "$chain" == "true" ]]; then
            module=$(get_next_module "$module")
        else
            module=""
        fi
    done
}

clean_local_m2
build_module $module_input $chain

if [[ "${is_setup_minikube_requested}" == "true" ]]; then
    run_minikube_setup
fi

${BASEDIR}/detect-secrets.sh