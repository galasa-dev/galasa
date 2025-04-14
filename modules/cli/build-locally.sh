#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
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
# Functions
#-----------------------------------------------------------------------------------------
function usage {
    info "Syntax: build-locally.sh [OPTIONS]"
    cat << EOF
Options are:
-c | --clean : Do a clean build. One of the --clean or --delta flags are mandatory.
-d | --delta : Do a delta build. One of the --clean or --delta flags are mandatory.
EOF
}

#--------------------------------------------------------------------------
function read_boot_jar_version {
    export BOOT_JAR_VERSION=$(cat ${BASEDIR}/build.gradle | grep "galasaVersion[ ]*=" | cut -f2 -d"'" )
    info "Boot jar version is $BOOT_JAR_VERSION"
}


#----------------------------------------------------------------------------
function check_exit_code () {
    # This function takes 3 parameters in the form:
    # $1 an integer value of the expected exit code
    # $2 an error message to display if $1 is not equal to 0
    if [[ "$1" != "0" ]]; then 
        error "$2" 
        exit 1  
    fi
}


#--------------------------------------------------------------------------
# 
# Main script logic
#
#--------------------------------------------------------------------------

#-----------------------------------------------------------------------------------------
# Process parameters
#-----------------------------------------------------------------------------------------
build_type=""

while [ "$1" != "" ]; do
    case $1 in
        -c | --clean )          build_type="clean"
                                ;;
        -d | --delta )          build_type="delta"
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

#--------------------------------------------------------------------------
h1 "Building the CLI component"
#--------------------------------------------------------------------------

#--------------------------------------------------------------------------
h2 "Setting versions of things."
# Could get this bootjar from https://development.galasa.dev/main/maven-repo/obr/dev/galasa/galasa-boot/
read_boot_jar_version

