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

function upgrade_read_me_example {

    # Updates the version in the galasactl runs submit local example in the README.md

    h2 "upgrading the ivts README.md"

    cat $BASEDIR/README.md | sed "s/dev[.]galasa[.]ivts[.]obr\/.*\/obr/dev.galasa.ivts.obr\/$component_version\/obr/1" > $temp_dir/ivts-README.md
    cp $temp_dir/ivts-README.md $BASEDIR/README.md

    success "ivts README.md upgraded OK."

}

function upgrade_parent_build_gradle {

    # Updates the version in 'allprojects' for the IVTs bundle which sets the version of each IVT bundle.

    h2 "upgrading the dev.galasa.ivts parent build.gradle"

    cat $BASEDIR/dev.galasa.ivts/build.gradle | sed "s/version[ ]*=.*/version = '$component_version'/1" > $temp_dir/ivts-build.gradle
    cp $temp_dir/ivts-build.gradle $BASEDIR/dev.galasa.ivts/build.gradle

    success "dev.galasa.ivts parent build.gradle upgraded OK."
}

function upgrade_obr_build_gradle {

    # Updates the version of the dev.galasa.obr and dev.galasa.testcatalog plugins used in the IVT OBR.

    h2 "upgrading the dev.galasa.ivts.obr build.gradle"

    cat $BASEDIR/dev.galasa.ivts/dev.galasa.ivts.obr/build.gradle \
    | sed "s/id 'dev[.]galasa[.]obr' version '.*'/id 'dev.galasa.obr' version '$component_version'/1" \
    | sed "s/id 'dev[.]galasa[.]testcatalog' version '.*'/id 'dev.galasa.testcatalog' version '$component_version'/1" \
    > $temp_dir/obr-build.gradle
    cp $temp_dir/obr-build.gradle $BASEDIR/dev.galasa.ivts/dev.galasa.ivts.obr/build.gradle

    success "dev.galasa.ivts.obr build.gradle upgraded OK."

}

function upgrade_plugins_in_ivt_bundles {

    # Updates the version of the dev.galasa.tests plugin used in each IVT bundle.

    h2 "upgrading the dev.galasa.ivts.core build.gradle"

    cat $BASEDIR/dev.galasa.ivts/dev.galasa.ivts.core/build.gradle | sed "s/id 'dev[.]galasa[.]tests' version '.*'/id 'dev.galasa.tests' version '$component_version'/1" > $temp_dir/core-build.gradle
    cp $temp_dir/core-build.gradle $BASEDIR/dev.galasa.ivts/dev.galasa.ivts.core/build.gradle

    success "dev.galasa.ivts.core build.gradle upgraded OK."

    # More bundles to be added...
}

upgrade_read_me_example
upgrade_parent_build_gradle
upgrade_obr_build_gradle
upgrade_plugins_in_ivt_bundles