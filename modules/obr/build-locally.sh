#! /usr/bin/env bash

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
WORKSPACE_DIR=$(pwd)

cd "${BASEDIR}/../.."
REPO_ROOT=$(pwd)

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
    info "Syntax: build-locally.sh [OPTIONS]"
    cat << EOF
Options are:
--docker : Optional. Builds the Docker image for the OBR boot embedded build
-s | --detectsecrets true|false : Do we want to detect secrets in the entire repo codebase ? Default is 'true'. Valid values are 'true' or 'false'
-h | --help : Display this help text

Environment Variables:
SOURCE_MAVEN :
    Used to indicate where parts of the OBR can be obtained.
    Optional. Could be set to something like: https://development.galasa.dev/main/maven-repo/obr/
    Defaults to file://~/.m2/repository

LOGS_DIR :
    Controls where logs are placed.
    Optional. Defaults to creating a new temporary folder

GPG_PASSPHRASE :
    Mandatory.
    Controls how the obr is signed. Needs to be the alias of the private key of a
    public-private gpg pair. eg: For development you could use your signing github
    passphrase.

EOF
}

function check_exit_code () {
    # This function takes 3 parameters in the form:
    # $1 an integer value of the returned exit code
    # $2 an error message to display if $1 is not equal to 0
    if [[ "$1" != "0" ]]; then 
        error "$2" 
        exit 1  
    fi
}

#-----------------------------------------------------------------------------------------
# Process parameters
#-----------------------------------------------------------------------------------------
is_docker_build_requested=""
detectsecrets="true"
while [ "$1" != "" ]; do
    case $1 in
        --docker )              is_docker_build_requested="true"
                                ;;
        -h | --help )           usage
                                exit
                                ;;
        -s | --detectsecrets )  detectsecrets="$2"
                                shift
                                ;;
        * )                     error "Unexpected argument $1"
                                usage
                                exit 1
    esac
    shift
done

if [[ -z $GPG_PASSPHRASE ]]; then
    error "Environment variable GPG_PASSPHRASE needs to be set."
    usage
    exit 1
fi

if [[ "${detectsecrets}" != "true" ]] && [[ "${detectsecrets}" != "false" ]]; then
    error "--detectsecrets flag must be 'true' or 'false'. Was $detectesecrets"
    exit 1
fi

#-----------------------------------------------------------------------------------------
# Main logic.
#-----------------------------------------------------------------------------------------
source_dir="."

project=$(basename ${BASEDIR})
h1 "Building ${project}"


# Over-rode SOURCE_MAVEN if you want to build from a different maven repo...
if [[ -z ${SOURCE_MAVEN} ]]; then
    cd ~/.m2/repository
    local_maven_repo_folder=$(pwd)
    cd - 
    export SOURCE_MAVEN="file://$local_maven_repo_folder"
    # export SOURCE_MAVEN=https://development.galasa.dev/main/maven-repo/obr/
    info "SOURCE_MAVEN repo defaulting to ${SOURCE_MAVEN}."
    info "Set this environment variable if you want to over-ride this value."
else
    info "SOURCE_MAVEN set to ${SOURCE_MAVEN} by caller."
fi

# Create a temporary dir.
# Note: This bash 'spell' works in OSX and Linux.
if [[ -z ${LOGS_DIR} ]]; then
    export LOGS_DIR=$(mktemp -d 2>/dev/null || mktemp -d -t "galasa-logs")
    info "Logs are stored in the ${LOGS_DIR} folder."
    info "Over-ride this setting using the LOGS_DIR environment variable."
else
    mkdir -p ${LOGS_DIR} 2>&1 > /dev/null # Don't show output. We don't care if it already existed.
    info "Logs are stored in the ${LOGS_DIR} folder."
    info "Over-ridden by caller using the LOGS_DIR variable."
fi

info "Using source code at ${source_dir}"
cd ${BASEDIR}/${source_dir}
if [[ "${DEBUG}" == "1" ]]; then
    OPTIONAL_DEBUG_FLAG="-debug"
