---
path: "/docs/managers/zos-manager"
title: "z/OS Manager"
---

This Manager is at Beta level. You can view the <a href="https://javadoc.galasa.dev/index.html?overview-summary.html">Javadoc documentation for the Manager here</a>.<br>

[Overview](#overview)<br>
[Configuring](#configuring)<br>
[Provided annotation](#annotations)<br>
[Code snippets and examples](#codesnippets)<br>


# <a name="overview"></a>Overview
This Manager provides tests and Managers with access to and configuration information about z/OS images and Sysplexes. It offers services such as APF, DUMP, SMF and Log access. <br><br> Additionally, the z/OS Manager provides tests with interfaces to the following z/OS functions which are implemented by other Managers: <br><br> - <code>z/OS Batch</code> which enables tests and Managers to submit, monitor and retrieve the output of z/OS batch jobs. See <a href="/docs/running-simbank-tests/batch-accounts-open-test">BatchAccountsOpenTest</a> for a walkthrough of a test that employs this Manager.<br><br> - <code>z/OS Console</code> which allows tests and Managers to issue and retrieve the responses from z/OS console commands.
<br><br> - <code>z/OS File</code> which provides tests and Managers with the ability to manage and transfer files to and from z/OS. Supported file types include Sequential, PDS, PDSE, KSDS, ESDS or RRDS and z/OS UNIX files.
<br><br> - <code>z/OS TSO Command</code> which enables tests and Managers to issue and retrieve the responses from z/OS TSO commands. 
<br><br> - <code>z/OS UNIX Command</code> which enables tests and Managers to issue and retrieve the responses from z/OS UNIX commands.<br><br> 

<br><br> You can view the <a href="https://javadoc.galasa.dev/dev/galasa/zos/package-summary.html">Javadoc documentation for the Manager here</a>. <br><br>

## <a name="dependencies"></a>Including the Manager in a test

The z/OS Manager is not instantiated directly. To use the z/OS Manager in a test, import one or more of the following annotations into the test, as shown in the following examples: 

```
@ZosImage
public IZosImage imagePrimary;
```

```    
@ZosIpHost
public IIpHost hostPrimary;
```

```
@ZosIpPort
public IIpPort portPrimary;
```

You also need to add the Manager dependency into the _pom.xml_ file if you are using Maven, or into the _build.gradle_ file if you are using Gradle. 

If you are using Maven, add the following dependencies into the _pom.xml_ in the _dependencies_ section:

```
<dependency>
<groupId>dev.galasa</groupId>
<artifactId>dev.galasa.zos.manager</artifactId>
</dependency>
```

If you are using Gradle, add the following dependencies into ```build.gradle``` in the _dependencies_ closure:

```
dependencies {
compileOnly 'dev.galasa:dev.galasa.zos.manager'
}
```

## Testing in CICS Regions or IMS TM Systems on z/OS

To connect your Galasa test to a developer supplied environment with a provisioned CICS region or IMS TM system as a minimum you need to configure the following properties, even if you do not reference a `@ZosImage` in your Galasa test. This is because CICS regions and IMS TM systems sit on a z/OS LPAR, and so to provision and connect to a CICS region or IMS TM system in a test, you also need access to the z/OS image that it sits within to make requests on the CICS region or IMS TM system. You might need to configure additional z/OS-related CPS properties, depending on your test.  


```
zos.dse.tag.[tag].imageid=[IMAGEID]
    OR zos.tag.[tag].imageid=[IMAGEID] 
    OR zos.cluster.[CLUSTERID].images=[IMAGEID] (AND zos.dse.tag.[tag].clusterid=[CLUSTERID] if [CLUSTERID] is not DEFAULT)
zos.image.[IMAGEID].ipv4.hostname=[IP ADDRESS]
zos.image.[IMAGEID].credentials=[CREDENTIALID]
```

You also need to configure the following properties for the [CICS TS Manager](cics-ts-manager):

```
cicsts.provision.type=dse
cicsts.dse.tag.[TAG].applid=[APPLID]
```

or the following property for the [IMS TM Manager](ims-tm-manager):

```
imstm.dse.tag.[TAG].applid=[APPLID]
```


# <a name="configuring"></a>Configuring 

The following properties are used to configure the z/OS Manager.


<details>
<summary>Hostname of a z/OS system </summary>

| Property: | Hostname of a z/OS system |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].ipv4.hostname |
| Description: | A physical TCP/IP hostname value for a z/OS system |
| Required:  | Yes, if connecting to a z/OS image |
| Default value: | None |
| Valid values: | A valid TCP/IP hostname   |
| Examples: | <code>zos.image.SYSA.ipv4.hostname=dev.galasa.system1</code><br><code>zos.image.SIMBANK.ipv4.hostname=127.0.0.1</code><br> |

</details>

<details>
<summary>Credentials tag for logging onto a z/OS system </summary>

| Property: | Credentials tag for logging onto a z/OS system   |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].credentials |
| Description: |  Tag of the credentials that are stored in the CREDS and used to log onto a z/OS system  |
| Required:  | Yes, if connecting to a z/OS image |
| Default value: | None|
| Valid values: | Valid characters are A-Z, a - z, 0-9  |
| Examples: | <code>zos.image.SYSA.credentials=KEY_TO_CREDS_STORE</code><br><code>zos.image.SIMBANK.credentials=SIMBANK</code><br> |

</details>


<details>
<summary>Extra bundle required to implement the z/OS Batch Manager</summary>

| Property: | Extra bundle required to implement the zOS Batch Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.batch.manager |
| Description: | The name of the Bundle that implements the z/OS Batch Manager |
| Required:  | No |
| Default value: | dev.galasa.common.zosbatch.zosmf.manager |
| Valid values: | A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.   |
| Examples: | <code>zos.bundle.extra.batch.manager=dev.galasa.common.zosbatch.zosmf.manager</code><br> |

</details>
 
<details>
<summary>The z/OS Cluster ID</summary>

| Property: | The zOS Cluster ID |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.tag.[tag].clusterid | 
| Description: | The z/OS Cluster ID for the specified tag |
| Required:  | No |
| Default value: | None |
| Valid values: | Valid value is a character string with a maximum length of 32 |
| Examples: | <code>zos.tag.PLX1.clusterid=PLEXA</code><br> |

</details>
 
<details>
<summary>The images for a z/OS Cluster</summary>

| Property: | The images for a zOS Cluster |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.cluster.[clusterId].images | 
| Description: | The z/OS Images for the specified cluster. Specify more than one image by using commas. |
| Required:  | No |
| Default value: | None |
| Valid values: | Valid value is a character string with a maximum length of 32 |
| Examples: | <code>zos.cluster.PLEX1.images=SYSA,SYSB,SYSC</code><br> |

</details>
 
<details>
<summary>Extra bundle required to implement the z/OS Console Manager</summary>

| Property: | Extra bundle required to implement the zOS Console Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.console.manager |
| Description: | The name of the Bundle that implements the z/OS Console Manager |
| Required:  | No |
| Default value: | dev.galasa.common.zosconsole.zosmf.manager |
| Valid values: | A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.  |
| Examples: | <code>zos.bundle.extra.console.manager=dev.galasa.common.zosconsole.zosmf.manager</code><br> |

</details>
 
<details>
<summary>Developer Supplied Environment z/OS Image Cluster ID</summary>

| Property: | Developer Supplied Environment zOS Image Cluster ID |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.dse.tag.[tag].clusterid |
| Description: | The Cluster ID for the specified tag |
| Required:  | No |
| Default value: | None |
| Valid values: | A 1 - 8 length character name |
| Examples: | <code>zos.dse.tag.PLX1.clusterid=PLEXA</code><br> |

</details>
 
<details>
<summary>Developer Supplied Environment z/OS Image</summary>

| Property: | Developer Supplied Environment zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.dse.tag.[tag].imageid |
| Description: | The image ID of the Developer Supplied Environment for the specified tag |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid image ID |
| Examples: | <code>zos.dse.tag.MVS1.imageid=SYSA</code><br> |

</details>
 
<details>
<summary>Extra bundle required to implement the z/OS File Manager</summary>

| Property: | Extra bundle required to implement the zOS File Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.file.manager |
| Description: | The name of the Bundle that implements the z/OS File Manager |
| Required:  | No |
| Default value: | dev.galasa.common.zosfile.zosmf.manager |
| Valid values: |  A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.  |
| Examples: | <code>zos.bundle.extra.file.manager=dev.galasa.common.zosfile.zosmf.manager</code><br> |

</details>
 
<details>
<summary>IP Host ID of the z/OS Image</summary>

| Property: | IP Host ID of the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].iphostid |
| Description: | The IP Host ID of the z/OS Image for the supplied tag.<br>  If CPS property zos.image.[imageId].iphostid exists, then that is returned, otherwise the z/OS Image ID is returned |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid IP Host ID |
| Examples: | <code>zos.image.SYSA.iphostid=sysa.example.com</code><br> |

</details>
 
<details>
<summary>The z/OS Image</summary>

| Property: | The zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.tag.[tag].imageid |
| Description: | The image ID for the specified tag |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid z/OS image ID |
| Examples: | <code>zos.tag.MVS1.imageid=SYSA</code><br> |

</details>
 
<details>
<summary>Maximum slots for z/OS Image</summary>

| Property: | Maximum slots for zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].max.slots |
| Description: | The maximum slots available on a z/OS Image for the specified tag |
| Required:  | No |
| Default value: | 2 |
| Valid values: | 1 to 255 |
| Examples: | <code>zos.image.SYSA.max.slots=2</code><br> |

