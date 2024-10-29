#! /usr/bin/env bash 

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------------                   
#
# Objectives: Sets the version number of the galasa-boot.
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
set-galasa-boot-version.sh [OPTIONS]
Options are:
-v | --version xxx : Mandatory. Set the version number to something explicitly. 
    Re-builds the release.yaml based on the contents of sub-projects.
    For example '--version 0.29.0'
EOF
}

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

function update_build_gradle_version {
    source_file="${BASEDIR}/galasa-parent/galasa-boot/build.gradle"
    h1 "Updating the version in $source_file "

    temp_dir=$1

    set -o pipefail

    temp_file="$temp_dir/build.gradle"

    info "Using temporary file $temp_file"
    info "Updating file $source_file"

    cat $source_file | sed "s/version[ ]*=[ ]*.*$/version = '$component_version'/1" > $temp_file
    rc=$?; if [[ "${rc}" != "0" ]]; then error "Failed to set version into $source_file file."; exit 1; fi
    cp $temp_file ${source_file}
    rc=$?; if [[ "${rc}" != "0" ]]; then error "Failed to overwrite new version of $source_file file."; exit 1; fi

    success "$source_file updated OK."
}

function update_release_yaml_version {
    source_file="${BASEDIR}/release.yaml"
    h1 "Updating the version in $source_file "

    temp_dir=$1

    is_line_supressed=false

    set -o pipefail

    temp_file="$temp_dir/release.yaml"

    info "Using temporary file $temp_file"
    info "Updating file $source_file"

    #Read through the realease yaml and search for line matching the regex. Marks the line after the match and overwrites it.
    while IFS= read -r line
    do
        regex="^  - artifact: galasa-boot[ ]*$"
        if [[ "$line" =~ $regex ]]; then
            # We found the marker, so the next line needs supressing.
            echo "$line"
            is_line_supressed=true
        else
            if [[ $is_line_supressed == true ]]; then
                # Don't echo this line, but we only want to supress one line.
                is_line_supressed=false
                echo "    version: $component_version"
            else
                # Nothing special about this line, so echo it.
                echo "$line"
            fi
        fi

    done < $source_file > $temp_file
    cp $temp_file ${source_file}
    rc=$?; if [[ "${rc}" != "0" ]]; then error "Failed to overwrite new version of $source_file file."; exit 1; fi

    success "$source_file updated OK."
}

temp_dir=$BASEDIR/temp/versions
rm -fr $temp_dir
mkdir -p $temp_dir

update_build_gradle_version $temp_dir
update_release_yaml_version $temp_dir