else
    OPTIONAL_DEBUG_FLAG="-info"
fi


log_file=${LOGS_DIR}/${project}.txt
info "Log will be placed at ${log_file}"
date > ${log_file}

#------------------------------------------------------------------------------------
function check_docker_installed {
    which docker
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "The docker CLI tool is not available on your path. Install docker and try again."
        exit 1
    fi
    success "docker is installed. OK"
}

#------------------------------------------------------------------------------------
function get_galasabld_binary_location {
    # What's the architecture-variable name of the build tool we want for this local build ?
    export ARCHITECTURE=$(uname -m) # arm64 or amd64
    if [ $ARCHITECTURE == "x86_64" ]; then
        export ARCHITECTURE="amd64"
    fi

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
    export GALASA_BUILD_TOOL_NAME=galasabld-${os}-${ARCHITECTURE}

    # Favour the galasabld tool if it's on the path, else use a locally-built version or fail if not available.
    GALASABLD_ON_PATH=$(which galasabld)
    rc=$?
    if [[ "${rc}" == "0" ]]; then
        info "Using the 'galasabld' tool which is on the PATH"
        GALASA_BUILD_TOOL_PATH=${GALASABLD_ON_PATH}
    else
        GALASABLD_ON_PATH=$(which $GALASA_BUILD_TOOL_NAME)
        rc=$?
        if [[ "${rc}" == "0" ]]; then
            info "Using the '$GALASA_BUILD_TOOL_NAME' tool which is on the PATH"
            GALASA_BUILD_TOOL_PATH=${GALASABLD_ON_PATH}
        else
            info "The galasa build tool 'galasabld' or '$GALASA_BUILD_TOOL_NAME' is not on the path."
            export GALASA_BUILD_TOOL_PATH=${WORKSPACE_DIR}/buildutils/bin/${GALASA_BUILD_TOOL_NAME}
            if [[ ! -e ${GALASA_BUILD_TOOL_PATH} ]]; then
                error "Cannot find the $GALASA_BUILD_TOOL_NAME tools on locally built workspace."
                info "Try re-building the buildutils project"
                exit 1
            else
                info "Using the $GALASA_BUILD_TOOL_NAME tool at ${GALASA_BUILD_TOOL_PATH}"
            fi
        fi
    fi
}

#------------------------------------------------------------------------------------
function read_component_version {
    h2 "Getting the component version"
    export component_version=$(cat release.yaml | grep "version" | head -1 | cut -f2 -d':' | xargs)
    success "Component version is $component_version"
}

#------------------------------------------------------------------------------------
function download_dependencies {
    h2 "Downloading the dependencies to get release.yaml information"
    cd ${BASEDIR}/dependency-download

    gradle getDeps \
        -Dgalasa.source.repo=${SOURCE_MAVEN} \
        -Dgalasa.central.repo=https://repo.maven.apache.org/maven2/
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to download dependencies. rc=$rc"
        exit 1
    fi
    success "OK - dependencies downloaded."
}

#------------------------------------------------------------------------------------
function check_dependencies_present {
    h2 "Checking dependencies are present..."

    export framework_manifest_path=${BASEDIR}/dependency-download/build/dependencies/dev.galasa.framework.manifest.yaml
    export managers_manifest_path=${BASEDIR}/dependency-download/build/dependencies/dev.galasa.managers.manifest.yaml
    export extensions_manifest_path=${BASEDIR}/dependency-download/build/dependencies/dev.galasa.extensions.manifest.yaml
    # export framework_manifest_path=${WORKSPACE_DIR}/framework/release.yaml
    # export managers_manifest_path=${WORKSPACE_DIR}/managers/release.yaml

    declare -a required_files=(
    ${WORKSPACE_DIR}/${project}/dev.galasa.uber.obr/pom.template
    ${framework_manifest_path}
    ${extensions_manifest_path}
    ${managers_manifest_path}
    ${WORKSPACE_DIR}/obr/release.yaml
    )
    for required_file in "${required_files[@]}"
    do
        if [[ -e "${required_file}" ]]; then
            success "OK - File ${required_file} is present."
        else
            error "File ${required_file} is required, but missing. Clone the sibling project to make sure it exists."
            exit 1
        fi
    done
}


