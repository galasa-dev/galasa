---
title: "RSE API Manager"
---

This Manager is at Alpha level. You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/zosrseapi/package-summary.html){target="_blank"}.


## Overview

This Manager provides Galasa tests with access to a RSE API server.


## Provided annotations

The following annotations are available with the RSE API Manager


### RSE API

| Annotation: | RSE API |
| --------------------------------------- | :------------------------------------- |
| Name: | @Rseapi |
| Description: | The `@Rseapi` annotation requests the RSE API Manager to provide a RSE API server instance associated with a z/OS image.  The test can request multiple RSE API instances, with the default being associated with the **primary** zOS image. |
| Attribute: `imageTag` |  The tag of the zOS Image this variable is to be populated with |
| Syntax: | <pre lang="java">@ZosImage(imageTag="A")<br>public IZosImage zosImageA;<br>@Rseapi(imageTag="A")<br>public IRseapi rseapiA;<br></pre> |
| Notes: | The `IRseapi` interface has a number of methods to issue requests to the RSE API REST API. See [Rseapi](../../reference/javadoc/dev/galasa/zosrseapi/Rseapi.html){target="_blank"} and [IRseapi](../../reference/javadoc/dev/galasa/zosrseapi/IRseapi.html){target="_blank"} to find out more. |


## Configuration Properties

The following are properties used to configure the RSE API Manager.
 

### RSE API Server port is https

| Property: | RSE API Server port is https |
| --------------------------------------- | :------------------------------------- |
| Name: | rseapi.server.[imageid].https |
| Description: | Use https (SSL) for RSE API server |
| Required:  | No |
| Default value: | true |
| Valid values: | true or false |
| Examples: | `rseapi.server.https=true`<br>`rseapi.server.RSESYSA.https=true` |


### RSE API Image Servers

| Property: | RSE API Image Servers |
| --------------------------------------- | :------------------------------------- |
| Name: | rseapi.image.IMAGEID.servers |
| Description: | The RSE API servers for use with z/OS Image, the RSE API do not need to be running the actual z/OS Image |
| Required:  | No |
| Default value: | None |
| Valid values: | Comma separated RSE API server IDs |
| Examples: | `rseapi.image.MYLPAR.servers=RSESYSA,RSESYSB` |


### RSE API Server retry request

| Property: | RSE API Server retry request |
| --------------------------------------- | :------------------------------------- |
| Name: | rseapi.server.[SERVERID].request.retry |
| Description: | The number of times to retry when RSE API request fails |
| Required:  | No |
| Default value: | 3 |
| Valid values: | numerical value > 0 |
| Examples: | `rseapi.server.request.retry=5`<br>`rseapi.server.RSESYSA.request.retry=5` |


### RSE API Server Credentials

| Property: | RSE API Server Credentials |
| --------------------------------------- | :------------------------------------- |
| Name: | rseapi.server.[SERVERID].credentials |
| Description: | The z/OS credentials to use when accessing the RSE API server |
| Required:  | No |
| Default value: | None, however the RSE API Manager will use the default z/OS image credentials |
| Valid values: | Valid credential ID |
| Examples: | `rseapi.server.RSESYSA.credentials=ZOS` |


### RSE API Server Image

| Property: | RSE API Server Image |
| --------------------------------------- | :------------------------------------- |
| Name: | rseapi.server.SERVERID.image |
| Description: | The z/OS image ID this RSE API server lives on |
| Required:  | No |
| Default value: | The SERVERID value is used as the z/OS image ID |
| Valid values: | z/OS image IDs |
| Examples: | `rseapi.server.RSESYSA.image=SYSA` |


### RSE API Server port

| Property: | RSE API Server port |
| --------------------------------------- | :------------------------------------- |
| Name: | rseapi.server.[serverid].port |
| Description: | The port number of the RSE API server |
| Required:  | no |
| Default value: | 6800 |
| Valid values: | A valid port number |
| Examples: | `rseapi.server.port=6800`<br>`rseapi.server.RSESYSA.port=6800` |


### RSE API Sysplex Servers

| Property: | RSE API Sysplex Servers |
| --------------------------------------- | :------------------------------------- |
| Name: | rseapi.sysplex.[SYSPLEXID].default.servers |
| Description: | The RSE API servers active on the supplied sysplex |
| Required:  | No |
| Default value: | None |
| Valid values: | Comma separated RSE API server IDs |
| Examples: | `rseapi.sysplex.default.servers=RSASYSA,RSASYSB`<br>`rseapi.sysplex.PLEXA.default.servers=RSASYSA,RSASYSB` |

