---
title: "Creating a Galasa project"
---

# Creating a Galasa project using the command line

You can quickly create a project structure for your Galasa tests using the Galasa command line interface (Galasa CLI). This sets up all the necessary files and folders in your local storage.

This guide explains:

- The structure of a Galasa project
- How to create and build an example project
- The purpose of the generated files

Once you have created and built your project, you can run tests locally. Both Java and Gherkin tests are supported. Learn more in [Running a test locally](./runs-submit-local.md).


## Getting started

The easiest way to create a Galasa project is with the `galasactl project create` command. You can:

- Choose between Maven or Gradle as your build tool
- Customize artifact names and test naming conventions

**Build tools:** Maven and Gradle are build automation tools that compile your code and manage dependencies. They use different file formats:

- Maven uses `pom.xml` files
- Gradle uses `build.gradle`, `bnd.bnd`, and `settings.gradle` files

Both tools create artifacts (compiled files) that can be built, tested, and deployed to a Maven repository for use in the Galasa Ecosystem.

**Choosing a build tool:** You must specify either `--maven` or `--gradle` when running `galasactl project create`. You can specify both flags to generate files for both build systems.

The example in this guide creates a project with both Maven and Gradle files.


## A bit about Maven

Maven is _opinionated_, meaning it expects your project to follow a specific structure. When you create a Maven project, use the generated folder layout.

Maven projects use `pom.xml` (Project Object Model) files throughout. These XML files manage your project dependencies and build process.

??? info "Key elements in pom.xml files"

    #### Parent pom.xml elements

    Standard Maven project elements:

    ```xml
    <groupId>dev.galasa.example.banking</groupId>
    <artifactId>dev.galasa.example.banking</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>
    ```

    - `<groupId>`: Groups related Maven projects in a repository. All projects in a [test stream](../manage-ecosystem/test-streams.md) should share the same `groupId`.

    - `<artifactId>`: Must be unique for each project under a `groupId`. Convention uses reversed domain names (like `dev.galasa.example.banking`) to avoid naming conflicts.

    - `<version>`: Set to `0.1.0-SNAPSHOT` in this example.

    - `<packaging>`: Set to `pom` for parent projects.

    Sub-modules are listed in the parent pom.xml:

    ```xml
    <modules>
        <module>dev.galasa.example.banking.payee</module>
        <module>dev.galasa.example.banking.account</module>
        <module>dev.galasa.example.banking.obr</module>
    </modules>
    ```

    Other important parent pom.xml elements:

    - `<distributionManagement>`: Controls where Maven deploys built projects. Uses variables so the same project can deploy to different test stream repositories.

    - `<properties>`: Specifies file encoding and Java version numbers.

    - `<dependencyManagement>`: Sets dependency versions for all sub-modules. Uses a BOM (Bill of Materials) project from the Galasa team that includes all released Manager versions.

    - `<dependencies>`: Lists all Managers available to your tests. Maintaining this list in the parent pom.xml is easier than duplicating it in each sub-module.

    - `<plugins>`: Identifies Maven plugins for the build process:
    - `maven-bundle-plugin`: Builds OSGi bundles (indicated by `<packaging>bundle</packaging>`)
    - `galasa-maven-plugin`: Builds test catalogs and OBR projects (indicated by `<packaging>galasa-obr</packaging>`)


    #### Test project pom.xml elements

    - `<parent>`: References the parent pom.xml, inheriting all properties and dependencies. This avoids duplication and ensures changes apply to all sub-projects.

    - `<packaging>`: Set to `bundle` to build an OSGi bundle instead of a simple JAR.


    #### OBR pom.xml elements

    - `<packaging>`: Set to `galasa-obr` to trigger the Galasa Maven plugin to build the OBR project.


## A bit about Gradle

Gradle projects have a different structure than Maven projects:

