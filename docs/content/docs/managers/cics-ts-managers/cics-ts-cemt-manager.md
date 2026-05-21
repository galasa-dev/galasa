---
title: "CICS TS CEMT Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/cicsts/icemt.html){target="_blank"}.


## Overview

The CICS TS CEMT Manager provides Galasa tests with access to CEMT (CICS Execute Master Terminal) 3270 interaction capabilities. This Manager enables tests to inquire, set, discard, and manage CICS resources through the CEMT transaction interface.

The CEMT Manager is an internal Manager and is enabled automatically by the CICS TS Manager when required. It provides methods to interact with CEMT through a 3270 terminal to manage and monitor CICS resources at runtime.


## Annotations

The CEMT Manager does not provide any annotations. Access to CEMT functionality is obtained through the CICS TS Manager's `ICicsRegion` interface.


## Code Snippets

### Obtain CEMT interface from a CICS region

```java
@CicsRegion(cicsTag = "PRIMARY")
public ICicsRegion cicsRegion;

@Cics3270Terminal(cicsTag = "PRIMARY")
public ICicsTerminal cemtTerminal;

@Test
public void testCemtOperations() throws Exception {
    ICemt cemt = cicsRegion.cemt();
    
    // Inquire a resource
    CicstsHashMap resource = cemt.inquireResource(cemtTerminal, "PROGRAM", "MYPROG");
    
    if (resource != null) {
        logger.info("Program MYPROG found: " + resource.toString());
    }
}
```

### Set a resource state

```java
@Test
public void testSetResource() throws Exception {
    ICemt cemt = cicsRegion.cemt();
    
    // Disable a program
    CicstsHashMap result = cemt.setResource(cemtTerminal, "PROGRAM", "MYPROG", "DISABLED");
    
    logger.info("Program disabled: " + result.toString());
}
```

### Wait for a resource to become enabled

```java
@Test
public void testWaitForEnabled() throws Exception {
    ICemt cemt = cicsRegion.cemt();
    
    // Wait for a transaction to become enabled (uses default timeout)
    cemt.waitForEnabledResource(cemtTerminal, "TRANSACTION", "TRN1");
    
    logger.info("Transaction TRN1 is now enabled");
}
```

### Wait for a resource to become disabled with custom timeout

```java
@Test
public void testWaitForDisabledWithTimeout() throws Exception {
    ICemt cemt = cicsRegion.cemt();
    
    // Wait up to 60 seconds for a program to become disabled
    cemt.waitForDisabledResource(cemtTerminal, "PROGRAM", "MYPROG", 60000);
    
    logger.info("Program MYPROG is now disabled");
}
```

### Inquire resource with search text

```java
@Test
public void testInquireWithSearch() throws Exception {
    ICemt cemt = cicsRegion.cemt();
    
    // Check if a program is enabled
    boolean isEnabled = cemt.inquireResource(cemtTerminal, "PROGRAM", "MYPROG", "Ena");
    
    if (isEnabled) {
        logger.info("Program MYPROG is enabled");
    }
}
```

### Discard a resource

```java
@Test
public void testDiscardResource() throws Exception {
    ICemt cemt = cicsRegion.cemt();
    
    // Discard a program from memory
    cemt.discardResource(cemtTerminal, "PROGRAM", "MYPROG");
    
    logger.info("Program MYPROG has been discarded");
}
```

### Check if resource is enabled

```java
@Test
public void testIsResourceEnabled() throws Exception {
    ICemt cemt = cicsRegion.cemt();
    
    boolean enabled = cemt.isResourceEnabled(cemtTerminal, "TRANSACTION", "TRN1");
    
    if (enabled) {
        logger.info("Transaction TRN1 is enabled");
    } else {
        logger.info("Transaction TRN1 is not enabled");
    }
}
```

### Perform system property operation

```java
@Test
public void testSystemProperty() throws Exception {
    ICemt cemt = cicsRegion.cemt();
    
    // Set a system property and verify the response
    boolean success = cemt.performSystemProperty(cemtTerminal, 
        "SYSTEM", "DUMP", "DUMP COMPLETE");
    
    if (success) {
        logger.info("System dump completed successfully");
    }
}
```


## Configuration Properties

The following are properties used to configure the CEMT Manager.

### Default resource timeout

| Property: | Default resource timeout |
| --------------------------------------- | :------------------------------------- |
| Name: | `cemt.default.[imageid].timeout` |
| Description: | Provides a value for the default timeout in seconds for enabling/disabling resources on a z/OS image. This timeout is used by the `waitForEnabledResource` and `waitForDisabledResource` methods when no explicit timeout is provided. |
| Required:  | No |
| Default value: | `300` (seconds) |
| Valid values: | A positive integer representing seconds |
| Examples: | `cemt.default.MVSA.timeout=300`<br>`cemt.default.MVSA.timeout=600` |