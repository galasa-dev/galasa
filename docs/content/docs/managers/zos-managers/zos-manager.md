---
title: "z/OS Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/zos/package-summary.html){target="_blank"}.


## Overview

This Manager provides tests and Managers with access to and configuration information about z/OS images and Sysplexes. It offers services such as APF, DUMP, SMF and Log access.

Additionally, the z/OS Manager provides tests with interfaces to the following z/OS functions which are implemented by other Managers:

- `z/OS Batch` which enables tests and Managers to submit, monitor and retrieve the output of z/OS batch jobs. See [BatchAccountsOpenTest](../../running-simbank-tests/batch-accounts-open-test.md) for a walkthrough of a test that employs this Manager.

- `z/OS Console` which allows tests and Managers to issue and retrieve the responses from z/OS console commands.

- `z/OS File` which provides tests and Managers with the ability to manage and transfer files to and from z/OS. Supported file types include Sequential, PDS, PDSE, KSDS, ESDS or RRDS and z/OS UNIX files.

- `z/OS TSO Command` which enables tests and Managers to issue and retrieve the responses from z/OS TSO commands. 

- `z/OS UNIX Command` which enables tests and Managers to issue and retrieve the responses from z/OS UNIX commands.

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/zos/package-summary.html){target="_blank"}.


## Including the Manager in a test

The z/OS Manager is not instantiated directly. To use the z/OS Manager in a test, import one or more of the following annotations into the test, as shown in the following examples: 

```java
@ZosImage
public IZosImage imagePrimary;
```

```java
@ZosIpHost
public IIpHost hostPrimary;
```

```java
@ZosIpPort
public IIpPort portPrimary;
```

You also need to add the Manager dependency into the _pom.xml_ file if you are using Maven, or into the _build.gradle_ file if you are using Gradle. 

If you are using Maven, add the following dependencies into the _pom.xml_ in the _dependencies_ section:

```xml
<dependency>
    <groupId>dev.galasa</groupId>
    <artifactId>dev.galasa.zos.manager</artifactId>
</dependency>
```

If you are using Gradle, add the following dependencies into `build.gradle` in the _dependencies_ closure:

```groovy
dependencies {
    compileOnly 'dev.galasa:dev.galasa.zos.manager'
}
```

## Testing in CICS Regions or IMS TM Systems on z/OS

To connect your Galasa test to a developer supplied environment with a provisioned CICS region or IMS TM system as a minimum you need to configure the following properties, even if you do not reference a `@ZosImage` in your Galasa test. This is because CICS regions and IMS TM systems sit on a z/OS LPAR, and so to provision and connect to a CICS region or IMS TM system in a test, you also need access to the z/OS image that it sits within to make requests on the CICS region or IMS TM system. You might need to configure additional z/OS-related CPS properties, depending on your test.  

```properties
zos.dse.tag.[tag].imageid=[IMAGEID]
    OR zos.tag.[tag].imageid=[IMAGEID] 
    OR zos.cluster.[CLUSTERID].images=[IMAGEID] (AND zos.dse.tag.[tag].clusterid=[CLUSTERID] if [CLUSTERID] is not DEFAULT)
zos.image.[IMAGEID].ipv4.hostname=[IP ADDRESS]
zos.image.[IMAGEID].credentials=[CREDENTIALID]
```

You also need to configure the following properties for the [CICS TS Manager](../cics-ts-managers/cics-ts-manager.md):

```properties
cicsts.provision.type=dse
cicsts.dse.tag.[TAG].applid=[APPLID]
```

or the following property for the [IMS TM Manager](../ims-tm-managers/ims-tm-manager.md):

```properties
imstm.dse.tag.[TAG].applid=[APPLID]
```


## Configuration Properties

The following properties are used to configure the z/OS Manager.


### Hostname of a z/OS system

| Property: | Hostname of a z/OS system |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].ipv4.hostname |
| Description: | A physical TCP/IP hostname value for a z/OS system |
| Required:  | Yes, if connecting to a z/OS image |
| Default value: | None |
| Valid values: | A valid TCP/IP hostname   |
| Examples: | `zos.image.SYSA.ipv4.hostname=dev.galasa.system1`<br>`zos.image.SIMBANK.ipv4.hostname=127.0.0.1`<br> |


### Credentials tag for logging onto a z/OS system

| Property: | Credentials tag for logging onto a z/OS system   |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].credentials |
| Description: |  Tag of the credentials that are stored in the CREDS and used to log onto a z/OS system  |
| Required:  | Yes, if connecting to a z/OS image |
| Default value: | None|
| Valid values: | Valid characters are A-Z, a - z, 0-9  |
| Examples: | `zos.image.SYSA.credentials=KEY_TO_CREDS_STORE`<br>`zos.image.SIMBANK.credentials=SIMBANK`<br> |


### Extra bundle required to implement the z/OS Batch Manager

