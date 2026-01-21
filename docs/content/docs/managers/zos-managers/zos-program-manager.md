---
title: "zOS Program Manager"
---

This Manager is at Alpha level. 


## Overview

This Manager allows Galasa tests to compile and link z/OS programs.

The `@ZosProgram` annotation defines the program to the Galasa test. Program attributes, for example, program name, programming language and source location are specified by using the annotation  elements.

The source for the program is stored as a resource, along with the test. The z/OS Program Manager processes  each `@ZosProgram` annotation before any of the test methods are executed. 

The Manager retrieves the source from the test bundle, builds and submits the relevant compile and link JCL based on  the programs attributes and CPS properties. The batch job is saved with the test run archive. The  program can be executed in the test by retrieving the library containing the load module by using  the `getLoadLibrary()` method.  

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/zosprogram/package-summary.html){target="_blank"}.


## Including the Manager in a test

To use the z/OS program Manager in a test you must import the _@ZosProgram_ annotation into the test, as shown in the following example: 

```java
@ZosProgram
public IZosProgram zosProgramA;
```

You also need to add the Manager dependency into the pom.xml file if you are using Maven, or into the build.gradle file if you are using Gradle. 

If you are using Maven, add the following dependencies into the _pom.xml_ in the _dependencies_ section:

```xml
<dependency>
    <groupId>dev.galasa</groupId>
    <artifactId>dev.galasa.zosprogram.manager</artifactId>
</dependency>
```

If you are using Gradle, add the following dependencies into ```build.gradle``` in the _dependencies_ closure:

```groovy
dependencies {
    compileOnly 'dev.galasa:dev.galasa.zosprogram.manager'
}
```


## Configuration Properties

The following properties are used to configure the z/OS Program Manager.
 

### z/OS CICS data set Prefix

| Property: | zOS CICS data set Prefix |
| --------------------------------------- | :------------------------------------- |
| Name: | zosprogram.cics.[imageid].dataset.prefix |
| Description: |  The prefix of the CICS z/OS data sets that contain load modules (SDFHLOAD) and source copybooks, macros, link SYSIN (SDFHC370, SDFHCOB, SDFHPL1, SDFHMAC, SDFHSAMP) that are used in program compile and link JCL. |
| Required:  | Yes, for CICS programs only. The property is not used in non CICS programs |
| Default value: | 'CICS' |
| Valid values: | A comma separated list of one or more valid z/OS data set prefixes |
| Examples: | `zosprogram.cics.MVSA.dataset.prefix=CICS`<br>`zosprogram.cics.default.dataset.prefix=SYS1,CICS` |


### z/OS Language Environment data set prefix

| Property: | zOS Language Environment data set prefix |
| --------------------------------------- | :------------------------------------- |
| Name: | zosprogram.le.[imageid].dataset.prefix |
| Description: | The prefix of the Language Environment z/OS data sets that contain load modules (SCEERUN, SCEERUN2) and source copybooks, macros, link SYSIN etc (SCEESAMP) that are used in program compile and link JCL. |
| Required:  | Yes |
| Default value: | 'CEE' |
| Valid values: | A comma separated list of one or more valid zOS data set prefixess |
| Examples: | `zosprogram.le.MVSA.dataset.prefix=CEE`<br>`zosprogram.le.dataset.prefix=SYS1.LE,CEE` |


### z/OS Program Language compile SYSLIBs

| Property: | zOS Program Language compile SYSLIBs |
| --------------------------------------- | :------------------------------------- |
| Name: | zosprogram.[language].[imageid].compile.syslibs |
| Description: | The site specific and language specific (COBOL, C, PL1, ASSEMBLER) custom z/OS data sets that contain source copybooks and macros that are used in the compile SYSLIB concatenation in the z/OS program compile and link JCL. |
| Required:  | No |
| Default value: | None |
| Valid values: | A comma separated list of one or more valid zOS data sets |
| Examples: | `zosprogram.cobol.MVSA.compile.syslibs=TEAM.COPYBOOK`<br>`zosprogram.cobol.compile.syslibs=COMPANY.COPYBOOK,TEAM.COPYBOOK` |


### z/OS Program Language data set prefix

