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
    Re-builds the release.yaml based on the contents of sub-projects.
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

function upgrade_dependencies_on_framework {
    h1 "Upgrading anything which references the framework component"
    
    source_path=${BASEDIR}/galasa-managers-parent/buildSrc/src/main/groovy/galasa.manager.gradle
    temp_file=$temp_dir/dependency.gradle
    # The line marked this:  implementation 'dev.galasa:dev.galasa.framework:0.27.0' needs to change.
    info "Upgrading version framework in file $source_path"

    cat $source_path | sed "s/implementation[ \t]*.'dev.galasa:dev.galasa.framework:.*'[ \t]*$/implementation 'dev.galasa:dev.galasa.framework:$component_version'/1" > $temp_file
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version in file $source_path" ; exit 1 ; fi

    cp $temp_file $source_path
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version file with the modified one." ; exit 1 ; fi

    success "Upgraded dependencies on framework."
}

function upgrade_build_gradle {
    h1 "Upgrading the component version number in the master gradle."

    source_path=$BASEDIR/galasa-managers-parent/build.gradle
    info "File to change is at file$source_path"

    # The galasa-managers-parent/build.gradle file is where the 'master' version number 
    # of the this component lives.
    # For example: version = "0.29.0"
    
    cat $source_path | sed "s/version[ ]*=.*/version = \"$component_version\"/1" > $temp_dir/managers-build.gradle
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version in file $source_path" ; exit 1 ; fi

    cp $temp_dir/managers-build.gradle $source_path
    rc=$? ; if [[ "${rc}" != "0" ]]; then error "Failed to replace master version file with the modified one." ; exit 1 ; fi

    success "Upgraded build.gradle file OK."
}

function upgrade_kube_manager_ivt_readme() {
    cat $BASEDIR/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.kubernetes.manager.ivt/README.md \
    | sed "s/mvn[:]dev[.]galasa\/dev[.]galasa[.]uber[.]obr\/.*\/obr/mvn:dev.galasa\/dev.galasa.uber.obr\/$component_version\/obr/g" \
    > $temp_dir/manager-gradle-plugin
    cp $temp_dir/manager-gradle-plugin $BASEDIR/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.kubernetes.manager.ivt/README.md
    success "OK"
}

upgrade_build_gradle
upgrade_dependencies_on_framework
upgrade_kube_manager_ivt_readme