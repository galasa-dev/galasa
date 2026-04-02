---
name: using-galasa-managers
description: Provides information about how to add Galasa managers to a Galasa test project and best practices for commonly-used managers.
---

## Using Galasa Managers

- Galasa managers provide interfaces to work with various technologies. For example, the Galasa z/OS manager can be used to connect to z/OS systems and the Galasa Docker manager can be used to manipulate Docker containers.
- Refer to https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/docs/content/docs/managers/index.md for the most up-to-date list of managers that Galasa offers.

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

### Terminal Interaction Basics

When using the `ITerminal` interface from the z/OS 3270 manager or the `ICicsTerminal` interface from the CICS TS manager:

**Critical Rules:**
- Always chain method calls: `terminal.type("X").enter().waitForKeyboard()`
- Always call `waitForKeyboard()` after `enter()`, `clear()`, or PF keys
- Wait for content with `waitForTextInField("expected text")` before proceeding
- Extract field values: `terminal.retrieveFieldTextAfterFieldWithString("Label").trim()`
- **Match exact capitalization** in field names and expected values

**Common Pattern:**
```java
terminal.type("X")
    .enter()
    .waitForKeyboard()
    .waitForTextInField("EXPECTED MESSAGE");  // Verify screen loaded

String value = terminal.retrieveFieldTextAfterFieldWithString("Field Name").trim();
assertThat(value).as("Value should match").isEqualTo("Expected Value");
```

**For detailed terminal interaction guidance** (timing, field navigation, complete examples, troubleshooting), see [Terminal Interaction Reference](terminal-interaction-reference.md). Load that file only when working with terminal screens or debugging timing issues.

### Adding a manager to your project

To add a manager to your project:
1. Check that the manager has not already been added to `build.gradle` or `pom.xml`
2. Add the manager dependency:
    - **Gradle**: `implementation 'dev.galasa:dev.galasa.{manager}.manager'`
    - **Maven**: `<artifactId>dev.galasa.{manager}.manager</artifactId>` with `<scope>provided</scope>`
3. Build the project (see [writing-galasa-tests.md](writing-galasa-tests.md#building-galasa-projects))

**Note**: Manager dependencies inherit version from the Galasa framework version in your build configuration.

**IMPORTANT**: The z/OS 3270 manager requires the z/OS manager to also be added as a dependency, so **ALWAYS** be sure to add both if the user wants to interact with z/OS 3270 terminals in their tests.
**IMPORTANT**: If a user wants to interact with a CICS region, you should use the CICS TS manager, which includes using the `@CicsRegion` and `@CicsTerminal` annotations, and the z/OS and z/OS 3270 managers. See the [CICS TS IVT](https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/modules/ivts/galasa-ivts-parent/dev.galasa.zos.ivts/dev.galasa.zos.ivts.cicsts/src/main/java/dev/galasa/zos/ivts/cicsts/CICSTSManagerIVT.java) for an example CICS test suite.

Examples of how Galasa managers are used in test classes can be found in the Galasa repository's `ivts` subproject at https://github.com/galasa-dev/galasa/tree/main/modules/ivts
