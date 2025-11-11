#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------------                   
# Set Colors
#-----------------------------------------------------------------------------------------  
reset=$(tput sgr0)
green=$(tput setaf 76)

#-----------------------------------------------------------------------------------------                   
# Headers and Logging
#-----------------------------------------------------------------------------------------      
success() { printf "${green}✔ %s${reset}\n" "$@"
}

#-----------------------------------------------------------------------------------------                   
# Functions
#----------------------------------------------------------------------------------------- 

# Sets envs if they are specified on local machine (prevents null values)
function set_env_vars() {
  local GALASA_TOKEN_VALUE="$1"
  local SOURCE_MAVEN_VALUE="$2"
  local GALASA_BOOTSTRAP_VALUE="$3"
  local GPG_PASSPHRASE_VALUE="$4"

  local PROFILE_FILE="${HOME}/.bashrc"

  if [ -n "$GALASA_TOKEN_VALUE" ]; then
    echo "export GALASA_TOKEN=\"$GALASA_TOKEN_VALUE\"" >> "$PROFILE_FILE"
  fi

  if [ -n "$SOURCE_MAVEN_VALUE" ]; then
    echo "export SOURCE_MAVEN=\"$SOURCE_MAVEN_VALUE\"" >> "$PROFILE_FILE"
  fi

  if [ -n "$GALASA_BOOTSTRAP_VALUE" ]; then
    echo "export GALASA_BOOTSTRAP=\"$GALASA_BOOTSTRAP_VALUE\"" >> "$PROFILE_FILE"
  fi

  if [ -n "$GPG_PASSPHRASE_VALUE" ]; then
    echo "export GPG_PASSPHRASE=\"$GPG_PASSPHRASE_VALUE\"" >> "$PROFILE_FILE"
  fi

  success "Existing local envs copied sucessfully"
}

set_env_vars "$1" "$2" "$3" "$4"
§