#------------------------------------------------------------------------------------
function construct_bom_pom_xml {
    h2 "Generating a bom pom.xml from a template, using all the versions of everything..."

    cd ${WORKSPACE_DIR}/${project}/galasa-bom


    # Check local build version
    export GALASA_BUILD_TOOL_PATH=${WORKSPACE_DIR}/buildutils/bin/${GALASA_BUILD_TOOL_NAME}
    info "Using galasabld tool ${GALASA_BUILD_TOOL_PATH}"

    cmd="${GALASA_BUILD_TOOL_PATH} template \
    --releaseMetadata ${framework_manifest_path} \
    --releaseMetadata ${extensions_manifest_path} \
    --releaseMetadata ${managers_manifest_path} \
    --releaseMetadata ${WORKSPACE_DIR}/obr/release.yaml \
    --template pom.template \
    --output pom.xml \
    --bom \
    "
    echo "Command is $cmd" >> ${log_file}
    $cmd 2>&1 >> ${log_file}

    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to convert release.yaml files into a pom.xml ${project}. log file is ${log_file}"
        exit 1
    fi
    success "pom.xml built ok - log is at ${log_file}"
}

#------------------------------------------------------------------------------------
function construct_uber_obr_pom_xml {
    h2 "Generating a pom.xml from a template, using all the versions of everything..."

    cd ${WORKSPACE_DIR}/${project}/dev.galasa.uber.obr

    # Check local build version
    export GALASA_BUILD_TOOL_PATH=${WORKSPACE_DIR}/buildutils/bin/${GALASA_BUILD_TOOL_NAME}
    info "Using galasabld tool ${GALASA_BUILD_TOOL_PATH}"

    cmd="${GALASA_BUILD_TOOL_PATH} template \
    --releaseMetadata ${framework_manifest_path} \
    --releaseMetadata ${extensions_manifest_path} \
    --releaseMetadata ${managers_manifest_path} \
    --releaseMetadata ${WORKSPACE_DIR}/obr/release.yaml \
    --template pom.template \
    --output pom.xml \
    --obr \
    "
    echo "Command is $cmd" >> ${log_file}
    $cmd 2>&1 >> ${log_file}

    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to convert release.yaml files into a pom.xml ${project}. log file is ${log_file}"
        exit 1
    fi
    success "pom.xml built ok - log is at ${log_file}"
}

#------------------------------------------------------------------------------------
function construct_obr_generic_pom_xml {
    h2 "Generating a pom.xml from the OBR generic template, using all the versions of everything..."

    cd ${WORKSPACE_DIR}/${project}/obr-generic

    # Check local build version
    export GALASA_BUILD_TOOL_PATH=${WORKSPACE_DIR}/buildutils/bin/${GALASA_BUILD_TOOL_NAME}
    info "Using galasabld tool ${GALASA_BUILD_TOOL_PATH}"

    cmd="${GALASA_BUILD_TOOL_PATH} template \
    --releaseMetadata ${framework_manifest_path} \
    --releaseMetadata ${extensions_manifest_path} \
    --releaseMetadata ${managers_manifest_path} \
    --releaseMetadata ${WORKSPACE_DIR}/obr/release.yaml \
    --template pom.template \
    --output pom.xml \
    --obr \
    "
    echo "Command is $cmd" >> ${log_file}
    $cmd 2>&1 >> ${log_file}

    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to convert release.yaml files into a pom.xml ${project}. log file is ${log_file}"
        exit 1
    fi
    success "pom.xml built ok - log is at ${log_file}"
}

