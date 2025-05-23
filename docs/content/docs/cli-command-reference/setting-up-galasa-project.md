---
title: "Creating a Galasa project using the command line"
---


You can quickly and easily create a project structure to accommodate your own independent Galasa tests in your local storage by using the Galasa command line interface (Galasa CLI) that is provided with Galasa. 

Use the following information to discover more about the structure of a Galasa project, learn how to create and build your own example project, and understand the purpose of the artifacts that are generated. 

Once you have created and built your Galasa tests, you can run the tests in your local environment. You can run Galasa tests that are written in Java and Galasa tests that are written in Gherkin in your local environment. Find out more in the [Running a test locally](./runs-submit-local.md) topic. 

## Getting started

An easy way to get started with creating and building Galasa tests is by using the `galasactl project create` command. Passing parameters to the command, enable you to build your project using either Maven or Gradle, and allow you to choose your own artifact names and naming conventions for your tests. 

Maven and Gradle are both build tools, which read metadata from files which guide how the code within a module should be built. Maven and Gradle use different formats for these build files. Artifacts are created on disk and can later be built, tested and deployed to a Maven repository and used in the Galasa Ecosystem. Generated artifacts can be embellished and refactored to form the basis of future Galasa tests.


The `galasactl project create` command uses the `--maven` and `--gradle` parameters to allow you to decide which type of build system to use when you create a project. You must specify either the `--maven` flag or the `--gradle` flag when you run the `galasactl project create` command. You can specify both flags to tell the tool to generate Maven build artifacts (pom.xml files) and Gradle files.

The example provided in this topic creates a project containing both Maven and Gradle build infrastructure files by specifying the `--maven` and `--gradle` parameters in the command.  


## A bit about Maven

Maven is _opinionated_, which means that you need to comply with its expectations about how a project and its directories should be organised. When you create a Maven project, you should use the generated structure.

The most visible practical evidence that a project is a Maven project is its pervasive use of `pom.xml` (Project Object Model) files. These XML files contain the magic that allows Maven to manage your project dependencies and build orchestration. 


## A bit about Gradle

The Gradle project structure looks somewhat different to the Maven structure because Gradle projects use `build.gradle`, `bnd.bnd` and `settings.gradle` files rather than `pom.xml` files. 

The `build.gradle` files declare any dependencies that the test code has, and specify the Maven co-ordinates to use when publishing to a Maven repository.  The `bnd.bnd` files define the OSGi Bundles for the test projects and any Managers in the project and the `settings.gradle` file tells Gradle where to look for the dependencies and plug-ins that are required to build the project. 


## Project Structure

A full (parent) Galasa project includes several sub-projects, which can also be known as _modules_, some of which are mandatory and some optional. A parent project can contain the following sub-projects:

- A Managers sub-project, allowing you to extend the provided range of Managers. In practice, if you have no intention of writing a Manager, you can omit this.
- An OBR (OSGi Bundle Repository) sub-project, which is mandatory. Galasa uses the OBR to locate your test project(s) and reason about their interdependencies.
- One or more test sub-projects, that as the name implies, contain the tests themselves. 

The parent project establishes all the dependencies for the sub-projects or modules. It builds all the modules in the order of the dependencies - it builds the Manager module before the test projects that use it.

For simplicity, it is assumed that you will only have one version of a test in production at any one time. However, by establishing different versions of your tests, you can have test streams with different versions of the same test project. For the purposes of the forthcoming example, the version of all projects is set to `0.1.0-SNAPSHOT`. 


## Creating an example project

In the following example we are going to use the Galasa CLI to build a hierarchy of projects, where the parent project _dev.galasa.example.banking_ contains the following modules: 
- Two test sub-projects or modules called _dev.galasa.example.banking.payee_ and _dev.galasa.example.banking.account_ 
- A mandatory OBR sub-project called _dev.galasa.example.banking.obr_.

This setup is designed as an example of how to create a project structure that might ultimately be deployed to a Maven repository, so that your Galasa automation system can find everything it needs to run.

The following syntax examples assume a banking application is being tested. The banking application contains functionality that deals with `payee` and `account` object types, both of which can be manipulated.

On Mac or Unix:

```shell
galasactl project create \
        --package dev.galasa.example.banking \
      	--features payee,account \
   		--force \
		--obr \
		--log - \
		--maven \
		--gradle
```


On Windows (Powershell):

```powershell
galasactl project create `
        --package dev.galasa.example.banking `
      	--features payee,account `
   		--force `
		--obr `
		--log - `
		--maven `
		--gradle