| Property: | Extra bundle required to implement the zOS Batch Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.batch.manager |
| Description: | The name of the Bundle that implements the z/OS Batch Manager |
| Required:  | No |
| Default value: | dev.galasa.common.zosbatch.zosmf.manager |
| Valid values: | A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.   |
| Examples: | `zos.bundle.extra.batch.manager=dev.galasa.common.zosbatch.zosmf.manager`<br> |


### The z/OS Cluster ID

| Property: | The zOS Cluster ID |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.tag.[tag].clusterid | 
| Description: | The z/OS Cluster ID for the specified tag |
| Required:  | No |
| Default value: | None |
| Valid values: | Valid value is a character string with a maximum length of 32 |
| Examples: | `zos.tag.PLX1.clusterid=PLEXA`<br> |


### The images for a z/OS Cluster

| Property: | The images for a zOS Cluster |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.cluster.[clusterId].images | 
| Description: | The z/OS Images for the specified cluster. Specify more than one image by using commas. |
| Required:  | No |
| Default value: | None |
| Valid values: | Valid value is a character string with a maximum length of 32 |
| Examples: | `zos.cluster.PLEX1.images=SYSA,SYSB,SYSC`<br> |


### Extra bundle required to implement the z/OS Console Manager

| Property: | Extra bundle required to implement the zOS Console Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.console.manager |
| Description: | The name of the Bundle that implements the z/OS Console Manager |
| Required:  | No |
| Default value: | dev.galasa.common.zosconsole.zosmf.manager |
| Valid values: | A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.  |
| Examples: | `zos.bundle.extra.console.manager=dev.galasa.common.zosconsole.zosmf.manager`<br> |


### Developer Supplied Environment z/OS Image Cluster ID

| Property: | Developer Supplied Environment zOS Image Cluster ID |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.dse.tag.[tag].clusterid |
| Description: | The Cluster ID for the specified tag |
| Required:  | No |
| Default value: | None |
| Valid values: | A 1 - 8 length character name |
| Examples: | `zos.dse.tag.PLX1.clusterid=PLEXA`<br> |


### Developer Supplied Environment z/OS Image

| Property: | Developer Supplied Environment zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.dse.tag.[tag].imageid |
| Description: | The image ID of the Developer Supplied Environment for the specified tag |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid image ID |
| Examples: | `zos.dse.tag.MVS1.imageid=SYSA`<br> |


### Extra bundle required to implement the z/OS File Manager

| Property: | Extra bundle required to implement the zOS File Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.file.manager |
| Description: | The name of the Bundle that implements the z/OS File Manager |
| Required:  | No |
| Default value: | dev.galasa.common.zosfile.zosmf.manager |
| Valid values: |  A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.  |
| Examples: | `zos.bundle.extra.file.manager=dev.galasa.common.zosfile.zosmf.manager`<br> |


### IP Host ID of the z/OS Image

| Property: | IP Host ID of the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].iphostid |
| Description: | The IP Host ID of the z/OS Image for the supplied tag.<br>  If CPS property zos.image.[imageId].iphostid exists, then that is returned, otherwise the z/OS Image ID is returned |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid IP Host ID |
| Examples: | `zos.image.SYSA.iphostid=sysa.example.com`<br> |


### Telnet port number of the z/OS Image

| Property: | Telnet port number of the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].telnet.port |
| Description: | The port number for telnet 3270 access to the z/OS Image for the supplied tag. |
| Required:  | No |
| Default value: | 23 |
| Valid values: | A valid TCP/IP port number |
| Examples: | `zos.image.SYSA.telnet.port=992`<br> |


### TLS for telnet on the z/OS Image

| Property: | TLS for telnet on the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].telnet.tls |
| Description: | Set this to true if Transport Layer Security (TLS) is used on the telnet 3270 port of the z/OS Image for the supplied tag. |
| Required:  | No |
| Default value: | false |
| Valid values: | `true` or `false` |
| Examples: | `zos.image.SYSA.telnet.tls=true`<br> |


### Server certificate verification for telnet on the z/OS Image

| Property: | Server certificate verification for telnet on the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].telnet.verify |
| Description: | Set this to true to request verification of the certificate provided by the z/OS Image for the supplied tag when connecting to the telnet 3270 port.<br> This property is ignored if Transport Layer Security (TLS) is not in use.<br> By default, the trust store containing certificate authorities used for verification is `$JAVA_HOME/lib/security/cacerts`. To use a different trust store, you can specify a different path on a `-Djavax.net.ssl.trustStore` option for the `galasactl.jvm.local.launch.options` property in bootstrap.properties. |
| Required:  | No |
| Default value: | false |
| Valid values: | `true` or `false` |
| Examples: | `zos.image.SYSA.telnet.verify=true`<br> |


### The z/OS Image

| Property: | The zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.tag.[tag].imageid |
| Description: | The image ID for the specified tag |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid z/OS image ID |
| Examples: | `zos.tag.MVS1.imageid=SYSA`<br> |


### Maximum slots for z/OS Image

