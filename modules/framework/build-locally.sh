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
# Environment variable over-rides:
# LOGS_DIR - Optional. Where logs are placed. Defaults to creating a temporary directory.
# SOURCE_MAVEN - Optional. Where a maven repository is from which the build will draw artifacts.
# DEBUG - Optional. Defaults to 0 (off)
# SWAGGER_CODEGEN_CLI_JAR - Optional. Where the swagger-codegen-cli.jar file is located.
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

OPENAPI2BEANS="${BASEDIR}/galasa-parent/build/openapi2beans"

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
h3() { printf "\n${underline}${bold}${green}%s${reset}\n" "$@" ;}
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
-c | --clean : Do a clean build. One of the --clean or --delta flags are mandatory.
-d | --delta : Do a delta build. One of the --clean or --delta flags are mandatory.
-s | --detectsecrets true|false : Do we want to detect secrets in the entire repo codebase ? Default is 'true'. Valid values are 'true' or 'false'

Environment variables used:
DEBUG - Optional. Valid values "1" (on) or "0" (off). Defaults to "0" (off).
SOURCE_MAVEN - Optional. Where maven/gradle can look for pre-built development levels of things.
    Defaults to https://development.galasa.dev/main/maven-repo/obr/

EOF
}

#-------------------------------------------------------------
function check_exit_code () {
    # This function takes 2 parameters in the form:
    # $1 an integer value of the returned exit code
    # $2 an error message to display if $1 is not equal to 0
    if [[ "$1" != "0" ]]; then 
        error "$2" 
        exit 1  
    fi
}

#-------------------------------------------------------------
function download_dependencies {
    if [[ "${build_type}" == "clean" ]]; then
        h2 "Cleaning the dependencies out..."
        rm -rf build/dependencies
        success "OK"
    fi

    # Download the dependencies we define in gradle into a local folder
    h2 "Downloading dependencies"
    gradle \
    --warning-mode=all \
    -Dorg.gradle.java.home=${JAVA_HOME} \
    -PsourceMaven=${SOURCE_MAVEN} ${OPTIONAL_DEBUG_FLAG} \
    downloadDependencies \
    2>&1 >> ${log_file}

    rc=$? 
    check_exit_code $rc "Failed to run the gradle task to download our dependencies. rc=${rc}"
    success "OK"
}

#-------------------------------------------------------------
function generate_rest_docs {
    OPENAPI_YAML_FILE="${BASEDIR}/galasa-parent/dev.galasa.framework.api.openapi/src/main/resources/openapi.yaml"
    OUTPUT_DIR="${BASEDIR}/docs/generated/galasaapi"

    if [[ "${build_type}" == "clean" ]]; then
        h2 "Cleaning the generated documentation..."
        rm -rf ${OUTPUT_DIR}
        success "OK"
    fi

    h2 "Generate the REST API documentation..."

    # Pick up and use the swagger generator we just downloaded.
    # We don't know which version it is (dictated by the gradle build), but as there
    # is only one we can just pick the filename up..
    # Should end up being something like: ${BASEDIR}/galasa-parent/build/dependencies/swagger-codegen-cli-3.0.41.jar
    if [[ -z ${SWAGGER_CODEGEN_CLI_JAR} ]]; then
        export SWAGGER_CODEGEN_CLI_JAR=$(ls ${BASEDIR}/galasa-parent/build/dependencies/swagger-codegen-cli*)
        info "SWAGGER_CODEGEN_CLI_JAR environment variable is not set, setting to ${SWAGGER_CODEGEN_CLI_JAR}."
    fi

    if [[ ! -e ${SWAGGER_CODEGEN_CLI_JAR} ]]; then
        echo "The Swagger Generator cannot be found at ${SWAGGER_CODEGEN_CLI_JAR}."
        echo "Download it and set the SWAGGER_CODEGEN_CLI_JAR environment variable to point to it."
        exit 1
    fi

    java -jar ${SWAGGER_CODEGEN_CLI_JAR} generate \
    -i ${OPENAPI_YAML_FILE} \
    -l html2 \
    -o ${OUTPUT_DIR} \
    2>&1>> ${log_file}

    rc=$? 
    check_exit_code $rc "Failed to generate documentation from the openapi.yaml file. rc=${rc}"
    success "Generated REST API documentation at ${OUTPUT_DIR}/index.html"
}

