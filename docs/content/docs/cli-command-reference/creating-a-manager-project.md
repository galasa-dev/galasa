---
title: "Creating a Galasa manager project"
---

# Creating a Galasa manager project using the command line

A Galasa manager is a reusable component that provides test infrastructure and can inject resources into test classes. If you want to write your own manager — for example, to wrap an in-house API or provide shared test utilities — you can scaffold the project structure using the Galasa CLI.

This guide covers:

- How to create a manager project using `galasactl project create --manager`
- The CLI flags available when creating a manager
- How to combine a manager with test projects in a single command
- The folder structure and Java classes that are generated

If you want to create a standard test project (without a manager), see [Creating a Galasa project using the command line](./setting-up-galasa-project.md).


## Creating a manager project

Use `galasactl project create` with the `--manager` flag to scaffold a manager project:

=== "Linux or macOS"

    ```shell
    galasactl project create \
            --package dev.galasa.example.sample \
            --manager \
            --managerName sample \
            --obr \
            --maven \
            --gradle \
            --log -
    ```

=== "Windows (Powershell)"

    ```powershell
    galasactl project create `
            --package dev.galasa.example.sample `
            --manager `
            --managerName sample `
            --obr `
            --maven `
            --gradle `
            --log -
    ```

If `--managerName` is not specified, the tool derives the manager name from the last segment of the package name. For example, `--package dev.galasa.example.sample` without `--managerName` produces a manager named `sample`.


## Parameters explained

- `--package` (required): The Java package name for the generated project. Influences folder names, OSGi bundle names, Maven coordinates, and Java package declarations. Must use lowercase letters and numbers (`a-z`, `0-9`) with `.` separators, and must not contain Java reserved words.

  Example: `dev.galasa.example.sample`

- `--manager` (required for manager projects): Signals that the project being created is a manager rather than a test project.

- `--managerName` (optional): The short name used to derive generated class names. For example, `--managerName sample` produces `@SampleManager`, `ISampleManager`, `SampleManagerImpl`, and so on. Defaults to the last part of `--package` if omitted. Must be a valid Java identifier.

- `--obr` (optional, but recommended): Generates an OSGi Bundle Repository (OBR) sub-project alongside the manager bundle. An OBR is an index that tells Galasa where to find your bundles.

- `--features` (optional): A comma-separated list of features to test. Defaults to `feature1` if not specified, which means a `feature1` test project is always generated unless you explicitly pass `--features ""` to suppress it.

- `--maven` (optional): Generates Maven build files (`pom.xml`).

- `--gradle` (optional): Generates Gradle build files (`build.gradle`, `bnd.bnd`, `settings.gradle`).

  You must specify at least one of `--maven` or `--gradle`. Both can be specified together to generate files for both build systems.

- `--force` (optional): Overwrites existing files without warning. Use with care.

- `--log -` (optional): Sends tool output to the console.


## Combining manager and test projects

You can create both a manager and test projects in the same command:

=== "Linux or macOS"

    ```shell
    galasactl project create \
            --package dev.galasa.example \
            --manager \
            --managerName example \
            --features payee,account \
            --obr \
            --maven \
            --gradle \
            --log -
    ```

=== "Windows (Powershell)"

    ```powershell
    galasactl project create `
            --package dev.galasa.example `
            --manager `
            --managerName example `
            --features payee,account `
            --obr `
            --maven `
            --gradle `
            --log -
    ```

This creates:

- A manager bundle: `dev.galasa.example.manager`
- Test projects for each feature: `dev.galasa.example.payee`, `dev.galasa.example.account`
- A single OBR sub-project that includes both the manager and test bundles


## Understanding the generated files

The command generates the following folder structure for a standalone manager project (using `--package dev.galasa.example.sample --managerName sample --obr --maven --gradle`):

```
.
└── dev.galasa.example.sample
    ├── dev.galasa.example.sample.feature1
    │   ├── bnd.bnd
    │   ├── build.gradle
    │   ├── pom.xml
    │   └── src
    │       └── main
    │           ├── java
    │           │   └── dev
    │           │       └── galasa
    │           │           └── example
    │           │               └── sample
    │           │                   └── feature1
    │           │                       ├── TestFeature1.java
    │           │                       └── TestFeature1Extended.java
    │           └── resources
    │               └── textfiles
    │                   └── sampleText.txt
    ├── dev.galasa.example.sample.manager
    │   ├── bnd.bnd
    │   ├── build.gradle
    │   ├── pom.xml
    │   └── src
    │       ├── main
    │       │   └── java
    │       │       └── dev
    │       │           └── galasa
    │       │               └── example
    │       │                   └── sample
    │       │                       ├── ISampleManager.java
    │       │                       ├── ISampleResource.java
    │       │                       ├── SampleManagerException.java
    │       │                       ├── SampleResource.java
    │       │                       └── internal
    │       │                           ├── SampleManagerField.java
    │       │                           ├── SampleManagerImpl.java
    │       │                           ├── SampleResourceImpl.java
    │       │                           ├── SampleResourceManagement.java
    │       │                           └── properties
    │       │                               ├── SampleExampleProperty.java
    │       │                               └── SamplePropertiesSingleton.java
    │       └── test
    │           └── java
    │               └── dev
    │                   └── galasa
    │                       └── example
    │                           └── sample
    │                               └── internal
    │                                   └── SampleManagerImplTest.java
    ├── dev.galasa.example.sample.obr
    │   ├── build.gradle
    │   └── pom.xml
    ├── pom.xml
    └── settings.gradle
