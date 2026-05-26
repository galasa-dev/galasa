---
title: "CICS TS Resource Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/cicsts/cicsresource/ICicsResource.html){target="_blank"}.


## Overview

The CICS TS Resource Manager provides Galasa tests with the capability to manage CICS resources such as CICS Bundles and JVM servers. This Manager enables tests to create, configure, deploy, enable, disable, and delete CICS resources programmatically.

The CICS TS Resource Manager is an internal Manager and is enabled automatically by the CICS TS Manager when required. It provides comprehensive methods to manage the lifecycle of CICS resources including JVM servers, CICS bundles, and JVM profiles.


## Annotations

The CICS TS Resource Manager does not provide any annotations. Access to resource functionality is obtained through the CICS TS Manager's `ICicsRegion` interface.


## Code Snippets

### Obtain CICS Resource interface from a CICS region

```java
@CicsRegion(cicsTag = "PRIMARY")
public ICicsRegion cicsRegion;

@Cics3270Terminal(cicsTag = "PRIMARY")
public ICicsTerminal cicsTerminal;

@Test
public void testCicsResources() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    
    // Now you can create and manage CICS resources
}
```

### Create and deploy a CICS Bundle

```java
@Test
public void testCreateCicsBundle() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    
    // Create a CICS Bundle from test resources
    ICicsBundle bundle = cicsResource.newCicsBundle(
        cicsTerminal,
        this.getClass(),
        "MYBUNDLE",
        "TESTGRP",
        "/bundles/myapp",
        null  // No substitution parameters
    );
    
    // Build and deploy the bundle
    bundle.build();
    
    logger.info("CICS Bundle deployed and enabled");
}
```

### Create a CICS Bundle for an existing bundle directory

```java
@Test
public void testExistingBundle() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    
    // Reference an existing bundle on zOS UNIX
    ICicsBundle bundle = cicsResource.newCicsBundle(
        cicsTerminal,
        this.getClass(),
        "MYBUNDLE",
        "TESTGRP",
        "/u/bundles/myapp"  // Existing bundle directory
    );
    
    bundle.buildInstallResourceDefinition();
    bundle.enable();
}
```

### Create and manage a JVM server

```java
@Test
public void testCreateJvmServer() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    
    // Create a JVM profile
    IJvmprofile jvmprofile = cicsResource.newJvmprofile("MYJVMPRF");
    jvmprofile.setProfileValue("JAVA_HOME", "/usr/lpp/java/J8.0_64");
    jvmprofile.setProfileValue("WORK_DIR", "/u/cicsts/work");
    
    // Create a JVM server
    IJvmserver jvmserver = cicsResource.newJvmserver(
        cicsTerminal,
        "MYJVMSRV",
        "TESTGRP",
        jvmprofile
    );
    
    // Build and enable the JVM server
    jvmserver.build();
    
    logger.info("JVM server created and enabled");
}
```

### Create a JVM server with default profile

```java
@Test
public void testJvmServerWithDefaults() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    
    // Create a JVM server using CICS-supplied profile
    IJvmserver jvmserver = cicsResource.newJvmserver(
        cicsTerminal,
        "OSGI01",
        "TESTGRP",
        "DFHOSGI",
        JvmserverType.OSGI
    );
    
    jvmserver.build();
}
```

### Manage JVM server lifecycle

```java
@Test
public void testJvmServerLifecycle() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    IJvmserver jvmserver = cicsResource.newJvmserver(
        cicsTerminal, "MYJVMSRV", "TESTGRP", "MYJVMPRF", JvmserverType.OSGI
    );
    
    // Build and enable
    jvmserver.build();
    
    // Check if enabled
    if (jvmserver.isEnabled()) {
        logger.info("JVM server is enabled");
    }
    
    // Disable with escalation if needed
    PurgeType purgeType = jvmserver.disableWithEscalate();
    logger.info("JVM server disabled with: " + purgeType);
    
    // Re-enable
    jvmserver.enable();
    jvmserver.waitForEnable();
}
```

### Access JVM server logs

```java
@Test
public void testJvmServerLogs() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    IJvmserver jvmserver = cicsResource.newJvmserver(
        cicsTerminal, "MYJVMSRV", "TESTGRP", "MYJVMPRF", JvmserverType.OSGI
    );
    
    jvmserver.build();
    
    // Checkpoint logs before test
    jvmserver.checkpointLogs();
    
    // Run your test...
    
    // Get logs
    IJvmserverLog jvmLog = jvmserver.getJvmLog();
    IJvmserverLog stdOut = jvmserver.getStdOut();
    IJvmserverLog stdErr = jvmserver.getStdErr();
    
    // Save logs to results archive
    jvmserver.saveToResultsArchive();
}
```

### Create a Liberty JVM server

