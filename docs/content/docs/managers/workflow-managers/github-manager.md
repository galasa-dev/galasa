---
title: "GitHub Manager"
---

You can view the <a href="">[Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/githubissue/package-summary.html){target="_blank"}.


## Overview

If there is an open issue in a GitHub repository that tracks a known problem which is expected to cause a test class or test method to fail, you can add the _@GitHubIssue_ annotation to a test class or test method so that the expected failure is recorded as _Failed with Defects_  in the test report. 

Using this annotation on a test class or test method means that the expected failure is clearly identifiable in the test report, making it easier to spot any unexpected test failures and saving time by avoiding the investigation of a known issue. 


## Including the Manager in a test

To use the GitHub Manager in a test you must import the _@GitHubIssue_ annotation into the test, as shown in the following example: 

```java
import dev.galasa.githubissue.GitHubIssue;
//...
```

You also need to add the Manager dependency into the pom.xml file if you are using Maven, or into the build.gradle file if you are using Gradle. 

If you are using Maven, add the following dependencies into the _pom.xml_ in the _dependencies_ section:

```xml
<dependency>
    <groupId>dev.galasa</groupId>
    <artifactId>dev.galasa.githubissue.manager</artifactId>
</dependency>
```

If you are using Gradle, add the following dependencies into ```build.gradle``` in the _dependencies_ closure:

```groovy
dependencies {
    compileOnly 'dev.galasa:dev.galasa.githubissue.manager'
}
```

Finally, use the annotation that is provided by the Manager in the test. The annotation calls the Manager at runtime. In this example, the provided annotation is _@GitHubIssue_. 


## Using the Manager

The GitHub Manager is activated at runtime if there is a _@GitHubIssue_ annotation on the test class or any of its test methods. The presence of the _@GitHubIssue_ annotation means that there is a known problem which is expected to affect the result of the test. 

If the issue is expected to affect one or more specific test methods, add the annotation on the test method. If the scope of the issue is larger and might affect the whole test class, add the annotation at the class level.

Use the following examples, which are based on _BatchAccountsOpenTest_ to understand how to add the _@GitHubIssue_ annotation for GitHub issue number _1178_ to a test method and to a test class.


## Examples

GitHub issue _1178_ has a status of _open_ on GitHub in the _galasa-dev/projectmanagement_ repository. Issue _1178_ tracks a known problem with the behaviour of the 
Z/OS Batch Manager. The issue causes the test class _BatchAccountsOpenTest_ to fail.

The following example adds the _@GitHubIssue_ annotation on the test method:

```java
@Test
@GitHubIssue(issue = "1178", repo = "galasa-dev/projectmanagement")
public void batchOpenAccountsTest() throws TestBundleResourceException, IOException, ZosBatchException {
    // Create a list of accounts to create
    List<String> accountList = new LinkedList<>();
    accountList.add("901000001,20-40-60,1000");

    //...
```

The following example adds the _@GitHubIssue_ annotation on the test class:

```java
@Test
@GitHubIssue(issue = "1178", repo = "galasa-dev/projectmanagement")
public class BatchAccountsOpenTest {
    //...
```

Once the GitHub issue is closed, remove the _@GitHubIssue_ annotation from the test class or test method.


## Using regex

To ensure that the failing exception is due to the known problem that is identified in the GitHub issue, use regex in the annotation to check that the recorded test failure matches the string that is specified in regex. 

For example, GitHub issue _1179_ describes a problem in source code that results in a _HttpServerErrorException_ message being returned when a test is run. You can add the following annotation to your test to ensure that it is failing because of this exception, and not for a different reason:

```java
@GitHubIssue(issue = "1179", repo = "galasa-dev/projectmanagement",
    regex = "HttpServerErrorException")
```

If the failing exception does not match the regex, the test has failed for a different or unknown reason. If no regex is provided in the annotation, any failing exception is accepted.


## Setting overrides

You can override the result of a test method or a test class by using the _@GitHubIssue_ annotation in conjunction with the _@ContinueOnTestFailure_ annotation to return a test result for all test methods within a test class.


### Overriding a test method result

If a test method is annotated with _@GitHubIssue_, and the initial result of the method is _Failed_, the GitHub Manager uses the GitHub API to retrieve the current state of the GitHub issue from the annotation. If the issue is in _open_ status, the result of the test method is overridden from _Failed_ to _Failed With Defects_. If the issue is in _closed_ status, the result is not overridden, as that issue is supposedly fixed and is not expected to affect the test method anymore. In the latter scenario, further investigation into the cause of the failure is required.