#-------------------------------------------------------------
function build_code {
    h2 "Building..."

    goals="buildReleaseYaml build check jacocoTestReport"

    info "Building goals $goals"

    cmd="gradle --build-cache --parallel \
    ${CONSOLE_FLAG} \
    --warning-mode=all \
    -Dorg.gradle.java.home=${JAVA_HOME} \
    -PsourceMaven=${SOURCE_MAVEN} ${OPTIONAL_DEBUG_FLAG} \
    $goals \
    "

    info "Using this command: $cmd"
    $cmd 2>&1 >> ${log_file}
    rc=$? 
    check_exit_code $rc "Failed to build ${project} log is at ${log_file}"
    success "Built OK"
}

#-------------------------------------------------------------
function publish_to_maven {
    h2 "Publishing to local maven repo..."

    cmd="gradle --build-cache --parallel \
    ${CONSOLE_FLAG} \
    --warning-mode=all --debug  --info \
    -Dorg.gradle.java.home=${JAVA_HOME} \
    -PsourceMaven=${SOURCE_MAVEN} ${OPTIONAL_DEBUG_FLAG} \
    publishToMavenLocal"
    info "Command is $cmd"
    $cmd 2>&1 >> ${log_file}
    rc=$? 
    info "Log information is at ${log_file}"
    check_exit_code $rc "Failed to publish ${project} log is at ${log_file}"
    success "Published OK"
}

#-------------------------------------------------------------
function clean_up_local_maven_repo {
    h2 "Removing .m2 artifacts"
    rm -fr ~/.m2/repository/dev/galasa/dev.galasa.framework*
    rm -fr ~/.m2/repository/dev/galasa/dev.galasa
    rm -fr ~/.m2/repository/dev/galasa/galasa-boot
    rm -fr ~/.m2/repository/dev/galasa/galasa-testharness
    success "OK"
}

#-------------------------------------------------------------
function cleaning_up_before_we_start {
    if [[ "${build_type}" == "delta" ]]; then
        info "Skipping clean phase because this is a delta build."
    else
        h2 "Cleaning..."

        warn "Temporary fix: Remove the dex proto file so we don't use debris from last time."
        rm -fr ${WORKSPACE_DIR}/galasa-parent/dev.galasa.framework.auth.spi/src/main/proto/dex.proto

        gradle --no-daemon \
        ${CONSOLE_FLAG} \
        --warning-mode=all --debug \
        -Dorg.gradle.java.home=${JAVA_HOME} \
        -PsourceMaven=${SOURCE_MAVEN} ${OPTIONAL_DEBUG_FLAG} \
        clean \
        2>&1 >> ${log_file}
        rc=$? 
        check_exit_code $rc "Failed to clean ${project}"
        success "Cleaned OK"
    fi

    clean_up_local_maven_repo 
}

#-------------------------------------------------------------
function get_architecture() {
    h2 "Retrieving system architecture."
        raw_os=$(uname -s) # eg: "Darwin"
    os=""
    case $raw_os in
        Darwin*)
            os="darwin"
            ;;
        Linux*)
            os="linux"
            ;;
        *)
            error "Unsupported operating system is in use. $raw_os"
            exit 1
    esac

    architecture=$(uname -m)
    case $architecture in
        aarch64)
            architecture="arm64"
            ;;
        amd64)
            architecture="x86_64"
    esac
}

#-------------------------------------------------------------
function generate_beans {
    check_openapi2beans_is_installed

    h2 "Generating beans in the dev.framework.api.beans project."
    cmd="${OPENAPI2BEANS} generate --output ${BASEDIR}/galasa-parent/dev.galasa.framework.api.beans/src/main/java \
    --yaml $BASEDIR/galasa-parent/dev.galasa.framework.api.openapi/src/main/resources/openapi.yaml \
    --package dev.galasa.framework.api.beans.generated \
    --force"
    $cmd 
    rc=$? 
    check_exit_code $rc "Failed to generate the api beans"
    success "OK"
}

function check_openapi2beans_is_installed {
    h2 "Checking the openapi2beans tool is installed."
    
    if [[ ! -f ${OPENAPI2BEANS} ]]; then
        download_openapi2beans
    fi
    if [[ ! -x ${OPENAPI2BEANS} ]]; then
        chmod 700 ${OPENAPI2BEANS}
    fi

    success "OK - the openapi2beans tool is installed and available"
}

