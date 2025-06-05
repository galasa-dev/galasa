---
title: "IMS TM Manager"
---

This Manager is at Alpha level. You can view the <a href="https://javadoc.galasa.dev/dev/galasa/imstm/package-summary.html" target="_blank" rel="noopener noreferrer">Javadoc documentation for the Manager here</a>.<br>


[Overview](#overview)<br>
[Configuring](#configuring)<br>
[Provided annotation](#annotations)<br>


# <a name="overview"></a>Overview

This Manager provides IMS TM functions to Galasa tests. 

## Testing in IMS Systems on z/OS

To connect your Galasa test to a developer supplied environment with a provisioned IMS system as a minimum you need to configure the following property for the IMS TM Manager: 

```
imstm.dse.tag.[TAG].applid=[APPLID]
```

You also need to configure the following properties for the [z/OS Manager](zos-manager) as a minimum to connect to an IMS system, even if you do not reference a `@ZosImage` in your Galasa test. This is because IMS systems sit on a z/OS LPAR, and so to provision and connect to an IMS system in a test, you also need access to the z/OS image that it sits within to make requests on the IMS system. You might need to configure additional z/OS-related CPS properties, depending on your test.

```
zos.dse.tag.[tag].imageid=[IMAGEID]
    OR zos.tag.[tag].imageid=[IMAGEID] 
    OR zos.cluster.[CLUSTERID].images=[IMAGEID] (AND zos.dse.tag.[tag].clusterid=[CLUSTERID] if [CLUSTERID] is not DEFAULT)
zos.image.[IMAGEID].ipv4.hostname=[IP ADDRESS]
zos.image.[IMAGEID].credentials=[CREDENTIALID]
```


## <a name="configuring"></a>Configuration Properties

The following are properties that are used to configure the IMS TM Manager in the CPS.


<details>
<summary>Developer Supplied Environment - IMS TM System - Type</summary>

| Property: | Developer Supplied Environment - IMS TM System - Type |
| --------------------------------------- | :------------------------------------- |
| Name: | imstm.provision.type |
| Description: | Provisioners will use this property to determine if they should participate in provisioning. The DSE provisioner responds to `dse` and `mixed` (case insensitive). |
| Required:  | No |
| Default value: | dse |
| Valid values: | Any |
| Examples: | <code>imstm.provision.type=dse</code><br> |

</details>
 
<details>
<summary>Developer Supplied Environment - IMS TM System - Applid</summary>

| Property: | Developer Supplied Environment - IMS TM System - Applid |
| --------------------------------------- | :------------------------------------- |
| Name: | imstm.dse.tag.[TAG].applid |
| Description: | Provides the applid of the IMS TM system for the DSE provisioner. The applid setting is mandatory for a DSE system. This property is ignored if you set the <code>imstm.provision.type</code> property to specify any value other than `dse` or `mixed`. |
| Required:  | Yes if you want a DSE system, otherwise not required. |
| Default value: | None |
| Valid values: | A valid VTAM applid |
| Examples: | <code>imstm.dse.tag.PRIMARY.applid=IM1A</code><br>  |

</details>
 
<details>
<summary>Developer Supplied Environment - IMS TM System - Version</summary>

| Property: | Developer Supplied Environment - IMS TM System - Version |
| --------------------------------------- | :------------------------------------- |
| Name: | imstm.dse.tag.version<br>imstm.dse.tag.[TAG].version |
| Description: | Provides the version of the IMS TM system to user tests. |
| Required:  | Only requires setting if a test requests it. |
| Default value: | None |
| Valid values: | A valid V.R.M version format, e.g. 15.5.0 |
| Examples: | <code>imstm.dse.tag.PRIMARY.version=15.5.0</code><br> |

</details>

# <a name="annotations"></a>Provided annotation

The following annotations are available with the IMS TM Manager

<details>
<summary>IMS System</summary>

| Annotation: | IMS System |
| --------------------------------------- | :------------------------------------- |
| Name: | @ImsSystem |
| Description: | The <code>@ImsSystem</code> annotation requests the IMS TM Manager to provide an IMS TM System associated with a z/OS image.  The test can request multiple IMS Systems. |
| Attribute: `imsTag` |  The <code>imsTag</code> is used to identify the IMS System. Optional. The default value is <b>PRIMARY</b>. |
| Attribute: `imageTag` |  The <code>imageTag</code> is used to identify the associated z/OS image. Optional. The default value is <b>PRIMARY</b> |
| Syntax: | <code>@ImsSystem(imsTag="A", imageTag="MVSA")<br>public IImsSystem imsSystemA;</code> |
| Notes: | The <code>IImsSystem</code> interface defines <code>getTag()</code>, <code>getApplid()</code>, <code>getVersion()</code>, and <code>getZosImage()</code> methods for accessing the IMS System's attributes. The behaviour of the remaining methods are dependent on the provisioner that supplies the <code>IImsSystem</code> object. For the DSE provisioner, <code>isProvisionStart()</code> always returns <code>true</code>, while <code>startup()</code> and <code>shutdown()</code> always throw an exception. |

</details>

<details>
<summary>IMS Terminal</summary>

| Annotation: | IMS Terminal |
| --------------------------------------- | :------------------------------------- |
| Name: | @ImsTerminal |
| Description: | The <code>@ImsTerminal</code> annotation requests the IMS TM Manager to provide a 3270 terminal associated with an IMS System.  The test can request multiple IMS Terminals for each IMS System. Each <code>@ImsTerminal</code> annotation requires a corresponding <code>@ImsSystem</code> annotation in the same test class. |
| Attribute: `imsTag` |  The <code>imsTag</code> is used to identify the IMS System. Optional. The default value is <b>PRIMARY</b>. |
| Attribute: `connectAtStartup` |  If <code>connectAtStartup=true</code> the terminal will be connected and signed on to the associated IMS System when control is passed to the test. Optional. The default value is <b>true</b> |
| Attribute: `loginCredentialsTag` |  The <code>loginCredentialsTag</code> is used to identify the credentials that will be used to sign on to the IMS System. Required.
| Syntax: | <code>@ImsTerminal(imsTag="A", connectAtStartup=true, loginCredentialsTag="USER01")<br>public IImsTerminal terminalA;</code> |
| Notes: | The <code>IImsTerminal</code> interface defines <code>getImsSystem()</code>, <code>connectToImsSystem()</code>, <code>resetAndClear()</code>, and <code>getLoginCredentialsTag()</code> methods in addition to all methods defined for the <code>ITerminal</code> interface. |

</details>
