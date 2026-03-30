---
name: writing-galasa-tests
description: Provides information about how Galasa test classes are structured, how they are built, and how test environments are configured.
---

### Galasa Test Class Structure

Galasa test classes are Java files with this structure:

**Required elements:**
- Package: `{PACKAGE_NAME}.{FEATURE_NAME}`
- Imports: AssertJ (`assertThat`), Galasa annotations (`@Test`, `@Logger`)
- Class: `@Test` annotation + `@Summary("description")`
- Logger: `@Logger public Log logger;` (only if log messages are added to tests)
- Test methods: `@Test` annotation on each test method

**Optional lifecycle methods:**
- `@BeforeClass` / `@AfterClass`: One-time setup/cleanup
- `@Before` / `@After`: Per-test setup/cleanup

**Assertion rules:**
- Use AssertJ: `assertThat(value).as("Description").isEqualTo(expected)`
- Always include descriptive message with `.as()`
- Remove unused imports

**Example structure:**
```java
package dev.galasa.example.feature;

import static org.assertj.core.api.Assertions.assertThat;
import org.apache.commons.logging.Log;
import dev.galasa.*;
import dev.galasa.core.manager.Logger;

@Summary("Test suite description")
@Test
public class TestClass {
    @Logger
    public Log logger;
    
    @Test
    public void testSomething() throws Exception {
        assertThat(result).as("Should match expected").isEqualTo(expected);
    }
}
```

For complete examples, see [Galasa IVT tests](https://github.com/galasa-dev/galasa/tree/main/modules/ivts).

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

**IMPORTANT**: Every manager has its own set of supported CPS property. **DO NOT** assume that managers share the same CPS properties. The supported CPS properties for each manager can be found in the managers' documentation pages https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/docs/content/docs/managers/index.md. Example: the z/OS manager's supported CPS properties can be found at https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/docs/content/docs/managers/zos-managers/zos-manager.md

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