```

where

- ```--package``` is the name of a Java package. This parameter is mandatory. Use the provided example to help you to understand how you might create unique names for your Java test packages. Sections can reflect your company, organisation, the application under test, or any other name-spacing of your choice. The value chosen influences the names of the folders, OSGi bundles, Maven coordinates, and the eventual Java package name in which example tests are created. The name must be all lower-case letters and numbers (`a-z`, `0-9`), with `.` (period) character separators. No part of the name can clash with a Java language reserved word. <br>
- ```--features``` is a comma-separated list of one or more features of the application that you want to test. This parameter is optional, defaulting to `test`. The entries in this list influence the names of some folders, OSGi bundles, Maven coordinates and Java class names of the generated example tests. In the example provided, the banking application under test has a `payee` section and an `account` section which need testing. Creating subcomponents of your test suite to test these features or components of the application creates a naming structure in which your tests can be organised. These names must be lower-case letters and numbers (`a-z`, `0-9`), with no `.` (period) or special characters. No part of the name can clash with a Java language reserved word. <br>
- ```--force``` is an optional flag. If the flag is missing, then any file that the Galasa CLI tool tries to create which already exists causes a failure, and the original file is preserved. If this flag is used, then such files are silently over-written. Use this option carefully to avoid some of your files being over-written, resulting in the loss of some of your data.
- ```--obr``` Creates an OBR project. An OBR (OSGi Bundle Repository) is an index of OSGi bundles. Galasa testcases are built into OSGi bundles which are then connected into an OBR. When you specify the location of the OBR to Galasa, the OBR tells Galasa where the tests are stored. You can create an OBR from scratch, or you can create projects and add them into an existing OBR. For more information about OBRs, see the [Apache Felix](https://felix.apache.org/documentation/subprojects/apache-felix-osgi-bundle-repository.html){target="_blank"} website.

- ```--log -``` sends the trace and logging output that is generated by the tool to the console.
- ```--maven``` creates a project with a Maven project structure.
- ```--gradle``` creates a project with a Gradle project structure.


## Building the example project 

Run the following command to navigate to the parent folder (in this example the _dev.galasa.example.banking_ directory):

```shell
cd dev.galasa.example.banking
```

If you are building the project using Maven, use the following command:

```shell
mvn clean install
```

To build the project with Gradle, use the following command:

```shell
gradle clean build publishToMavenLocal
```

The built artifacts are typically placed in the `~/.m2/repository` in your home directory.


## Understanding the generated artifacts

Running the example Galasa CLI `project create` command with the `--maven` flag specified creates a number of files in the following standard folder structure:

```
.
└── dev.galasa.example.banking
    ├── dev.galasa.example.banking.account
    │   ├── bnd.bnd
    │   ├── build.gradle
    │   ├── pom.xml
    │   └── src
    │       └── main
    │           ├── java
    │           │   └── dev
    │           │       └── galasa
    │           │           └── example
    │           │               └── banking
    │           │                   └── account
    │           │                       ├── TestAccount.java
    │           │                       └── TestAccountExtended.java
    │           └── resources
    │               └── textfiles
    │                   └── sampleText.txt
    ├── dev.galasa.example.banking.obr
    │   ├── build.gradle
    │   └── pom.xml
    ├── dev.galasa.example.banking.payee
    │   ├── bnd.bnd
    │   ├── build.gradle
    │   ├── pom.xml
    │   └── src
    │       └── main
    │           ├── java
    │           │   └── dev
    │           │       └── galasa
    │           │           └── example
    │           │               └── banking
    │           │                   └── payee
    │           │                       ├── TestPayee.java
    │           │                       └── TestPayeeExtended.java
    │           └── resources
    │               └── textfiles
    │                   └── sampleText.txt
    ├── pom.xml
    └── settings.gradle