- `build.gradle` files declare dependencies and Maven coordinates for publishing
- `bnd.bnd` files define OSGi Bundles for test projects and Managers
- `settings.gradle` files tell Gradle where to find required dependencies and plugins

??? info "Key elements in Gradle files"

    #### Parent settings.gradle elements

    The parent `settings.gradle` file defines the project structure and sub-modules:

    ```gradle
    pluginManagement {
        repositories {
            mavenLocal()
            gradlePluginPortal()
            mavenCentral()
        }
    }

    include 'dev.galasa.example.banking.account'
    include 'dev.galasa.example.banking.payee'
    include 'dev.galasa.example.banking.obr'
    ```

    - `pluginManagement`: Tells Gradle where it should look to find the plugins that are part of the build
    - `include`: Lists all sub-modules (sub-projects) that are part of the build

    #### Test project build.gradle elements

    The `build.gradle` file in test projects declares dependencies and publishing configuration:

    ```gradle
    plugins {
        id 'java'
        id 'maven-publish'
        id 'dev.galasa.tests' version '0.48.0'
        id 'biz.aQute.bnd.builder' version '6.4.0'
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    group = 'dev.galasa.example.banking'
    version = '0.0.1-SNAPSHOT'

    dependencies {
        implementation platform('dev.galasa:galasa-bom:0.48.0')

        implementation 'dev.galasa:dev.galasa'
        implementation 'dev.galasa:dev.galasa.framework'
        implementation 'dev.galasa:dev.galasa.core.manager'
        implementation 'dev.galasa:dev.galasa.artifact.manager'
        implementation 'commons-logging:commons-logging'
        implementation 'org.assertj:assertj-core'
    }

    publishing {
        publications {
            maven(MavenPublication) {
                from components.java
            }
        }
    }
    ```

    Key elements:

    - `plugins`: Applies the Galasa Java plugin which configures the build for Galasa tests
    - `version`: Sets the project version (should match across all modules)
    - `dependencies`: Lists all Managers and libraries required by the tests

    #### Test project bnd.bnd elements

    The `bnd.bnd` file defines the OSGi bundle metadata:

    ```
    Bundle-Version: 0.0.1-SNAPSHOT
    Bundle-Name: dev.galasa.example.banking.account
    Import-Package: *
    ```

    Key elements:

    - `Bundle-Name`: Human-readable name for the bundle
    - `Bundle-Version`: Version of the bundle
    - `Import-Package`: List of packages to import from other bundles

    #### OBR build.gradle elements

    The OBR project `build.gradle` file uses the Galasa OBR plugin:

    ```gradle
    plugins {
        id 'base'
        id 'maven-publish'
        id 'dev.galasa.obr' version '0.48.0'
        id 'dev.galasa.testcatalog' version '0.48.0'
    }

    group = 'dev.galasa.example.banking'
    version = '0.0.1-SNAPSHOT'

    dependencies {
        bundle project(':dev.galasa.example.banking.account')
        bundle project(':dev.galasa.example.banking.payee')
    }

    def testcatalog = file('build/testcatalog.json')
    def obrFile = file('build/galasa.obr')

    tasks.withType(PublishToMavenLocal) { task ->
        task.dependsOn genobr
        task.dependsOn mergetestcat
    }

    publishing {
        publications {
            maven(MavenPublication) {
                artifact obrFile
                artifact (testcatalog) {
                    classifier "testcatalog"
                    extension "json"
                }
            }
        }
    }
    ```

    Key elements:

    - `plugins`: Applies the Galasa OBR plugin which builds the OSGi Bundle Repository
    - `dependencies`: Lists all test bundles to include in the OBR using the `bundle` configuration


## Project Structure

A complete Galasa project contains several sub-projects (also called _modules_):


- **Managers sub-project** (optional): Lets you extend the provided Managers. You can omit this if you do not plan to write custom Managers.

- **OBR sub-project** (mandatory): An OSGi Bundle Repository (OBR) that helps Galasa locate your test projects and understand their dependencies.