function download_openapi2beans {
    get_architecture

    h2 "Downloading openapi2beans tool"
    url=https://development.galasa.dev/main/binary/bld/openapi2beans-${os}-${architecture}
    
    curl --create-dirs -o ${OPENAPI2BEANS} ${url} 
    rc=$? 
    check_exit_code $rc "Failed to download the openapi2beans tool."

}

#-------------------------------------------------------------
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

#-------------------------------------------------------------
function update_release_yaml {
    h2 "Updating release.yaml"

    # After running 'gradle build', a release.yaml file should have been automatically generated
    generated_release_yaml="${BASEDIR}/galasa-parent/build/release.yaml"
    current_release_yaml="${BASEDIR}/release.yaml"

    if [[ -f ${generated_release_yaml} ]]; then
        cp ${generated_release_yaml} ${current_release_yaml}
        success "Updated release.yaml OK"
    else
        warn "Failed to automatically generate release.yaml, please ensure any changed bundles have had their versions updated in ${current_release_yaml}"
    fi
}

#-------------------------------------------------------------
function build_framework_uml_diagrams {

    make_sure_plantuml_tool_is_available

    h2 "Building plantuml diagrams for framework internal docs." 
    java -jar $BASEDIR/temp/plantuml.jar -tpng "${BASEDIR}/docs/design/**.plantuml"
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to generate UML diagrams" ; exit 1 ; fi

}

function make_sure_plantuml_tool_is_available {
    h2 "Making sure the plantuml tool is available"
    mkdir -p temp || exit 1
    if [[ -e $BASEDIR/temp/plantuml.jar ]]; then
        info "Plantuml jar is already downloaded. No need to download it again"
    else 
        info "Downloading the plantuml tool..."
        url=https://github.com/plantuml/plantuml/releases/download/v1.2024.3/plantuml-epl-1.2024.3.jar
        cd "$BASEDIR/temp" || exit 1
        curl -O $url
        rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to download the plantuml tool jar." ; exit 1 ; fi
        mv plantuml-*.jar plantuml.jar
        cd - || exit 1
    fi
    success "OK"
}

#-----------------------------------------------------------------------------------------
# Process parameters
#-----------------------------------------------------------------------------------------
build_type=""
detectsecrets="true"
while [ "$1" != "" ]; do
    case $1 in
        -c | --clean )          build_type="clean"
                                ;;
        -d | --delta )          build_type="delta"
                                ;;
        -s | --detectsecrets )  detectsecrets="$2"
                                shift
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

if [[ "${build_type}" == "" ]]; then
    error "Need to use either the --clean or --delta parameter."
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

source_dir="galasa-parent"

project=$(basename ${BASEDIR})
h1 "Building ${project}"
info "Build type is --'${build_type}'"

# Debug or not debug ? Override using the DEBUG flag.
if [[ -z ${DEBUG} ]]; then
    export DEBUG=0
    # export DEBUG=1
    info "DEBUG defaulting to ${DEBUG}."
    info "Over-ride this variable if you wish. Valid values are 0 and 1."
else
    info "DEBUG set to ${DEBUG} by caller."
fi

# Over-rode SOURCE_MAVEN if you want to build from a different maven repo...
if [[ -z ${SOURCE_MAVEN} ]]; then
    export SOURCE_MAVEN=https://development.galasa.dev/main/maven-repo/obr/
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

# auto plain rich or verbose
CONSOLE_FLAG=--console=plain

log_file=${LOGS_DIR}/${project}.txt
info "Log will be placed at ${log_file}"


build_framework_uml_diagrams

cleaning_up_before_we_start

# Currently the bean generation stuff doesn't work 100%
# So I generated beans locally, fixed them up, and have started to use them.
generate_beans



build_code
publish_to_maven
update_release_yaml

if [[ -z ${SWAGGER_CODEGEN_CLI_JAR} ]]; then
    download_dependencies
fi

generate_rest_docs

if [[ "$detectsecrets" == "true" ]]; then
    $REPO_ROOT/tools/detect-secrets.sh 
    check_exit_code $? "Failed to detect secrets"
fi

success "Project ${project} built - OK - log is at ${log_file}"