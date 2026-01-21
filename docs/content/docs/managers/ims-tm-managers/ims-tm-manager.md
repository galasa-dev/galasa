---
title: "IMS TM Manager"
---

This Manager is at Alpha level. You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/imstm/package-summary.html){target="_blank"}.


## Overview

This Manager provides IMS TM functions to Galasa tests. 


## Testing in IMS Systems on z/OS

To connect your Galasa test to a developer supplied environment with a provisioned IMS system as a minimum you need to configure the following property for the IMS TM Manager: 

```properties
imstm.dse.tag.[TAG].applid=[APPLID]
```

You also need to configure the following properties for the [z/OS Manager](../zos-managers/zos-manager.md) as a minimum to connect to an IMS system, even if you do not reference a `@ZosImage` in your Galasa test. This is because IMS systems sit on a z/OS LPAR, and so to provision and connect to an IMS system in a test, you also need access to the z/OS image that it sits within to make requests on the IMS system. You might need to configure additional z/OS-related CPS properties, depending on your test.

```properties
zos.dse.tag.[tag].imageid=[IMAGEID]
    OR zos.tag.[tag].imageid=[IMAGEID] 
    OR zos.cluster.[CLUSTERID].images=[IMAGEID] (AND zos.dse.tag.[tag].clusterid=[CLUSTERID] if [CLUSTERID] is not DEFAULT)
zos.image.[IMAGEID].ipv4.hostname=[IP ADDRESS]
zos.image.[IMAGEID].credentials=[CREDENTIALID]
```


## Configuration Properties

The following are properties that are used to configure the IMS TM Manager in the CPS.


### Developer Supplied Environment - IMS TM System - Type

| Property: | Developer Supplied Environment - IMS TM System - Type |
| --------------------------------------- | :------------------------------------- |
| Name: | imstm.provision.type |
| Description: | Provisioners will use this property to determine if they should participate in provisioning. The DSE provisioner responds to `dse` and `mixed` (case insensitive). |
| Required:  | No |
| Default value: | dse |
| Valid values: | Any |
| Examples: | `imstm.provision.type=dse` |


### Developer Supplied Environment - IMS TM System - Applid

| Property: | Developer Supplied Environment - IMS TM System - Applid |
| --------------------------------------- | :------------------------------------- |
| Name: | imstm.dse.tag.[TAG].applid |
| Description: | Provides the applid of the IMS TM system for the DSE provisioner. The applid setting is mandatory for a DSE system. This property is ignored if you set the `imstm.provision.type` property to specify any value other than `dse` or `mixed`. |
| Required:  | Yes if you want a DSE system, otherwise not required. |
| Default value: | None |
| Valid values: | A valid VTAM applid |
| Examples: | `imstm.dse.tag.PRIMARY.applid=IM1A`  |


### Developer Supplied Environment - IMS TM System - Version

| Property: | Developer Supplied Environment - IMS TM System - Version |
| --------------------------------------- | :------------------------------------- |
| Name: | imstm.dse.tag.version<br>imstm.dse.tag.[TAG].version |
| Description: | Provides the version of the IMS TM system to user tests. |
| Required:  | Only requires setting if a test requests it. |
| Default value: | None |
| Valid values: | A valid V.R.M version format, e.g. 15.5.0 |
| Examples: | `imstm.dse.tag.PRIMARY.version=15.5.0` |


## Provided annotation

The following annotations are available with the IMS TM Manager


### IMS System

| Annotation: | IMS System |
| --------------------------------------- | :------------------------------------- |
| Name: | @ImsSystem |
| Description: | The `@ImsSystem` annotation requests the IMS TM Manager to provide an IMS TM System associated with a z/OS image.  The test can request multiple IMS Systems. |
| Attribute: `imsTag` |  The `imsTag` is used to identify the IMS System. Optional. The default value is **PRIMARY**. |
| Attribute: `imageTag` |  The `imageTag` is used to identify the associated z/OS image. Optional. The default value is **PRIMARY** |
| Syntax: | <pre lang="java">@ImsSystem(imsTag="A", imageTag="MVSA")<br>public IImsSystem imsSystemA;</pre> |
| Notes: | The `IImsSystem` interface defines `getTag()`, `getApplid()`, `getVersion()`, and `getZosImage()` methods for accessing the IMS System's attributes. The behaviour of the remaining methods are dependent on the provisioner that supplies the `IImsSystem` object. For the DSE provisioner, `isProvisionStart()` always returns `true`, while `startup()` and `shutdown()` always throw an exception. |


### IMS Terminal

| Annotation: | IMS Terminal |
| --------------------------------------- | :------------------------------------- |
| Name: | @ImsTerminal |
| Description: | The `@ImsTerminal` annotation requests the IMS TM Manager to provide a 3270 terminal associated with an IMS System.  The test can request multiple IMS Terminals for each IMS System. Each `@ImsTerminal` annotation requires a corresponding `@ImsSystem` annotation in the same test class. |
| Attribute: `imsTag` |  The `imsTag` is used to identify the IMS System. Optional. The default value is **PRIMARY**. |
| Attribute: `connectAtStartup` |  If `connectAtStartup=true` the terminal will be connected and signed on to the associated IMS System when control is passed to the test. Optional. The default value is **true** |
| Attribute: `loginCredentialsTag` |  The `loginCredentialsTag` is used to identify the credentials that will be used to sign on to the IMS System. Required.
| Syntax: | <pre lang="java">@ImsTerminal(imsTag="A", connectAtStartup=true, loginCredentialsTag="USER01")<br>public IImsTerminal terminalA;</pre> |
| Notes: | The `IImsTerminal` interface defines `getImsSystem()`, `connectToImsSystem()`, `resetAndClear()`, and `getLoginCredentialsTag()` methods in addition to all methods defined for the `ITerminal` interface. |

