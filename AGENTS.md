# AGENTS.md

This file provides guidance to agents when working with Galasa.

# IMPORTANT

**When starting ANY task, you MUST**:
- Read this AGENTS.md first
- Read the Galasa Javadoc at `docs/content/docs/reference/javadoc`

## What is Galasa?

Galasa is an open source **deep integration testing framework** designed for testing complex, interconnected enterprise systems. Unlike unit tests that test individual components in isolation, Galasa specializes in:

- **Integration Testing**: Testing how multiple systems work together (e.g., CICS, z/OS, databases, web services)
- **End-to-End Testing**: Validating complete business workflows across multiple platforms
- **Mainframe Testing**: First-class support for z/OS, CICS, and other mainframe technologies
- **Automated Testing**: Running tests automatically without manual intervention

Details about the project and how to get started can be found at `docs/content/docs/cli-command-reference/index.md`

# Developing Galasa

The following sections describe how to develop Galasa from this repository.

## Modules

The Galasa project is structured into modules for each component of Galasa. The following modules can be found in the `modules` directory:
- `platform`: A Gradle platform listing dependencies used in Galasa
- `wrapping`: Dependencies that are not originally OSGi bundles, which are wrapped into OSGi bundles
- `buildutils`: CLI tools used in Galasa's build process
- `cli`: The Galasa CLI tool code
- `extensions`: Technology-specific implementations of Galasa data stores (e.g. configuration store, credentials store, auth store, etc.)
- `framework`: The core Galasa framework code
- `managers`: The Galasa managers for use by Galasa tests
- `gradle`: Gradle plugins used to build Gradle-based Galasa test projects
- `maven`: Maven plugins used to build Maven-based Galasa test projects
- `obr`: The Galasa project's OSGi Bundle Repository (OBR), which lists the OSGi bundles that are part of the Galasa project
- `ivts`: IVTs used to test Galasa projects

The following directories exist at the root-level of the project:
- `docs`: The user-facing Galasa documentation
- `developer-docs`: Developer-facing documentation for Galasa (not public)
- `tools`: The Galasa project's build scripts and tools
- `.github`: The project's GitHub configuration and CI configuration

## Building the Galasa codebase

To build this project, you can run the `./tools/build-locally.sh` script, which will then invoke the `build-locally.sh` scripts for each of the modules in the correct order.

# Using Galasa

The following sections describe how to use Galasa for integration testing. If a task requires you to use Galasa (e.g. create a project, write a test, etc.), you **MUST**:

- Read the Galasa managers documentation at `docs/content/docs/managers`
- Read the Galasa FAQs at `docs/content/docs/faqs`
- Read the Galasa CLI tool reference at `docs/content/docs/reference/cli-syntax`

## Getting started

