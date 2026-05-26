---
title: "Java Windows Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/java/windows/package-summary.html){target="_blank"}.


## Overview

The Java Windows Manager provides Galasa tests with Java installations on Windows images. This Manager extends the Java Manager to provide platform-specific Java installation and configuration capabilities for Windows systems.

The Java Windows Manager automatically downloads, extracts, and configures Java installations on Windows images, making them ready for use in tests.


## Annotations

The following annotations are available with the Java Windows Manager.


### Java Windows Installation

| Annotation: | Java Windows Installation |
| --------------------------------------- | :------------------------------------- |
| Name: | `@JavaWindowsInstallation` |
| Description: | The `@JavaWindowsInstallation` annotation requests a Java installation on a Windows image. The Manager will download, extract, and configure the Java installation automatically. |
| Attribute: `javaTag` |  The tag to identify this Java installation. Default is "PRIMARY". |
| Attribute: `imageTag` |  The tag of the Windows image where Java should be installed. Default is "PRIMARY". |
| Attribute: `javaType` |  The type of Java distribution (e.g., ORACLE, OPENJDK). Default is determined by CPS properties. |
| Attribute: `javaVersion` |  The Java version to install (e.g., v8, v11, v17). Default is determined by CPS properties. |
| Attribute: `javaJvm` |  The JVM type (e.g., hotspot, openj9). Default is "hotspot". |
| Syntax: | <pre lang="java">@JavaWindowsInstallation(javaTag = "PRIMARY", imageTag = "PRIMARY")<br>public IJavaWindowsInstallation javaInstallation;<br></pre> |
| Notes: | The `IJavaWindowsInstallation` interface provides access to the Java installation, including the Java command path and installation directories.<br><br> See [JavaWindowsInstallation](../../reference/javadoc/dev/galasa/java/windows/JavaWindowsInstallation.html){target="_blank"} and [IJavaWindowsInstallation](../../reference/javadoc/dev/galasa/java/windows/IJavaWindowsInstallation.html){target="_blank"} to find out more. |


## Code Snippets

### Provision Java on Windows

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@JavaWindowsInstallation(javaTag = "PRIMARY", imageTag = "PRIMARY")
public IJavaWindowsInstallation javaInstallation;

@Test
public void testJavaOnWindows() throws Exception {
    // Java is automatically installed and ready to use
    String javaCommand = javaInstallation.getJavaCommand();
    
    logger.info("Java command: " + javaCommand);
    
    // Execute a Java command
    ICommandShell shell = windowsImage.getCommandShell();
    String response = shell.issueCommand(javaCommand + " -version");
    
    logger.info("Java version: " + response);
}
```

### Specify Java version

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@JavaWindowsInstallation(
    javaTag = "JAVA11",
    imageTag = "PRIMARY",
    javaVersion = JavaVersion.v17
)
public IJavaWindowsInstallation java11;

@Test
public void testSpecificJavaVersion() throws Exception {
    String javaCommand = java11.getJavaCommand();
    
    ICommandShell shell = windowsImage.getCommandShell();
    String response = shell.issueCommand(javaCommand + " -version");
    
    assertThat(response).contains("11.");
}
```

### Use multiple Java installations

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@JavaWindowsInstallation(
    javaTag = "JAVA11",
    imageTag = "PRIMARY",
    javaVersion = JavaVersion.v17
)
public IJavaWindowsInstallation java11;

@JavaWindowsInstallation(
    javaTag = "JAVA17",
    imageTag = "PRIMARY",
    javaVersion = JavaVersion.v17
)
public IJavaWindowsInstallation java17;

