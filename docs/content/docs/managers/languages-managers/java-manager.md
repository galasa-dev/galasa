---
title: "Java Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/java/package-summary.html){target="_blank"}.


## Overview

The Java Manager provides Galasa tests with access to Java installations. This Manager enables tests to retrieve Java archives, configure Java installations, and manage Java-related resources for testing Java applications.

The Java Manager is a foundational Manager that works in conjunction with platform-specific managers (Java Ubuntu Manager, Java Windows Manager) to provide Java installations on different operating systems.


## Annotations

The Java Manager does not provide direct annotations for test classes. Java installations are typically obtained through platform-specific managers like Java Ubuntu Manager or Java Windows Manager.


## Code Snippets

### Retrieve Java archive

```java
@Test
public void testJavaArchive() throws Exception {
    IJavaInstallation javaInstallation = getJavaInstallation();
    
    // Retrieve the Java archive
    Path archivePath = javaInstallation.retrieveArchive();
    
    logger.info("Java archive downloaded to: " + archivePath);
}
```

### Get Java command path

```java
@Test
public void testJavaCommand() throws Exception {
    IJavaInstallation javaInstallation = getJavaInstallation();
    
    // Get the java command path
    String javaCommand = javaInstallation.getJavaCommand();
    
    logger.info("Java command: " + javaCommand);
}
```

### Retrieve Jacoco agent for code coverage

```java
@Test
public void testJacocoAgent() throws Exception {
    IJavaInstallation javaInstallation = getJavaInstallation();
    
    // Retrieve the Jacoco agent for code coverage
    Path jacocoAgent = javaInstallation.retrieveJacocoAgent();
    
    logger.info("Jacoco agent: " + jacocoAgent);
}
```


## Configuration Properties

The following are properties used to configure the Java Manager.

### Java default version

| Property: | Java default version |
| --------------------------------------- | :------------------------------------- |
| Name: | `java.default.version` |
| Description: | Specifies the default Java version to use when no specific version is requested. |
| Required:  | No |
| Default value: | `v11` |
| Valid values: | `v8`, `v11`, `v17`, `v21`, or other valid Java version identifiers |
| Examples: | `java.default.version=v11`<br>`java.default.version=v17` |

### Java archive download location

| Property: | Java archive download location |
| --------------------------------------- | :------------------------------------- |
| Name: | `java.[type].[version].[jvm].download.location` |
| Description: | Specifies the location from which to download the Java archive. The location can be an HTTP/HTTPS URL or a Maven coordinate. |
| Required:  | Yes, for each Java type/version/JVM combination you want to use |
| Valid values: | A valid HTTP/HTTPS URL or Maven coordinate |
| Examples: | `java.oracle.v11.hotspot.download.location=https://example.com/java/jdk-11.tar.gz`<br>`java.openjdk.v17.hotspot.download.location=mvn://org.openjdk:jdk:17` |

### Jacoco agent location

| Property: | Jacoco agent location |
| --------------------------------------- | :------------------------------------- |
| Name: | `java.jacoco.agent.location` |
| Description: | Specifies the location of the Jacoco agent JAR file for code coverage. |
| Required:  | No, only if code coverage is required |
| Valid values: | A valid HTTP/HTTPS URL or Maven coordinate |
| Examples: | `java.jacoco.agent.location=https://example.com/jacoco/jacocoagent.jar`<br>`java.jacoco.agent.location=mvn://org.jacoco:org.jacoco.agent:0.8.7` |

### Code coverage enable

| Property: | Code coverage enable |
| --------------------------------------- | :------------------------------------- |
| Name: | `java.jacoco.code.coverage` |
| Description: | Enables or disables code coverage collection using Jacoco. |
| Required:  | No |
| Default value: | `false` |
| Valid values: | `true` or `false` |
| Examples: | `java.jacoco.code.coverage=true` |

### Code coverage save location

| Property: | Code coverage save location |
| --------------------------------------- | :------------------------------------- |
| Name: | `java.jacoco.save.location` |
| Description: | Specifies where to save the code coverage execution data (exec files). |
| Required:  | No, only if code coverage is enabled |
| Valid values: | A valid HTTP/HTTPS URL |
| Examples: | `java.jacoco.save.location=https://coverage-server.example.com/upload` |

### Code coverage save credentials

| Property: | Code coverage save credentials |
| --------------------------------------- | :------------------------------------- |
| Name: | `java.jacoco.save.credentials` |
| Description: | Specifies the credentials ID for uploading code coverage data to the save location. |
| Required:  | No, only if the save location requires authentication |
| Valid values: | A valid credentials ID from the Galasa Credentials Store |
| Examples: | `java.jacoco.save.credentials=COVERAGE_CREDS` |


## Java Types

The Java Manager supports different Java types:

- **Oracle** - Oracle JDK distributions
- **OpenJDK** - OpenJDK distributions
- **IBM** - IBM Java/Semeru distributions
- **Adopt** - AdoptOpenJDK distributions

## Java Versions

Common Java versions supported:

- **v8** - Java 8
- **v11** - Java 11 (LTS)
- **v17** - Java 17 (LTS)
- **v21** - Java 21 (LTS)

## JVM Types

- **hotspot** - HotSpot JVM
- **openj9** - Eclipse OpenJ9 JVM


## Best Practices

1. **Use LTS versions** - Prefer Long Term Support (LTS) versions like Java 11, 17, or 21 for stability.

2. **Configure download locations** - Ensure Java archive download locations are properly configured in CPS.

3. **Use code coverage selectively** - Enable code coverage only when needed as it can impact performance.

4. **Cache Java archives** - Configure your environment to cache Java archives to avoid repeated downloads.

5. **Use platform-specific managers** - Use Java Ubuntu Manager or Java Windows Manager for platform-specific Java installations rather than using the Java Manager directly.

6. **Specify versions explicitly** - Always specify the Java version explicitly in your tests to ensure consistency.