#------------------------------------------------------------------------------------
function check_developer_attribution_present {
    h2 "Checking that pom has developer attribution."
    cat ${BASEDIR}/galasa-bom/pom.template | grep "<developers>" >> /dev/null
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "The pom.template must have developer attribution inside. \
        This is needed so that we can publish artifacts to maven central."
        exit 1
    fi
    success "OK. Pom template contains developer attribution, which maven central needs at the point we publish."
}


#------------------------------------------------------------------------------------
function build_generated_bom_pom {
    h2 "Building the generated pom.xml to package-up things into an OBR we can publish..."
    cd ${BASEDIR}/galasa-bom

    mvn \
    -Dgpg.passphrase=${GPG_PASSPHRASE} \
    -Dgalasa.source.repo=${SOURCE_MAVEN} \
    -Dgalasa.central.repo=https://repo.maven.apache.org/maven2/ install \
    2>&1 >> ${log_file}

    rc=$? ; if [[ "${rc}" != "0" ]]; then
        error "Failed to push built obr into maven repo ${project}. log file is ${log_file}"
        exit 1
    fi
    success "OK"
}

#------------------------------------------------------------------------------------
function build_generated_uber_obr_pom {
    h2 "Building the generated pom.xml to package-up things into an OBR we can publish..."
    cd ${BASEDIR}/dev.galasa.uber.obr

    mvn \
    -Dgpg.passphrase=${GPG_PASSPHRASE} \
    -Dgalasa.source.repo=${SOURCE_MAVEN} \
    -Dgalasa.central.repo=https://repo.maven.apache.org/maven2/ install \
    2>&1 >> ${log_file}

    rc=$? ; if [[ "${rc}" != "0" ]]; then
        error "Failed to push built obr into maven repo ${project}. log file is ${log_file}"
        exit 1
    fi
    success "OK"
}

#------------------------------------------------------------------------------------
function build_generated_obr_generic_pom {
    h2 "Building the generated OBR generic pom.xml..."
    cd ${BASEDIR}/obr-generic

    mvn install \
    -Dgpg.passphrase=${GPG_PASSPHRASE} \
    -Dgalasa.source.repo=${SOURCE_MAVEN} \
    -Dgalasa.central.repo=https://repo.maven.apache.org/maven2/ \
    dev.galasa:galasa-maven-plugin:$component_version:obrembedded \
    2>&1 >> ${log_file}

    rc=$?; if [[ "${rc}" != "0" ]]; then
        error "Failed to push OBR generic build into maven repo ${project}. log file is ${log_file}"
        exit 1
    fi
    success "OK"
}

#------------------------------------------------------------------------------------
function generate_javadoc_pom_xml {
    h2 "Generate a pom.xml we can use with the javadoc"
    #------------------------------------------------------------------------------------
    cd ${WORKSPACE_DIR}/obr/javadocs

    ${GALASA_BUILD_TOOL_PATH} template \
    --releaseMetadata ${framework_manifest_path} \
    --releaseMetadata ${extensions_manifest_path} \
    --releaseMetadata ${managers_manifest_path} \
    --releaseMetadata ${WORKSPACE_DIR}/obr/release.yaml \
    --template pom.template \
    --output pom.xml \
    --javadoc

    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to create the pom.xml for javadoc" ;  exit 1 ; fi
    success "OK - pom.xml file created at ${WORKSPACE_DIR}/obr/javadocs/pom.xml"
}

