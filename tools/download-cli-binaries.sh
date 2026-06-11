#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------------
#
# Objectives: Download all Galasa CLI binaries from the development server
#
# Parameters:
#   $1 - Release branch (e.g., 'release', 'main')
#   $2 - Output directory (optional, defaults to current directory)
#
#-----------------------------------------------------------------------------------------

# Where is this script executing from?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
export ORIGINAL_DIR=$(pwd)

if [ -z "$TERM" ]; then
    export TERM="xterm-256color"
fi

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
# Main logic
#-----------------------------------------------------------------------------------------

h1 "Downloading Galasa CLI Binaries"

# Check parameters
if [ -z "$1" ]; then
    error "Release branch parameter is required"
    info "Usage: $0 <release-branch> [output-directory]"
    info "Example: $0 release ./temp"
    exit 1
fi

RELEASE_BRANCH="$1"
OUTPUT_DIR="${2:-.}"

# Create output directory if it doesn't exist
if [ ! -d "$OUTPUT_DIR" ]; then
    info "Creating output directory: $OUTPUT_DIR"
    mkdir -p "$OUTPUT_DIR"
fi

# Base URL for CLI binaries
BASE_URL="https://development.galasa.dev/${RELEASE_BRANCH}/binary/cli"

info "Fetching list of CLI binaries from ${BASE_URL}..."

# Fetch the directory listing and extract binary filenames
BINARIES=$(curl -s "${BASE_URL}/" | sed -n 's/.*href="\(galasactl[^"]*\)".*/\1/p' | sort -u)

if [ -z "$BINARIES" ]; then
    error "No binaries found at ${BASE_URL}"
    exit 1
fi

h2 "Found binaries:"
echo "$BINARIES"

# Download each binary
h2 "Downloading binaries to ${OUTPUT_DIR}..."
DOWNLOAD_COUNT=0
FAILED_COUNT=0

echo "$BINARIES" | while read -r binary; do
    if [ -n "$binary" ]; then
        info "Downloading ${binary}..."
        if wget -q "${BASE_URL}/${binary}" -O "${OUTPUT_DIR}/${binary}"; then
            success "Downloaded ${binary}"
            DOWNLOAD_COUNT=$((DOWNLOAD_COUNT + 1))
        else
            warn "Failed to download ${binary}"
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    fi
done

h2 "Downloaded files in ${OUTPUT_DIR}:"
ls -lh "${OUTPUT_DIR}"/galasactl* 2>/dev/null || warn "No files downloaded"

success "Download complete"
