---
title: "CICS TS CEDA Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/cicsts/package-summary.html){target="_blank"}.


## Overview

The CICS TS CEDA Manager provides Galasa tests with access to CEDA (CICS External Definition Attributes) 3270 interaction capabilities. This Manager enables tests to create, install, delete, and manage CICS resources through the CEDA transaction interface.

The CEDA Manager is an internal Manager and is enabled automatically by the CICS TS Manager when required. It provides methods to interact with CEDA through a 3270 terminal to manage CICS resource definitions.


## Dependencies

The CEDA Manager has a dependency on the following Managers:

- CICS TS Manager
- z/OS 3270 Terminal Manager


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


## Methods

The CEDA Manager provides the following methods through the `ICeda` interface:

### createResource

```java
void createResource(ICicsTerminal terminal, String resourceType, 
                   String resourceName, String groupName, 
                   String resourceParameters) throws CedaException
```

Creates a CICS resource definition in the specified group.

**Parameters:**
- `terminal` - A 3270 terminal logged on to the CICS region
- `resourceType` - The type of resource (e.g., "PROGRAM", "TRANSACTION", "FILE")
- `resourceName` - The name of the resource to create
- `groupName` - The CEDA group name where the resource will be created
- `resourceParameters` - Additional parameters for the resource definition (can be null)

### installGroup

```java
void installGroup(ICicsTerminal terminal, String groupName) throws CedaException
```

Installs all resources in the specified CEDA group.

**Parameters:**
- `terminal` - A 3270 terminal logged on to the CICS region
- `groupName` - The name of the group to install

### installResource

```java
void installResource(ICicsTerminal terminal, String resourceType, 
                    String resourceName, String cedaGroup) throws CedaException
```

Installs a specific resource from a CEDA group.

**Parameters:**
- `terminal` - A 3270 terminal logged on to the CICS region
- `resourceType` - The type of resource to install
- `resourceName` - The name of the resource to install
- `cedaGroup` - The CEDA group containing the resource

### deleteGroup

```java
void deleteGroup(ICicsTerminal terminal, String groupName) throws CedaException
```

Deletes an entire CEDA group and all its resource definitions.

**Parameters:**
- `terminal` - A 3270 terminal logged on to the CICS region
- `groupName` - The name of the group to delete

### deleteResource

```java
void deleteResource(ICicsTerminal terminal, String resourceType, 
                   String resourceName, String groupName) throws CedaException
```

Deletes a specific resource definition from a CEDA group.

**Parameters:**
- `terminal` - A 3270 terminal logged on to the CICS region
- `resourceType` - The type of resource to delete
- `resourceName` - The name of the resource to delete
- `groupName` - The CEDA group containing the resource

### resourceExists

```java
boolean resourceExists(ICicsTerminal terminal, String resourceType, 
                      String resourceName, String groupName) throws CedaException
```

Checks if a resource exists in the specified CEDA group.

**Parameters:**
- `terminal` - A 3270 terminal logged on to the CICS region
- `resourceType` - The type of resource to check
- `resourceName` - The name of the resource to check
- `groupName` - The CEDA group to search

**Returns:** `true` if the resource exists, `false` otherwise


## Configuration Properties

The CEDA Manager does not have any specific CPS properties. It uses the CICS TS Manager configuration.