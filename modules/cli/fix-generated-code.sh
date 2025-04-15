#! /usr/bin/env sh 

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------------                   
#
# Objectives: Fix anything we need to in the generated code.
# 
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
# cd "${BASEDIR}"

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

mkdir -p temp

# Explanation:
# ------------
# We have a bunch of model files generated.
# They all have this inside: for example:
#
#   ApiVersion *string `json:"apiVersion,omitempty"`
#   Kind *string `json:"kind,omitempty"`
#
# This is OK, but when rendering to yaml all the names of properties get screwed up. 
# eg: the `ApiVersion` field is rendered as `apiVersion` in json (good) but in yaml it renders as `apiversion` (bad).
# ... but we want them to contain the same property names for json or yaml.
#
# So we need to add the yaml annotations to make it look like this:
#
#   ApiVersion *string `json:"apiVersion,omitempty" yaml:"apiVersion,omitempty"`
#   Kind *string `json:"kind,omitempty" yaml:"kind,omitempty"`
#
# Then the `ApiVersion` field can render to `apiVersion` in the yaml just as it does in the json. To be consistent.
#
cd pkg/galasaapi
rc=$? ; if [[ "$rc" != "0" ]]; then error "Failed to change folders into the generated code" ; exit 1; fi

# List all the model files we want to run the blanket transform over...
ls model*.go > $BASEDIR/temp/file-list.txt
rc=$? ; if [[ "$rc" != "0" ]]; then error "Failed to list generated model files" ; exit 1; fi

while IFS= read -r file_to_process; do
    info "Processing file: ${file_to_process}"

    # Note the following about sed:
    # (.*) collects the group. The name of the property as json will render it.
    # \1 repeats that group in the substitution. So we can use it twice, once to repeat the json annotation, and once for the yaml one.
    # eg: In the following example, the string "apiVersion,omitempty" is collected as the first group... so...
    #
    #   ApiVersion *string `json:"apiVersion,omitempty"`
    # 
    # ...will become...
    #
    #   ApiVersion *string `json:"apiVersion,omitempty" yaml:"apiVersion,omitempty"`
    #
    cat ${file_to_process} | sed "s/json:\(.*\)\"\`/json:\1\" yaml:\1\"\`/g" > ${BASEDIR}/temp/${file_to_process}.temp
    rc=$? ; if [[ "$rc" != "0" ]]; then error "Failed to substitute yaml serialisation annotation into generated model file ${file_to_process}" ; exit 1; fi

    # Copy the transformed version over the top of the original, leaving a copy in the temp folder to diagnose problems with.
    cp ${BASEDIR}/temp/${file_to_process}.temp ${file_to_process}
    rc=$? ; if [[ "$rc" != "0" ]]; then error "Failed to copy fixed code over the generated original code at ${file_to_process}" ; exit 1; fi

done < $BASEDIR/temp/file-list.txt
rc=$? ; if [[ "$rc" != "0" ]]; then error "Failed inside while loop." ; exit 1; fi