#--------------------------------------------------------------------------
# Create a temporary folder which is never checked in.
function download_dependencies {
    h2 "Making sure the tools folder is present."

    info "Making sure the boot jar we embed is a fresh one from maven."
    rm -fr pkg/embedded/templates/galasahome/lib/*.jar

    mkdir -p build/dependencies
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to ensure the tools folder is present. rc=${rc}" ; exit 1 ; fi
    success "OK"

    #--------------------------------------------------------------------------
    # Download the dependencies we define in gradle into a local folder
    h2 "Downloading dependencies"
    gradle --warning-mode all --info --debug installJarsIntoTemplates
    rc=$? ; if [[ "${rc}" != "0" ]]; then  error "Failed to run the gradle build to get our dependencies. rc=${rc}" ; exit 1 ; fi
    success "OK"
}


#--------------------------------------------------------------------------
function go_mod_tidy {
    h2 "Tidying up go.mod..."

    if [[ "${build_type}" == "clean" ]]; then
        h2 "Tidying go mod file..."
        go mod tidy
        rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to tidy go mod. rc=${rc}" ; exit 1 ; fi
    fi

    success "OK"
}

#--------------------------------------------------------------------------
# Invoke the generator
function generate_rest_client {
    h2 "Generate the openapi client go code..."

    # Pick up and use the openapi generator we just downloaded.
    # We don't know which version it is (dictated by the gradle build), but as there
    # is only one we can just pick the filename up..
    # Should end up being something like: ${BASEDIR}/build/dependencies/openapi-generator-cli-6.2.0.jar
    export OPENAPI_GENERATOR_CLI_JAR=$(ls ${BASEDIR}/build/dependencies/openapi-generator-cli*)


    if [[ "${build_type}" == "clean" ]]; then
        h2 "Cleaning the generated code out..."
        rm -fr ${BASEDIR}/pkg/galasaapi/*
    fi

    mkdir -p build
    ./genapi.sh 2>&1 > build/generate-log.txt
    rc=$? ; if [[ "${rc}" != "0" ]]; then cat build/generate-log.txt ; error "Failed to generate the code from the yaml file. rc=${rc}" ; exit 1 ; fi
    rm -f build/generate-log.txt
    success "Code generation OK"

    #--------------------------------------------------------------------------
    # Invoke the generator again with different parameters
    h2 "Generate the openapi client go code... part II"
    ./generate.sh 2>&1 > build/generate-log.txt
    rc=$? ; if [[ "${rc}" != "0" ]]; then cat build/generate-log.txt ; error "Failed to generate II the code from the yaml file. rc=${rc}" ; exit 1 ; fi
    rm -f build/generate-log.txt
    success "Code generation part II - OK"

    #--------------------------------------------------------------------------
    # Invoke the generator again with different parameters
    h2 "Generate the openapi client go code... part III - fixing it up."
    ./fix-generated-code.sh 2>&1 > build/generate-fix-log.txt
    rc=$? ; if [[ "${rc}" != "0" ]]; then cat build/generate-fix-log.txt ; error "Failed to generate III the code. (Fixing it) rc=${rc}" ; exit 1 ; fi
    rm -f build/generate-fix-log.txt
    success "Code generation part III - OK"
}

#--------------------------------------------------------------------------
#
# Clean out old things if the clean option was specified.
#
#--------------------------------------------------------------------------
function clean {
    if [[ "${build_type}" == "clean" ]]; then
        h2 "Cleaning the binaries out..."
        make clean
        rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to build binary executable galasactl programs. rc=${rc}" ; exit 1 ; fi
        success "Binaries cleaned up - OK"
    fi
}

#--------------------------------------------------------------------------
#
# Build the executables
#
#--------------------------------------------------------------------------
function build_executables {
    h2 "Building new binaries..."
    set -o pipefail # Fail everything if anything in the pipeline fails. Else we are just checking the 'tee' return code.
    make all | tee ${BASEDIR}/build/compile-log.txt
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to build binary executable galasactl programs. rc=${rc}. See log at ${BASEDIR}/build/compile-log.txt" ; exit 1 ; fi
    success "New binaries built - OK"
}



#--------------------------------------------------------------------------
#
# Testing what was built...
#
#--------------------------------------------------------------------------

#--------------------------------------------------------------------------
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
    case $architecture in
        aarch64)
            architecture="arm64"
            ;;
    esac

    export galasactl_command="galasactl-${os}-${architecture}"
    info "galasactl command is ${galasactl_command}"
    success "OK"
}

#--------------------------------------------------------------------------
# Invoke the galasactl command to create a project.
function generate_sample_code {
    h2 "Invoke the tool to create a sample project."

    BUILD_SYSTEM_FLAGS=$*

    cd $BASEDIR/temp

    export PACKAGE_NAME="dev.galasa.example.banking"
    ${BASEDIR}/bin/${galasactl_command} project create --development --package ${PACKAGE_NAME} --features payee,account --obr ${BUILD_SYSTEM_FLAGS}
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error " Failed to create the galasa test project using galasactl command. rc=${rc}"
        exit 1
    fi
    success "OK"
}

#--------------------------------------------------------------------------
# Now build the source it created using maven
function build_generated_source_maven {
    h2 "Building the sample project we just generated."
    cd ${BASEDIR}/temp/${PACKAGE_NAME}
    mvn clean test install
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error " Failed to build the generated source code which galasactl created."
        exit 1
    fi
    success "OK"
}

#--------------------------------------------------------------------------
# Now build the source it created using gradle
function build_generated_source_gradle {
    h2 "Building the sample project we just generated."
    cd ${BASEDIR}/temp/${PACKAGE_NAME}
    gradle build publishToMavenLocal
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error " Failed to build the generated source code which galasactl created."
        exit 1
    fi
    success "OK"
}

#--------------------------------------------------------------------------
# Build a portfolio
function build_portfolio {
    h2 "Building a portfolio file"

    cd ${BASEDIR}/temp

    cmd="${BASEDIR}/bin/${galasactl_command} runs prepare \
    --portfolio my.portfolio \
    --bootstrap file://${GALASA_HOME}/bootstrap.properties \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccountExtended \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
    --log ${BASEDIR}/temp/portfolio-create-log.txt"

    info "Command is: $cmd"
    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to build a portfolio file"
        exit 1
    fi
    success "Built portfolio OK"
}


#--------------------------------------------------------------------------
# Initialise Galasa home
function galasa_home_init {
    h2 "Initialising galasa home directory"

    cd ${BASEDIR}/temp

    cmd="${BASEDIR}/bin/${galasactl_command} local init \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to initialise galasa home"
        exit 1
    fi
    success "Galasa home initialised"
}




#--------------------------------------------------------------------------
function launch_test_on_ecosystem {
    h2 "Launching test on an ecosystem..."

    if [[ "${GALASA_BOOTSTRAP}" == "" ]]; then
        error "GALASA_BOOTSTRAP environment variable is not set. It should refer to a remote ecosystem"
        exit 1
    fi

    # hostname=$(echo -n "${GALASA_BOOTSTRAP}" | sed -e "s/http:\/\///g" | sed -e "s/https:\/\///g" | sed -e "s/.bootstrap//g")
    # info "Host name for boostrap is ${hostname}"

    cd ${BASEDIR}/temp

    cmd="${BASEDIR}/bin/${galasactl_command} runs submit \
    --bootstrap $GALASA_BOOTSTRAP \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccountExtended \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '2' because the ecosystem doesn't know about this testcase.
    if [[ "${rc}" != "2" ]]; then
        error "Failed to submit a test to a remote ecosystem, and get Unknown back."
        exit 1
    fi
    success "Submitting test to ecosystem worked OK"
}



#--------------------------------------------------------------------------

#--------------------------------------------------------------------------
# Return to the top folder so we can do other things.
cd ${BASEDIR}




#--------------------------------------------------------------------------
# Initialise Galasa home
function galasa_home_init {
    h2 "Initialising galasa home directory"

    cd ${BASEDIR}/temp

    export GALASA_HOME=${BASEDIR}/temp/home

    cmd="${BASEDIR}/bin/${galasactl_command} local init \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to initialise galasa home"
        exit 1
    fi
    success "Galasa home initialised"
}


#--------------------------------------------------------------------------
function launch_test_on_ecosystem {
    h2 "Launching test on an ecosystem..."

    if [[ "${GALASA_BOOTSTRAP}" == "" ]]; then
        error "GALASA_BOOTSTRAP environment variable is not set. It should refer to a remote ecosystem"
        exit 1
    fi

    rm -fr ~/.galasa-old
    mv ~/.galasa ~/.galasa-old

    # hostname=$(echo -n "${GALASA_BOOTSTRAP}" | sed -e "s/http:\/\///g" | sed -e "s/https:\/\///g" | sed -e "s/.bootstrap//g")
    # info "Host name for boostrap is ${hostname}"

    cd ${BASEDIR}/temp

    cmd="${BASEDIR}/bin/${galasactl_command} runs submit \
    --bootstrap $GALASA_BOOTSTRAP \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
    --log -"

    info "Command is: $cmd"

    $cmd
    rc=$?
    # We expect a return code of '2' because the ecosystem doesn't know about this testcase.
    if [[ "${rc}" != "2" ]]; then
        error "Failed to submit a test to a remote ecosystem, and get Unknown back."
        exit 1
    fi
    success "Submitting test to ecosystem worked OK"

    mv ~/.galasa-old ~/.galasa
}

#--------------------------------------------------------------------------
# Build the documentation
function generate_galasactl_documentation {
    generated_docs_folder=${BASEDIR}/docs/generated
    h2 "Generating documentation"
    info "Documentation will be placed in ${generated_docs_folder}"
    mkdir -p ${generated_docs_folder}

    # Figure out which type of machine this script is currently running on.
    unameOut="$(uname -s)"
    case "${unameOut}" in
        Linux)      machine=linux;;
        Darwin)     machine=darwin;;
        *)          error "Unknown machine type ${unameOut}"
                    exit 1
    esac

    architecture="$(uname -m)"
    case $architecture in
        aarch64)    architecture=arm64
    esac

    # Call the documentation generator, which builds .md files
    info "Using program ${BASEDIR}/bin/gendocs-galasactl-${machine}-${architecture} to generate the documentation..."
    ${BASEDIR}/bin/gendocs-galasactl-${machine}-${architecture} ${generated_docs_folder}
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to generate documentation. rc=${rc}" ; exit 1 ; fi

    # The files have a line "###### Auto generated by cobra at 17/12/2022"
    # As we are (currently) checking-in these .md files, we don't want them to show as
    # changed in git (which compares the content, not timestamps).
    # So lets remove these lines from all the .md files.
    info "Removing lines with date/time in, to limit delta changes in git..."
    mkdir -p ${BASEDIR}/build
    temp_file="${BASEDIR}/build/temp.md"
    for FILE in ${generated_docs_folder}/*; do
        mv -f ${FILE} ${temp_file}
        cat ${temp_file} | grep -v "###### Auto generated by" > ${FILE}
        rm ${temp_file}
        success "Processed file ${FILE}"
    done
    success "Documentation generated - OK"
}



#--------------------------------------------------------------------------
function check_artifact_saved_in_ras {
    # The extended type of test saves an artifact into the RAS store
    # The artifact has "Hello Galasa !" inside.
    # Lets check the RAS to make sure that file is present.
    expected_string_in_test_artifact="Hello Galasa \!"
    grep -R "$expected_string_in_test_artifact" $GALASA_HOME/ras > /dev/null
    rc=$?
    if [[ "${rc}" != "0" ]]; then
        error "Failed to find the string \'$expected_string_in_test_artifact\" in RAS. Test case should have generated it."
        exit 1
    fi
    success "Confirmed that test case saved a test artifact to RAS"
}

function run_test_locally_using_galasactl {
    export LOG_FILE=$1

    h2 "Submitting 2 local tests using galasactl in a local JVM"

    cd ${BASEDIR}/temp/*banking

    BUNDLE=dev.galasa.example.banking.payee
    JAVA_CLASS=dev.galasa.example.banking.payee.TestPayeeExtended
    JAVA_CLASS_2=dev.galasa.example.banking.payee.TestPayee
    OBR_GROUP_ID=dev.galasa.example.banking
    OBR_ARTIFACT_ID=dev.galasa.example.banking.obr
    OBR_VERSION=0.0.1-SNAPSHOT

    # Could get this bootjar from https://development.galasa.dev/main/maven-repo/obr/dev/galasa/galasa-boot/
    read_boot_jar_version
    export GALASA_VERSION=$(cat ${BASEDIR}/VERSION )

    export M2_PATH=$(cd ~/.m2 ; pwd)
    export BOOT_JAR_PATH=${GALASA_HOME}/lib/${GALASA_VERSION}/galasa-boot-${BOOT_JAR_VERSION}.jar


    # Local .m2 content over-rides these anyway...
    # use development version of the OBR
    export REMOTE_MAVEN=https://development.galasa.dev/main/maven-repo/obr/
    # else go to maven central
    #export REMOTE_MAVEN=https://repo.maven.apache.org/maven2

    unset GALASA_BOOTSTRAP

    rm -f results.junit
    rm -f results.yaml
    rm -f results.json

    cmd="${BASEDIR}/bin/${galasactl_command} runs submit local \
    --obr mvn:${OBR_GROUP_ID}/${OBR_ARTIFACT_ID}/${OBR_VERSION}/obr \
    --class ${BUNDLE}/${JAVA_CLASS} \
    --class ${BUNDLE}/${JAVA_CLASS_2} \
    --remoteMaven ${REMOTE_MAVEN} \
    --throttle 1 \
    --requesttype MikeCLI \
    --poll 10 \
    --progress 1 \
    --reportjunit results.junit --reportyaml results.yaml --reportjson results.json  \
    --log ${LOG_FILE}"

    # --reportjson myreport.json \
    # --reportyaml myreport.yaml \


    # --noexitcodeontestfailures \
    # --galasaVersion 0.26.0 \

    info "Command is ${cmd}"
    $cmd
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to run the test. See details in log file ${LOG_FILE}" ; exit 1 ; fi
    success "Test ran OK"

    check_artifact_saved_in_ras
}

function test_on_windows {
    WINDOWS_HOST="cics-galasa-test"
    scp ${BASEDIR}/bin/galasactl-windows-x86_64.exe ${WINDOWS_HOST}:galasactl.exe
    ssh ${WINDOWS_HOST} ./galasactl.exe runs submit local  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount --log -
}

function cleanup_local_maven_repo {
    rm -fr ~/.m2/repository/dev/galasa/example
}

function cleanup_temp {
    rm -fr ${BASEDIR}/temp
    mkdir -p ${BASEDIR}/temp
    cd ${BASEDIR}/temp
}


#-------------------------------------------
#Detect secrets
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




# The steps to build the CLI
clean
download_dependencies
generate_rest_client
# go_mod_tidy - don't tidy the go.mod as it gets rid of transitive/indirect dependencies.
build_executables



# Now the steps to test it.

h2 "Setting up GALASA_HOME"
export GALASA_HOME=${BASEDIR}/temp/home
success "GALASA_HOME is set to be ${GALASA_HOME}"

cleanup_temp
calculate_galasactl_executable
galasa_home_init

# Local environments don't support the portfolio yet.
# build_portfolio

generate_galasactl_documentation

# Gradle ...
cleanup_temp
galasa_home_init
generate_sample_code --gradle
cleanup_local_maven_repo
build_generated_source_gradle
run_test_locally_using_galasactl ${BASEDIR}/temp/local-run-log-gradle.txt

# Maven ...
cleanup_temp
galasa_home_init
generate_sample_code --maven
cleanup_local_maven_repo
build_generated_source_maven
run_test_locally_using_galasactl ${BASEDIR}/temp/local-run-log-maven.txt

# Both Gradle and Maven ...
cleanup_temp
galasa_home_init
generate_sample_code --maven --gradle
cleanup_local_maven_repo
build_generated_source_maven
run_test_locally_using_galasactl ${BASEDIR}/temp/local-run-log-maven.txt
cleanup_local_maven_repo
build_generated_source_gradle
run_test_locally_using_galasactl ${BASEDIR}/temp/local-run-log-gradle.txt


check_secrets

# launch_test_on_ecosystem
# test_on_windows

#--------------------------------------------------------------------------
h2 "Use the results.."
info "Binary executable programs are found in the 'bin' folder."
ls ${BASEDIR}/bin | grep -v "gendocs"