| Property: | Maximum slots for zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].max.slots |
| Description: | The maximum slots available on a z/OS Image for the specified tag |
| Required:  | No |
| Default value: | 2 |
| Valid values: | 1 to 255 |
| Examples: | `zos.image.SYSA.max.slots=2`<br> |


### Code page for z/OS Image

| Property: | Code page for zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].codepage |
| Description: | The EBCDIC code page used on a z/OS image for the specified tag. EBCDIC features a variety of code pages, and a subset of characters, including square brackets and currency symbols, are encoded differently between code pages. Setting the correct code page for a z/OS image can resolve issues with displaying these characters. |
| Required:  | No |
| Default value: | 037 |
| Valid values: | A valid java.nio.charset EBCDIC character encoding |
| Examples: | `zos.image.SYSA.codepage=1047`<br> |


### The SYSNAME for z/OS Image

| Property: | The SYSNAME for zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].sysname |
| Description: | The SYSNAME for the z/OS image | 
| Required:  | No |
| Default value: | The image ID of the image |
| Valid values: | The name must be 1-8 characters long; the valid characters are A-Z, 0-9, $, @, and #. |
| Examples: | `zos.image.SYSA.sysname=SYSA`<br> |


### The VTAM logon command template for the z/OS Image

| Property: | The VTAM logon command template for the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.vtam.logon<br>zos.image.[imageId].vtam.logon |
| Description: | A template for the command to log on to an application running on the zOS Image. The {0} argument in the template will be replaced with the VTAM application identifier for the requested application. |
| Required:  | No |
| Default value: | LOGON APPLID({0}) |
| Valid values: | A valid java.text.MessageFormat pattern with precisely one FormatElement |
| Examples: | `zos.image.vtam.logon=LOGON APPLID {0}`<br>`zos.image.SYSA.vtam.logon={0}`|


### The logon initial text for the z/OS Image

| Property: | The logon initial text for the z/OS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.logon.initial.text<br>zos.image.[imageId].logon.initial.text |
| Description: | A text string that is expected to be present on a 3270 terminal that has been connected to the z/OS image but before logon to any application system has been attempted. |
| Required:  | No |
| Default value: | None |
| Valid values: | Any text string |
| Examples: | `zos.image.logon.initial.text=VAMP`<br>`zos.image.SYSA.logon.initial.text=SYSA MAIN MENU` |


### The run data set HLQ for the z/OS Image

