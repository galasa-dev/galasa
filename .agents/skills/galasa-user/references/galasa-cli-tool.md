---
name: galasa-cli-tool
description: Provides information about how to use the Galasa CLI tool to set up a local environment, create Galasa test projects, and run Galasa tests.
---

## Getting started

- The Galasa CLI tool, called `galasactl` is installed on this machine and is used to run Galasa tests.
- You can check that the Galasa CLI tool is installed by running `galasactl --version`.
- For any `galasactl` command that is mentioned in this file, you can find the corresponding reference information for that command at - https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/modules/cli/docs/generated/{COMMAND_NAME}.md/ (where `{COMMAND_NAME}` is the name of the command where any spaces are replaced with `_`)

**Note**: If the Galasa CLI tool is not installed, you should direct the user to the [Installing the Galasa CLI online](https://galasa.dev/docs/cli-command-reference/installing-cli-tool/) documentation page, or the [Installing the Galasa CLI offline](https://galasa.dev/docs/cli-command-reference/installing-offline/) page if they do not have access to the internet.

### Initialising the local environment

**Note**: The `~` character represents the current user's home directory. This can be retrieved by running `echo $HOME`, where `$HOME` is the environment variable that points to the current user's home directory.

- Before creating a Galasa project, make sure that the `~/.galasa` directory exists and is populated with the following files:
  - `bootstrap.properties`: A file containing properties to configure the core framework
  - `cps.properties`: A file containing key-value properties to configure test environments
  - `dss.properties`: A file containing key-value properties that are set dynamically at runtime. **IMPORTANT**: You **MUST NOT** edit this file under any circumstances
  - `credentials.properties` file: A file containing key-value properties to configure the credentials used to access your configured test systems
- If the `~/.galasa` directory does not exist, run the `galasactl local init` command to create it

### Creating a Galasa project

- Create a new Galasa project in the current directory by running the `galasactl project create` command. The command requires the following flags to be passed in:
  -  `--package {PACKAGE_NAME}`: The name of the Java package to be used for the Galasa project. This **MUST** be a valid Java package name, like `dev.galasa.example`.
  -  `--features {FEATURES_TO_TEST}`: One or more application features to be tested in the Galasa project. If multiple features are specified, the `--features` command can be supplied with commas to separate different features (e.g. `--features customer,account`).
  -  `--gradle` or `--maven`: The build tool to be used for the Galasa project, either Gradle or Maven. Only one of these flags is needed. Prefer Gradle wherever possible.
  -  `--obr`: Tells Galasa to create an OBR project

**Note**: OBR (OSGi Bundle Repository) projects package test bundles for distribution and execution. The `--obr` flag is recommended for most projects.

- If a user wants to create a Galasa project in a different directory, you should navigate to that directory before running the `galasactl project create` command.
- If a user asks to create a single test suite, do not create multiple features. Instead, create a single feature and add all the tests to that feature as a single test class.
- Do **NOT** use the `--gherkin` flag. This flag is reserved for Gherkin-based projects.

**IMPORTANT**: If a user does not provide a package name or features, ask them to give you this information. **NEVER** expect the user to provide the Galasa CLI command themselves.

For example, running the command `galasactl project create --package dev.galasa.example --features account --obr --gradle`, the created project will have the following structure:
```
.
└── dev.galasa.example
    ├── dev.galasa.example.account
    │   ├── src
    │   │   └── main
    │   │       ├── resources
    │   │       |   └── textfiles
    │   │       |       └── sampleText.txt
    │   │       └── java
    │   │           └── dev
    │   │               └── galasa
    │   │                   └── example
    │   │                       └── account
    │   │                           ├── TestAccount.java
    │   │                           └── TestAccountExtended.java
    │   └── build.gradle
    │   └── bnd.bnd
    └── dev.galasa.example.account.obr
        └── build.gradle
```

Where:
- `dev.galasa.example` is the root of the project.
- `dev.galasa.example.account` is the name of the test project for the `account` feature.
- `dev.galasa.example.account.obr` is the name of the OBR project.
- `src/main/resources/textfiles/sampleText.txt` is a sample text file that is used by the `TestAccountExtended` class.
- `dev.galasa.example.account.TestAccount.java` is a simple Galasa test class for the `account` feature.
- `dev.galasa.example.account.TestAccountExtended.java` is a more in-depth Galasa test class for the `account` feature.
- `build.gradle` is the Gradle build file for the project.
- `bnd.bnd` is the BND file for the project.
- `dev.galasa.example.account.obr/build.gradle` is the Gradle build file for the OBR project.


**IMPORTANT**: The `Test{FEATURE}Extended` and `Test{Feature}` classes can be deleted and replaced with your own test classes, and the `textfiles` directory can also be deleted. They simply serve as initial templates for a Galasa project.

**IMPORTANT**: **ALWAYS** read the created project structure to make sure that all of the expected files were successfully created.

If the user asks to create a Galasa project with a specific test class, you **MUST** replace the templated test class files and associated resources with a new test class that the user asked for.

## Running Galasa tests

To run Galasa tests in a local Java Virtual Machine (JVM), you can use the `galasactl runs submit local` command. This command **MUST** have the following flags supplied to it:
- `--class {CLASS_NAME}`: The fully qualified name of the test class to be run in the form `{BUNDLE_NAME}/{CLASS_NAME}`, where `{BUNDLE_NAME}` is the name of the bundle that contains the test class and `{CLASS_NAME}` is the fully qualified name of the test class. The `{BUNDLE_NAME}` can be found in the `Bundle-Name` manifest header in the test project's `bnd.bnd` file.
  - Example `bnd.bnd` content: `Bundle-Name: dev.galasa.example.account`
  - Example: `--class dev.galasa.example.account/dev.galasa.example.account.TestAccount`
- `--obr {OBR_COORDINATES}`: A list of maven coordinates of the OBR bundles which refer to your test class bundles. The format of this flag is 'mvn:${GROUP_ID}/${ARTIFACT_ID}/${VERSION}/obr', where '{GROUP_ID}' and `{VERSION}` can be found in the `build.gradle` file of the OBR project, and `{ARTIFACT_ID}` is typically the OBR project's directory name. Multiple instances of this flag can be used to describe multiple OBRs.
  - Example: `--obr mvn:dev.galasa.example/dev.galasa.example.obr/0.0.1-SNAPSHOT/obr`
- `--log -`: Output log messages to the terminal instead of a log file.

Optionally, the `--trace` flag can be supplied if you would like to enable trace-level logging for the test run.

- If a user would like to run specific test methods, the `--methods` flag can be supplied any number of times with the name of the test method or test methods to run. For example: `--methods testMethod1 --methods testMethod2`

- **IMPORTANT**: Every Galasa test run is assigned a unique run name, in the form `XY` where `X` is a single capital letter (example: `L` for local) and `Y` is a number (example: `12`). You **MUST** retain knowledge of this run name for context in case the user want information about the test run's result.

**IMPORTANT**: If a user does not provide enough information for you to fill in the required flags, ask them to give you this information. **NEVER** expect the user to provide the Galasa CLI command themselves.

## Viewing Galasa test results

- Galasa test runs are saved in a local Result Archive Store (RAS), which is found at `~/.galasa/ras` by default.
- Inside the `~/.galasa/ras` folder, you will find folders corresponding to all of the test runs that have previously been executed.
- Inside each test run folder, you will find the following files:
    - `structure.json`: The structure of the test run and its details, including:
      - Test run name (e.g., `L12`)
      - Test run ID (unique identifier)
      - Start and end times
      - Test run status: `Passed`, `Failed`, `EnvFail` (environment failure), or `Ignored`
      - Test methods executed and their individual results
    - `run.log`: The log file for the test run.
    - `artifacts/`: A directory containing artifacts associated with the test run.
    - `artifacts.properties`: A file containing key-value pairs, where the keys correspond to the individual artifact paths that have been saved and the values correspond to the artifact's content type.