@Test
public void testMultipleJavaVersions() throws Exception {
    ICommandShell shell = windowsImage.getCommandShell();
    
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
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@JavaWindowsInstallation(javaTag = "PRIMARY", imageTag = "PRIMARY")
public IJavaWindowsInstallation javaInstallation;

@Test
public void testRunJavaApplication() throws Exception {
    ICommandShell shell = windowsImage.getCommandShell();
    
    // Upload a JAR file
    Path localJar = Paths.get("target/myapp.jar");
    Path remoteJar = windowsImage.getRunDirectory().resolve("myapp.jar");
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
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@JavaWindowsInstallation(javaTag = "PRIMARY", imageTag = "PRIMARY")
public IJavaWindowsInstallation javaInstallation;

@Test
public void testWithCodeCoverage() throws Exception {
    // When code coverage is enabled in CPS, the Java command
    // automatically includes the Jacoco agent
    String javaCommand = javaInstallation.getJavaCommand();
    
    // The command will include: -javaagent:C:\path\to\jacocoagent.jar=...
    logger.info("Java command with coverage: " + javaCommand);
    
    ICommandShell shell = windowsImage.getCommandShell();
    String response = shell.issueCommand(
        javaCommand + " -jar C:\\path\\to\\app.jar"
    );
    
    // Code coverage data is automatically collected and saved
}
```

### Specify Java type and JVM

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@JavaWindowsInstallation(
    javaTag = "OPENJDK",
    imageTag = "PRIMARY",
    javaType = JavaType.openjdk,
    javaVersion = JavaVersion.v17,
    javaJvm = "hotspot"
)
public IJavaWindowsInstallation openJdk;

@Test
public void testOpenJdk() throws Exception {
    String javaCommand = openJdk.getJavaCommand();
    
    ICommandShell shell = windowsImage.getCommandShell();
    String response = shell.issueCommand(javaCommand + " -version");
    
    assertThat(response).contains("OpenJDK");
}
```

### Handle Windows paths

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@JavaWindowsInstallation(javaTag = "PRIMARY", imageTag = "PRIMARY")
public IJavaWindowsInstallation javaInstallation;

@Test
public void testWindowsPaths() throws Exception {
    String javaCommand = javaInstallation.getJavaCommand();
    
    // Java command will use Windows path format
    // e.g., C:\javawindows\javas\jdk-11\bin\java.exe
    logger.info("Java command: " + javaCommand);
    
    ICommandShell shell = windowsImage.getCommandShell();
    
    // Use Windows path separators
    String response = shell.issueCommand(
        javaCommand + " -cp C:\\myapp\\lib\\* com.example.Main"
    );
    
    logger.info("Application output: " + response);
}
```


## Configuration Properties

The Java Windows Manager uses the same configuration properties as the Java Manager, plus any Windows Manager properties for the target image.

See the [Java Manager](./java-manager.md) documentation for Java-specific properties.

See the [Windows Manager](../windows-managers/windows-manager.md) documentation for Windows image properties.


## Installation Process

When a Java Windows installation is requested, the Manager performs the following steps:

1. **Check for existing installation** - Checks if Java is already installed in the manager's home directory on the Windows image
2. **Download archive** - If not present, downloads the Java archive from the configured location
3. **Extract archive** - Extracts the Java archive (ZIP format) to the run directory using PowerShell
4. **Move to final location** - Moves the extracted Java to the manager's home directory
5. **Configure** - Sets up the Java installation for use
6. **Add Jacoco agent** - If code coverage is enabled, downloads and configures the Jacoco agent

The installation is cached in the manager's home directory, so subsequent tests on the same image can reuse the installation.


## Archive Format

The Java Windows Manager expects Java archives in ZIP format. The archive should contain a single root directory with the Java installation.

Example archive structure:
```
jdk-11.0.12/
  bin/
    java.exe
    javac.exe
    ...
  lib/
    ...
  conf/
    ...
```


## Best Practices

1. **Use ZIP archives** - Ensure Java archives for Windows are in ZIP format, not TAR.GZ.

2. **Use appropriate Java versions** - Choose Java versions that match your application requirements and Windows compatibility.

3. **Handle Windows paths** - Remember to use backslashes (`\`) or double backslashes (`\\`) in Windows paths.

4. **Cache installations** - The Manager automatically caches Java installations, but ensure your test environment has sufficient disk space.

5. **Use code coverage selectively** - Enable code coverage only when needed as it adds overhead.

6. **Specify versions explicitly** - Always specify Java version, type, and JVM explicitly for reproducible tests.

7. **Clean up test artifacts** - Ensure test artifacts are cleaned up after tests complete.

8. **Use unique tags** - When using multiple Java installations, use unique tags to avoid conflicts.

9. **Configure download locations** - Ensure Java archive download locations are accessible from your test environment.

10. **Test with multiple versions** - If your application supports multiple Java versions, test with all supported versions.

11. **Use PowerShell-compatible archives** - Ensure ZIP archives can be extracted using PowerShell's `Expand-Archive` cmdlet.