| Property: | The run data set HLQ for the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.run.[imageId].dataset.hlq |
| Description: | The data set HLQ(s) for temporary data sets created on z/OS Image.<br>  If CPS property zos.run.[imageId].dataset.hlq exists, then that is returned |
| Required:  | No |
| Default value: | runuser.GALASA |
| Valid values: | A data set name can be one name segment, or a series of joined name segments. Segments are limited to eight characters, the first of which must be alphabetic (A to Z) or special (# @ $). The remaining seven characters are either alphabetic, numeric (0 - 9), special, a hyphen (-). Name segments are separated by a period (.). |
| Examples: | `zos.run.SYSA.dataset.hlq=USERID.GALASA`<br> |


### The run data UNIX path prefix for the z/OS Image

| Property: | The run data UNIX path prefix for the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.run.[imageId].unix.path.prefix |
| Description: | The UNIX path prefix for temporary data sets created on z/OS Image.<br>  If CPS property zos.run.[image].unix.path.prefix exists, then that is returned |
| Required:  | No |
| Default value: | /u/runuser/Galasa |
| Valid values: | A valid path |
| Examples: | `zos.run.SYSA.unix.path.prefix=/u/userid/Galasa`<br> |


### Extra bundle required to implement the z/OS TSO Command Manager

| Property: | Extra bundle required to implement the zOS TSO Command Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.tsocommand.manager |
| Description: | The name of the Bundle that implements the z/OS TSO Command Manager |
| Required:  | No |
| Default value: | dev.galasa.zostsocommand.ssh.manager |
| Valid values: | A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.   |
| Examples: | `zos.bundle.extra.tsocommand.manager=dev.galasa.zostsocommand.ssh.manager` |


### Extra bundle required to implement the z/OS UNIX Command Manager

| Property: | Extra bundle required to implement the zOS UNIX Command Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.unixcomand.manager |
| Description: | The name of the Bundle that implements the z/OS UNIX Command Manager |
| Required:  | No |
| Default value: | dev.galasa.zosunixcommand.ssh.manager |
| Valid values: | A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.   |
| Examples: | `zos.bundle.extra.unix.manager=dev.galasa.zosunixcommand.ssh.manager` |


### z/OS Batch restrict processing to the server on the specified image

| Property: | zOS Batch restrict processing to the server on the specified image |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.batchjob.[imageId].restrict.to.image |
| Description: | Use only the server (e.g. zOSMF, RSE API, etc) running on the image associated with the z/OS Batch job |
| Required:  | No |
| Default value: | false |
| Valid values: | true or false |
| Examples: | `zosbatch.batchjob.SYSA.restrict.to.image=true`<br>`zosbatch.batchjob.default.restrict.to.image=false` |


### z/OS Batch default input class

| Property: | zOS Batch default input class |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.default.input.class<br>zosbatch.default.[imageId].input.class |
| Description: | The default input class to set on the job card for submitted jobs |
| Required:  | No |
| Default value: | A |
| Valid values: | a valid JES input class literal |
| Examples: | `zosbatch.default.SYSA.input.class=S`<br>`zosbatch.default.input.class=A` |


### z/OS Batch job execution wait timeout

| Property: | zOS Batch job execution wait timeout |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.batchjob.[imageId].timeout |
| Description: | The value in seconds to wait for the z/OS Batch job execution to complete when submitted via zOSMF |
| Required:  | No |
| Default value: | 350 |
| Valid values: | 0 to {@link Integer#MAX_VALUE} |
| Examples: | `zosbatch.batchjob.SYSA.timeout=350`<br>`zosbatch.batchjob.default.timeout=60` |


### z/OS Batch jobname prefix

| Property: | zOS Batch jobname prefix |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.jobname.[imageId].prefix |
| Description: | The z/OS Batch jobname prefix when submitted via zOSMF |
| Required:  | No |
| Default value: | GAL |
| Valid values: | 1-7 characters |
| Examples: | `zosbatch.jobname.SYSA.prefix=JOB`<br>`zosbatch.jobname.default.prefix=XXX` |


### z/OS Batch default MSGCLASS

| Property: | zOS Batch default MSGCLASS |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.default.class<br>zosbatch.default.[imageId].message.class |
| Description: | The default message class to set on the job card for submitted jobs |
| Required:  | No |
| Default value: | A |
| Valid values: | a valid JES message class literal |
| Examples: | `zosbatch.default.SYSA.message.class=S`<br>`zosbatch.default.message.class=A` |


### z/OS Batch default message level

| Property: | zOS Batch default message level |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.default.message.level<br>zosbatch.default.[imageId].message.level |
| Description: | The default message level to set on the job card for submitted jobs |
| Required:  | No |
| Default value: | (1,1) |
| Valid values: | a valid JES message level |
| Examples: | `zosbatch.default.SYSA.message.level=(1,1)`<br>`zosbatch.default.message.level=(2,0)` |


### z/OS Batch job truncate JCL

| Property: | zOS Batch job truncate JCL |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.batchjob.[imageId].truncate.jcl.records |
| Description: | The z/OSMF submit job will fail if supplied with JCL records greater than 80 characters. Setting this property to true will truncate any records to 80 characters and issue a warning message. |
| Required:  | No |
| Default value: | true |
| Valid values: | true or false |
| Examples: | `zosbatch.batchjob.SYSA.truncate.jcl.records=true`<br>`zosbatch.batchjob.default.truncate.jcl.records=false` |


### z/OS Batch job use SYSAFF

| Property: | zOS Batch job use SYSAFF |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.batchjob.[imageId].use.sysaff |
| Description: | Use the run the z/OS Batch job on the specified image by specifying {@code /*JOBPARM SYSAFF=[imageid]} |
| Required:  | No |
| Default value: | true |
| Valid values: | true or false |
| Examples: | `zosbatch.batchjob.SYSA.use.sysaff=true`<br>`zosbatch.batchjob.default.use.sysaff=false` |


### Restrict z/OS console processing to the zOSMF server on the specified image

| Property: | Restrict zOS console processing to the zOSMF server on the specified image |
| --------------------------------------- | :------------------------------------- |
| Name: | zosconsole.console.restrict.to.image<br>zosconsole.console.[imageId].restrict.to.image |
| Description: | Use only the zOSMF server running on the image associated with the z/OS Console |
| Required:  | No |
| Default value: | false |
| Valid values: | true or false |
| Examples: | `zosconsole.console.restrict.to.image=true`<br>`zosconsole.console.SYSA.restrict.to.image=true` |


### z/OS File the maximum number of items from a UNIX directory list

| Property: | zOS File the maximum number of items from a UNIX directory list |
| --------------------------------------- | :------------------------------------- |
| Name: | zosfile.unix.[imageId].directory.list.max.items |
| Description: | The maximum number of items the server (e.g. zOSMF, RSE API, etc) returns when listing the content of a UNIX directory |
| Required:  | No |
| Default value: | 1000 |
| Valid values: | 0 to 65535 |
| Examples: | `zosfile.unix.SYSA.directory.list.max.items=1000`<br> |


### z/OS File restrict processing to the server on the specified image

| Property: | zOS File restrict processing to the server on the specified image |
| --------------------------------------- | :------------------------------------- |
| Name: | zosfile.file.restrict.to.image<br>zosfile.file.[imageId].restrict.to.image |
| Description: | Use only the server (e.g. zOSMF, RSE API, etc) running on the image associated with the z/OS data set or file |
| Required:  | No |
| Default value: | false |
| Valid values: | true or false |
| Examples: | `zosfile.file.restrict.to.image=true`<br>`zosfile.file.SYSA.restrict.to.image=true` |


### z/OS File UNIX permission bits to be used in creating the file or directory

| Property: | zOS File UNIX permission bits to be used in creating the file or directory |
| --------------------------------------- | :------------------------------------- |
| Name: | zosfile.[imageId].unix.file.permission<br>zosfile.[imageId].unix.file.permission |
| Description: | The UNIX file or directory permission bits to be used in creating the file or directory |
| Required:  | No |
| Default value: | None |
| Valid values: | Valid values are r,w,x,s,- |
| Examples: | `zosfile.unix.file.permission=rwxrwx---`<br>`zosfile.SYSA.unix.file.permission=rwxrwxrrx` |


## Annotations provided by the Manager

The following annotations are available with the z/OS Manager

### z/OS Batch

| Annotation: | z/OS Batch |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosBatch |
| Description: | The `@ZosBatch` annotation requests the z/OS Manager to provide a z/OS Batch instance associated with a z/OS image.  The test can request multiple z/OS Batch instances, with the default being associated with the **primary** z/OS image.<br> At test end, the Manager stores the job output with the test results archive and removes jobs from the JES queue. |
| Attribute: `imageTag` |  The `imageTag` is used to identify the z/OS image. |
| Syntax: | <pre lang="java">@ZosImage(imageTag="A")<br>public IZosImage zosImageA;<br><br>@ZosBatch(imageTag="A")<br>public IZosBatch zosBatchA;<br></pre> |
| Notes: | The `IZosBatch` interface has a single method, {@link IZosBatch#submitJob(String, IZosBatchJobname)} to submit a JCL  as a `String` and returns a `IZosBatchJob` instance.<br><br> See [ZosBatch](../../reference/javadoc/dev/galasa/zosbatch/ZosBatch.html){target="_blank"}, [IZosBatch](../../reference/javadoc/dev/galasa/zosbatch/IZosBatch.html){target="_blank"} and [IZosBatchJob](../../reference/javadoc/dev/galasa/zosbatch/IZosBatchJob.html){target="_blank"} to find out more. |


### z/OS Console

| Annotation: | z/OS Console |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosConsole |
| Description: | The `@ZosConsole` annotation requests the z/OS Manager to provide a z/OS Console instance associated with a z/OS image.  The test can request multiple z/OS Console instances, with the default being associated with the **primary** z/OS image.<br> |
| Attribute: `imageTag` |  The tag of the z/OS Image this variable is to be populated with |
| Syntax: | <pre lang="java">@ZosImage(imageTag="A")<br>public IZosImage zosImageA;<br><br>@ZosConsole(imageTag="A")<br> public IZosConsole zosConsoleA;<br></pre> |
| Notes: | The `IZosConsole` interface has two methods, {@link IZosConsole#issueCommand(String)} and {@link IZosConsole#issueCommand(String, String)} to issue a command to the z/OS console and returns a `IZosConsoleCommand` instance.<br><br> See [ZosConsole](../../reference/javadoc/dev/galasa/zosconsole/ZosConsole.html){target="_blank"}, [IZosConsole](../../reference/javadoc/dev/galasa/zosconsole/IZosConsole.html){target="_blank"} and [IZosConsoleCommand](../../reference/javadoc/dev/galasa/zosconsole/IZosConsoleCommand.html){target="_blank"} to find out more. |


### z/OS File

| Annotation: | z/OS File |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosFileHandler |
| Description: | The `@ZosFileHandler` annotation requests the z/OS Manager to provide a handler instance to manage data sets and UNIX files on a z/OS image.  A single z/OS File Handler instance can manage multiple z/OS data sets and UNIX files on multiple z/OS images.<br> |
| Syntax: | <pre lang="java">@ZosFileHandler<br>public IZosFileHandler zosFileHandler;<br></pre> |
| Notes: | The `IZosFileHandler` interface has three methods supplying file name and z/OS image:<br> {@link IZosFileHandler#newDataset(String, dev.galasa.zos.IZosImage)}<br>  {@link IZosFileHandler#newVSAMDataset(String, dev.galasa.zos.IZosImage)}<br> {@link IZosFileHandler#newUNIXFile(String, dev.galasa.zos.IZosImage)}<br> returning an object representing the type of file requested. This can be an existing file or can be created via a method on the file object.<br><br> See [ZosFileHandler](../../reference/javadoc/dev/galasa/zosfile/ZosFileHandler.html){target="_blank"}, [IZosFileHandler](../../reference/javadoc/dev/galasa/zosfile/IZosFileHandler.html){target="_blank"}, [IZosDataset](../../reference/javadoc/dev/galasa/zosfile/IZosDataset.html){target="_blank"}, [IZosVSAMDataset](../../reference/javadoc/dev/galasa/zosfile/IZosVSAMDataset.html){target="_blank"} and [IZosUNIXFile](../../reference/javadoc/dev/galasa/zosfile/IZosUNIXFile.html){target="_blank"} to find out more. |


### z/OS TSO Command

| Annotation: | z/OS TSO Command |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosTSOCommand |
| Description: | The `@ZosTSOCommand` annotation requests the z/OS Manager to provide a z/OS TSO Command instance associated with a z/OS image.  The test can request multiple z/OS TSO Command instances, with the default being associated with the **primary** z/OS image.<br> |
| Attribute: `imageTag` |  The tag of the z/OS Image this variable is to be populated with |
| Syntax: | <pre lang="java">@ZosImage(imageTag="A")<br>public IZosImage zosImageA;<br><br>@ZosTSOCommand(imageTag="A")<br> public IZosTSOCpmmand zosTSOA;<br></pre> |
| Notes: | The `IZosTSOCommand` interface provides the methods {@link IZosTSOCommand#issueCommand(String)} and {@link IZosTSOCommand#issueCommand(String, long)} to issue a command to z/OS TSO Command and returns a `String`.<br><br> See [IZosTSOCommand](../../reference/javadoc/dev/galasa/zostsocommand/IZosTSOCommand.html){target="_blank"} to find out more. |


### z/OS UNIX Command

| Annotation: | z/OS UNIX Command |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosUNIXCommand |
| Description: | The `@ZosUNIXCommand` annotation requests the z/OS Manager to provide a z/OS UNIX instance associated with a z/OS image.  The test can request multiple z/OS UNIX Command instances, with the default being associated with the **primary** z/OS image.<br> |
| Attribute: `imageTag` |  The tag of the z/OS Image this variable is to be populated with |
| Syntax: | <pre lang="java">@ZosImage(imageTag="A")<br>public IZosImage zosImageA;<br><br>@ZosUNIXCommand(imageTag="A")<br> public IZosUNIXCommand zosUNIXCommandA;<br></pre> |
| Notes: | The `IZosUNIXCommand` interface provides the methods {@link IZosUNIXCommand#issueCommand(String)} and {@link IZosUNIXCommand#issueCommand(String, long)} to issue a command to z/OS UNIX and returns a String response.<br><br> See [IZosUNIXCommand](../../reference/javadoc/dev/galasa/zosunixcommand/IZosUNIXCommand.html){target="_blank"} to find out more. |


## Code snippets and examples

Use the following code snippets to help you get started with the z/OS Manager.


### Request a z/OS TSO Command instance

The following snippet shows the code that is required to request a z/OS TSO Command instance in a Galasa test:

```java
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosTSOCommand(imageTag="A")
public IZosTSOCommand tsoCommand;
```

The code creates a z/OS TSO Command instance associated with the z/OS Image allocated in the *zosImageA* field.


### Issue a z/OS TSO Command and retrieve the immediate response

Issue the z/OS TSO `TIME` Command and retrieve the response:

```java
String tsoCommandString = "TIME";
String tsoResponse = tsoCommand.issueCommand(tsoCommandString);
```

The String `tsoResponse`  contains the output of the TSO TIME command, e.g. 

```
IKJ56650I TIME-12:01:00 PM. CPU-00:00:00 SERVICE-290 SESSION-00:00:00 APRIL 1,2020
```


### Request a z/OS UNIX Command instance

The following snippet shows the code that is required to request a z/OS UNIX Command instance in a Galasa test:

```java
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosUNIXCommand(imageTag="A")
public IZosUNIXCommand unixCommand;
```

The code creates a z/OS UNIX Command instance associated with the z/OS Image allocated in the *zosImageA* field.


### Issue a z/OS UNIX Command and retrieve response

Issue the z/OS UNIX `date` Command and retrieve the response:

```java
String unixCommandString = "date";
String unixResponse = unixCommand.issueCommand(unixCommandString);
```

The String `unixResponse`  contains the output of the UNIX TIME command, e.g. 

```
Wed Apr 1 12:01:00 BST 2020
```


### Request a z/OS Console instance

The following snippet shows the code that is required to request a z/OS Console instance in a Galasa test:

```java
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosBatch(imageTag="A")
public IZosConsole zosConsole;
```

The code creates a z/OS Console instance associated with the z/OS Image allocated in the *zosImageA* field.


### Issue a z/OS Console command and retrieve the immediate response

Issue a z/OS Console command and retrieve the immediate console command response:

```java
String command = "D A,L";
IZosConsoleCommand consoleCommand = zosConsole.issueCommand(command);
String immediateResponse = consoleCommand.getResponse();

```


### Issue a z/OS Console command and retrieve the delayed response

Issue a z/OS Console command and retrieve the delayed console command response:

```java
String command = "D A,L";
IZosConsoleCommand consoleCommand = zosConsole.issueCommand(command);
String delayedResponse = consoleCommand.requestResponse();

```


### Request a z/OS Batch instance

The following snippet shows the code that is required to request a z/OS Batch instance in a Galasa test:

```java
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosBatch(imageTag="A")
public IZosBatch zosBatch;
```


The code creates a z/OS Batch instance associated with the allocated with the z/OS Image allocated in the *zosImageA* field.


### Submit a z/OS Batch Job

Submit a z/OS Batch Job using the supplied JCL and a Galasa allocated Job Name:

```java
String jcl = "//STEP1    EXEC PGM=IEFBR14";
IZosBatchJob batchJob = zosBatch.submitJob(jcl, null);
```

### Submit a z/OS Batch Job with job card parameters

Submit a z/OS Batch Job using the supplied JCL, a Galasa allocated Job Name and overidding the default input and message class:

```java
String jcl = "//STEP1    EXEC PGM=IEFBR14";
ZosBatchJobcard jobcard = new ZosBatchJobcard().
                          .setInputClass("B")
                          .setMsgClass("X");
IZosBatchJob batchJob = zosBatch.submitJob(jcl, null, jobcard);
```


### Wait for z/OS Batch Job to complete

Wait for z/OS Batch job to complete and check maximum return code:

```java
if (batchJob.waitForJob() > 0) {
    logger.info("Batch job failed RETCODE=" + batchJob.getRetcode();
}
```

prints, for example:

```
Batch job failed RETCODE=CC 0020
```

or

```
Batch job failed RETCODE=ABEND S0C4
```


### Retrieve the job output

Use the following code to retrieve the output from a z/OS Batch Job:

```java
IZosBatchJobOutput jobOutput = batchJob.retrieveOutput();
List<IZosBatchJobOutputSpoolFile> spoolFiles = jobOutput.getSpoolFiles();
for (IZosBatchJobOutputSpoolFile spoolFile : spoolFiles) {
    String ddName = spoolFile.getDdname();
    String output = spoolFile.getRecords();
    //...
}

```


### Obtain a list of active jobs

Use the following code to obtain a list of active jobs called *MYJOB1* with an owner of *USERID*:

```java
List<IZosBatchJob> jobs = zosBatch.getJobs("MYJOB1", "USERID");
for (IZosBatchJob job : jobs) {
    if (job.getStatus().equals("ACTIVE")) {
        //...
    }
}

```


### Retrieve the content of a specific spool file from an active CICS region

Use the following code to retrieve and process the output from the *MSGUSR* spool file:

```java
List<IZosBatchJob> jobs = zosBatch.getJobs("CICSRGN", "CICSUSR");
for (IZosBatchJob job : jobs) {
    if (job.getStatus().equals("ACTIVE")) {
        String msgusr = cicsJob.getSpoolFile("MSGUSR");
        if (msgusr.contains("DFHAC2236")) {
            //...
        }
        break;
    }
}

```

The code retrieves a list of CICS regions named *CICSRGN* with and owner of *CICSUSR*. It then loops through until it finds the first active region. The content of the *MSGUSR* spool file is obtained and checked for the string *DFHAC2236*.

In this example, we assume there will only one spool file with the ddname of *MSGUSR*. If this were not the case, the following code could be used:

```java
List<IZosBatchJob> jobs = zosBatch.getJobs("CICSRGN", "CICSUSR");
for (IZosBatchJob job : jobs) {
    List<IZosBatchJobOutputSpoolFile> spoolFiles = job.retrieveOutput().getSpoolFiles();
    for (IZosBatchJobOutputSpoolFile spoolFile : spoolFiles) {
        if (spoolFile.getDdname().equals("SYSOUT") &&
            spoolFile.getStepname().equals("STEP2")) {
            String output = spoolFile.getRecords();
            //...
        }
    }
}

```

Here, the code retrieves the content of the *SYSOUT* spool file for job step *STEP2*.


### Request a z/OS File Handler instance

The following snippet shows the code that is required to request a z/OS File Handler instance in a Galasa test:

```java
@ZosFileHandler
public IZosFileHandler zosFileHandler;
```


### Read the content of an existing sequential data set

Create a new *IZosDataset* object representing an existing sequential data set. If the data set exists, retrieve the content in text mode:

```java
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
//...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.EXISTING.DATASET.SEQ", zosImage);
if (dataSet.exists()) {
    String content = dataSet.retrieveAsText();
    //...
}
```


### Read the content of an existing partitioned data set member

Create a new *IZosDataset* object representing an existing partitioned data set (PDS). If the PDS exists, check if the member exists and retrieve it's content in text mode:

```java
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
//...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.EXISTING.DATASET.SEQ, zosImage);
    String memberName = "MEMBER1";
    if (dataSet.exists() && dataSet.memberExists(memberName)) {
        String content = dataSet.memberRetrieveAsText(memberName);
        //...
    }
```


### Create a new sequential data set

Create a new *IZosDataset* object representing a new sequential data set. If the data set does not exist, allocate the data set with attributes to the equivalent of the following JCL:

```
//NEWDS    DD DSN=GALASA.NEW.DATASET.SEQ,DISP=(NEW,CATLG),
//            DSORG=PS,RECFM=FB,LRECL=80,BLKSIZE=32720,
//            UNIT=SYSDA,SPACE=(TRK,(1,1))
```
Finally, content is written to the data set in text mode:


```java
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
//...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.NEW.DATASET.SEQ", zosImage);
    if (!dataSet.exists()) {
        dataSet.setDatasetOrganization(DatasetOrganization.SEQUENTIAL);
        dataSet.setRecordFormat(RecordFormat.FIXED_BLOCKED);
        dataSet.setRecordlength(80);
        dataSet.setBlockSize(32720);
        dataSet.setUnit("SYSDA");
        dataSet.setSpace(SpaceUnit.TRACKS, 1, 1);
        dataSet.create();
    }
    List<String> records = new ArrayList<>();
    records.add("RECORD 1");
    records.add("RECORD 2");
    records.add("RECORD 3");
    dataSet.storeText(String.join("\n", records));
```


### Create a new partitioned data set member

Create a new *IZosDataset* object representing a new partitioned data (PDS) set member. If the data set does not exist, allocate the PDS with attributes to the equivalent of the following JCL:

```
//NEWPDS   DD DSN=GALASA.NEW.DATASET.PDS,DISP=(NEW,CATLG),
//            DSORG=PS,RECFM=FB,LRECL=80,BLKSIZE=32720,
//            UNIT=SYSDA,SPACE=(TRK,(1,1,15))
```
Finally, content is written to a member in the PDS in text mode:


```java
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
//...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.NEW.DATASET.PDS", zosImage);
if (!dataSet.exists()) {
    dataSet.setDatasetOrganization(DatasetOrganization.SEQUENTIAL);
    dataSet.setRecordFormat(RecordFormat.FIXED_BLOCKED);
    dataSet.setRecordlength(80);
    dataSet.setBlockSize(32720);
    dataSet.setUnit("SYSDA");
    dataSet.setSpace(SpaceUnit.TRACKS, 1, 1);
    dataSet.setDirectoryBlocks(15);
    dataSet.create();
}
String memberName = "MEMBER1";
List<String> records = new ArrayList<>();
    records.add("RECORD 1");
    records.add("RECORD 2");
    records.add("RECORD 3");
    dataSet.memberStoreText(memberName, String.join("\n", records));
}
```

To create a PDS/E, i.e. the JCL equivalent of

```
DSNTYPE=LIBRARY
```

use

```
dataSet.setDatasetType(DSType.LIBRARY);
```

instead of setting the number of directory blocks.


### Create a new VSAM KSDS

Create a new *IZosVSAMDataset* object representing a new VSAM KSDS data set. If the data set is allocated with a minimum set of attributes:

```java
IZosVSAMDataset vsamDataSet = zosFileHandler.newVSAMDataset("ROBERTD.GALASA.TEST.DS.ANOTHER.KSDS", zosImage);
vsamDataSet.setSpace(VSAMSpaceUnit.CYLINDERS, 1, 1);
vsamDataSet.setRecordSize(50, 101);
vsamDataSet.create();
```


### Read the contents of a z/OS UNIX File

Create a new *IZosDataset* object representing a UNIX file. If the file exists, retrieve the content in text mode:

```java
IZosUNIXFile unixFile = zosFileHandler.newUNIXFile("/tmp/Galasa/existingFile", zosImage);
if (unixFile.exists()) {
    unixFile.setDataType(UNIXFileDataType.TEXT);
    String content = unixFile.retrieve();
}
```


### Read the contents of a z/OS UNIX File

Create a new *IZosDataset* object representing a new UNIX file. If UNIX file does not exist, create it. Write to the file in binary mode:

```java
IZosUNIXFile unixFile = zosFileHandler.newUNIXFile("/tmp/Galasa/newFile", zosImage);
if (!unixFile.exists()) {
    unixFile.create();
}
List<String> properties = new ArrayList<>();
properties.add("dev.galasa.property1=value1");
properties.add("dev.galasa.property2=value2");
properties.add("dev.galasa.property3=value3");
unixFile.setDataType(UNIXFileDataType.BINARY);
unixFile.store(String.join("\n", properties));
```


### List the contents of a z/OS UNIX Directory

Create a new *IZosDataset* object representing a new UNIX directory. If UNIX directory exists, list its contents:

```java
IZosUNIXFile unixDirectory = zosFileHandler.newUNIXFile("/tmp/Galasa/", zosImage);
if (unixDirectory.exists())
{
    Map<String, String> dir = unixDirectory.directoryListRecursive();
    for (Map.Entry<String, String> entry : dir.entrySet()) {
        logger.info(String.format("%2$-9s: %1$s", entry.getKey(), entry.getValue()));
   }
}
```

Example output:

```
directory: /tmp/Galasa/dira
file     : /tmp/Galasa/dira/file1
file     : /tmp/Galasa/dira/file2
file     : /tmp/Galasa/existingFile
file     : /tmp/Galasa/newFile
```

