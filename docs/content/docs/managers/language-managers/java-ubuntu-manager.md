---
title: "Java Ubuntu Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/java/ubuntu/package-summary.html){target="_blank"}.


## Overview

The Java Ubuntu Manager provides Galasa tests with Java installations on Ubuntu Linux images. This Manager extends the Java Manager to provide platform-specific Java installation and configuration capabilities for Ubuntu systems.

The Java Ubuntu Manager automatically downloads, extracts, and configures Java installations on Ubuntu Linux images, making them ready for use in tests.


## Annotations

The following annotations are available with the Java Ubuntu Manager.


### Java Ubuntu Installation

| Annotation: | Java Ubuntu Installation |
| --------------------------------------- | :------------------------------------- |
| Name: | `@JavaUbuntuInstallation` |
| Description: | The `@JavaUbuntuInstallation` annotation requests a Java installation on an Ubuntu Linux image. The Manager will download, extract, and configure the Java installation automatically. |
| Attribute: `javaTag` |  The tag to identify this Java installation. Default is "PRIMARY". |
| Attribute: `imageTag` |  The tag of the Linux image where Java should be installed. Default is "PRIMARY". |
| Attribute: `javaType` |  The type of Java distribution (e.g., ORACLE, OPENJDK). Default is determined by CPS properties. |
| Attribute: `javaVersion` |  The Java version to install (e.g., v8, v11, v17). Default is determined by CPS properties. |
| Attribute: `javaJvm` |  The JVM type (e.g., hotspot, openj9). Default is "hotspot". |
| Syntax: | <pre lang="java">@JavaUbuntuInstallation(javaTag = "PRIMARY", imageTag = "PRIMARY")<br>public IJavaUbuntuInstallation javaInstallation;<br></pre> |
| Notes: | The `IJavaUbuntuInstallation` interface provides access to the Java installation, including the Java command path and installation directories.<br><br> See [JavaUbuntuInstallation](https://galasa.dev/docs/reference/javadoc/dev/galasa/java/ubuntu/JavaUbuntuInstallation.html){target="_blank"} and [IJavaUbuntuInstallation](https://galasa.dev/docs/reference/javadoc/dev/galasa/java/ubuntu/IJavaUbuntuInstallation.html){target="_blank"} to find out more. |


## Code Snippets

### Provision Java on Ubuntu

```java
@LinuxImage(imageTag = "PRIMARY", operatingSystem = OperatingSystem.ubuntu)
public ILinuxImage linuxImage;

@JavaUbuntuInstallation(javaTag = "PRIMARY", imageTag = "PRIMARY")
public IJavaUbuntuInstallation javaInstallation;

@Test
public void testJavaOnUbuntu() throws Exception {
    // Java is automatically installed and ready to use
    String javaCommand = javaInstallation.getJavaCommand();
    
    logger.info("Java command: " + javaCommand);
    
    // Execute a Java command
    ICommandShell shell = linuxImage.getCommandShell();
    String response = shell.issueCommand(javaCommand + " -version");
    
    logger.info("Java version: " + response);
}
```

### Specify Java version

```java
@LinuxImage(imageTag = "PRIMARY", operatingSystem = OperatingSystem.ubuntu)
public ILinuxImage linuxImage;

@JavaUbuntuInstallation(
    javaTag = "JAVA11",
    imageTag = "PRIMARY",
    javaVersion = JavaVersion.v11
)
public IJavaUbuntuInstallation java11;

@Test
public void testSpecificJavaVersion() throws Exception {
    String javaCommand = java11.getJavaCommand();
    
    ICommandShell shell = linuxImage.getCommandShell();
    String response = shell.issueCommand(javaCommand + " -version");
    
    assertThat(response).contains("11.");
}
```

### Use multiple Java installations

```java
@LinuxImage(imageTag = "PRIMARY", operatingSystem = OperatingSystem.ubuntu)
public ILinuxImage linuxImage;

@JavaUbuntuInstallation(
    javaTag = "JAVA11",
    imageTag = "PRIMARY",
    javaVersion = JavaVersion.v11
)
public IJavaUbuntuInstallation java11;

@JavaUbuntuInstallation(
    javaTag = "JAVA17",
    imageTag = "PRIMARY",
    javaVersion = JavaVersion.v17
)
public IJavaUbuntuInstallation java17;

@Test
public void testMultipleJavaVersions() throws Exception {
    ICommandShell shell = linuxImage.getCommandShell();
    
    // Test with Java 11
    String java11Version = shell.issueCommand(java11.getJavaCommand() + " -version");
    logger.info("Java 11: " + java11Version);
    
    // Test with Java 17
    String java17Version = shell.issueCommand(java17.getJavaCommand() + " -version");
    logger.info("Java 17: " + java17Version);
}
```

### Run a Java application

```java
@LinuxImage(imageTag = "PRIMARY", operatingSystem = OperatingSystem.ubuntu)
public ILinuxImage linuxImage;

@JavaUbuntuInstallation(javaTag = "PRIMARY", imageTag = "PRIMARY")
public IJavaUbuntuInstallation javaInstallation;

@Test
public void testRunJavaApplication() throws Exception {
    ICommandShell shell = linuxImage.getCommandShell();
    
    // Upload a JAR file
    Path localJar = Paths.get("target/myapp.jar");
    Path remoteJar = linuxImage.getRunDirectory().resolve("myapp.jar");
    Files.copy(localJar, remoteJar);
    
    // Run the Java application
    String javaCommand = javaInstallation.getJavaCommand();
    String response = shell.issueCommand(
        javaCommand + " -jar " + remoteJar.toString()
    );
    
    logger.info("Application output: " + response);
}
```

### Use with code coverage

```java
@LinuxImage(imageTag = "PRIMARY", operatingSystem = OperatingSystem.ubuntu)
public ILinuxImage linuxImage;

@JavaUbuntuInstallation(javaTag = "PRIMARY", imageTag = "PRIMARY")
public IJavaUbuntuInstallation javaInstallation;

@Test
public void testWithCodeCoverage() throws Exception {
    // When code coverage is enabled in CPS, the Java command
    // automatically includes the Jacoco agent
    String javaCommand = javaInstallation.getJavaCommand();
    
    // The command will include: -javaagent:/path/to/jacocoagent.jar=...
    logger.info("Java command with coverage: " + javaCommand);
    
    ICommandShell shell = linuxImage.getCommandShell();
    String response = shell.issueCommand(
        javaCommand + " -jar /path/to/app.jar"
    );
    
    // Code coverage data is automatically collected and saved
}
```

### Specify Java type and JVM

```java
@LinuxImage(imageTag = "PRIMARY", operatingSystem = OperatingSystem.ubuntu)
public ILinuxImage linuxImage;

@JavaUbuntuInstallation(
    javaTag = "OPENJDK",
    imageTag = "PRIMARY",
    javaType = JavaType.openjdk,
    javaVersion = JavaVersion.v17,
    javaJvm = "hotspot"
)
public IJavaUbuntuInstallation openJdk;

@Test
public void testOpenJdk() throws Exception {
    String javaCommand = openJdk.getJavaCommand();
    
    ICommandShell shell = linuxImage.getCommandShell();
    String response = shell.issueCommand(javaCommand + " -version");
    
    assertThat(response).contains("OpenJDK");
}
```


## Configuration Properties

The Java Ubuntu Manager uses the same configuration properties as the Java Manager, plus any Linux Manager properties for the target image.

See the [Java Manager](./java-manager.md) documentation for Java-specific properties.

See the [Linux Manager](../unix-managers/linux-manager.md) documentation for Linux image properties.


## Installation Process

When a Java Ubuntu installation is requested, the Manager performs the following steps:

1. **Check for existing installation** - Checks if Java is already installed in the manager's home directory on the Linux image
2. **Download archive** - If not present, downloads the Java archive from the configured location
3. **Extract archive** - Extracts the Java archive to the run directory
4. **Move to final location** - Moves the extracted Java to the manager's home directory
5. **Configure** - Sets up the Java installation for use
6. **Add Jacoco agent** - If code coverage is enabled, downloads and configures the Jacoco agent

The installation is cached in the manager's home directory, so subsequent tests on the same image can reuse the installation.


## Best Practices

1. **Use appropriate Java versions** - Choose Java versions that match your application requirements.

2. **Cache installations** - The Manager automatically caches Java installations, but ensure your test environment has sufficient disk space.

3. **Use code coverage selectively** - Enable code coverage only when needed as it adds overhead.

4. **Specify versions explicitly** - Always specify Java version, type, and JVM explicitly for reproducible tests.

5. **Clean up test artifacts** - Ensure test artifacts are cleaned up after tests complete.

6. **Use unique tags** - When using multiple Java installations, use unique tags to avoid conflicts.

7. **Configure download locations** - Ensure Java archive download locations are accessible from your test environment.

8. **Test with multiple versions** - If your application supports multiple Java versions, test with all supported versions.