| Property: | zOS Program Language data set prefix |
| --------------------------------------- | :------------------------------------- |
| Name: | zosprogram.[language].[imageid].dataset.prefix |
| Description: | The prefix of the language specific z/OS data sets that contain STEPLIB load modules that are used in program compile and link JCL, for example, in COBOL - SIGYCOMP, in C - SCCNCMP, in PL1 - SIBMZCMP |
| Required:  | An entry is required for each language used, for example, COBOL, C, PL1, ASSEMBLER |
| Default value: | None |
| Valid values: | A comma separated list of one or more valid zOS data set prefixes |
| Examples: | `zosprogram.cobol.MVSA.dataset.prefix=IGY.V6R3M0`<br>`zosprogram.cobol.dataset.prefix=SYS1.COBOL,IGY.V6R3M0` |


### z/OS Program Language link SYSLIBs

| Property: | zOS Program Language link SYSLIBs |
| --------------------------------------- | :------------------------------------- |
| Name: | zosprogram.[language].[imageid].link.syslibs |
| Description: | The site specific and language specific (COBOL, C, PL1, ASSEMBLER) custom z/OS data sets that contain load modules that are used in the link SYSLIB concatenation in the z/OS program compile and link JCL. |
| Required:  | No |
| Default value: | None |
| Valid values: | A comma separated list of zOS data sets |
| Examples: | `zosprogram.cobol.MVSA.link.syslibs=TEAM.LOADLIB`<br>`zosprogram.cobol.link.syslibs=COMPANY.LOADLIB,TEAM.LOADLIB` |


## Annotations provided by the Manager

The following annotations are available with the z/OS Program Manager


### z/OS Program

| Annotation: | z/OS Program |
| --------------------------------------- | :------------------------------------- |
| Name: | @ZosProgram |
| Description: | The `@ZosProgram` annotation requests the z/OS Program Manager to Compile and Bind a program on a z/OS image.  The test can request multiple z/OS Program instances |
| Attribute: `name` |  The program name. Required. |
| Attribute: `location` |  Path to the location of the program source in the Galasa test bundle. This can be either the full path including the file name or the directory containing the source with the name specified in the name attribute with the extension specified in the language attribute. Optional. The default value is "resources". |
| Attribute: `language` |  The programming language. Required. See [ZosProgram.Language](../../reference/javadoc/dev/galasa/zosprogram/ZosProgram.Language.html){target="_blank"}. <br><br>  |
| Attribute: `cics` |  Is a CICS program and requires the CICS translator. Optional. The default value is false.|
| Attribute: `loadlib` |  The load module data set name. Optional. The default value is "".|
| Attribute: `imageTag` |  The `imageTag` is used to identify the z/OS image. Optional. The default value is "primary".|
| Attribute: `compile` |  Compile this zOS program. Optional. The default value is true.|
| Syntax: | <pre lang="java">@ZosImage(imageTag="A")<br>public IZosImage zosImageA;<br><br>@ZosProgram(imageTag="A")<br>public IZosProgram zosProgramA;<br></pre> |
| Notes: | The `IZosProgram` interface has a number of methods to manage the z/OS Program. See [ZosProgram](../../reference/javadoc/dev/galasa/zosprogram/ZosProgram.html){target="_blank"} and [IZosProgram](../../reference/javadoc/dev/galasa/zosprogram/IZosProgram.html){target="_blank"} to find out more. |


## Code snippets and examples

Use the following code snippets to help you get started with the z/OS Program Manager.
 

### Compile and Link a COBOL program

The following snippet shows the code that is required to compile and link a *COBOL* program called *MYPROG* in a Galasa test:

```java
@ZosProgram(name = "MYPROG",
    location = "source",
    language = Language.COBOL,
    imageTag = "A")
public IZosProgram myprog;
```

The program source is stored in a file named *MYPROG.cbl* in a folder named *source* in the test bundle resources folder. 
The manager builds the JCL to compile and link the source code and submits it on the zOS Image allocated in the *zosImageA* field.


### Run the compiled program

The following snippet shows the code required to run the compiled program in a batch job:

```java
@ZosImage(imageTag = "A")
public IZosImage image;

@ZosBatch(imageTag = "A")
public IZosBatch zosBatch;

//...

StringBuilder jcl = new StringBuilder();
jcl.append("//STEP1   EXEC PGM=");
jcl.append(myprog.getName());
jcl.append("\n");
jcl.append("//STEPLIB DD DSN=");
jcl.append(myprog.getLoadlib().getName());
jcl.append(",DISP=SHR\n");
jcl.append("//SYSOUT  DD SYSOUT=*");
IZosBatchJob job = zosBatch.submitJob(jcl.toString(), null);
//...
```

The manager created a load library for *MYPROG* because the *@ZosProgram* annotation did not specify one. The name of the library is obtained using the *getLoadlib()* method on the field so that it can be added to the *STEPLIB* in the JCL. 