```java
@ZosImage(imageTag = "PRIMARY")
public IZosImage zosImage;

@ZosLibertyServer(imageTag = "PRIMARY")
public IZosLibertyServer libertyServer;

@Test
public void testLibertyJvmServer() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    
    // Create JVM profile for Liberty
    IJvmprofile jvmprofile = cicsResource.newJvmprofile("WLPPROF");
    jvmprofile.setProfileValue("JAVA_HOME", "/usr/lpp/java/J8.0_64");
    jvmprofile.setProfileValue("WLP_USER_DIR", libertyServer.getWlpUserDir().getUnixPath());
    jvmprofile.setProfileValue("WLP_OUTPUT_DIR", libertyServer.getWlpOutputDir().getUnixPath());
    
    // Create Liberty JVM server
    IJvmserver jvmserver = cicsResource.newLibertyJvmserver(
        cicsTerminal,
        "WLPJVM",
        "TESTGRP",
        jvmprofile,
        libertyServer
    );
    
    jvmserver.build();
}
```

### Configure JVM profile with multiple options

```java
@Test
public void testJvmProfileConfiguration() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    
    // Create profile with Map
    Map<String, String> profileOptions = new HashMap<>();
    profileOptions.put("JAVA_HOME", "/usr/lpp/java/J8.0_64");
    profileOptions.put("WORK_DIR", "/u/cicsts/work");
    profileOptions.put("CLASSPATH", "/u/myapp/lib/*");
    profileOptions.put("-Xmx", "512m");
    profileOptions.put("-Xms", "256m");
    
    IJvmprofile jvmprofile = cicsResource.newJvmprofile("MYJVMPRF", profileOptions);
    
    // Or create and add options individually
    IJvmprofile jvmprofile2 = cicsResource.newJvmprofile("MYJVMPRF2");
    jvmprofile2.setProfileValue("JAVA_HOME", "/usr/lpp/java/J8.0_64");
    jvmprofile2.setProfileValue("WORK_DIR", "/u/cicsts/work");
}
```

### Manage CICS Bundle lifecycle

```java
@Test
public void testBundleLifecycle() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    
    ICicsBundle bundle = cicsResource.newCicsBundle(
        cicsTerminal, this.getClass(), "MYBUNDLE", "TESTGRP", "/bundles/myapp", null
    );
    
    // Deploy and install
    bundle.build();
    
    // Check status
    if (bundle.isEnabled()) {
        logger.info("Bundle is enabled");
    }
    
    // Disable, discard and re-install (useful for updates)
    bundle.disableDiscardInstall();
    
    // Clean up
    bundle.disableDiscardDelete();
}
```

### Set JVM server thread limit

```java
@Test
public void testThreadLimit() throws Exception {
    ICicsResource cicsResource = cicsRegion.cicsResource();
    IJvmserver jvmserver = cicsResource.newJvmserver(
        cicsTerminal, "MYJVMSRV", "TESTGRP", "MYJVMPRF", JvmserverType.OSGI
    );
    
    jvmserver.build();
    
    // Set thread limit
    jvmserver.setThreadLimit(50);
    
    // Get current thread count
    int threadCount = jvmserver.getThreadCount();
    logger.info("Current thread count: " + threadCount);
}
```


## Configuration Properties

The following are properties used to configure the CICS TS Resource Manager.

### Default resource timeout

| Property: | Default resource timeout |
| --------------------------------------- | :------------------------------------- |
| Name: | `cicsresource.default.[imageid].timeout` |
| Description: | Provides a value for the default timeout in seconds for JVM servers on a z/OS image. This timeout is used when waiting for JVM servers to enable or disable. |
| Required:  | No |
| Default value: | `120` (seconds) |
| Valid values: | A positive integer representing seconds |
| Examples: | `cicsresource.default.MVSA.timeout=120`<br>`cicsresource.default.MVSA.timeout=300` |


## Resource Types

### ICicsResource

The main interface for creating CICS resources. Obtained from `ICicsRegion.cicsResource()`.

[View ICicsResource Javadoc](../../reference/javadoc/dev/galasa/cicsts/cicsresource/ICicsResource.html)

### ICicsBundle

Represents a CICS Bundle resource with methods to manage its lifecycle.

[View ICicsBundle Javadoc](../../reference/javadoc/dev/galasa/cicsts/cicsresource/ICicsBundle.html)

### IJvmserver

Represents a CICS JVM server resource with comprehensive lifecycle management.

[View IJvmserver Javadoc](../../reference/javadoc/dev/galasa/cicsts/cicsresource/IJvmserver.html)

### IJvmprofile

Represents a JVM profile with methods to configure JVM options.

[View IJvmprofile Javadoc](../../reference/javadoc/dev/galasa/cicsts/cicsresource/IJvmprofile.html)