</details>

<details>
<summary>Code page for z/OS Image</summary>

| Property: | Code page for zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].codepage |
| Description: | The EBCDIC code page used on a z/OS image for the specified tag. EBCDIC features a variety of code pages, and a subset of characters, including square brackets and currency symbols, are encoded differently between code pages. Setting the correct code page for a z/OS image can resolve issues with displaying these characters. |
| Required:  | No |
| Default value: | 037 |
| Valid values: | A valid java.nio.charset EBCDIC character encoding |
| Examples: | <code>zos.image.SYSA.codepage=1047</code><br> |

</details>
 
<details>
<summary>The SYSNAME for z/OS Image</summary>

| Property: | The SYSNAME for zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.[imageId].sysname |
| Description: | The SYSNAME for the z/OS image | 
| Required:  | No |
| Default value: | The image ID of the image |
| Valid values: | The name must be 1-8 characters long; the valid characters are A-Z, 0-9, $, @, and #. |
| Examples: | <code>zos.image.SYSA.sysname=SYSA</code><br> |

</details>
 
<details>
<summary>The VTAM logon command template for the z/OS Image</summary>

| Property: | The VTAM logon command template for the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.vtam.logon<br>zos.image.[imageId].vtam.logon |
| Description: | A template for the command to log on to an application running on the zOS Image. The {0} argument in the template will be replaced with the VTAM application identifier for the requested application. |
| Required:  | No |
| Default value: | LOGON APPLID({0}) |
| Valid values: | A valid java.text.MessageFormat pattern with precisely one FormatElement |
| Examples: | <code>zos.image.vtam.logon=LOGON APPLID {0}</code><br> <code>zos.image.SYSA.vtam.logon={0} </code>|

