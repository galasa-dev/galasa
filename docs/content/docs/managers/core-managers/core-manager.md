---
title: "Core Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/core/manager/package-summary.html){target="_blank"}.


## Overview

The Core Manager provides tests with access to some of the core features within the Galasa Framework. The Core Manager is always initialised and included in a test run and contributes the `Logger`, `StoredArtifactRoot`, `ResourceString`, `RunName` and `TestProperty` annotations.

The `Logger` annotation is provided by the Core Manager to create the log which is then automatically stored in the Result Archive Store (RAS) by the Galasa framework.

The `StoredArtifactRoot` annotation lets your test write specific output to the RAS.  Whilst the Galasa framework automatically sends test output to be stored in the RAS, this annotation enables you to write code to send output specific to your application to be stored. You can use this output for review or to enable compares to be done when tests fail.

The Core Manager uses methods including `getCredentials`, `getUsernamePassword`, `getRunName` and `ConfidentialText` credentials. `getCredentials` lets you retrieve a user id and password from a file to use in your test as well as other forms of credentials such as tokens, whilst `getUsernamePassword` lets you retrieve a user id and password only from a file to use in your test and `ConfidentialText` ensures that the password value is masked. The ability to get credentials from a file means that you do not need to hard code these values in your test and that the test can be run in different environments without the need to change a single line of code. The Core Manager supports [Gherkin keywords](https://github.com/galasa-dev/simplatform/blob/main/galasa-simbank-tests/dev.galasa.simbank.manager/src/main/java/dev/galasa/simbank/manager/internal/gherkin/SimbankStatementOwner.java){target="_blank"}.


## Including the Manager in a test

To use the Core Manager in a test you must import the _@CoreManager_ annotation into the test, as shown in the following example: 

```java
@CoreManager
public ICoreManager coreManager;
```

You also need to add the Manager dependency into the pom.xml file if you are using Maven, or into the build.gradle file if you are using Gradle. 

If you are using Maven, add the following dependencies into the _pom.xml_ in the _dependencies_ section:

```xml
<dependency>
    <groupId>dev.galasa</groupId>
    <artifactId>dev.galasa.core.manager</artifactId>
</dependency>
```

If you are using Gradle, add the following dependencies into ```build.gradle``` in the _dependencies_ closure:

```groovy
dependencies {
    compileOnly 'dev.galasa:dev.galasa.core.manager'
}
```


## Configuration Properties

The following are properties used to configure the Core Manager:


### Resource String Pattern CPS Property

| Property: | Resource String Pattern CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | core.resource.string.[length].pattern |
| Description: | The Resource String Pattern CPS property allows Galasa to form a string based upon a certain standard or pattern, for example, must begin with the letter `A`, must end with a numeral. The patterns are formed from the Galasa `ResourcePoolingService` which uses a custom syntax. This property is used when the Core Manager provisions a `@ResourceString` into a test.  This string can used for anything within the test class, for example, to create new z/OS PDS names, or a piece of data that the test will use. The string must be unique to that test across all the other tests within the Ecosystem.  |
| Required:  | No |
| Default value: | {A-Z} for each byte for the specified length |
| Valid values: | For each character the value can be a constant or a random choice from a literal, eg {A-Z} results in a single character between A and Z inclusive. {0-9} or {a-zA-Z0-9} are options. DFH{A-Z}{0-1}{0-9}{0-9}{0-9}, results in DFHA1789 for example, the 5th character can only be 0 or 1. 
| Examples: | `core.resource.string.8.length={A-Z}{A-Z}{A-Z}{A-Z}{A-Z}{A-Z}{A-Z}{A-Z}` |


### Test Property CPS Property

| Property: | Test Property CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | test.prefix.infix.infix.infix.suffix |
| Description: |  Enables a test property value to be extracted from the CPS or Overrides file for use in a test. See [Code snippets and examples](#code-snippets-and-examples) for more information. |
| Required:  | No |
| Default value: | NA|
| Valid values: | Any valid string value | 
| Examples: | `test.prefix.infix.suffix=value` |


## Annotations provided by the Manager

The following annotations are provided by the Core Manager:


### Logger

| Name: | Logger |
| --------------------------------------- | :------------------------------------- |
| Name: | @Logger |
| Description: | Gives the test access to the log which is then automatically stored in the Result Archive Store (RAS) by the Galasa framework. An object of type `Log` can be annotated with this annotation. |
| Syntax:  | <pre lang="java">@Logger<br>public Log logger;</pre>|


### Resource String

| Name: | ResourceString |
| --------------------------------------- | :------------------------------------- |
| Name: | @ResourceString |
| Description: | A unique (within the ecosystem) string of a set length. The Resource String Pattern CPS property `core.resource.string.[length].pattern` determines the pattern of the random string. Annotates a public `IResource` object type |
| Attribute: `tag` |  Tag name |
| Attribute: `length` |  Default value is 8. |
| Syntax:  | <pre lang="java">@ResourceString(tag = "tagname", length=8)<br>public IResourceString resourceString;</pre>|


### Run Name

| Name: | RunName |
| --------------------------------------- | :------------------------------------- |
| Name: | @RunName |
| Description: | The name of the test run. Can be used for making resource names unique to this run. The test run name is unique across all local and automated runs that are in the system at that point. |
| Syntax:  | <pre lang="java">@RunName<br>public String runName;</pre>|


### Stored Artifact Root

| Name: | StoredArtifactRoot |
| --------------------------------------- | :------------------------------------- |
| Name: | @StoredArtifactRoot |
| Description: | Lets your test write specific output to the RAS. An object of type `Path` can be annotated with this annotation. |
| Syntax:  | `artifactRoot.resolve(folder).resolve(file);`|


### Test Property

| Name: | TestProperty |
| --------------------------------------- | :------------------------------------- |
| Name: | @TestProperty |
| Description: | Enables a value to be extracted from the CPS or Overrides file for use in the test |
| Attribute: `prefix` |  Set the prefix of the property that you want to extract  |
| Attribute: `suffix` |  Set the suffix of the property that you want to extract |
| Attribute: `infixes` | Set selection precedence on the property that you want to extract. See [Code snippets and examples](#code-snippets-and-examples) for more information. Default is {}|
| Attribute: `required` |  Default is `true` |
| Syntax:  |`@TestProperty(prefix = "string", suffix = "string", infixes = "string", required = true)` |


## Code snippets and examples

### Extract credentials from the Galasa credentials store

You can extract credentials by using the `getUsernamePassword` method. The Core Manager uses the `getUsernamePassword` method to retrieve a user id and password from the credentials store to use in your test.

```java
import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
//...

@Test
//...
ICredentialsUsernamePassword credentials = coreManger.getUsernamePassword("SIMBANK");
credentials.getPassword();
credentials.getUsername();
```

You can edit the credentials.properties file in your .galasa folder. The following example shows the contents of the credentials.properties file that is set up as part of Galasa SIMBANK tutorials:

```properties
secure.credentials.SIMBANK.username=IBMUSER
secure.credentials.SIMBANK.password=SYS1
```


### Mask the value of an extracted password

To mask the password, for example to prevent it from being displayed in recorded screens, use the Core Manager `registerConfidentialText` method.

```java
coreManager.registerConfidentialText("SYS1", "IBMUSER password");
```


### Store a request in the test results archive

Use the following example code to understand how to archive messages in a particular folder and file structure in the RAS:

```java
Path requestPath = artifactRoot.resolve("communications").resolve("messages").;
Files.write(requestPath, content.getBytes(), new SetContentType(ResultArchiveStoreContentType.TEXT),
StandardOpenOption.CREATE);
```

Messages in the RAS are stored in the following structure:

```
communications
|
---messages 
```

If the folder or file do not exist, it is created by using the `resolve` method.


### Specify a folder in the RAS in which to store test result output

If you want to produce output to allow compares to be done when tests fail, you can elect a folder in which to store the output by using the _@StoredArtifactRoot_ annotation on an _IPath_ object. 

Use the following example to understand the code that needs to be added to the test class:

```java
@StoredArtifactRoot
    public Path rasRoot;
Path jobOutput = \
rasRoot.resolve("zosBatchJobs").resolve("checkOutputIsStoredInRAS");
Files.write(jobOutput, content.getBytes(), StandardOpenOption.CREATE);
```

The `resolve` method finds the directory (or creates one if needed) and a file is then created in the RAS by using the PATH API.


### Using the Test Property annotation

This example has the following CPS properties set in the CPS file: 

```properties
test.projectA.first.choice.data=3
test.projectA.first.data=2
```

where `projectA` is the prefix, `data` is the suffix, and `first` and `choice` are infixes.

The following code is used in the test:

```java
@TestProperty(prefix = "projectA", suffix = "data", infixes = {"first","choice"})
public String property;
```

In this example, if the property _test.projectA.first.choice.data_ is found in the CPS, then this is extracted for use in the test. If _test.projectA.first.choice.data_ is not found, then property _test.projectA.first.data_ is used instead.


### Example - using the Resource String and Logger annotation

The following example imports the @ResourceString annotation and sets the tag name to `myString` and the string length to `4`. The value of `myResourceString` is written to the log which is automatically stored in the Result Archive Store (RAS) by the Galasa framework. 

```java
@ResourceString(tag="myString", length = 4)
public IResourceString myResourceString;

    @Test
    	logger.info(myResourceString.getString());    
```