```


## More about the parent project

The top level folder, which is called `dev.galasa.example.banking` in this example, is the parent project. The parent project is a convenient container in which to hold all of the generated files. In Maven the `pom.xml` in the parent project is used to build all the other generated files. In Gradle, the `settings.gradle` file is used.

Within the example parent project structure there are three generated OSGi bundle sub-projects:

- The payee bundle project, _dev.galasa.example.banking.payee_, which contains two Galasa tests - _TestPayee.java_ and _TestPayeeExtended.java_ - both of which relate to testing the `payee` feature.
- The account test bundle project, _dev.galasa.example.banking.account_, which contains two Galasa tests - _TestAccount.java_ and _TestAccountExtended.java_ - both of which relate to testing the `account` feature.

- An OSGi Bundle Repository [(OBR)](https://felix.apache.org/documentation/subprojects/apache-felix-osgi-bundle-repository.html){target="_blank"} which holds metadata listing the tests that are available in the Galasa test projects.


## More about the test projects

Within each of the Galasa test projects  - `payee` and `account` - you can see the following files and folders:

- A pom.xml file (for use by the Maven build tool)

- A build.gradle file (for use by the Gradle build tool)

- A bnd.bnd file (for use by the Gradle build tool)

- A `src` tree holding source code

- Two java files in each `feature` test project

- A text resource file in each `feature` test project, which is read and used by the tests at run-time.


## About the tests

The _TestAccount.java_ and the _TestPayee.java_ source files show how a Core Manager can be injected into your test class before any test methods being used. 

The _TestAccountExtended.java_ and the _TestPayeeExtended.java_ source files show:

- How to obtain the `run-id` identifier by which an instance of running the test can be known. This can be useful for naming artifacts, logging or otherwise.

- How to read a text file resource which is embedded within the test OSGi bundle. This can be useful for getting data files for use in the testing of the application.

- How logging can be performed to help debug issues in the test code itself.

- How a file that is created by the test run can be captured together with other test results for later viewing.


## Additional notes on the key elements of pom.xml files

The following sections provide a little more information about some of the elements that are found within the various pom.xml files.

### The parent pom.xml file elements

- The `<project>` and `<modelVersion>` elements are standard prologues to a `pom.xml` file.

The following extract from the generated parent pom.xml shows some of the key elements that are described:

```xml
<groupId>dev.galasa.example.banking</groupId>
<artifactId>dev.galasa.example.banking</artifactId>
<version>0.0.1-SNAPSHOT</version>	
<packaging>pom</packaging>
```

- The `<groupId>` is used to group related Maven projects in a Maven repository. It is recommended (but not enforced) that all projects in a [test stream](../manage-ecosystem/test-streams) share the same `groupId`.
- The `<artifactId>` must be unique for each Maven project under a `groupId`. To prevent confusion, you could make it unique across `groupId`s. The `groupId` and `artifactId` can nominally be anything you choose, but if you were to ever consider publishing the project on Maven Central, you would have to ensure that they were unique across Maven Central. Because of this, and to avoid future name collisions, it is conventional to use (reversed) company domain names, which leads to patterns like `dev.galasa.example.banking`.
- The `<version>` in this project is set to `0.1.0-SNAPSHOT`. 
- `<packaging>` indicates what type of Maven project this is - in this case, a `pom` project.

The following extract from the parent pom.xml shows the module elements that are contained within the generated parent pom.xml:

```xml
<modules>
	<module>dev.galasa.example.banking.payee</module>
	<module>dev.galasa.example.banking.account</module>
	<module>dev.galasa.example.banking.obr</module>
</modules>
```	

- `<modules>` details what sub-modules (sub-projects) are contained within this parent project. Usually, when the parent project is built, so are the sub-modules.

Other elements that are contained within the generated parent pom.xml are listed in the following section: 

- `<distributionManagement>` controls where Maven deploys the project when built. A variable is used so that the same project can be built and deployed to different test stream repositories.
- The `<properties>` element specifies properties such as file encoding and Java version numbers. 
- `<dependencyManagement>` establishes the versions of dependencies in all of the sub-modules. A BOM project is provided by the Galasa team that includes the versions of all of the released Managers. Set the version of Galasa you wish to run against, for example 0.20.0, and all the Manager versions are imported.
- `<dependencies>` list all the Managers you wish to make available for your tests and custom Manager if present. You could include `<dependencies>` in each of the sub-modules, but it is easier to maintain the list here.
- `<plugins>` identify the Maven plugins to be used during the build process. The `maven-bundle-plugin` builds OSGi bundles (the Manager and test projects), indicated by `<packaging>bundle</packaging>`. The `galasa-maven-plugin` is used in two ways - to build a test catalog for each bundle project and to build the `<packaging>galasa-obr</packaging>` project.

### The test project pom.xml file elements

- The `<parent>` element signifies that all the properties and dependencies found in the parent pom.xml file are to be used for this project - avoiding duplication and allowing changes to ripple through all sub-projects.
- The `<packaging>` element is set to `bundle` so an OSGi bundle is built instead of a simple JAR.


## The test pom.xml file elements

 - The `<packaging>` element is set to `galasa-obr` which causes the Galasa Maven plugin to build this project.


 ## Importing the example test project into Eclipse

If you want to edit source code using an IDE, you might want to import the projects into your IDE workspace. 

The following steps show you how to import an example test project built using Maven into Eclipse:

1. Launch Eclipse and choose _File > Import..._
1. In the _Select_ dialog, expand _Maven_, choose _Existing Maven Projects_ and click _Next_.
1. Navigate to your root project directory - _dev.galasa.example.banking_ in this case - and follow the remaining prompts to complete the import. If you see a warning or error dialog, opt to resolve the error later.
1. View your set of projects in _Package Explorer_.

Complete the following steps to import an example test project built using Gradle into Eclipse:

1. Launch Eclipse and choose _File > Import..._
1. In the _Select_ dialog, expand _Gradle_, choose _Existing Gradle Projects_ and click _Next_.
1. Navigate to your root project directory - _dev.galasa.example.banking_ in this case - and follow the remaining prompts to complete the import. If you see a warning or error dialog, opt to resolve the error later.
1. View your set of projects in _Package Explorer_.