</details>
 
<details>
<summary>The logon initial text for the z/OS Image</summary>

| Property: | The logon initial text for the z/OS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.image.logon.initial.text<br>zos.image.[imageId].logon.initial.text |
| Description: | A text string that is expected to be present on a 3270 terminal that has been connected to the z/OS image but before logon to any application system has been attempted. |
| Required:  | No |
| Default value: | None |
| Valid values: | Any text string |
| Examples: | <code>zos.image.logon.initial.text=VAMP</code><br><code>zos.image.SYSA.logon.initial.text=SYSA MAIN MENU</code> |

</details>
 
<details>
<summary>The run data set HLQ for the z/OS Image</summary>

| Property: | The run data set HLQ for the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.run.[imageId].dataset.hlq |
| Description: | The data set HLQ(s) for temporary data sets created on z/OS Image.<br>  If CPS property zos.run.[imageId].dataset.hlq exists, then that is returned |
| Required:  | No |
| Default value: | runuser.GALASA |
| Valid values: | A data set name can be one name segment, or a series of joined name segments. Segments are limited to eight characters, the first of which must be alphabetic (A to Z) or special (# @ $). The remaining seven characters are either alphabetic, numeric (0 - 9), special, a hyphen (-). Name segments are separated by a period (.). |
| Examples: | <code>zos.run.SYSA.dataset.hlq=USERID.GALASA</code><br> |

</details>
 
<details>
<summary>The run data UNIX path prefix for the z/OS Image</summary>

| Property: | The run data UNIX path prefix for the zOS Image |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.run.[imageId].unix.path.prefix |
| Description: | The UNIX path prefix for temporary data sets created on z/OS Image.<br>  If CPS property zos.run.[image].unix.path.prefix exists, then that is returned |
| Required:  | No |
| Default value: | /u/runuser/Galasa |
| Valid values: | A valid path |
| Examples: | <code>zos.run.SYSA.unix.path.prefix=/u/userid/Galasa</code><br> |

</details>
 
<details>
<summary>Extra bundle required to implement the z/OS TSO Command Manager</summary>

| Property: | Extra bundle required to implement the zOS TSO Command Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.tsocommand.manager |
| Description: | The name of the Bundle that implements the z/OS TSO Command Manager |
| Required:  | No |
| Default value: | dev.galasa.zostsocommand.ssh.manager |
| Valid values: | A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.   |
| Examples: | <code>zos.bundle.extra.tsocommand.manager=dev.galasa.zostsocommand.ssh.manager</code> |

</details>
 
<details>
<summary>Extra bundle required to implement the z/OS UNIX Command Manager</summary>

| Property: | Extra bundle required to implement the zOS UNIX Command Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | zos.bundle.extra.unixcomand.manager |
| Description: | The name of the Bundle that implements the z/OS UNIX Command Manager |
| Required:  | No |
| Default value: | dev.galasa.zosunixcommand.ssh.manager |
| Valid values: | A 1 - 8 length character name. A name containing more than 8 characters must be segmented by periods; 1 to 8 characters can be specified between periods. Valid characters are A-Z, a - z, 0-9, special characters.   |
| Examples: | <code>zos.bundle.extra.unix.manager=dev.galasa.zosunixcommand.ssh.manager</code> |

</details>
 
<details>
<summary>z/OS Batch restrict processing to the server on the specified image</summary>

| Property: | zOS Batch restrict processing to the server on the specified image |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.batchjob.[imageId].restrict.to.image |
| Description: | Use only the server (e.g. zOSMF, RSE API, etc) running on the image associated with the z/OS Batch job |
| Required:  | No |
| Default value: | false |
| Valid values: | true or false |
| Examples: | <code>zosbatch.batchjob.SYSA.restrict.to.image=true</code><br> <code>zosbatch.batchjob.default.restrict.to.image=false</code> |

</details>
 
<details>
<summary>z/OS Batch default input class</summary>

| Property: | zOS Batch default input class |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.default.input.class<br>zosbatch.default.[imageId].input.class |
| Description: | The default input class to set on the job card for submitted jobs |
| Required:  | No |
| Default value: | A |
| Valid values: | a valid JES input class literal |
| Examples: | <code>zosbatch.default.SYSA.input.class=S</code><br> <code>zosbatch.default.input.class=A</code> |

</details>
 
<details>
<summary>z/OS Batch job execution wait timeout</summary>

| Property: | zOS Batch job execution wait timeout |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.batchjob.[imageId].timeout |
| Description: | The value in seconds to wait for the z/OS Batch job execution to complete when submitted via zOSMF |
| Required:  | No |
| Default value: | 350 |
| Valid values: | 0 to {@link Integer#MAX_VALUE} |
| Examples: | <code>zosbatch.batchjob.SYSA.timeout=350</code><br> <code>zosbatch.batchjob.default.timeout=60</code> |

</details>
 
<details>
<summary>z/OS Batch jobname prefix</summary>

| Property: | zOS Batch jobname prefix |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.jobname.[imageId].prefix |
| Description: | The z/OS Batch jobname prefix when submitted via zOSMF |
| Required:  | No |
| Default value: | GAL |
| Valid values: | 1-7 characters |
| Examples: | <code>zosbatch.jobname.SYSA.prefix=JOB</code><br> <code>zosbatch.jobname.default.prefix=XXX</code> |

</details>
 
<details>
<summary>z/OS Batch default MSGCLASS</summary>

| Property: | zOS Batch default MSGCLASS |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.default.class<br>zosbatch.default.[imageId].message.class |
| Description: | The default message class to set on the job card for submitted jobs |
| Required:  | No |
| Default value: | A |
| Valid values: | a valid JES message class literal |
| Examples: | <code>zosbatch.default.SYSA.message.class=S</code><br> <code>zosbatch.default.message.class=A</code> |

</details>
 
<details>
<summary>z/OS Batch default message level</summary>

| Property: | zOS Batch default message level |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.default.message.level<br>zosbatch.default.[imageId].message.level |
| Description: | The default message level to set on the job card for submitted jobs |
| Required:  | No |
| Default value: | (1,1) |
| Valid values: | a valid JES message level |
| Examples: | <code>zosbatch.default.SYSA.message.level=(1,1)</code><br> <code>zosbatch.default.message.level=(2,0)</code> |

</details>
 
<details>
<summary>z/OS Batch job truncate JCL</summary>

| Property: | zOS Batch job truncate JCL |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.batchjob.[imageId].truncate.jcl.records |
| Description: | The z/OSMF submit job will fail if supplied with JCL records greater than 80 characters. Setting this property to true will truncate any records to 80 characters and issue a warning message. |
| Required:  | No |
| Default value: | true |
| Valid values: | true or false |
| Examples: | <code>zosbatch.batchjob.SYSA.truncate.jcl.records=true</code><br> <code>zosbatch.batchjob.default.truncate.jcl.records=false</code> |

</details>
 
<details>
<summary>z/OS Batch job use SYSAFF</summary>

| Property: | zOS Batch job use SYSAFF |
| --------------------------------------- | :------------------------------------- |
| Name: | zosbatch.batchjob.[imageId].use.sysaff |
| Description: | Use the run the z/OS Batch job on the specified image by specifying {@code /*JOBPARM SYSAFF=[imageid]} |
| Required:  | No |
| Default value: | true |
| Valid values: | true or false |
| Examples: | <code>zosbatch.batchjob.SYSA.use.sysaff=true</code><br> <code>zosbatch.batchjob.default.use.sysaff=false</code> |

</details>
 
<details>
<summary>Restrict z/OS console processing to the zOSMF server on the specified image</summary>

| Property: | Restrict zOS console processing to the zOSMF server on the specified image |
| --------------------------------------- | :------------------------------------- |
| Name: | zosconsole.console.restrict.to.image<br>zosconsole.console.[imageId].restrict.to.image |
| Description: | Use only the zOSMF server running on the image associated with the z/OS Console |
| Required:  | No |
| Default value: | false |
| Valid values: | true or false |
| Examples: | <code>zosconsole.console.restrict.to.image=true</code><br> <code>zosconsole.console.SYSA.restrict.to.image=true</code> |

</details>
 
<details>
<summary>z/OS File the maximum number of items from a UNIX directory list</summary>

| Property: | zOS File the maximum number of items from a UNIX directory list |
| --------------------------------------- | :------------------------------------- |
| Name: | zosfile.unix.[imageId].directory.list.max.items |
| Description: | The maximum number of items the server (e.g. zOSMF, RSE API, etc) returns when listing the content of a UNIX directory |
| Required:  | No |
| Default value: | 1000 |
| Valid values: | 0 to 65535 |
| Examples: | <code>zosfile.unix.SYSA.directory.list.max.items=1000</code><br> |

</details>
 
<details>
<summary>z/OS File restrict processing to the server on the specified image</summary>

| Property: | zOS File restrict processing to the server on the specified image |
| --------------------------------------- | :------------------------------------- |
| Name: | zosfile.file.restrict.to.image<br>zosfile.file.[imageId].restrict.to.image |
| Description: | Use only the server (e.g. zOSMF, RSE API, etc) running on the image associated with the z/OS data set or file |
| Required:  | No |
| Default value: | false |
| Valid values: | true or false |
| Examples: | <code>zosfile.file.restrict.to.image=true</code><br> <cods>zosfile.file.SYSA.restrict.to.image=true</code> |

</details>
 
<details>
<summary>z/OS File UNIX permission bits to be used in creating the file or directory</summary>

| Property: | zOS File UNIX permission bits to be used in creating the file or directory |
| --------------------------------------- | :------------------------------------- |
| Name: | zosfile.[imageId].unix.file.permission<br>zosfile.[imageId].unix.file.permission |
| Description: | The UNIX file or directory permission bits to be used in creating the file or directory |
| Required:  | No |
| Default value: | None |
| Valid values: | Valid values are r,w,x,s,- |
| Examples: | <code>zosfile.unix.file.permission=rwxrwx---</code><br> <code>zosfile.SYSA.unix.file.permission=rwxrwxrrx</code> |

</details>


# <a name="annotations"></a>Annotations provided by the Manager

The following annotations are available with the z/OS Manager
<details>
<summary>z/OS Batch</summary>

| Annotation: | z/OS Batch |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosBatch |
| Description: | The <code>@ZosBatch</code> annotation requests the z/OS Manager to provide a z/OS Batch instance associated with a z/OS image.  The test can request multiple z/OS Batch instances, with the default being associated with the <b>primary</b> z/OS image.<br> At test end, the Manager stores the job output with the test results archive and removes jobs from the JES queue. |
| Attribute: `imageTag` |  The <code>imageTag</code> is used to identify the z/OS image. |
| Syntax: | @ZosImage(imageTag="A")<br> public IZosImage zosImageA;<br> @ZosBatch(imageTag="A")<br> public IZosBatch zosBatchA;<br></code> |
| Notes: | The <code>IZosBatch</code> interface has a single method, {@link IZosBatch#submitJob(String, IZosBatchJobname)} to submit a JCL  as a <code>String</code> and returns a <code>IZosBatchJob</code> instance.<br><br> See <a href="https://javadoc.galasa.dev/dev/galasa/zosbatch/ZosBatch.html" target="_blank">ZosBatch</a>, <a href="https://javadoc.galasa.dev/dev/galasa/zosbatch/IZosBatch.html" target="_blank">IZosBatch</a> and <a href="https://javadoc.galasa.dev/dev/galasa/zosbatch/IZosBatchJob.html" target="_blank">IZosBatchJob</a> to find out more. |

</details>

<details>
<summary>z/OS Console</summary>

| Annotation: | z/OS Console |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosConsole |
| Description: | The <code>@ZosConsole</code> annotation requests the z/OS Manager to provide a z/OS Console instance associated with a z/OS image.  The test can request multiple z/OS Console instances, with the default being associated with the <b>primary</b> z/OS image.<br> |
| Attribute: `imageTag` |  The tag of the z/OS Image this variable is to be populated with |
| Syntax: | @ZosImage(imageTag="A")<br> public IZosImage zosImageA;<br> @ZosConsole(imageTag="A")<br> public IZosConsole zosConsoleA;<br></code> |
| Notes: | The <code>IZosConsole</code> interface has two methods, {@link IZosConsole#issueCommand(String)} and {@link IZosConsole#issueCommand(String, String)} to issue a command to the z/OS console and returns a <code>IZosConsoleCommand</code> instance.<br><br> See <a href="https://javadoc.galasa.dev/dev/galasa/zosconsole/ZosConsole.html" target="_blank">ZosConsole</a>, <a href="https://javadoc.galasa.dev/dev/galasa/zosconsole/IZosConsole.html" target="_blank">IZosConsole</a> and <a href="https://javadoc.galasa.dev/dev/galasa/zosconsole/IZosConsoleCommand.html" target="_blank">IZosConsoleCommand</a> to find out more. |

</details>

<details>
<summary>z/OS File</summary>

| Annotation: | z/OS File |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosFileHandler |
| Description: | The <code>@ZosFileHandler</code> annotation requests the z/OS Manager to provide a handler instance to manage data sets and UNIX files on a z/OS image.  A single z/OS File Handler instance can manage multiple z/OS data sets and UNIX files on multiple z/OS images.<br> |
| Syntax: | <code>@ZosFileHandler<br> public IZosFileHandler zosFileHandler;<br></code> |
| Notes: | The <code>IZosFileHandler</code> interface has three methods supplying file name and z/OS image:<br> {@link IZosFileHandler#newDataset(String, dev.galasa.zos.IZosImage)}<br>  {@link IZosFileHandler#newVSAMDataset(String, dev.galasa.zos.IZosImage)}<br> {@link IZosFileHandler#newUNIXFile(String, dev.galasa.zos.IZosImage)}<br> returning an object representing the type of file requested. This can be an existing file or can be created via a method on the file object.<br><br> See <a href="https://javadoc.galasa.dev/dev/galasa/zosfile/ZosFileHandler.html" target="_blank">ZosFileHandler</a>, <a href="https://javadoc.galasa.dev/dev/galasa/zosfile/IZosFileHandler.html" target="_blank">IZosFileHandler</a>, <a href="https://javadoc.galasa.dev/dev/galasa/zosfile/IZosDataset.html" target="_blank">IZosDataset</a>, <a href="https://javadoc.galasa.dev/dev/galasa/zosfile/IZosVSAMDataset.html" target="_blank">IZosVSAMDataset</a> and <a href="https://javadoc.galasa.dev/dev/galasa/zosfile/IZosUNIXFile.html" target="_blank">IZosUNIXFile</a> to find out more. |

</details>

<details>
<summary>z/OS TSO Command</summary>

| Annotation: | z/OS TSO Command |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosTSOCommand |
| Description: | The <code>@ZosTSOCommand</code> annotation requests the z/OS Manager to provide a z/OS TSO Command instance associated with a z/OS image.  The test can request multiple z/OS TSO Command instances, with the default being associated with the <b>primary</b> z/OS image.<br> |
| Attribute: `imageTag` |  The tag of the z/OS Image this variable is to be populated with |
| Syntax: | @ZosImage(imageTag="A")<br> public IZosImage zosImageA;<br> @ZosTSOCommand(imageTag="A")<br> public IZosTSOCpmmand zosTSOA;<br></code> |
| Notes: | The <code>IZosTSOCommand</code> interface provides the methods {@link IZosTSOCommand#issueCommand(String)} and {@link IZosTSOCommand#issueCommand(String, long)} to issue a command to z/OS TSO Command and returns a <code>String</code>.<br><br> See <a href="https://javadoc.galasa.dev/dev/galasa/zostsocommand/IZosTSOCommand.html" target="_blank">IZosTSOCommand</a> to find out more. |

</details>

<details>
<summary>z/OS UNIX Command</summary>

| Annotation: | z/OS UNIX Command |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosUNIXCommand |
| Description: | The <code>@ZosUNIXCommand</code> annotation requests the z/OS Manager to provide a z/OS UNIX instance associated with a z/OS image.  The test can request multiple z/OS UNIX Command instances, with the default being associated with the <b>primary</b> z/OS image.<br> |
| Attribute: `imageTag` |  The tag of the z/OS Image this variable is to be populated with |
| Syntax: | @ZosImage(imageTag="A")<br> public IZosImage zosImageA;<br> @ZosUNIXCommand(imageTag="A")<br> public IZosUNIXCommand zosUNIXCommandA;<br></code> |
| Notes: | The <code>IZosUNIXCommand</code> interface provides the methods {@link IZosUNIXCommand#issueCommand(String)} and {@link IZosUNIXCommand#issueCommand(String, long)} to issue a command to z/OS UNIX and returns a <a href="https://javadoc.galasa.dev/dev/galasa/zosunixcommand/String.html" target="_blank">String</a> response.<br><br> See <a href="https://javadoc.galasa.dev/dev/galasa/zosunixcommand/IZosUNIXCommand.html" target="_blank">IZosUNIXCommand</a> to find out more. |

</details>



# <a name="codesnippets"></a>Code snippets and examples

Use the following code snippets to help you get started with the z/OS Manager.
 
<details><summary>Request a z/OS TSO Command instance</summary>

The following snippet shows the code that is required to request a z/OS TSO Command instance in a Galasa test:

```
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosTSOCommand(imageTag="A")
public IZosTSOCommand tsoCommand;
```

The code creates a z/OS TSO Command instance associated with the z/OS Image allocated in the *zosImageA* field.
</details>

<details><summary>Issue a z/OS TSO Command and retrieve the immediate response</summary>

Issue the z/OS TSO `TIME` Command and retrieve the response:

```
String tsoCommandString = "TIME";
String tsoResponse = tsoCommand.issueCommand(tsoCommandString);
```

The String `tsoResponse`  contains the output of the TSO TIME command, e.g. 

```
IKJ56650I TIME-12:01:00 PM. CPU-00:00:00 SERVICE-290 SESSION-00:00:00 APRIL 1,2020
```
</details>
 
<details><summary>Request a z/OS UNIX Command instance</summary>

The following snippet shows the code that is required to request a z/OS UNIX Command instance in a Galasa test:

```
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosUNIXCommand(imageTag="A")
public IZosUNIXCommand unixCommand;
```

The code creates a z/OS UNIX Command instance associated with the z/OS Image allocated in the *zosImageA* field.
</details>

<details><summary>Issue a z/OS UNIX Command and retrieve response</summary>

Issue the z/OS UNIX `date` Command and retrieve the response:

```
String unixCommandString = "date";
String unixResponse = unixCommand.issueCommand(unixCommandString);
```

The String `unixResponse`  contains the output of the UNIX TIME command, e.g. 

```
Wed Apr 1 12:01:00 BST 2020
```
</details>
 
<details><summary>Request a z/OS Console instance</summary>

The following snippet shows the code that is required to request a z/OS Console instance in a Galasa test:

```
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosBatch(imageTag="A")
public IZosConsole zosConsole;
```

The code creates a z/OS Console instance associated with the z/OS Image allocated in the *zosImageA* field.
</details>

<details><summary>Issue a z/OS Console command and retrieve the immediate response</summary>

Issue a z/OS Console command and retrieve the immediate console command response:

```
String command = "D A,L";
IZosConsoleCommand consoleCommand = zosConsole.issueCommand(command);
String immediateResponse = consoleCommand.getResponse();

```
</details>


<details><summary>Issue a z/OS Console command and retrieve the delayed response</summary>

Issue a z/OS Console command and retrieve the delayed console command response:

```
String command = "D A,L";
IZosConsoleCommand consoleCommand = zosConsole.issueCommand(command);
String delayedResponse = consoleCommand.requestResponse();

```
</details>
 
<details><summary>Request a z/OS Batch instance</summary>

The following snippet shows the code that is required to request a z/OS Batch instance in a Galasa test:

```
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosBatch(imageTag="A")
public IZosBatch zosBatch;
```


The code creates a z/OS Batch instance associated with the allocated with the z/OS Image allocated in the *zosImageA* field.
</details>

<details><summary>Submit a z/OS Batch Job</summary>

Submit a z/OS Batch Job using the supplied JCL and a Galasa allocated Job Name:

```
String jcl = "//STEP1    EXEC PGM=IEFBR14";
IZosBatchJob batchJob = zosBatch.submitJob(jcl, null);
```
</details>


<details><summary>Submit a z/OS Batch Job with job card parameters</summary>

Submit a z/OS Batch Job using the supplied JCL, a Galasa allocated Job Name and overidding the default input and message class:

```
String jcl = "//STEP1    EXEC PGM=IEFBR14";
ZosBatchJobcard jobcard = new ZosBatchJobcard().
                          .setInputClass("B")
                          .setMsgClass("X");
IZosBatchJob batchJob = zosBatch.submitJob(jcl, null, jobcard);
```
</details>

<details><summary>Wait for z/OS Batch Job to complete</summary>

Wait for z/OS Batch job to complete and check maximum return code:

```
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
</details>


<details><summary>Retrieve the job output</summary>

Use the following code to retrieve the output from a z/OS Batch Job:

```
IZosBatchJobOutput jobOutput = batchJob.retrieveOutput();
List<IZosBatchJobOutputSpoolFile> spoolFiles = jobOutput.getSpoolFiles();
for (IZosBatchJobOutputSpoolFile spoolFile : spoolFiles) {
    String ddName = spoolFile.getDdname();
    String output = spoolFile.getRecords();
    ...
}

```
</details>

<details><summary>Obtain a list of active jobs</summary>

Use the following code to obtain a list of active jobs called *MYJOB1* with an owner of *USERID*:

```
List<IZosBatchJob> jobs = zosBatch.getJobs("MYJOB1", "USERID");
for (IZosBatchJob job : jobs) {
    if (job.getStatus().equals("ACTIVE")) {
        ...
    }
}

```
</details>

<details><summary>Retrieve the content of a specific spool file from an active CICS region</summary>

Use the following code to retrieve and process the output from the *MSGUSR* spool file:

```
List<IZosBatchJob> jobs = zosBatch.getJobs("CICSRGN", "CICSUSR");
for (IZosBatchJob job : jobs) {
    if (job.getStatus().equals("ACTIVE")) {
        String msgusr = cicsJob.getSpoolFile("MSGUSR");
        if (msgusr.contains("DFHAC2236")) {
            ...
        }
        break;
    }
}

```


The code retrieves a list of CICS regions named *CICSRGN* with and owner of *CICSUSR*. It then loops through until it finds the first active region. The content of the *MSGUSR* spool file is obtained and checked for the string *DFHAC2236*.

In this example, we assume there will only one spool file with the ddname of *MSGUSR*. If this were not the case, the following code could be used:

```
List<IZosBatchJob> jobs = zosBatch.getJobs("CICSRGN", "CICSUSR");
for (IZosBatchJob job : jobs) {
    List<IZosBatchJobOutputSpoolFile> spoolFiles = job.retrieveOutput().getSpoolFiles();
    for (IZosBatchJobOutputSpoolFile spoolFile : spoolFiles) {
        if (spoolFile.getDdname().equals("SYSOUT") &&
            spoolFile.getStepname().equals("STEP2")) {
            String output = spoolFile.getRecords();
            ...
        }
    }
}

```

Here, the code retrieves the content of the *SYSOUT* spool file for job step *STEP2*.
</details>
 
<details><summary>Request a z/OS File Handler instance</summary>

The following snippet shows the code that is required to request a z/OS File Handler instance in a Galasa test:

```
@ZosFileHandler
public IZosFileHandler zosFileHandler;
```
</details>

<details><summary>Read the content of an existing sequential data set</summary>

Create a new *IZosDataset* object representing an existing sequential data set. If the data set exists, retrieve the content in text mode:

```
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.EXISTING.DATASET.SEQ", zosImage);
if (dataSet.exists()) {
    String content = dataSet.retrieveAsText();
    ...
}
```
</details>


<details><summary>Read the content of an existing partitioned data set member</summary>

Create a new *IZosDataset* object representing an existing partitioned data set (PDS). If the PDS exists, check if the member exists and retrieve it's content in text mode:

```
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.EXISTING.DATASET.SEQ, zosImage);
    String memberName = "MEMBER1";
    if (dataSet.exists() && dataSet.memberExists(memberName)) {
        String content = dataSet.memberRetrieveAsText(memberName);
        ...
    }
```
</details>


<details><summary>Create a new sequential data set</summary>

Create a new *IZosDataset* object representing a new sequential data set. If the data set does not exist, allocate the data set with attributes to the equivalent of the following JCL:

```
//NEWDS    DD DSN=GALASA.NEW.DATASET.SEQ,DISP=(NEW,CATLG),
//            DSORG=PS,RECFM=FB,LRECL=80,BLKSIZE=32720,
//            UNIT=SYSDA,SPACE=(TRK,(1,1))
```
Finally, content is written to the data set in text mode:


```
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
...
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
</details>

<details><summary>Create a new partitioned data set member</summary>

Create a new *IZosDataset* object representing a new partitioned data (PDS) set member. If the data set does not exist, allocate the PDS with attributes to the equivalent of the following JCL:

```
//NEWPDS   DD DSN=GALASA.NEW.DATASET.PDS,DISP=(NEW,CATLG),
//            DSORG=PS,RECFM=FB,LRECL=80,BLKSIZE=32720,
//            UNIT=SYSDA,SPACE=(TRK,(1,1,15))
```
Finally, content is written to a member in the PDS in text mode:


```
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
...
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
</details>

<details><summary>Create a new VSAM KSDS</summary>

Create a new *IZosVSAMDataset* object representing a new VSAM KSDS data set. If the data set is allocated with a minimum set of attributes:

```
IZosVSAMDataset vsamDataSet = zosFileHandler.newVSAMDataset("ROBERTD.GALASA.TEST.DS.ANOTHER.KSDS", zosImage);
vsamDataSet.setSpace(VSAMSpaceUnit.CYLINDERS, 1, 1);
vsamDataSet.setRecordSize(50, 101);
vsamDataSet.create();
```
</details>

<details><summary>Read the contents of a z/OS UNIX File</summary>

Create a new *IZosDataset* object representing a UNIX file. If the file exists, retrieve the content in text mode:

```
IZosUNIXFile unixFile = zosFileHandler.newUNIXFile("/tmp/Galasa/existingFile", zosImage);
if (unixFile.exists()) {
    unixFile.setDataType(UNIXFileDataType.TEXT);
    String content = unixFile.retrieve();
}
```

</details>

<details><summary>Read the contents of a z/OS UNIX File</summary>

Create a new *IZosDataset* object representing a new UNIX file. If UNIX file does not exist, create it. Write to the file in binary mode:

```
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

</details>

<details><summary>List the contents of a z/OS UNIX Directory</summary>

Create a new *IZosDataset* object representing a new UNIX directory. If UNIX directory exists, list its contents:

```
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

</details>