#------------------------------------------------------------------------------------
function build_javadoc_pom {
    h2 "Building the javadoc with maven"
    cd ${WORKSPACE_DIR}/obr/javadocs
    mvn clean install \
    --settings ${WORKSPACE_DIR}/obr/settings.xml \
    --batch-mode \
    --errors \
    --fail-at-end \
    -Dgpg.skip=true \
    -Dgalasa.source.repo=${SOURCE_MAVEN} \
    -Dgalasa.central.repo=https://repo.maven.apache.org/maven2/ \
    -Dmaven.javadoc.failOnError=true

    rc=$? ; if [[ "${rc}" != "0" ]]; then error "maven failed for javadoc build" ;  exit 1 ; fi

    success "OK - Build the galasa-uber-javadoc-*.zip file:"
    ls ${WORKSPACE_DIR}/obr/javadocs/target/*.zip
}

#------------------------------------------------------------------------------------
function check_secrets {
    h2 "updating secrets baseline"
    cd ${BASEDIR}
    detect-secrets scan --update .secrets.baseline
    rc=$? 
    check_exit_code $rc "Failed to run detect-secrets. Please check it is installed properly" 
    success "updated secrets file"

    h2 "running audit for secrets"
    detect-secrets audit .secrets.baseline
    rc=$? 
    check_exit_code $rc "Failed to audit detect-secrets."
    
    #Check all secrets have been audited
    secrets=$(grep -c hashed_secret .secrets.baseline)
    audits=$(grep -c is_secret .secrets.baseline)
    if [[ "$secrets" != "$audits" ]]; then 
        error "Not all secrets found have been audited"
        exit 1  
    fi
    success "secrets audit complete"

    h2 "Removing the timestamp from the secrets baseline file so it doesn't always cause a git change."
    mkdir -p temp
    rc=$? 
    check_exit_code $rc "Failed to create a temporary folder"
    cat .secrets.baseline | grep -v "generated_at" > temp/.secrets.baseline.temp
    rc=$? 
    check_exit_code $rc "Failed to create a temporary file with no timestamp inside"
    mv temp/.secrets.baseline.temp .secrets.baseline
    rc=$? 
    check_exit_code $rc "Failed to overwrite the secrets baseline with one containing no timestamp inside."
    success "secrets baseline timestamp content has been removed ok"
}

function check_secrets_unless_supressed() {
    # Check if the script is being called directly or from another script
    if [[ -z "${IN_CHAIN_MODE}" ]]; then
        info "Script invoked directly, running detect-secrets.sh script"

        # Run the detect-secrets.sh in root
        cd "${WORKSPACE_DIR}/.."
        TOOL_DIR=$(pwd)
        $TOOL_DIR/tools/detect-secrets.sh
    fi
}

#------------------------------------------------------------------------------------
function build_boot_embedded_docker_image {
    h2 "Building Galasa boot embedded Docker image..."
    JDK_IMAGE="ghcr.io/galasa-dev/openjdk:17"

    docker build -f "${BASEDIR}/dockerfiles/dockerfile.bootembedded" \
    -t galasa-boot-embedded:latest \
    --build-arg jdkImage="${JDK_IMAGE}" \
    ${BASEDIR}

    rc=$?
    check_exit_code ${rc} "Failed to build the OBR boot embedded Docker image."

    success "Boot embedded Docker image built OK"
}

# #------------------------------------------------------------------------------------
# h2 "Packaging the javadoc into a docker file"
# #------------------------------------------------------------------------------------
# cd ${WORKSPACE_DIR}/obr/javadocs
# docker --file ${WORKSPACE_DIR}/automation/dockerfiles/javadocs/javadocs-image-dockerfile .

# rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to create the docker image containing the javadoc" ;  exit 1 ; fi
# success "OK"

read_component_version
download_dependencies
get_galasabld_binary_location
check_dependencies_present
construct_uber_obr_pom_xml
construct_bom_pom_xml
construct_obr_generic_pom_xml
check_developer_attribution_present
build_generated_uber_obr_pom
build_generated_bom_pom
build_generated_obr_generic_pom

h1 "Building the javadoc using the OBR..."
generate_javadoc_pom_xml
build_javadoc_pom

if [[ "$detectsecrets" == "true" ]]; then
    $REPO_ROOT/tools/detect-secrets.sh 
    check_exit_code $? "Failed to detect secrets"
fi

if [[ "${is_docker_build_requested}" == "true" ]]; then
    check_docker_installed
    build_boot_embedded_docker_image
fi

success "Project ${project} built - OK - log is at ${log_file}"