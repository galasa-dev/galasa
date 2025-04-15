#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Objectives: 
# Give the tooling a spin to basically make sure it still works.


# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
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
underline() { printf "${underline}${bold}%s${reset}\n" "$@"
}
h1() { printf "\n${underline}${bold}${blue}%s${reset}\n" "$@"
}
h2() { printf "\n${underline}${bold}${white}%s${reset}\n" "$@"
}
debug() { printf "${white}%s${reset}\n" "$@"
}
info() { printf "${white}➜ %s${reset}\n" "$@"
}
success() { printf "${green}✔ %s${reset}\n" "$@"
}
error() { printf "${red}✖ %s${reset}\n" "$@"
}
warn() { printf "${tan}➜ %s${reset}\n" "$@"
}
bold() { printf "${bold}%s${reset}\n" "$@"
}
note() { printf "\n${underline}${bold}${blue}Note:${reset} ${blue}%s${reset}\n" "$@"
}

#-------------------------------------------------------------------------
# ERROR and exit - script broken, much duplicated within test-galasactl-local.sh.
# Being kept around incase of future revival
#-------------------------------------------------------------------------
error "Script is currently out of use and broken. Use test-galasactl-local.sh for a more up to date version."
exit 1

#-------------------------------------------------------------------------
# Clean
#-------------------------------------------------------------------------
rm -fr temp
mkdir -p temp

# rm -fr ~/.galasa/*

#-------------------------------------------------------------------------
# Set galasactl version to use
#-------------------------------------------------------------------------
raw_os=$(uname -s) # eg: "Darwin"
os=""

case $raw_os in
    Darwin*) 
        os="darwin" 
        ;;
	Linux*)
    	os="linux"
        ;;
    *) 
        error "Failed to recognise which operating system is in use. $raw_os"
        exit 1
esac

architecture=$(uname -m)

export GALASACTL="${BASEDIR}/bin/galasactl-${os}-${architecture}"

#-------------------------------------------------------------------------
# Run tool, generate source
#-------------------------------------------------------------------------
cd temp
${GALASACTL} project create --package dev.galasa.example.banking --features payee,account --obr --maven --gradle --log -
rc=$?
if [[ "${rc}" != "0" ]]; then 
    error "Failed. rc=${rc}"
    exit 1
fi

#-------------------------------------------------------------------------
# Add a long-running test
#-------------------------------------------------------------------------
cat << EOF >> dev.galasa.example.banking/dev.galasa.example.banking.account/src/main/java/dev/galasa/example/banking/account/TestLongRunningAccount.java
package dev.galasa.example.banking.account;

import static org.assertj.core.api.Assertions.*;

import java.lang.Thread;

import dev.galasa.core.manager.*;
import dev.galasa.Test;

/**
 * A sample galasa test class 
 */
@Test
public class TestLongRunningAccount {

	// Galasa will inject an instance of the core manager into the following field
	@CoreManager
	public ICoreManager core;

	/**
	 * Test which demonstrates that the managers have been injected ok.
	 */
	@Test
	public void simpleSampleTest() throws Exception {
        int secondsToSleep = 20;
        Thread.sleep(secondsToSleep*1000);
		assertThat(core).isNotNull();
	}

}
EOF

#-------------------------------------------------------------------------
# Add a quick-running failing test
#-------------------------------------------------------------------------
cat << EOF >> dev.galasa.example.banking/dev.galasa.example.banking.account/src/main/java/dev/galasa/example/banking/account/TestFailingAccount.java
package dev.galasa.example.banking.account;

import static org.assertj.core.api.Assertions.*;

import java.lang.Thread;

import dev.galasa.core.manager.*;
import dev.galasa.Test;

/**
 * A sample galasa test class 
 */
@Test
public class TestFailingAccount {

	// Galasa will inject an instance of the core manager into the following field
	@CoreManager
	public ICoreManager core;

	/**
	 * Test which demonstrates that the managers have been injected ok.
	 */
	@Test
	public void simpleSampleTest() throws Exception {
        // Fail on purpose.
		assertThat(true).isFalse();
	}

}
EOF

#-------------------------------------------------------------------------
# Build the generated code
#-------------------------------------------------------------------------
cd dev.galasa.example.banking
mvn clean test install

#-------------------------------------------------------------------------
# Run the test
#-------------------------------------------------------------------------

export TEST_BUNDLE=dev.galasa.example.banking.payee
export TEST_JAVA_CLASS=dev.galasa.example.banking.payee.TestPayee
export TEST_OBR_GROUP_ID=dev.galasa.example.banking
export TEST_OBR_ARTIFACT_ID=dev.galasa.example.banking.obr
export TEST_OBR_VERSION=0.0.1-SNAPSHOT


# Could get this bootjar from https://development.galasa.dev/main/maven-repo/obr/dev/galasa/galasa-boot/0.27.0/
export BOOT_JAR_VERSION=$(cat ${BASEDIR}/build.gradle | grep "galasaVersion" | head -1 | cut -f2 -d"'")

export OBR_VERSION=$(cat ${BASEDIR}/VERSION)

export M2_PATH=$(cd ~/.m2 ; pwd)
export BOOT_JAR_PATH=~/.galasa/lib/${OBR_VERSION}/galasa-boot-${BOOT_JAR_VERSION}.jar

    


# Local .m2 content over-rides these anyway...
# use development version of the OBR
export REMOTE_MAVEN=https://development.galasa.dev/main/maven-repo/obr/
# else go to maven central
#export REMOTE_MAVEN=https://repo.maven.apache.org/maven2


echo "my.file.based.property = 23" > ${BASEDIR}/temp/extra-overrides.properties

${GALASACTL} runs submit local \
--obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
--class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestLongRunningAccount \
--class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
--class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccountExtended \
--class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestFailingAccount \
--class dev.galasa.example.banking.payee/dev.galasa.example.banking.payee.TestPayee \
--class dev.galasa.example.banking.payee/dev.galasa.example.banking.payee.TestPayeeExtended \
--remoteMaven https://development.galasa.dev/main/maven-repo/obr/ \
--galasaVersion 0.27.0  \
--log - 2>&1 | tee ${BASEDIR}/temp/log.txt

# --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestLongRunningAccount \
# \
# --log - 2>&1 | tee ${BASEDIR}/temp/log.txt

# --override my.property=HELLO \
# --overridefile ${BASEDIR}/temp/extra-overrides.properties 

# --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
# --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccountExtended \
# --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestFailingAccount \
# --class dev.galasa.example.banking.payee/dev.galasa.example.banking.payee.TestPayee \
# --class dev.galasa.example.banking.payee/dev.galasa.example.banking.payee.TestPayeeExtended \

# --throttle 7 \
# --requesttype MikeCLI \
# --poll 10 \
# --progress 1 \
# 

# --reportjson myreport.json \
# --reportyaml myreport.yaml \


# --noexitcodeontestfailures \



rc=$?
echo "Exit code detected by calling script is ${rc}"
if [[ "${rc}" != "0" ]]; then 
    echo "Failed to run the test"
    exit 1
fi
echo "Test ran OK"