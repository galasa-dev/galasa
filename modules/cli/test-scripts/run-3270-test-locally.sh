#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
echo "Running script $0"

# This script can be ran locally or executed in a pipeline to test the various built binaries of galasactl
# This script tests the 'galasactl project create' and 'galasactl runs submit local' commands
# Pre-requesite: the CLI must have been built first so the binaries are present in the ./bin directory


# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
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

rm -fr "${BASEDIR}/../temp/home"
mkdir -p "${BASEDIR}/../temp/home"
cd "${BASEDIR}/../temp/home"
export GALASA_HOME=$(pwd)
cd "$BASEDIR"



galasactl local init --development --galasahome "$GALASA_HOME"
rc=$? ; if [[ "$rc" != "0" ]]; then error "Failed to create galasa home folder" ; exit 1 ; fi


overrides_file=$BASEDIR/../temp/overrides-for-3270-test.properties

if [[ "${ZOS_HOSTNAME}" == "" ]]; then 
    error "Env variable ZOS_HOSTNAME is not set. Set it and re-try" 
    exit 1
fi


cat << EOF >> "$overrides_file"

zos.dse.tag.PRIMARY.clusterid=PLEX2
zos.dse.tag.PRIMARY.imageid=MV26
zos.cluster.PLEX2.images=MV26

zos.image.MV26.sysplex=PLEX2
zos.image.MV26.default.hostname=${ZOS_HOSTNAME}
zos.image.MV26.ipv4.hostname=${ZOS_HOSTNAME}
zos.image.MV26.telnet.port=992
zos.image.MV26.telnet.tls=true
zos.image.MV26.credentials=PLEX2
EOF


credentials_file=$GALASA_HOME/credentials.properties
cat << EOF >> "$credentials_file"

secure.credentials.PLEX2.username=${ZOS_TEST_USERNAME}
secure.credentials.PLEX2.password=${ZOS_TEST_PASSWORD}
EOF

galasa_version=$(cat "$BASEDIR/../VERSION")

galasactl runs submit local \
--obr "mvn:dev.galasa/dev.galasa.ivts.obr/${galasa_version}/obr" \
--class dev.galasa.zos.ivts/dev.galasa.zos.ivts.zos3270.Zos3270IVT \
--overridefile "$overrides_file" \
--log - \
--tags 3270 \
--trace

