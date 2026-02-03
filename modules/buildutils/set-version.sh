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

#-------------------------------------------------------------------------------
function replace_line_following {

    source_file=$1
    target_file=$2
    temp_dir=$3
    regex_line_before="$4"
    regex_line_replaced="$5"
    substitute_for="$6"

    h2 "Updating the line in file $source_file which satisfies the regex $regex_line_before on the line before, and has $regex_line_replaced on the line being replaced."

    # Read through the release yaml and set the version of the framework bundle explicitly.
    # It's on the line after the line containing 'release:'
    # The line we need to change looks like this: version: 0.29.0
    is_line_supressed=false
    while IFS= read -r line
    do

        if [[ "$line" =~ $regex_line_before ]]; then
            # We found the marker, so the next line needs supressing.
            echo "$line"
            is_line_supressed=true
        else
            if [[ $is_line_supressed == true ]]; then
                if [[ "$line" =~ $regex_line_replaced ]]; then 
                    # The line to be replaced has the desired contents also.
                    is_line_supressed=true
                else 
                    # The substitutionm shouldn't zap this line as it doesn't match the criteria.
                    is_line_supressed=false
                fi
            fi

            if [[ $is_line_supressed == true ]]; then
                # Don't echo this line, but we only want to supress one line.
                is_line_supressed=false
                echo "${substitute_for}"
            else
                # Nothing special about this line, so echo it.
                echo "$line"
            fi
        fi

    done < $source_file > $temp_dir/temp.txt

    cp $temp_dir/temp.txt ${target_file}

    success "updated OK."
}


# The root command of the galasabld tool has a version inside the cobra command.
cat $BASEDIR/pkg/cmd/root.go | sed "s/^.*Version[:].*$/	Version: \"$component_version\",/1" > $temp_dir/root.go.txt
cp $temp_dir/root.go.txt $BASEDIR/pkg/cmd/root.go


replace_line_following ${BASEDIR}/openapi2beans/JavaChecker/pom.xml ${BASEDIR}/openapi2beans/JavaChecker/pom.xml $temp_dir "^.*dev.galasa.platform.*$" "version" "				<version>$component_version</version>"