---
title: "zOS MF Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/zosmf/package-summary.html){target="_blank"}.



## Overview

This Manager provides Galasa tests with access to a zOS/MF server. Use the z/OS MF Manager to simplify areas of z/OS system management. The z/OS MF Manager can be called from a test or from other Managers. For example, the z/OS Manager calls the z/OS MF Manager to implement z/OS file, console and batch functions via the relevant interface.


## Provided annotations

The following annotations are available with the zOS MF Manager


### z/OS MF

| Annotation: | z/OS MF |
| --------------------------------------- | :------------------------------------- |
| Name: | @Zosmf |
| Description: | The `@Zosmf` annotation requests the z/OSMF Manager to provide a z/OSMF instance associated with a z/OS image.  The test can request multiple z/OSMF instances, with the default being associated with the **primary** zOS image. |
| Attribute: `imageTag` |  The tag of the zOS Image this variable is to be populated with |
| Syntax: | <pre lang="java">@ZosImage(imageTag="A")<br>public IZosImage zosImageA;<br><br>@Zosmf(imageTag="A")<br>public IZosmf zosmfA;<br></pre> |
| Notes: | The `IZosmf` interface has a number of methods to issue requests to the zOSMF REST API. See [Zosmf](../../reference/javadoc/dev/galasa/zosmf/Zosmf.html){target="_blank"} and [IZosmf](../../reference/javadoc/dev/galasa/zosmf/IZosmf.html){target="_blank"} to find out more. |


## Configuration Properties

The following are properties used to configure the zOS MF Manager.
 

### zOSMF Server port is https

| Property: | zOSMF Server port is https |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.[imageid].https |
| Description: | Use https (SSL) for zOSMF server |
| Required:  | No |
| Default value: | true |
| Valid values: | true or false |
| Examples: | `zosmf.server.https=true`<br>`zosmf.server.SYSA.https=true` |


### zOSMF Image Servers

| Property: | zOSMF Image Servers |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.image.IMAGEID.servers |
| Description: | The zOSMF servers for use with z/OS Image, the zOS/MF do not need to be running the actual z/OS Image |
| Required:  | No |
| Default value: | None |
| Valid values: | Comma separated zOS/MF server IDs |
| Examples: | `zosmf.image.MYLPAR.servers=MFSYSA,MFSYSB`<br> |


### zOSMF Server retry request

| Property: | zOSMF Server retry request |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.[SERVERID].request.retry |
| Description: | The number of times to retry when zOSMF request fails |
| Required:  | No |
| Default value: | 3 |
| Valid values: | numerical value > 0 |
| Examples: | `zosmf.server.request.retry=5`<br>`zosmf.server.MFSYSA.request.retry=5` |


### zOSMF Server Credentials

| Property: | zOSMF Server Credentials |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.[SERVERID].credentials |
| Description: | The z/OS credentials to use when accessing the zOS/MF server |
| Required:  | No |
| Default value: | None, however the zOS/MF Manager will use the default z/OS image credentials |
| Valid values: | Valid credential ID |
| Examples: | `zosmf.server.MFSYSA.credentials=ZOS`<br> |


### zOSMF Server Image

| Property: | zOSMF Server Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.SERVERID.image |
| Description: | The z/OS image ID this zOS/MF server lives on |
| Required:  | No |
| Default value: | The SERVERID value is used as the z/OS image ID |
| Valid values: | z/OS image IDs |
| Examples: | `zosmf.server.MFSYSA.image=SYSA`<br> |


### zOSMF Server port

| Property: | zOSMF Server port |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.server.[serverid].port |
| Description: | The port number of the zOS/MF server |
| Required:  | No |
| Default value: | 443 |
| Valid values: | A valid IP port number |
| Examples: | `zosmf.server.port=443`<br>`zosmf.server.MFSYSA.port=443` |


### zOSMF Sysplex Servers

| Property: | zOSMF Sysplex Servers |
| --------------------------------------- | :------------------------------------- |
| Name: | zosmf.sysplex.[SYSPLEXID].default.servers |
| Description: | The zOSMF servers active on the supplied sysplex |
| Required:  | No |
| Default value: | None |
| Valid values: | Comma separated zOS/MF server IDs |
| Examples: | `zosmf.sysplex.default.servers=MFSYSA,MFSYSB`<br>`zosmf.sysplex.PLEXA.default.servers=MFSYSA,MFSYSB` |

