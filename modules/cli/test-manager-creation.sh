#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Stress test script for manager project creation feature
# This script tests various scenarios for creating manager projects

set -e  # Exit on error

BASEDIR=$(dirname "$0")
cd "${BASEDIR}"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Manager Project Creation Stress Test${NC}"
echo -e "${BLUE}========================================${NC}"

# Determine the galasactl executable for this platform
raw_os=$(uname -s)
os=""
case $raw_os in
    Darwin*) os="darwin" ;;
    Windows*) os="windows" ;;
    Linux*) os="linux" ;;
    *) echo -e "${RED}Failed to recognise OS: $raw_os${NC}"; exit 1 ;;
esac

architecture=$(uname -m)
case $architecture in
    aarch64) architecture="arm64" ;;
esac

GALASACTL="./bin/galasactl-${os}-${architecture}"

if [ ! -f "$GALASACTL" ]; then
    echo -e "${RED}Error: galasactl executable not found at $GALASACTL${NC}"
    echo "Please build it first with: make all"
    exit 1
fi

echo -e "${GREEN}Using galasactl: $GALASACTL${NC}"

# Create a temp directory for testing
TEST_DIR="./temp/manager-stress-test"
rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"
cd "$TEST_DIR"

echo ""
echo -e "${BLUE}Test 1: Basic manager creation with Maven${NC}"
$GALASACTL project create --package dev.galasa.test.docker --manager --maven
if [ -d "dev.galasa.test.docker/dev.galasa.test.docker.manager" ]; then
    echo -e "${GREEN}✓ Manager project created${NC}"
    cd dev.galasa.test.docker
    mvn clean test
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Maven build successful${NC}"
    else
        echo -e "${RED}✗ Maven build failed${NC}"
        exit 1
    fi
    cd ..
else
    echo -e "${RED}✗ Manager project not created${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Test 2: Manager creation with custom name and Gradle${NC}"
$GALASACTL project create --package dev.galasa.test.kubernetes --manager --managerName k8s --gradle
if [ -d "dev.galasa.test.kubernetes/dev.galasa.test.kubernetes.manager" ]; then
    echo -e "${GREEN}✓ Manager project created with custom name${NC}"
    cd dev.galasa.test.kubernetes
    gradle build
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Gradle build successful${NC}"
    else
        echo -e "${RED}✗ Gradle build failed${NC}"
        exit 1
    fi
    cd ..
else
    echo -e "${RED}✗ Manager project not created${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Test 3: Manager creation with OBR${NC}"
$GALASACTL project create --package dev.galasa.test.http --manager --managerName http --obr --maven
if [ -d "dev.galasa.test.http/dev.galasa.test.http.obr" ]; then
    echo -e "${GREEN}✓ OBR project created${NC}"
    if [ -f "dev.galasa.test.http/dev.galasa.test.http.obr/pom.xml" ]; then
        echo -e "${GREEN}✓ OBR pom.xml exists${NC}"
        # Check if OBR references the manager
        if grep -q "dev.galasa.test.http.manager" dev.galasa.test.http/dev.galasa.test.http.obr/pom.xml; then
            echo -e "${GREEN}✓ OBR references manager bundle${NC}"
        else
            echo -e "${RED}✗ OBR does not reference manager bundle${NC}"
            exit 1
        fi
    else
        echo -e "${RED}✗ OBR pom.xml not found${NC}"
        exit 1
    fi
else
    echo -e "${RED}✗ OBR project not created${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Test 4: Manager creation with both Maven and Gradle${NC}"
$GALASACTL project create --package dev.galasa.test.selenium --manager --maven --gradle
if [ -f "dev.galasa.test.selenium/dev.galasa.test.selenium.manager/pom.xml" ] && \
   [ -f "dev.galasa.test.selenium/dev.galasa.test.selenium.manager/build.gradle" ]; then
    echo -e "${GREEN}✓ Both Maven and Gradle files created${NC}"
    cd dev.galasa.test.selenium
    mvn clean test
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Maven build successful${NC}"
    fi
    gradle build
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Gradle build successful${NC}"
    fi
    cd ..
else
    echo -e "${RED}✗ Maven or Gradle files missing${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Test 5: Verify generated Java files${NC}"
MANAGER_DIR="dev.galasa.test.docker/dev.galasa.test.docker.manager"
EXPECTED_FILES=(
    "src/main/java/dev/galasa/test/docker/DockerManager.java"
    "src/main/java/dev/galasa/test/docker/IDockerManager.java"
    "src/main/java/dev/galasa/test/docker/IDockerResource.java"
    "src/main/java/dev/galasa/test/docker/DockerManagerException.java"
    "src/main/java/dev/galasa/test/docker/internal/DockerManagerImpl.java"
    "src/main/java/dev/galasa/test/docker/internal/DockerManagerField.java"
    "src/main/java/dev/galasa/test/docker/internal/DockerResourceImpl.java"
    "src/test/java/dev/galasa/test/docker/internal/DockerManagerImplTest.java"
)

ALL_FILES_EXIST=true
for file in "${EXPECTED_FILES[@]}"; do
    if [ -f "$MANAGER_DIR/$file" ]; then
        echo -e "${GREEN}✓ $file exists${NC}"
    else
        echo -e "${RED}✗ $file missing${NC}"
        ALL_FILES_EXIST=false
    fi
done

if [ "$ALL_FILES_EXIST" = true ]; then
    echo -e "${GREEN}✓ All expected Java files generated${NC}"
else
    echo -e "${RED}✗ Some Java files missing${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Test 6: Verify manager lifecycle methods${NC}"
MANAGER_IMPL="$MANAGER_DIR/src/main/java/dev/galasa/test/docker/internal/DockerManagerImpl.java"
REQUIRED_METHODS=("initialise" "youAreRequired" "provisionGenerate" "provisionDiscard")

for method in "${REQUIRED_METHODS[@]}"; do
    if grep -q "public void $method" "$MANAGER_IMPL"; then
        echo -e "${GREEN}✓ Method $method() found${NC}"
    else
        echo -e "${RED}✗ Method $method() missing${NC}"
        exit 1
    fi
done

echo ""
echo -e "${BLUE}Test 7: Verify OSGi bundle configuration${NC}"
BND_FILE="$MANAGER_DIR/bnd.bnd"
if [ -f "$BND_FILE" ]; then
    echo -e "${GREEN}✓ bnd.bnd file exists${NC}"
    if grep -q "Export-Package" "$BND_FILE"; then
        echo -e "${GREEN}✓ Export-Package declaration found${NC}"
    fi
    if grep -q "Import-Package" "$BND_FILE"; then
        echo -e "${GREEN}✓ Import-Package declaration found${NC}"
    fi
else
    echo -e "${RED}✗ bnd.bnd file missing${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Test 8: Test mutual exclusivity (should fail)${NC}"
$GALASACTL project create --package dev.galasa.test.fail --manager --features test1,test2 2>&1 | grep -q "mutually exclusive"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Mutual exclusivity check works${NC}"
else
    echo -e "${RED}✗ Mutual exclusivity check failed${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}All stress tests passed!${NC}"
echo -e "${GREEN}========================================${NC}"

# Cleanup
cd ../..
echo ""
echo "Test artifacts are in: $TEST_DIR"
echo "To clean up, run: rm -rf $TEST_DIR"
