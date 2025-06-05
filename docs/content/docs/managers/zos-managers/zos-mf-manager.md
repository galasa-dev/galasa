---
title: "zOS MF Manager"
---

This Manager is at Beta level. You can view the <a href="https://javadoc.galasa.dev/overview-summary.html" target="_blank" rel="noopener noreferrer">Javadoc documentation for the Manager here</a>.<br>


[Overview](#overview)<br>
[Provided annotation](#annotations)<br>
[Configuring](#configuring)<br>


# <a name="overview"></a>Overview
This Manager provides Galasa tests with access to a zOS/MF server. Use the z/OS MF Manager to simplify areas of z/OS system management. The z/OS MF Manager can be called from a test or from other Managers. For example, the z/OS Manager calls the z/OS MF Manager to implement z/OS file, console and batch functions via the relevant interface.


## <a name="overview"></a>Provided annotations

The following annotations are available with the zOS MF Manager
<details>
<summary>z/OS MF</summary>

| Annotation: | z/OS MF |
| --------------------------------------- | :------------------------------------- |
| Name: | @Zosmf |
| Description: | The <code>@Zosmf</code> annotation requests the z/OSMF Manager to provide a z/OSMF instance associated with a z/OS image.  The test can request multiple z/OSMF instances, with the default being associated with the <b>primary</b> zOS image. |
| Attribute: `imageTag` |  The tag of the zOS Image this variable is to be populated with |
| Syntax: | @ZosImage(imageTag="A")<br> public IZosImage zosImageA;<br> @Zosmf(imageTag="A")<br> public IZosmf zosmfA;<br></code> |
| Notes: | The <code>IZosmf</code> interface has a number of methods to issue requests to the zOSMF REST API. See <a href="https://javadoc.galasa.dev/dev/galasa/zosmf/Zosmf.html" target="_blank">Zosmf</a> and <a href="https://javadoc.galasa.dev/dev/galasa/zosmf/IZosmf.html" target="_blank">IZosmf</a> to find out more. |

</details>




## <a name="configuring"></a>Configuration Properties

The following are properties used to configure the zOS MF Manager.
 
<details>
<summary>zOSMF Server port is https</summary>

| Property: | zOSMF Server port is https |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.[imageid].https |
| Description: | Use https (SSL) for zOSMF server |
| Required:  | No |
| Default value: | true |
| Valid values: | true or false |
| Examples: | <code>zosmf.server.https=true</code><br> <code>zosmf.server.SYSA.https=true</code> |

</details>
 
<details>
<summary>zOSMF Image Servers</summary>

| Property: | zOSMF Image Servers |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.image.IMAGEID.servers |
| Description: | The zOSMF servers for use with z/OS Image, the zOS/MF do not need to be running the actual z/OS Image |
| Required:  | No |
| Default value: | None |
| Valid values: | Comma separated zOS/MF server IDs |
| Examples: | <code>zosmf.image.MYLPAR.servers=MFSYSA,MFSYSB</code><br> |

</details>
 
<details>
<summary>zOSMF Server retry request</summary>

| Property: | zOSMF Server retry request |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.[SERVERID].request.retry |
| Description: | The number of times to retry when zOSMF request fails |
| Required:  | No |
| Default value: | 3 |
| Valid values: | numerical value > 0 |
| Examples: | <code>zosmf.server.request.retry=5</code><br> <code>zosmf.server.MFSYSA.request.retry=5</code> |

</details>
 
<details>
<summary>zOSMF Server Credentials</summary>

| Property: | zOSMF Server Credentials |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.[SERVERID].credentials |
| Description: | The z/OS credentials to use when accessing the zOS/MF server |
| Required:  | No |
| Default value: | None, however the zOS/MF Manager will use the default z/OS image credentials |
| Valid values: | Valid credential ID |
| Examples: | <code>zosmf.server.MFSYSA.credentials=ZOS</code><br> |

</details>
 
<details>
<summary>zOSMF Server Image</summary>

| Property: | zOSMF Server Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.SERVERID.image |
| Description: | The z/OS image ID this zOS/MF server lives on |
| Required:  | No |
| Default value: | The SERVERID value is used as the z/OS image ID |
| Valid values: | z/OS image IDs |
| Examples: | <code>zosmf.server.MFSYSA.image=SYSA</code><br> |

</details>
 
<details>
<summary>zOSMF Server port</summary>

| Property: | zOSMF Server port |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.[serverid].port |
| Description: | The port number of the zOS/MF server |
| Required:  | No |
| Default value: | 443 |
| Valid values: | A valid IP port number |
| Examples: | <code>zosmf.server.port=443</code><br> <code>zosmf.server.MFSYSA.port=443</code> |

</details>
 
<details>
<summary>zOSMF Sysplex Servers</summary>

| Property: | zOSMF Sysplex Servers |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.sysplex.[SYSPLEXID].default.servers |
| Description: | The zOSMF servers active on the supplied sysplex |
| Required:  | No |
| Default value: | None |
| Valid values: | Comma separated zOS/MF server IDs |
| Examples: | <code>zosmf.sysplex.default.servers=MFSYSA,MFSYSB</code><br> <code>zosmf.sysplex.PLEXA.default.servers=MFSYSA,MFSYSB</code> |

</details>
