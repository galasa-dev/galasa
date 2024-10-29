#! /usr/bin/env bash 

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
#-----------------------------------------------------------------------------------------                   
#
# Objectives: Calculate transient dependencies so we can include them in the bnd bundle
# 
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
cd "${BASEDIR}"

cd "${BASEDIR}/.."
WORKSPACE_DIR=$(pwd)
cd "${BASEDIR}"

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
    info "Syntax: calculate-dependencies.sh [OPTIONS]"
    cat << EOF
Options are:
<none>

Environment variables used:
<none>

EOF
}


function create_temp_project {
    h2 "Creating a temporary project so we can calculate the dependencies it has"
    mkdir -p ${BASEDIR}/temp
    cat << EOF > ${BASEDIR}/temp/pom.xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>dev.galasa</groupId>
    <artifactId>dependency-finder</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>io.etcd</groupId>
            <artifactId>jetcd-core</artifactId>
            <version>0.5.9</version>
        </dependency>
    </dependencies>

</project>
EOF
    success "OK"
}


function list_transient_dependencies_using_maven {

    cd ${BASEDIR}/temp
    h2 "This might be good in the bnd file of the cps extension:"
    mvn dependency:tree | sed "s/\[INFO\]//g" | grep -v "BUILD SUCCESS" | grep -v "\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-" | grep -v "Total time" | grep -v "Finished at" \
    | grep -v "Scanning for projects" | grep -v "Building dependency-finder" | grep -v "maven-dependency-plugin" | grep -v "dev.galasa:dependency-finder" \
    | grep -v "^[ \t]*$" | sed "s/^[|+ \t\\\-]*//" | sed "s/:compile.*//g" | sed "s/:runtime.*//g" \
    | cut -d ':' -f2,4 \
    | sed "s/:/-/g" | sed "s/$/.jar; lib:=true,\\\/" | sed "s/^/    /" \
    | sort | uniq \
    > $BASEDIR/temp/dependencies_maven.txt

    info "See $BASEDIR/temp/dependencies_maven.txt"
    success "OK"


    h2 "This might be good in the gradle file of the cps extension:"
    mvn dependency:tree | sed "s/\[INFO\]//g" | grep -v "BUILD SUCCESS" | grep -v "\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-" | grep -v "Total time" | grep -v "Finished at" \
    | grep -v "Scanning for projects" | grep -v "Building dependency-finder" | grep -v "maven-dependency-plugin" | grep -v "dev.galasa:dependency-finder" \
    | grep -v "^[ \t]*$" | sed "s/^[|+ \t\\\-]*//" | sed "s/:compile.*//g" | sed "s/:runtime.*//g" \
    | sed "s/^/    implementation ('/" \
    | sed "s/$/')/" \
    | sed "s/:jar:/:/" \
    | sort | uniq \
    > $BASEDIR/temp/dependencies_maven_imports.txt

    info "See $BASEDIR/temp/dependencies_maven_imports.txt"
    success "OK"


    # mvn dependency:tree | sed "s/\[INFO\]//g" | grep -v "BUILD SUCCESS" | grep -v "\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-" | grep -v "Total time" | grep -v "Finished at" \
    # | grep -v "Scanning for projects" | grep -v "Building dependency-finder" | grep -v "maven-dependency-plugin" | grep -v "dev.galasa:dependency-finder"
}



function list_transient_dependencies_using_gradle {
    cd ${BASEDIR}/*parent
    h2 "This might be good in the bnd file of the cps extension:"
    gradle dev.galasa.cps.etcd:dependencies --configuration runtimeClasspath > $BASEDIR/temp/dependencies.txt
    sed '/Indicates repeated occurrences/,$d' $BASEDIR/temp/dependencies.txt > $BASEDIR/temp/dependencies2.txt
    cd ${BASEDIR}/temp
    csplit $BASEDIR/temp/dependencies2.txt "/Runtime classpath of source set/" > /dev/null
    mv $BASEDIR/temp/xx01 $BASEDIR/temp/dependencies3.txt
    cat $BASEDIR/temp/dependencies3.txt | grep -v "runtimeClasspath - Runtime classpath of source set" \
    | sed "s/^[+ \\|\-]*//" \
    | cut -d ':' -f2,3 \
    | sed "s/:[a-zA-Z0-9.\\-]* -> /:/" \
    | sed "s/ [(][*][)]$//"  \
    | sed "s/:/-/" \
    | grep -v "^[ \t]*$" \
    | sed "s/$/.jar/" \
    | sed "s/^/    /" \
    | sed "s/$/; lib:=true,\\\/" \
    | grep -v "dev.galasa" \
    | grep -v "bcel" \
    | grep -v "commons-io-.*.jar" \
    | grep -v "commons-lang3-.*.jar" \
    | grep -v "commons-logging-" \
    | grep -v "easymock-.*.jar" \
    | grep -v "log4j-api-.*.jar" \
    | grep -v "log4j-core-.*.jar" \
    | grep -v "log4j-slf4j-impl-.*.jar" \
    | grep -v "org.apache.felix.bundlerepository-.*.jar" \
    | grep -v "org.osgi" \
    | sort | uniq \
    > $BASEDIR/temp/dependencies_gradle.txt
    # cat $BASEDIR/temp/dependencies_gradle.txt
# 
    rm $BASEDIR/temp/dependencies2.txt $BASEDIR/temp/dependencies3.txt
    info "See $BASEDIR/temp/dependencies_gradle.txt"
    success "OK"

    cd ${BASEDIR}/*parent
    h2 "This might be good in the gradle file of the cps extension:"
    gradle dev.galasa.cps.etcd:dependencies --configuration runtimeClasspath > $BASEDIR/temp/dependencies.txt
    sed '/Indicates repeated occurrences/,$d' $BASEDIR/temp/dependencies.txt > $BASEDIR/temp/dependencies2.txt
    cd ${BASEDIR}/temp
    csplit $BASEDIR/temp/dependencies2.txt "/Runtime classpath of source set/" > /dev/null
    mv $BASEDIR/temp/xx01 $BASEDIR/temp/dependencies3.txt
    cat $BASEDIR/temp/dependencies3.txt | grep -v "runtimeClasspath - Runtime classpath of source set" \
    | sed "s/^[+ \\|\-]*//" \
    | sed "s/:[a-zA-Z0-9.\\-]* -> /:/" \
    | sed "s/ [(][*][)]$//"  \
    | grep -v "dev.galasa" \
    | grep -v "bcel" \
    | grep -v "commons-io" \
    | grep -v "commons-lang3" \
    | grep -v "commons-logging" \
    | grep -v "easymock" \
    | grep -v "org.apache.felix.bundlerepository" \
    | grep -v "org.osgi" \
    | sed "s/^/     implementation('/" \
    | sed "s/$/')/" \
    | grep -v "     implementation('')" \
    | sort | uniq \
    > $BASEDIR/temp/dependencies_gradle_imports.txt

    # | grep -v "log4j-api" \
    # | grep -v "log4j-core" \
    # | grep -v "log4j-slf4j-impl" \

    info "See $BASEDIR/temp/dependencies_gradle_imports.txt"
    success "OK"
}

create_temp_project
# list_transient_dependencies_using_maven
list_transient_dependencies_using_gradle