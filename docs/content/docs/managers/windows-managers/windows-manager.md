---
title: "Windows Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/windows/package-summary.html){target="_blank"}.


## Overview

The Windows Manager provides Galasa tests with access to Windows images. This Manager enables tests to connect to Windows systems, execute commands, access the file system, and manage Windows-based resources.

The Windows Manager provides provisioning capabilities for Windows images through either Developer Supplied Environment (DSE) provisioning or through cloud provisioners like OpenStack.


## Dependencies

The Windows Manager has a dependency on the following Managers:

- IP Network Manager


## Annotations

The following annotations are available with the Windows Manager.


### Windows Image

| Annotation: | Windows Image |
| --------------------------------------- | :------------------------------------- |
| Name: | `@WindowsImage` |
| Description: | The `@WindowsImage` annotation requests the Windows Manager to allocate a Windows image to the test. The test can then access the command shell and interact with the Windows system. |
| Attribute: `imageTag` |  The `imageTag` is used to identify the Windows image to other Managers. If a test is using multiple Windows images, each separate Windows image must have a unique tag. Default is "PRIMARY". |
| Attribute: `capabilities` |  The `capabilities` attribute specifies any required capabilities of the image, if any, in an array. |
| Syntax: | <pre lang="java">@WindowsImage(imageTag = "PRIMARY")<br>public IWindowsImage windowsImage;<br></pre> |
| Notes: | The `IWindowsImage` interface gives the test access to the Windows image, including command shell access, file system paths, and credentials.<br><br> See [WindowsImage](https://galasa.dev/docs/reference/javadoc/dev/galasa/windows/WindowsImage.html){target="_blank"} and [IWindowsImage](https://galasa.dev/docs/reference/javadoc/dev/galasa/windows/IWindowsImage.html){target="_blank"} to find out more. |


### Windows IP Host

| Annotation: | Windows IP Host |
| --------------------------------------- | :------------------------------------- |
| Name: | `@WindowsIpHost` |
| Description: | The `@WindowsIpHost` annotation represents an IP Host for a Windows image that has been provisioned for the test. |
| Attribute: `imageTag` |  The `imageTag` should match the `imageTag` of the `@WindowsImage` this variable is to be populated with. |
| Syntax: | <pre lang="java">@WindowsIpHost(imageTag = "PRIMARY")<br>public IIpHost windowsHost;<br></pre> |
| Notes: | The `IIpHost` interface gives the test access to the IPv4/6 address and port information for the Windows image.<br><br> See [WindowsIpHost](https://galasa.dev/docs/reference/javadoc/dev/galasa/windows/WindowsIpHost.html){target="_blank"} and [IIpHost](https://galasa.dev/docs/reference/javadoc/dev/galasa/ipnetwork/IIpHost.html){target="_blank"} to find out more. |


## Code Snippets

### Connect to a Windows image

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@Test
public void testWindowsConnection() throws Exception {
    // Get the image ID
    String imageId = windowsImage.getImageID();
    logger.info("Connected to Windows image: " + imageId);
    
    // Get the IP host
    IIpHost ipHost = windowsImage.getIpHost();
    logger.info("Windows host: " + ipHost.getHostname());
}
```

### Execute commands on Windows

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@Test
public void testExecuteCommand() throws Exception {
    ICommandShell shell = windowsImage.getCommandShell();
    
    // Execute a simple command
    String response = shell.issueCommand("echo Hello from Windows");
    logger.info("Response: " + response);
    
    // Execute a PowerShell command
    String psResponse = shell.issueCommand("powershell -Command \"Get-Date\"");
    logger.info("Current date: " + psResponse);
}
```

### Access file system paths

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@Test
public void testFilePaths() throws Exception {
    // Get standard Windows paths
    Path root = windowsImage.getRoot();           // C:\
    Path home = windowsImage.getHome();           // C:\Users\<user>
    Path temp = windowsImage.getTmp();            // C:\Users\<user>\AppData\Local\Temp
    Path runDir = windowsImage.getRunDirectory(); // Test-specific run directory
    
    logger.info("Root: " + root);
    logger.info("Home: " + home);
    logger.info("Temp: " + temp);
    logger.info("Run directory: " + runDir);
}
```

### Get Windows credentials

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@Test
public void testCredentials() throws Exception {
    ICredentials credentials = windowsImage.getDefaultCredentials();
    
    if (credentials instanceof ICredentialsUsernamePassword) {
        ICredentialsUsernamePassword userPass = (ICredentialsUsernamePassword) credentials;
        String username = userPass.getUsername();
        logger.info("Windows username: " + username);
    }
}
```

### Work with files

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@Test
public void testFileOperations() throws Exception {
    ICommandShell shell = windowsImage.getCommandShell();
    Path runDir = windowsImage.getRunDirectory();
    
    // Create a file
    Path testFile = runDir.resolve("test.txt");
    shell.issueCommand("echo Test content > " + testFile.toString());
    
    // Read the file
    String content = shell.issueCommand("type " + testFile.toString());
    logger.info("File content: " + content);
    
    // Delete the file
    shell.issueCommand("del " + testFile.toString());
}
```

### Use multiple Windows images

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage primaryWindows;

@WindowsImage(imageTag = "SECONDARY")
public IWindowsImage secondaryWindows;

@Test
public void testMultipleImages() throws Exception {
    // Execute command on primary
    ICommandShell primaryShell = primaryWindows.getCommandShell();
    String primaryResponse = primaryShell.issueCommand("hostname");
    logger.info("Primary hostname: " + primaryResponse);
    
    // Execute command on secondary
    ICommandShell secondaryShell = secondaryWindows.getCommandShell();
    String secondaryResponse = secondaryShell.issueCommand("hostname");
    logger.info("Secondary hostname: " + secondaryResponse);
}
```

### Request specific capabilities

```java
@WindowsImage(imageTag = "PRIMARY", capabilities = {"desktop", "office"})
public IWindowsImage windowsImage;

@Test
public void testWithCapabilities() throws Exception {
    // This image will have desktop and office capabilities
    ICommandShell shell = windowsImage.getCommandShell();
    
    // Verify Office is installed
    String response = shell.issueCommand(
        "powershell -Command \"Get-ItemProperty 'HKLM:\\Software\\Microsoft\\Office'\""
    );
    
    logger.info("Office info: " + response);
}
```

### Get IP host information

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@WindowsIpHost(imageTag = "PRIMARY")
public IIpHost windowsHost;

@Test
public void testIpHost() throws Exception {
    // Get hostname
    String hostname = windowsHost.getHostname();
    logger.info("Hostname: " + hostname);
    
    // Get IP address
    String ipAddress = windowsHost.getIpv4Address();
    logger.info("IP Address: " + ipAddress);
    
    // Get port information (if applicable)
    int port = windowsHost.getPort();
    logger.info("Port: " + port);
}
```

### Run PowerShell scripts

```java
@WindowsImage(imageTag = "PRIMARY")
public IWindowsImage windowsImage;

@Test
public void testPowerShellScript() throws Exception {
    ICommandShell shell = windowsImage.getCommandShell();
    Path runDir = windowsImage.getRunDirectory();
    
    // Create a PowerShell script
    Path scriptPath = runDir.resolve("test.ps1");
    shell.issueCommand("echo Get-Process > " + scriptPath.toString());
    
    // Execute the script
    String response = shell.issueCommand(
        "powershell -ExecutionPolicy Bypass -File " + scriptPath.toString()
    );
    
    logger.info("Running processes: " + response);
}
```


## Configuration Properties

The following are properties used to configure the Windows Manager.

### DSE Windows image host ID

| Property: | DSE Windows image host ID |
| --------------------------------------- | :------------------------------------- |
| Name: | `windows.dse.tag.[imageTag].hostid` |
| Description: | Specifies the host ID for a Developer Supplied Environment Windows image. This property maps an image tag to a specific Windows host. |
| Required:  | Yes, if using DSE provisioning |
| Valid values: | A valid host ID string |
| Examples: | `windows.dse.tag.PRIMARY.hostid=WINHOST1` |

### Windows image hostname

| Property: | Windows image hostname |
| --------------------------------------- | :------------------------------------- |
| Name: | `windows.image.[imageid].ipv4.hostname` |
| Description: | The hostname or IP address of the Windows image. |
| Required:  | Yes |
| Valid values: | A valid DNS name or IPv4/6 address |
| Examples: | `windows.image.WINHOST1.ipv4.hostname=192.168.1.100`<br>`windows.image.WINHOST1.ipv4.hostname=windows-server.example.com` |

### Windows image credentials

| Property: | Windows image credentials |
| --------------------------------------- | :------------------------------------- |
| Name: | `windows.image.[imageid].credentials` |
| Description: | The credentials tag for accessing the Windows image. The credentials should be stored in the Galasa Credentials Store. |
| Required:  | Yes, if credentials are required |
| Valid values: | A valid credentials ID |
| Examples: | `windows.image.WINHOST1.credentials=WINCREDS` |

### Retain run directory

| Property: | Retain run directory |
| --------------------------------------- | :------------------------------------- |
| Name: | `windows.image.[imageid].retain.run.directory` |
| Description: | Specifies whether to retain the run directory on the Windows image after the test completes. |
| Required:  | No |
| Default value: | `false` |
| Valid values: | `true` or `false` |
| Examples: | `windows.image.WINHOST1.retain.run.directory=true` |

### Windows Manager extra bundles

| Property: | Windows Manager extra bundles |
| --------------------------------------- | :------------------------------------- |
| Name: | `windows.bundle.extra.managers` |
| Description: | Extra Galasa Managers that may be required to enable the Windows Manager. This may be required if your Windows images are provisioned through a cloud platform. |
| Required:  | No |
| Valid values: | A valid Galasa Manager package name or comma-separated list |
| Examples: | `windows.bundle.extra.managers=dev.galasa.openstack.manager` |


## Methods

### IWindowsImage Methods

```java
String getImageID()
```

Returns the ID of the Windows image.

**Returns:** The image ID

---

```java
IIpHost getIpHost()
```

Returns the IP Network Host details for the Windows image.

**Returns:** The `IIpHost` object

---

```java
ICredentials getDefaultCredentials() throws WindowsManagerException
```

Returns the default credentials for accessing the Windows image.

**Returns:** The credentials object

---

```java
ICommandShell getCommandShell() throws WindowsManagerException
```

Returns a command shell for executing commands on the Windows image.

**Returns:** The command shell

---

```java
Path getRoot() throws WindowsManagerException
```

Returns the root directory path (typically C:\).

**Returns:** The root path

---

```java
Path getHome() throws WindowsManagerException
```

Returns the home directory path for the current user.

**Returns:** The home directory path

---

```java
Path getTmp() throws WindowsManagerException
```

Returns the temporary directory path.

**Returns:** The temp directory path

---

```java
Path getRunDirectory() throws WindowsManagerException
```

Returns the test-specific run directory path.

**Returns:** The run directory path


## Best Practices

1. **Use DSE for known environments** - Use Developer Supplied Environment provisioning when you have specific Windows hosts to test against.

2. **Clean up resources** - Ensure files and processes created during tests are cleaned up, unless `retain.run.directory` is set to true.

3. **Handle Windows paths** - Remember to use backslashes or double backslashes in Windows paths.

4. **Use PowerShell for complex operations** - PowerShell provides more powerful scripting capabilities than cmd.exe.

5. **Set appropriate credentials** - Ensure Windows credentials are properly configured in the Credentials Store.

6. **Use unique image tags** - When using multiple Windows images, use unique tags to avoid conflicts.

7. **Check command responses** - Always check command responses for errors before proceeding.

8. **Use run directory for test files** - Store test-specific files in the run directory to avoid conflicts.

9. **Configure capabilities** - Use the capabilities attribute to request Windows images with specific software installed.

10. **Handle long-running commands** - Be aware that some Windows commands may take time to complete.