```


## Generated Java classes

The generated manager follows the patterns used by official Galasa managers. Each class has a defined role:

| Class | Role |
|-------|------|
| `ISampleManager` | Public API interface for the manager, exposed to test code |
| `ISampleResource` | Public API interface for resources provisioned by the manager |
| `SampleResource.java` | Annotation used in test classes to inject a resource (e.g., `@SampleResource ISampleResource sampleResource;`) |
| `SampleManagerException` | Custom checked exception for errors originating from this manager |
| `SampleManagerImpl` | Internal implementation; contains lifecycle methods called by the Galasa framework |
| `SampleManagerField` | Internal meta-annotation applied to `@SampleResource`; used by the Galasa framework to discover which annotations belong to this manager. The actual field injection is performed by `SampleManagerImpl.provisionGenerate()`. |
| `SampleResourceImpl` | Internal implementation of the resource |
| `SampleResourceManagement` | Internal — manages the lifecycle of provisioned resources |
| `SampleExampleProperty` | Internal — defines a CPS (Configuration Properties Store) property for the manager |
| `SamplePropertiesSingleton` | Internal — singleton that provides access to the manager's CPS properties |
| `SampleManagerImplTest` | Unit test for the manager implementation |

### Manager lifecycle methods

`SampleManagerImpl` contains stub implementations of the key lifecycle methods called by the Galasa framework during a test run:

| Method | When it is called |
|---|---|
| `initialise()` | When the manager is first loaded by the framework |
| `youAreRequired()` | When the framework determines a test needs this manager |
| `provisionGenerate()` | Before test execution — provision and return resources |
| `provisionDiscard()` | After test execution — clean up provisioned resources |


## Building the generated project

Navigate to the parent folder:

```shell
cd dev.galasa.example.sample
```

=== "Maven"

    ```shell
    mvn clean install
    ```

=== "Gradle"

    ```shell
    gradle clean build publishToMavenLocal
    ```

Built artifacts are placed in `~/.m2/repository` in your user home directory.


## Using the manager as a dependency

Once your manager is built and published to your local Maven repository or a remote Maven repository, you can add it as a dependency in any test project.

=== "Maven"

    Add the manager bundle to the `<dependencies>` section of your test project's `pom.xml`:

    ```xml
    <dependency>
        <groupId>dev.galasa.example.sample</groupId>
        <artifactId>dev.galasa.example.sample.manager</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
    ```

=== "Gradle"

    Add the manager bundle to the `dependencies` block in your test project's `build.gradle`:

    ```groovy
    dependencies {
        implementation 'dev.galasa.example.sample:dev.galasa.example.sample.manager:0.0.1-SNAPSHOT'
    }
    ```

You can then inject the manager into your test class using the generated annotation:

```java
@Test
public class MyTest {

	@SampleResource
	public ISampleResource sampleResource;

    @Test
    public void myTest() throws Exception {
        // use sampleResource here
    }
}
```
