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

function bump_version {
    source_file=$1
    temp_file=$2

    cat $source_file | sed "s/dev.galasa.framework:.*'/dev.galasa.framework:$component_version'/1" > $temp_file
    cp $temp_file $source_file

    cat $source_file | sed "s/version[ ]*=.*/version = '$component_version'/1" > $temp_file
    cp $temp_file $source_file
}


# Extensions base
bump_version $BASEDIR/galasa-extensions-parent/buildSrc/src/main/groovy/galasa.extensions.gradle $temp_dir/galasa.extensions.gradle
bump_version $BASEDIR/galasa-extensions-parent/build.gradle $temp_dir/extensions-parent.build.gradle

# Couchdb...
bump_version $BASEDIR/galasa-extensions-parent/dev.galasa.ras.couchdb/build.gradle $temp_dir/couchdb-build.gradle

# etcd...
bump_version $BASEDIR/galasa-extensions-parent/dev.galasa.cps.etcd/build.gradle $temp_dir/etcd-build.gradle

# REST CPS...
bump_version $BASEDIR/galasa-extensions-parent/dev.galasa.cps.rest/build.gradle $temp_dir/restcps-build.gradle

# CouchDB Auth Store...
bump_version $BASEDIR/galasa-extensions-parent/dev.galasa.auth.couchdb/build.gradle $temp_dir/couchdbauth-build.gradle

# Kafka...
bump_version $BASEDIR/galasa-extensions-parent/dev.galasa.events.kafka/build.gradle $temp_dir/kafka-build.gradle

# Common...
bump_version $BASEDIR/galasa-extensions-parent/dev.galasa.extensions.common/build.gradle $temp_dir/common-build.gradle

# The framework version is the first one in the file.
cat $BASEDIR/release.yaml | sed "s/version:.*/version: $component_version/1" > $temp_dir/release.yaml
cp $temp_dir/release.yaml $BASEDIR/release.yaml

# There is a bundle in the extensions which needs a bump also.
cat $BASEDIR/galasa-extensions-parent/buildSrc/src/main/groovy/galasa.extensions.gradle | sed "s/dev.galasa:dev.galasa:.*'/dev.galasa:dev.galasa:$component_version'/1" > $temp_dir/galasa.extensions.gradle
cp $temp_dir/galasa.extensions.gradle $BASEDIR/galasa-extensions-parent/buildSrc/src/main/groovy/galasa.extensions.gradle


# The top-level build.gradle has this: id 'dev.galasa.githash' version '$version' apply false
cat $BASEDIR/galasa-extensions-parent/build.gradle | sed  "s/id 'dev[.]galasa[.]githash' version '.*'/id 'dev.galasa.githash' version '$component_version'/1" > $temp_dir/top-build.gradle
cp $temp_dir/top-build.gradle $BASEDIR/galasa-extensions-parent/build.gradle