### Overriding a test class result

At the end of a test class run, the results of each individual test method are combined to determine whether the result of the test class should be overridden. 

Use the following scenarios and examples to understand the expected test class result and what to do next. In each scenario, the issue is in an _open_ state.


### Scenario 1

Scenario 1 uses the _@GitHubIssue_ annotation on the test method _testNotNull_ with the _@ContinueOnTestFailure_ annotation:

```java
@Test
@ContinueOnTestFailure
public class SimBankIVT {

    @GitHubIssue(issue = "1178")
    @Test
    public void testNotNull() {
        // Failing test
    }

    @Test
    public void checkBankIsAvailable() {
        // Failing test
    }
}
```

Results:

- Test method `testNotNull()` result: ![](failed-with-defects.svg){ valign=middle } _Failed with defects_
- Test method `checkBankIsAvailable()` result: ![](failed.svg){ valign=middle } _Failed_
- Test class `SimBankIVT` result: ![](failed.svg){ valign=middle } **Failed**

Next steps:

Test method _testNotNull_ returned a result of _Failed With Defects_. Test method _checkBankIsAvailable_ returned a result of _Failed_ for an unknown reason. Further investigation is required to understand why test method _checkBankIsAvailable_ failed.


### Scenario 2: 

Scenario 2 uses the _@GitHubIssue_ annotation on the test method _testNotNull_ with the _@ContinueOnTestFailure_ annotation:

```java
@Test
@ContinueOnTestFailure
public class SimBankIVT {

    @GitHubIssue(issue = "1178")
    @Test
    public void testNotNull() {
        // Failing test
    }

    @Test
    public void checkBankIsAvailable() {
        // Passing test
    }
}
```

Results:

- Test method `testNotNull()` result: ![](failed-with-defects.svg){ valign=middle } _Failed with defects_
- Test method `checkBankIsAvailable()` result: ![](passed.svg){ valign=middle } _Passed_
- Test class `SimBankIVT` result: ![](passed-with-defects.svg){ valign=middle } **Passed with defects**

Next steps:

The first method, _testNotNull_, returned a result of _Failed With Defects_.  As the _ContinueOnTestFailure_ annotation is used, the test continued to run and the following method _checkBankIsAvailable_ returned a result of _Passed_. All methods that are expected to pass returned a result of _Passed_, so the overall result of the test is _Passed With Defects_. No further investigation is required.


### Scenario 3: 

Scenario 3 uses the _@GitHubIssue_ annotation on the test method _checkBankIsAvailable_ without the _@ContinueOnTestFailure_ annotation:

```java
@Test
public class SimBankIVT {

    @Test
    public void testNotNull() {
        // Passing test
    }

    @Test
    @GitHubIssue(issue = "1178")
    public void checkBankIsAvailable() {
        // Failing test
    }
}
```

Results:

- Test method `testNotNull()` result: ![](passed.svg){ valign=middle } _Passed_
- Test method `checkBankIsAvailable()` result: ![](failed-with-defects.svg){ valign=middle } _Failed with defects_
- Test class `SimBankIVT` result: ![](passed-with-defects.svg){ valign=middle } **Passed with defects**

Next steps:

The method _checkBankIsAvailable_ returned a result of _Failed With Defects_. As the test did not use the _ContinueOnTestFailure_ annotation, the test would have ended prematurely. However, as the _checkBankIsAvailable_ method was the last test method, the results of all test methods in the class are known. All methods that are expected to pass returned a result of _Passed_, so the overall result of the test is _Passed With Defects_. No further investigation required.


### Scenario 4:

Scenario 4 uses the _@GitHubIssue_ annotation on the test method _testNotNull_ without the _@ContinueOnTestFailure_ annotation:

```java
@Test
public class SimBankIVT {

    @Test
    @GitHubIssue(issue = "1178")
    public void testNotNull() {
        // Failing test
    }

    @Test
    public void checkBankIsAvailable() {
        // ...
    }
}
```

Results:

- Test method `testNotNull()` result: ![](failed-with-defects.svg){ valign=middle } _Failed with defects_
- Test method `checkBankIsAvailable()` result: _Unknown_
- Test class `SimBankIVT` result: ![](failed-with-defects.svg){ valign=middle } **Failed with defects**

Next steps:

The _testNotNull_ method returned a status of _Failed With Defects_. As the _ContinueOnTestFailure_ annotation is not used, the results of the following methods – in this example _checkBankIsAvailable_ – are not known. The overall result returned is _Failed With Defects_. To check the result of all methods, add the _@ContinueOnTestFailure_ annotation for future test runs.


