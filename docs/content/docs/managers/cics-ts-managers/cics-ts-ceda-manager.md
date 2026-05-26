---
title: "CICS TS CEDA Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/cicsts/ICeda.html){target="_blank"}.


## Overview

The CICS TS CEDA Manager provides Galasa tests with access to CEDA (CICS External Definition Attributes) 3270 interaction capabilities. This Manager enables tests to create, install, delete, and manage CICS resources through the CEDA transaction interface.

The CEDA Manager is an internal Manager and is enabled automatically by the CICS TS Manager when required. It provides methods to interact with CEDA through a 3270 terminal to manage CICS resource definitions.


## Annotations

The CEDA Manager does not provide any annotations. Access to CEDA functionality is obtained through the CICS TS Manager's `ICicsRegion` interface.


## Code Snippets

### Obtain CEDA interface from a CICS region

```java
@CicsRegion(cicsTag = "PRIMARY")
public ICicsRegion cicsRegion;

@Cics3270Terminal(cicsTag = "PRIMARY")
public ICicsTerminal cemtTerminal;

@Test
public void testCedaOperations() throws Exception {
    ICeda ceda = cicsRegion.ceda();
    
    // Create a CICS resource
    ceda.createResource(cemtTerminal, "PROGRAM", "MYPROG", "MYGROUP", 
        "LANGUAGE(COBOL)");
    
    // Install the group
    ceda.installGroup(cemtTerminal, "MYGROUP");
}
```

### Create and install a CICS resource

```java
@Test
public void testCreateAndInstallResource() throws Exception {
    ICeda ceda = cicsRegion.ceda();
    
    // Create a transaction resource
    ceda.createResource(cemtTerminal, "TRANSACTION", "TRN1", "TESTGRP", 
        "PROGRAM(PROG1)");
    
    // Install the specific resource
    ceda.installResource(cemtTerminal, "TRANSACTION", "TRN1", "TESTGRP");
}
```

### Check if a resource exists

```java
@Test
public void testResourceExists() throws Exception {
    ICeda ceda = cicsRegion.ceda();
    
    boolean exists = ceda.resourceExists(cemtTerminal, "PROGRAM", "MYPROG", "MYGROUP");
    
    if (exists) {
        logger.info("Resource MYPROG exists in group MYGROUP");
    }
}
```

### Delete a CICS resource

```java
@Test
public void testDeleteResource() throws Exception {
    ICeda ceda = cicsRegion.ceda();
    
    // Delete a specific resource
    ceda.deleteResource(cemtTerminal, "PROGRAM", "MYPROG", "MYGROUP");
    
    // Delete an entire group
    ceda.deleteGroup(cemtTerminal, "MYGROUP");
}
```

## Configuration Properties

The CEDA Manager does not have any specific CPS properties. It uses the CICS TS Manager configuration.