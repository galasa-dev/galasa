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
# cd "${BASEDIR}"

cd "${BASEDIR}/.."
WORKSPACE_DIR=$(pwd)

set -o pipefail


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
    h1 "Syntax"
    cat << EOF
set-version.sh [OPTIONS]
Options are:
-v | --version xxx : Mandatory. Set the version number to something explicitly. 
    For example '--version 0.29.0'
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

temp_dir=$BASEDIR/temp/version_bump
mkdir -p $temp_dir

function upgrade_docs_build_gradle {
    h1 "Upgrading the overall docs version in the build.gradle thats fed into the build process"
    
    source_path=${BASEDIR}/build.gradle
    temp_file=$temp_dir/build.gradle
    # This line needs to change:
    #    version="0.43.0"
    info "Upgrading version in file $source_path"

    cat $source_path | sed "s/version=".*"[ \t]*$/version=\"$component_version\"/1" > $temp_file
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace version in file $temp_file" ; exit 1 ; fi

    cp $temp_file $source_path
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version file with the modified one." ; exit 1 ; fi

    success "Upgraded docs version in build.gradle."
}

function upgrade_index {
    h1 "Upgrading the version in the index.md file that shows the release highlights"
    
    source_path=${BASEDIR}/content/index.md
    temp_file=$temp_dir/index.md
    # This line needs to change:
    #     [0.43.0 highlights](./releases/posts/v0.43.0.md)
    info "Upgrading version in file $source_path"

    cat $source_path | sed "s/.*[ ]*highlights\]([.]\/releases\/posts\/v.*[.]md)/[$component_version highlights](.\/releases\/posts\/v$component_version.md)/1" > $temp_file
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace version in file $temp_file" ; exit 1 ; fi

    cp $temp_file $source_path
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version file with the modified one." ; exit 1 ; fi

    success "Upgraded version in index.md."
}

function upgrade_installing_cli {
    h1 "Upgrading the version in the installing-cli-tool.md file"
    
    source_path=${BASEDIR}/content/docs/cli-command-reference/installing-cli-tool.md
    temp_file=$temp_dir/installing-cli-tool.md
    # These lines need to change
    #    (version 0.43.0 for example)
    #    `brew install --no-quarantine galasactl@0.43.0`
    info "Upgrading version in file $source_path"

    cat $source_path \
    | sed "s/(version[ ].*/(version $component_version for example): /1" \
    | sed "s/galasactl@.*/galasactl@$component_version\`/1" \
    > $temp_file
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace version in file $temp_file" ; exit 1 ; fi

    cp $temp_file $source_path
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version file with the modified one." ; exit 1 ; fi

    success "Upgraded version in installing-cli-tool.md."
}

function upgrade_ecosystem_installing {
    h1 "Upgrading the version in the ecosystem-installing-k8s.md file"
    
    source_path=${BASEDIR}/content/docs/ecosystem/ecosystem-installing-k8s.md
    temp_file=$temp_dir/ecosystem-installing-k8s.md
    # These lines needs to change:
    #     for example version 0.43.0 - 
    #     --set galasaVersion=0.43.0 --wait
    #     --set galasaVersion=0.43.0 --wait
    info "Upgrading version in file $source_path"

    cat $source_path \
    | sed "s/for[ ]example[ ]version[ ].*/for example version $component_version - by running the following command:/1" \
    | sed "s/galasaVersion=.*/galasaVersion=$component_version --wait/g" \
    > $temp_file
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace version in file $temp_file" ; exit 1 ; fi

    cp $temp_file $source_path
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version file with the modified one." ; exit 1 ; fi

    success "Upgraded version in ecosystem-installing-k8s.md."    
}

function upgrade_running_simbank_offline {
    h1 "Upgrading the version in the running-simbank-tests-cli-offline.md file"
    
    source_path=${BASEDIR}/content/docs/running-simbank-tests/running-simbank-tests-cli-offline.md
    temp_file=$temp_dir/running-simbank-tests-cli-offline.md
    # These lines need to change:
    #     The following example uses SimBank OBR version `0.43.0`.
    #     mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr
    #     mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr
    #     mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr
    #     mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr
    info "Upgrading version in file $source_path"

    cat $source_path \
    | sed "s/SimBank[ ]OBR[ ]version[ ]\`.*\`[.]/SimBank OBR version \`$component_version\`./1" \
    | sed "s/mvn[:]dev[.]galasa\/dev[.]galasa[.]simbank[.]obr\/.*\/obr/mvn:dev.galasa\/dev.galasa.simbank.obr\/$component_version\/obr/g" \
    > $temp_file
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace version in file $temp_file" ; exit 1 ; fi

    cp $temp_file $source_path
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version file with the modified one." ; exit 1 ; fi

    success "Upgraded version in running-simbank-tests-cli-offline.md."    
}

function upgrade_running_simbank {
    h1 "Upgrading the version in the running-simbank-tests-cli.md file"
    
    source_path=${BASEDIR}/content/docs/running-simbank-tests/running-simbank-tests-cli.md
    temp_file=$temp_dir/running-simbank-tests-cli.md
    # These lines need to change:
    #     The following example uses SimBank OBR version `0.43.0` and Galasa uber OBR version `0.43.0`.
    #     mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr
    #     mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr
    #     mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr
    #     mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr
    info "Upgrading version in file $source_path"

    cat $source_path \
    | sed "s/SimBank[ ]OBR[ ]version[ ]\`.*\`[ ]and[ ]Galasa[ ]uber[ ]OBR[ ]version[ ]\`.*\`[.]/SimBank OBR version \`$component_version\` and Galasa uber OBR version \`$component_version\`./1" \
    | sed "s/mvn[:]dev[.]galasa\/dev[.]galasa[.]simbank[.]obr\/.*\/obr/mvn:dev.galasa\/dev.galasa.simbank.obr\/$component_version\/obr/g" \
    > $temp_file
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace version in file $temp_file" ; exit 1 ; fi

    cp $temp_file $source_path
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version file with the modified one." ; exit 1 ; fi

    success "Upgraded version in running-simbank-tests-cli.md."    
}

upgrade_docs_build_gradle
upgrade_index
upgrade_installing_cli
upgrade_ecosystem_installing
upgrade_running_simbank_offline
upgrade_running_simbank