- **Test sub-projects** (one or more): Contain your actual tests.

The parent project manages dependencies for all sub-projects and builds them in the correct order (for example, building Managers before the tests that use them).

**Versioning:** For simplicity, this guide assumes you will have one version of a test in production at a time. However, you can maintain different versions for different test streams. In this example, all projects use version `0.1.0-SNAPSHOT`.


## Creating an example project

This example creates a project hierarchy where the parent project _dev.galasa.example.banking_ contains:

- Two test sub-projects: _dev.galasa.example.banking.payee_ and _dev.galasa.example.banking.account_
- One OBR sub-project: _dev.galasa.example.banking.obr_

This structure can be deployed to a Maven repository so your Galasa automation system can find everything needed to run tests.

The example assumes you are testing a banking application with `payee` and `account` features.

=== "Linux or macOS"

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

=== "Windows (Powershell)"

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

**Parameters explained:**

- `--package` (required): The Java package name. This influences folder names, OSGi bundle names, Maven coordinates, and Java package names. Must be lowercase letters and numbers (`a-z`, `0-9`) with `.` separators. Cannot use Java reserved words.

  Example: `dev.galasa.example.banking` could represent your company (dev.galasa), application type (example), and application name (banking).

- `--features` (optional): Comma-separated list of application features to test. Defaults to `test` if not specified. Influences folder names, OSGi bundles, Maven coordinates, and Java class names. Must be lowercase letters and numbers (`a-z`, `0-9`) with no special characters. Cannot use Java reserved words.

  Example: `payee,account` creates separate test modules for these banking features.

- `--force` (optional): Overwrites existing files without warning. Use carefully to avoid data loss. Without this flag, the command fails if files already exist.

- `--obr` (required): Creates an OBR (OSGi Bundle Repository) project. An OBR is an index of OSGi bundles that tells Galasa where tests are stored. Learn more at [Apache Felix](https://felix.apache.org/documentation/subprojects/apache-felix-osgi-bundle-repository.html){target="_blank"}.

- `--log -`: Sends tool output to the console.

- `--maven`: Creates Maven project files.

- `--gradle`: Creates Gradle project files.


## Building the example project

Navigate to the parent folder:

```shell
cd dev.galasa.example.banking
```

=== "Maven"

    ```shell
    mvn clean install
    ```

=== "Gradle"

    ```shell
    gradle clean build publishToMavenLocal
    ```

Built artifacts are placed in `~/.m2/repository` in your home directory.


## Understanding the generated files

The `galasactl project create` command generates this folder structure:

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


## The parent project

The top-level folder (`dev.galasa.example.banking`) is the parent project. It is a container for all generated files.

The parent project contains three OSGi bundle sub-projects:

- **dev.galasa.example.banking.payee**: Contains two tests (_TestPayee.java_ and _TestPayeeExtended.java_) for the `payee` feature

- **dev.galasa.example.banking.account**: Contains two tests (_TestAccount.java_ and _TestAccountExtended.java_) for the `account` feature

- **dev.galasa.example.banking.obr**: An [OBR](https://felix.apache.org/documentation/subprojects/apache-felix-osgi-bundle-repository.html){target="_blank"} containing metadata about available tests


## The test projects

Each test project (`payee` and `account`) contains:

- `pom.xml` (for Maven)
- `build.gradle` (for Gradle)
- `bnd.bnd` (for Gradle)
- `src` folder with source code
- Two Java test files
- A text resource file used by tests at runtime


## About the generated tests

**Basic tests** (_TestAccount.java_ and _TestPayee.java_):

- Show how to inject a Core Manager into your test class

**Extended tests** (_TestAccountExtended.java_ and _TestPayeeExtended.java_) demonstrate:

- Getting the `run-id` identifier for naming artifacts and logging
- Reading text file resources embedded in the test OSGi bundle
- Using logging to debug test code
- Capturing test-created files with other test results