- The Galasa CLI tool, called `galasactl` is installed on this machine and is used to run Galasa tests.
- You **MUST** check that the Galasa CLI tool is installed by running `galasactl --version`.
- For any `galasactl` command that is mentioned in this AGENTS.md file, you can find the corresponding reference information for that command at - `docs/content/docs/reference/cli-syntax/{COMMAND_NAME}.md` (where `{COMMAND_NAME}` is the name of the command where spaces replaced with `_` , like `docs/content/docs/reference/cli-syntax/galasactl_runs_submit.md)

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

### Galasa Test Class Structure

Galasa projects are Java projects. The test classes are `.java` files that follow this structure:

```java
package {PACKAGE_NAME}.{FEATURE_NAME};

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.apache.commons.logging.Log;

import dev.galasa.Before;
import dev.galasa.BeforeClass;
import dev.galasa.AfterClass;
import dev.galasa.After;
import dev.galasa.Summary;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;

/**
 * Javadoc describing the test purpose and scope.
 */
@Summary("Brief description of what this test suite validates")
@Test
public class YourTestClass {

    @Logger
    public Log logger;

    @BeforeClass
    public void setupEnvironment() throws Exception {
        // Setup expensive resources here
    }

    @Test
    public void testSomething() throws Exception {
        // Test implementation
    }

    @Test
    public void testSomethingElse() throws Exception {
        // Another test implementation
    }

    @Before
    public void beforeEachTest() throws Exception {
        // Setup before each test
    }

    @After
    public void afterEachTest() throws Exception {
        // Cleanup after each test
    }

    @AfterClass
    public void cleanup() throws Exception {
        // Final cleanup
    }
}
```

Key Points:
- The `@Test` annotation **MUST** be used to mark the test class as a Galasa test suite.
- The `@Test` annotation **MUST** be used to mark the test methods as Galasa tests.
- You **MUST** use the AssertJ library for assertions. **ALWAYS** include a message in assertions, like:
    ```java
    assertThat(condition).as("Descriptive message").isTrue();
    ``` 
- If you are writing a test suite that requires an environment to be set up for test methods, you can use the `@BeforeClass` and `@AfterClass` annotations to mark the methods that set up and clean up the environment. Otherwise, these methods can be omitted.
- The `@Before` and `@After` annotations can be used to mark methods that are run before and after each test method, respectively. These are not required and can be omitted.
- **ALWAYS** remove any unused imports from the test class.

## Using Galasa Managers

- Galasa managers provide interfaces to work with various technologies. For example, the Galasa z/OS manager can be used to connect to z/OS systems and the Galasa Docker manager can be used to manipulate Docker containers.
- Refer to `modules/managers/galasa-managers-parent` for the most up-to-date list of managers that Galasa offers.

Core managers provided by the Galasa framework:
- z/OS Managers: z/OS Batch, z/OS Console, z/OS File, z/OS Program, z/OS TSO, z/OS UNIX
- CICS TS Managers: CICS Terminal, CICS Region, CICS Resource
- HTTP Manager: HTTP client functionality
- Artifact Manager: Test artifact management
- Core Manager: Core Galasa functionality

### Injecting Managers into Test Classes

Managers are injected into test classes using annotations. Example:

```java
@ZosImage(imageTag = "PRIMARY")
public IZosImage zosImage;

@Zos3270Terminal(imageTag = "PRIMARY")
public ITerminal terminal;

// You can customize terminal size if needed (default is 24x80)
@Zos3270Terminal(imageTag = "PRIMARY", primaryColumns = 100, primaryRows = 30)
public ITerminal largeTerminal;

@HttpClient
public IHttpClient httpClient;

@CicsRegion(cicsTag = "PRIMARY")
public ICicsRegion cics;

@CicsTerminal(cicsTag = "PRIMARY")
public ICicsTerminal terminal;
```

The `imageTag` and `cicsTag` parameters reference CPS properties that define target systems.

### Terminal Interaction: Timing and Best Practices

When using the `ITerminal` interface from the z/OS 3270 manager or the `ICicsTerminal` interface from the CICS TS manager, understanding timing and keyboard states is critical for writing reliable tests.

#### Understanding Keyboard Lock States

3270 terminals have two keyboard states:
- **Locked**: Keyboard is locked while the terminal processes a command or waits for the host to respond
- **Unlocked**: Keyboard is ready to accept input

**Key actions that lock the keyboard:**
- `enter()` - Submits data to the host
- `clear()` - Clears the screen
- `pf3()`, `pf5()`, `pf10()`, etc. - Function key presses
- Any action that sends data to the host

**Critical Rule:** Always call `waitForKeyboard()` after any action that locks the keyboard.

#### Method Chaining Pattern

Always chain terminal method calls rather than invoking them on separate lines:

```java
// BAD - Don't do this
terminal.type("Hello, World!");
terminal.enter();
terminal.waitForKeyboard();

// GOOD - Chain the calls
terminal.type("Hello, World!").enter().waitForKeyboard();
```

#### Timing Best Practices

**1. Wait for Keyboard After Commands**

Always use `waitForKeyboard()` after commands that lock the keyboard:

```java
// Clearing the screen
terminal.clear().waitForKeyboard();

// Submitting data
terminal.type("1").enter().waitForKeyboard();

// Pressing function keys
terminal.pf3().waitForKeyboard();
```

**2. Wait for Screen Content to Load**

`waitForKeyboard()` ensures the keyboard is unlocked, but the screen content may still be loading. Use `waitForTextInField()` to ensure specific content has appeared:

```java
// Navigate to a screen and wait for it to fully load
terminal.type("OMEN")
    .enter()
    .waitForKeyboard()
    .waitForTextInField("Select an option");  // Wait for menu to appear
```

**3. Common Timing Mistakes to Avoid**

- ❌ Not waiting for keyboard after `enter()`, `clear()`, or PF keys
- ❌ Proceeding to next action before screen content loads
- ❌ Not verifying expected text appears before interacting with fields

```java
// WRONG - May fail if screen hasn't loaded
terminal.type("1").enter().waitForKeyboard();
terminal.type("1234").enter().waitForKeyboard();  // May type into wrong field!

// CORRECT - Verify screen loaded before proceeding
terminal.type("1").enter().waitForKeyboard()
    .waitForTextInField("CUSTOMER NUMBER");  // Ensure we're on the right screen
terminal.type("1234").enter().waitForKeyboard();
```

#### Field Navigation and Interaction

**Moving Between Fields:**

```java
// Tab to next field
terminal.tab();

// Back tab to previous field
terminal.backTab();

// Position cursor at a specific field by its label
terminal.positionCursorToFieldContaining("Customer Number");
```

**Typical field interaction pattern:**

```java
// Navigate to field, enter data, and submit
terminal.positionCursorToFieldContaining("Customer Number")
    .type("1234")
    .enter()
    .waitForKeyboard()
    .waitForTextInField("Customer lookup successful");
```

#### Extracting Data from Screens

**CRITICAL: Capitalization Matters**

When a user provides expected values or field names in quotes (like "Customer Number", "Sort Code", or "Mr Galasa Tester"), you **MUST** adhere to the exact capitalization of letters in the quotes. Incorrect capitalization will cause assertions to fail or field lookups to return incorrect results.

Examples:
- If the user says the field is called "Customer Number", use `"Customer Number"` (not `"customer number"` or `"CUSTOMER NUMBER"`)
- If the user says the expected value is "Sort Code", use `"Sort Code"` (not `"sort code"` or `"SORT CODE"`)
- If the user says the name should be "Mr Galasa Tester", use `"Mr Galasa Tester"` (not `"mr galasa tester"`)

**1. Retrieving Labeled Field Values**

Use `retrieveFieldTextAfterFieldWithString()` to get the value of a labeled field:

```java
// Get the value after a label (works even if on different lines)
String id = terminal.retrieveFieldTextAfterFieldWithString("ID");
assertThat(id.trim()).as("ID should match").isEqualTo("987654");

String name = terminal.retrieveFieldTextAfterFieldWithString("Customer Name");
assertThat(name.trim()).as("Name should match").isEqualTo("Mr Galasa Tester");
```

**Important:** Always use `.trim()` when comparing field values, as they may contain leading/trailing whitespace.

**2. Checking for Messages or Unlabeled Text**

Use `retrieveScreen()` to get the entire screen content as a string:

```java
// Check for success/error messages
String screen = terminal.retrieveScreen();
assertThat(screen).as("Success message should appear").contains("Customer lookup successful");

// Check for error messages
assertThat(screen).as("Should not show error").doesNotContain("ERROR");
```

**3. Verifying Screen State**

Before interacting with a screen, verify you're on the expected screen:

```java
// Verify we're on the customer display screen
terminal.waitForTextInField("Display Customer");

// Or check for specific prompt text
terminal.waitForTextInField("Provide a CUSTOMER number");
```

#### Complete Interaction Example

Here's a complete example showing proper timing and verification:

```java
@Test
public void testCustomerLookup() throws Exception {
    // Navigate to customer display screen
    terminal.type("1")                              // Select option 1
        .enter()                                    // Submit
        .waitForKeyboard()                          // Wait for keyboard to unlock
        .waitForTextInField("CUSTOMER NUMBER");     // Verify screen loaded
    
    // Enter customer number
    terminal.type("1234")                           // Type customer number
        .enter()                                    // Submit
        .waitForKeyboard()                          // Wait for keyboard to unlock
        .waitForTextInField("Customer lookup successful");  // Verify success
    
    // Extract and verify field values
    String sortCode = terminal.retrieveFieldTextAfterFieldWithString("Sort code");
    assertThat(sortCode.trim()).as("Sort code should match").isEqualTo("987654");
    
    String customerName = terminal.retrieveFieldTextAfterFieldWithString("Customer name");
    assertThat(customerName.trim()).as("Customer name should match").isEqualTo("Mr Michael Z Barker");
    
    // Return to main menu
    terminal.pf3()                                  // Press F3
        .waitForKeyboard()                          // Wait for keyboard to unlock
        .waitForTextInField("Select an option");    // Verify back at main menu
}
```

#### Error Detection

Error messages vary by application. When checking for errors:

1. Ask the user what specific error text to expect
2. Use `retrieveScreen()` to get full screen content
3. Check for the specific error text in assertions

```java
// Check for specific error message
String screen = terminal.retrieveScreen();
assertThat(screen).as("Should show invalid customer error")
    .contains("Customer not found");
```

#### Terminal Size Configuration

Default terminal size is 24 rows × 80 columns. To use a different size:

```java
@Zos3270Terminal(imageTag = "PRIMARY", primaryColumns = 100, primaryRows = 30)
public ITerminal largeTerminal;
```

The framework handles different screen sizes automatically, so you typically don't need to worry about this unless your application requires a specific size.

### Adding a manager to your project

To add a manager to your project, follow the steps below:
1. Check that the manager has not already been added to the test project's `build.gradle` (for Gradle projects) or `pom.xml` (for Maven projects).
2. Add the manager dependency to the test project's `build.gradle` (for Gradle projects) or `pom.xml` (for Maven projects).
    1. For Gradle projects, manager dependencies are placed in the `build.gradle` file's `dependencies` section in the form:
        ```groovy
        implementation 'dev.galasa:dev.galasa.core.manager'
        ```
         Replacing `dev.galasa.core.manager` with the appropriate manager dependency, like `dev.galasa.zos3270.manager` for the z/OS 3270 terminal manager or `dev.galasa.docker.manager` for the Docker manager.

**Note**: Manager dependencies typically don't require explicit version numbers as they inherit from the Galasa framework version defined in your project's build configuration.
   2. For Maven projects, manager dependencies are placed in the `pom.xml` file's `<dependencies>` section in the form:
      ```xml
	    <dependency>
	    	<groupId>dev.galasa</groupId>
	    	<artifactId>dev.galasa.core.manager</artifactId>
	    	<scope>provided</scope>
	    </dependency>
      ```
         Replacing `dev.galasa.core.manager` with the appropriate manager dependency, like `dev.galasa.zos3270.manager` for the z/OS 3270 terminal manager or `dev.galasa.docker.manager` for the Docker manager.
3. Build the Galasa project to pull in the new dependency imports and avoid import errors. See [Building Galasa projects](#building-galasa-projects) for detailed instructions.

**IMPORTANT**: The z/OS 3270 manager requires the z/OS manager to also be added as a dependency, so **ALWAYS** be sure to add both if the user wants to interact with z/OS 3270 terminals in their tests.
**IMPORTANT**: If a user wants to interact with a CICS region, you should use the CICS TS manager, which includes using the `@CicsRegion` and `@CicsTerminal` annotations, and the z/OS and z/OS 3270 managers. See the [CICS TS IVT](./modules/ivts/galasa-ivts-parent/dev.galasa.zos.ivts/dev.galasa.zos.ivts.cicsts/src/main/java/dev/galasa/zos/ivts/cicsts/CICSTSManagerIVT.java) for an example CICS test suite.

Examples of how Galasa managers are used in test classes can be found in the Galasa repository's `ivts` subproject at `modules/ivts`

## Building Galasa projects

- If you are using Gradle: Run `gradle clean build publishToMavenLocal` from the Galasa project's root directory to build the project.
- If you are using Maven: Run `mvn clean install` from the Galasa project's root directory to build the project.

**Note**: When building for the first time, Gradle/Maven will download all required dependencies. This may take several minutes. Subsequent builds will be faster.

**Tip**: To check which version of Galasa is being used, examine the `build.gradle` or `pom.xml` file for the Galasa version property.


## Configuring test environments

Galasa tests often need to connect to external systems. The details of these systems can be configured into the Configuration Property Store (CPS) file found at `~/.galasa/cps.properties` by default.

CPS properties are key-value pairs in the form:
```properties
NAMESPACE.PROPERTY_NAME=PROPERTY_VALUE
```

Where:
- `NAMESPACE`: The namespace of the manager that this property is associated with, typically the name of the manager itself. Example: The z/OS manager is `zos`, the Docker manager is `docker`, and the z/OS 3270 manager is `zos3270`.
- `PROPERTY_NAME`: The name of the CPS property to configure.
- `PROPERTY_VALUE`: The value to be associated with the CPS property.

**IMPORTANT**: Every manager has its own set of supported CPS property. **DO NOT** assume that managers share the same CPS properties. The supported CPS properties for each manager can be found in the managers' documentation pages `docs/content/managers`. Example: the z/OS manager's supported CPS properties can be found at `docs/content/managers/zos-managers/index.md#configuration-properties`

Galasa tests often also need to supply credentials to connect to protected systems. Credentials can be configured into the Credentials Store file found at `~/.galasa/credentials.properties` by default.

Credentials properties are key-value pairs in the form:
```properties
secure.credentials.TAG.CREDENTIALS_TYPE=CREDENTIAL_VALUE
```

Where:
- `TAG`: A tag that identifies the credentials. This tag is used to reference the credentials from the CPS properties.
- `CREDENTIALS_TYPE`: The type of credentials. Supported types are: `username`, `password`, and `token`.
- `CREDENTIAL_VALUE`: The value of the credentials.

Example: To create a username `MYUSER` and password `PASSW0RD` associated with the tag `SYSTEM1`, you would set these properties:

```properties
secure.credentials.SYSTEM1.username=MYUSER
secure.credentials.SYSTEM1.password=PASSW0RD
```

Then, the CPS properties can use the credentials like this:
```properties
zos.image.MYZOSSYSTEM.credentials=SYSTEM1
```

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

## Troubleshooting Common Issues

### Build Failures
- Ensure all manager dependencies are correctly specified in `build.gradle` or `pom.xml`
- Check that Java version is compatible (Java 17 or later recommended)

### Test Execution Failures
- Verify CPS properties are correctly configured in `~/.galasa/cps.properties`
- Ensure credentials are properly set in `~/.galasa/credentials.properties`
- Check that the OBR coordinates match your project's group ID and version
- Review `run.log` in the RAS directory for detailed error messages

### Manager Import Errors
- Run the build command after adding new manager dependencies
- Verify the manager artifact ID matches the documentation
- For z/OS 3270 manager, ensure both z/OS and z/OS 3270 managers are added

## Quick Reference Examples

### Common Manager Combinations
- **z/OS Testing**: z/OS Manager + z/OS 3270 Manager
- **CICS Testing**: CICS Region Manager + z/OS Manager + z/OS 3270 Terminal Manager
- **Web Testing**: HTTP Manager + Artifact Manager

# Maintaining this AGENTS.md file

This AGENTS.md file is a living document that should be updated to keep it up-to-date with the latest information about using Galasa.

**IMPORTANT**: At the end of every task, check if there has been any useful corrections or additional context that should be added to this file, and follow the steps below:

- If there is any context or information about Galasa in a conversation that would be useful to add to this AGENTS.md file to aid future conversations, add it in the most relevant part of this file. If the user rejects the addition of the context or information, do not add it to this file.
- Respect that the file is written in markdown and use markdown syntax to format the text. For example, use `**bold**` for bold text, `*italic*` for italic text.
