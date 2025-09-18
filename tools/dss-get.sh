#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------------                   
#
# Objectives: Connect to the dss and list the values stored within it.
# 
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
# cd "${BASEDIR}"

cd "${BASEDIR}/.."
PROJECT_DIR=$(pwd)

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
    info "Syntax: dss-get.sh [OPTIONS]"
    cat << EOF
Options are:
-h | --help : Display this help text
-p | --pod  : Optional name of the etcd pod name to be queried. 
              If missing we'll get the pod name from kubernetes
EOF
}


#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------
pod=""
while [ "$1" != "" ]; do
    case $1 in
        -h | --help )   usage
                        exit
                        ;;

        --pod )      pod="$2"
                        shift
                        ;;

        * )             error "Unexpected argument $1"
                        usage
                        exit 1
    esac
    shift
done

if [[ "$pod" == "" ]]; then 
    pod=$(kubectl get pods | grep etcd | cut -f1 -d' ')
fi

kubectl -it exec techcobweb-galasa-1-etcd-0  -- etcdctl get dss --prefix=true > "$BASEDIR/etcd-temp.values"
done=0
while [[ "$done" == "0" ]]; do
    IFS= read -r key
    rc=$?
    if [[ "$rc" != "0" ]]||[[ -z $key ]]; then
        done=1
    else
        key=$(echo "$key" | tr -d '\r\n'| xargs )
        IFS= read -r value
        rc=$?
        if [[ "$rc" != "0" ]]||[[ -z $value ]]; then
        done=1
        else 
        value=$(echo "$value" | tr -d '\r\n' | xargs )
        str=$(echo "$key=$value")
        # hex="$(printf '%s' "$str" | hexdump -ve '/1 "%02X"')"
        echo "$str"
        fi
    fi
done < "$BASEDIR/etcd-temp.values"
rm "$BASEDIR/etcd-temp.values"