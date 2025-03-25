#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------------
#
# Objectives: Sets up and populates a local Docker registry with Docker images used for
# Galasa development.
#
# Note: This script has only been tested on macOS.
#
#-----------------------------------------------------------------------------------------

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null

export ORIGINAL_DIR=$(pwd)

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
    info "Syntax: setup-minikube-docker-registry.sh [OPTIONS]"
    cat << EOF
Options are:
--build : Builds the Galasa Docker images as well as setting up a local Docker registry
-h | --help : Display this help text
EOF
}

#-----------------------------------------------------------------------------------------
# Process parameters
#-----------------------------------------------------------------------------------------
is_build_locally_requested=""
while [ "$1" != "" ]; do
    case $1 in
        --build )       is_build_locally_requested="true"
                        ;;
        -h | --help )   usage
                        exit
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
function clean_up_before_set_up {
    h2 "Cleaning up any existing Docker containers..."

    docker stop docker-to-minikube-socat | xargs docker rm

    success "Clean-up completed OK"
}

#-----------------------------------------------------------------------------------------
function loop_get_url_until_success {
    url=$1
    h2 "Looping to wait for things to start up"
    http_status=""
    while [[ "$http_status" != "200" ]]; do
        info "sleeping for 2 secs..."
        sleep 2
        info "querying $url to see if things are started yet..."
        http_status=$(curl -s -o /dev/null -w "%{http_code}" $url)
        info "returned http status code was $http_status"
    done
    success "Things are started"
}

#-----------------------------------------------------------------------------------------
function check_exit_code {
    # This function takes 2 parameters in the form:
    # $1 an integer value of the returned exit code
    # $2 an error message to display if $1 is not equal to 0
    if [[ "$1" != "0" ]]; then
        error "$2"
        exit 1
    fi
}

#-----------------------------------------------------------------------------------------
function check_tool_is_installed {
    # This function expects the name of the tool to be passed in
    tool_name=$1

    which ${tool_name}
    rc=$?
    check_exit_code ${rc} "${tool_name} is not available on your path. Install ${tool_name} and try again."
    success "${tool_name} is installed. OK"
}

#-----------------------------------------------------------------------------------------
function check_docker_installed {
    check_tool_is_installed "docker"
    docker version > /dev/null 2>&1
    rc=$?
    check_exit_code ${rc} "The docker daemon is not running. Start it and try again."
}

#-----------------------------------------------------------------------------------------
function check_minikube_installed {
    check_tool_is_installed "minikube"
}

#-----------------------------------------------------------------------------------------
function build_galasa_modules_and_images {
    h2 "Building Galasa modules and Docker images used for the Galasa service..."

    info "Building Galasa modules for the galasa-boot-embedded Docker image..."
    ${BASEDIR}/build-locally.sh --module wrapping --docker

    rc=$?
    check_exit_code ${rc} "Failed to build the Galasa modules for the galasa-boot-embedded Docker image"

    # This separate webui build step will not be needed if the webui project is moved into the monorepo...
    build_galasa_webui_image

    success "Galasa service Docker images built OK"
}

#-----------------------------------------------------------------------------------------
function build_galasa_webui_image {
    cd "${PROJECT_DIR}/.."
    expected_webui_dir=$(pwd)/webui

    info "Building the Galasa web UI and webui Docker image, assuming the 'webui' project is cloned to '${expected_webui_dir}'..."
    ${expected_webui_dir}/build-locally.sh --clean --docker

    rc=$?
    check_exit_code ${rc} "Failed to build the Galasa web UI and webui Docker image. Check that the webui project is cloned to ${expected_webui_dir} and try again"
}

#-----------------------------------------------------------------------------------------
function push_galasa_images_to_local_registry {
    h2 "Pushing built images to local registry..."

    push_image_to_local_registry "galasa-boot-embedded:latest"
    push_image_to_local_registry "webui:latest"

    success "Images pushed OK"
}

#-----------------------------------------------------------------------------------------
function push_image_to_local_registry {
    # This function expects the name of the image to push
    image_name=$1
    retagged_image_name="localhost:5000/${image_name}"

    h2 "Retagging ${image_name} for local registry..."
    docker tag ${image_name} ${retagged_image_name}

    rc=$?
    check_exit_code ${rc} "Failed to retag the ${image_name} to ${retagged_image_name}. Check that image ${image_name} has been built and try again."
    success "Image tagged OK"

    h2 "Pushing image to local Docker registry..."
    docker push ${retagged_image_name}

    rc=$?
    check_exit_code ${rc} "Failed to push the ${retagged_image_name} image to the local Docker registry"
    success "Image ${retagged_image_name} pushed to local Docker registry OK"
}

#-----------------------------------------------------------------------------------------
function setup_local_registry_for_minikube {
    h2 "Configuring minikube to be able to pull images from a local Docker registry..."
    minikube_ip=$(minikube ip)
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        info "minikube is not running. Starting minikube now..."
        minikube start

        rc=$?
        check_exit_code ${rc} "Failed to start minikube cluster"
    fi

    # See https://minikube.sigs.k8s.io/docs/handbook/registry
    minikube addons enable registry

    # Check if the socat container is running
    socat_container_name="docker-to-minikube-socat"
    container_count=$(docker ps -a | grep ${socat_container_name} | wc -l | xargs)
    if [[ "${container_count}" == "1" ]]; then
        docker stop ${socat_container_name}
        docker start ${socat_container_name}
        rc=$?
        check_exit_code ${rc} "Failed to restart the existing docker-to-minikube registry mapping container"
    else
        docker run --name ${socat_container_name} -d --network=host alpine sh -c "apk add socat && socat TCP-LISTEN:5000,reuseaddr,fork TCP:$(minikube ip):5000"
        rc=$?
        check_exit_code ${rc} "Failed to start a container that exposes the local docker registry to minikube"
    fi

    info "Waiting for container to be ready for use..."
    loop_get_url_until_success "http://localhost:5000"

    success "minikube can now pull images from a local Docker registry OK"
}

#-----------------------------------------------------------------------------------------
# Main logic
#-----------------------------------------------------------------------------------------

check_docker_installed
check_minikube_installed

clean_up_before_set_up
setup_local_registry_for_minikube

if [[ "${is_build_locally_requested}" == "true" ]]; then
    build_galasa_modules_and_images
fi

push_galasa_images_to_local_registry

success "Local Docker registry has been successfully set up and populated with locally-built Galasa images!"
info "If you wish to install a local Galasa service on minikube using the Helm chart, set the 'galasaRegistry' Helm value to 'localhost:5000' so minikube can pull the images from the local registry."