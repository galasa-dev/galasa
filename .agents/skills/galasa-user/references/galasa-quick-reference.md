---
name: galasa-quick-reference
description: Quick reference for common Galasa commands, patterns, and configurations. Load this first for simple tasks.
---

## When to Use This Reference

Use this quick reference for:
- Simple project creation and test execution
- Common command syntax lookups
- Basic manager setup
- Standard test patterns

For detailed guidance, load the specific skill files as needed.

## Command Quick Reference

### Environment Setup
```bash
# Initialize Galasa environment (creates ~/.galasa directory)
galasactl local init

# Check CLI version
galasactl --version
```

### Project Creation
```bash
# Create new Galasa project (Gradle example - recommended)
galasactl project create --package {PACKAGE_NAME} --features {FEATURE} --gradle --obr

# Create new Galasa project (Maven example)
galasactl project create --package {PACKAGE_NAME} --features {FEATURE} --maven --obr

# Example
galasactl project create --package dev.galasa.example --features account --gradle --obr
```

### Building Projects
```bash
# Gradle (recommended)
gradle clean build publishToMavenLocal

# Maven
mvn clean install
```

### Running Tests
```bash
# Run a test class
galasactl runs submit local \
  --class {BUNDLE_NAME}/{FULLY_QUALIFIED_CLASS} \
  --obr mvn:{GROUP_ID}/{ARTIFACT_ID}/{VERSION}/obr \
  --log -

# Example
galasactl runs submit local \
  --class dev.galasa.example.account/dev.galasa.example.account.TestAccount \
  --obr mvn:dev.galasa.example/dev.galasa.example.obr/0.0.1-SNAPSHOT/obr \
  --log -

# Run specific test methods
--methods testMethod1 --methods testMethod2

# Enable trace logging
--trace
```

## Manager Quick Reference

### Common Manager Dependencies

**Gradle (`build.gradle`)**:
```groovy
dependencies {
    implementation 'dev.galasa:dev.galasa.{manager}.manager'
}
```

**Maven (`pom.xml`)**:
```xml
<dependency>
    <groupId>dev.galasa</groupId>
    <artifactId>dev.galasa.{manager}.manager</artifactId>
    <scope>provided</scope>
</dependency>
```

### Manager Injection Patterns

```java
// z/OS and 3270 Terminal
@ZosImage(imageTag = "PRIMARY")
public IZosImage zosImage;

@Zos3270Terminal(imageTag = "PRIMARY")
public ITerminal terminal;

// CICS (requires z/OS managers too, but does not need ZosImage and Zos3270Terminal fields if used)
@CicsRegion(cicsTag = "PRIMARY")
public ICicsRegion cics;

@CicsTerminal(cicsTag = "PRIMARY")
public ICicsTerminal terminal;

// HTTP Client
@HttpClient
public IHttpClient httpClient;

// Logger (only include if log messages are added)
@Logger
public Log logger;
```

### Critical Manager Rules

- **z/OS 3270**: Always add both `dev.galasa.zos.manager` AND `dev.galasa.zos3270.manager`
- **CICS**: Requires `dev.galasa.cicsts.manager` + z/OS managers
- **Build after adding**: Run build command after adding new managers

## Common Test Patterns

### Test Class Structure
```java
@Test
public class YourTestClass {

    @BeforeClass
    public void setup() throws Exception {
        // One-time setup
    }
    
    @Test
    public void testSomething() throws Exception {
        // Test implementation
    }
    
    @AfterClass
    public void cleanup() throws Exception {
        // One-time cleanup
    }
}
```

### Terminal Interaction (Critical Pattern)
```java
// ALWAYS chain methods and wait for keyboard
terminal.type("value")
    .enter()
    .waitForKeyboard()
    .waitForTextInField("Expected text");

// Extract field values (always trim)
String value = terminal.retrieveFieldTextAfterFieldWithString("Label").trim();

// Verify with assertions (always include message)
assertThat(value).as("Field should match").isEqualTo("expected");

// Function keys
terminal.pf3().waitForKeyboard();  // F3 to exit/return
terminal.pf5().waitForKeyboard();  // F5 to delete
```

### Assertions (AssertJ)
```java
// Always include descriptive message with .as()
assertThat(actual).as("Description of what's being checked").isEqualTo(expected);
assertThat(screen).as("Should contain success message").contains("Success");
assertThat(value).as("Should not be empty").isNotEmpty();
```

## Configuration Quick Reference

### CPS Properties (`~/.galasa/cps.properties`)
```properties
# Format: NAMESPACE.PROPERTY_NAME=VALUE
zos.image.PRIMARY.ipv4.hostname=system.example.com
zos.image.PRIMARY.telnet.port=992
zos.image.PRIMARY.telnet.tls=true
zos.image.PRIMARY.credentials=SYSTEM1
```

### Credentials (`~/.galasa/credentials.properties`)
```properties
# Format: secure.credentials.TAG.TYPE=VALUE
secure.credentials.SYSTEM1.username=MYUSER
secure.credentials.SYSTEM1.password=PASSW0RD
```

## Test Results Location

- **RAS Directory**: `~/.galasa/ras/`
- **Run folders**: Named by run ID (e.g., `L12`)
- **Key files**:
  - `structure.json`: Test run details and status
  - `run.log`: Full test execution log
  - `artifacts/`: Test artifacts

## When to Load Detailed Documentation

Load specific skill files when you need:
- **galasa-cli-tool.md**: Detailed CLI command explanations, project structure details
- **writing-galasa-tests.md**: Test class structure details, build configuration, CPS/credentials setup
- **using-galasa-managers.md**: Manager injection details, basic terminal interaction
- **terminal-interaction-reference.md**: Detailed terminal timing, field navigation, complete examples
- **troubleshooting-galasa.md**: Error resolution guidance