## Configuration Properties

You must configure your CPS properties so that the GitHub Manager knows the GitHub instance in which to look for the specified issue. The GitHub Manager converts the GitHub instance URL that is provided in the CPS to the correct URL for the GitHub API, removing the need to manually supply a URL for the GitHub API. Alternatively, you can set the GitHub repository in the _@GitHubIssue_ annotation. The value in the annotation takes precedence over the value in the CPS.

The following are properties used to configure the GitHub Manager:


### GitHub Instance URL CPS Property

| Property: | GitHub Instance URL CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | githubissue.instance.url |
| Description: | The GitHub instance for the issue  |
| Required:  | No |
| Default value: | https://github.com |
| Valid values: | A valid GitHub instance, for example, _https://github.acompany.com_  |
| Examples: | `githubissue.instance.DEFAULT.url=https://github.com` |


### GitHub Instance Credentials CPS Property

| Property: | GitHub Instance Credentials CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | githubissue.instance.credentials |
| Description: | The credentials ID to use for the GitHub instance  |
| Required:  | No |
| Default value: | _GITHUB_ |
| Valid values: | _GITHUB_, _DEFAULT_, _IBM_  |
| Examples: | `githubissue.instance.DEFAULT.credentials=GITHUB` |


### GitHub Instance Repository CPS Property

| Property: | GitHub Instance Repository CPS Property |
| ------------- | :------------------------------------- |
| Name: | githubissue.instance.repository |
| Description: | The GitHub repository that contains the GitHub issue |
| Required:  | No, can be provided in the annotation |
| Default value: | N/A |
| Valid values: | A valid repository, for example, _galasa-dev/projectmanagement_ |
| Examples: | `githubissue.instance.DEFAULT.repository=galasa-dev/projectmanagement` |

Note: You must provide credentials for your GitHub Enterprise instances in the credentials store, so that the request to the GitHub API is authenticated.

For example, if you set the _GitHub Instance Credentials CPS Property_ to:

```properties
githubissue.instance.url=https://github.com
```

then you need to edit the _credentials.properties_ file in the _.galasa folder_ to set the username and password for the GitHub instance. For example: 

```properties
secure.credentials.GITHUB.username=COMPANYUSER
secure.credentials.GITHUB.password=Password123
```


## Annotations provided by the Manager

The following annotation is provided by the GitHub Manager:


### GitHub Issue

| Name: | GitHub Issue |
| --------------------------------------- | :------------------------------------- |
| Name: | @GitHubIssue |
| Description: | If present on a method or class, this annotation will tell the GitHub Manager to influence the result of the method or class based on the known problem in the GitHub issue. |
| Syntax:  | @GitHubIssue( githubId = "DEFAULT", issue = "1178", repo = "galasa-dev/projectmanagement")|
| Attribute `issue`: | The number of the issue. Required.|
| Attribute `githubId`: | Optional. Default value is "DEFAULT".|
| Attribute `repo`: | Optional. If not present in the annotation, the CPS is checked. If a value is not present in the CPS, the Manager logs a warning message.|
| Attribute `regex`: | Optional. Use to narrow down the failing exception.|

The annotation can be used at the class or method level.

Use of the _@ContinueOnTestFailure_ annotation alongside the _@GitHubIssue_ annotation can affect the test result. For examples, see the [Overrides](#setting-overrides) section.


## Code snippets and examples

### Add the @GitHubIssue and @ContinueOnTestFailure annotation on a test class

Use the following code to add GitHub issue _1178_ in the _galasa-dev/projectmanagement_ repository on the SimBank _ProvisionedAccountCreditTest_ test class. Ensure all test methods in the test class are run by using the _@ContinueOnTestFailure_ annotation:

```java
@Test
@GitHubIssue( issue = "1178", repo = "galasa-dev/projectmanagement" )
@ContinueOnTestFailure
public class ProvisionedAccountCreditTest {
    //...
```


### Add the @GitHubIssue annotation on a test method

Use the following code to add GitHub issue _1178_ in the _galasa-dev/projectmanagement_ repository on the _updateAccountWebServiceTest_ method in the _ProvisionedAccountCreditTest_ test class:

```java
@Test
@GitHubIssue( issue = "1178", repo = "galasa-dev/projectmanagement" )
public void updateAccountWebServiceTest() throws Exception {
    // Obtain the initial balance
    BigDecimal userBalance = account.getBalance();

    //...
```

