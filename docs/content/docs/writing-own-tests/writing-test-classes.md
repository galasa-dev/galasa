---
title: "Writing test classes"
---

Having created your project structure and built the parent and sub-projects, you are ready to start writing your own test classes.


## The anatomy of a Galasa test class

Some classes have `internal` as part of their package name. Such classes are not intended to be used in test code, and if you try doing so then at run-time you are likely to experience `Class not found` errors. This might be especially pertinent if an IDE, for example Eclipse, shows such classes in auto-complete drop-downs.

All Galasa tests must have a `@Test` annotation (`dev.galasa.Test`) defined and in place for it to be recognised by Galasa. Doing this allows the Galasa Maven plugin to differentiate between test and utility classes within a project. Only classes that use `@Test` are included in the test catalog.

Once loaded by the Galasa test runner, there are a few further annotations that dictate how a test is executed. These are `@BeforeClass`, `@Before`, `@Test`, `@After` and `@AfterClass`. These are visually similar to how they appear in JUnit, but are handled differently in Galasa.

### Galasa test method annotations

**`@Test`**
<br>
This annotation identifies a method as one that contains test code. Such methods are executed by Galasa by the order in which they appear in the test class - from top to bottom. If a test method fails, the following test methods are bypassed to encourage short, sharp parallel testing. This behaviour can be overridden using the `@ContinueOnTestFailure` annotation on the `class` statement.

When a test method succeeds it is marked as PASSED, and if there is an exception it is marked FAILED. Managers can override this marking - for example, if a test throws a specific exception, a Manager could set the result to DISASTER.

**`@BeforeClass` and `@AfterClass`**
<br>
Methods marked `@Beforeclass` and `@AfterClass` are executed once before and once after all tests in the class respectively. They are executed in the order they appear in the sources.

Such methods are used to perform setup and tear-down for the test class, for example, setting up an HTTP server that all of the `@Test` methods will use.

Unlike JUnit, these are not static methods as they need access to some _class fields_/_manager annotated fields_ which are described later.

Failures that occur in `@BeforeClass` and `@AfterClass` usually cause tests to fail with `ENVIRONMENT_FAILURE`, which indicates that the test itself didn't fail, but the set up did for some reason. For this reason, `@BeforeClass` and `@AfterClass` should not contain any test code.

**`@Before`** and **`@After`**
<br>
Methods marked `@Before` and `@After` are executed before and after every `@Test` method respectively, in the order in which they appear in the source.

These methods are typically used to reset resources before a test or perhaps check logs for post-test error messages.

Failures that occur in `@Before` and `@After` result in the test method being marked as FAILED, as it is likely that such methods contain test checking code.

**Manager annotated fields**
<br>
Before any of the `@` methods in a test class are executed, the Galasa test runner requests any active Managers to populate their _test fields_ with appropriate values. These fields are highly-dependent on the Managers, and you will need to review the Manager reference documentation to appreciate what is available.

As an example, the test field

```java
@RunName
public String runName;
```

is provided by the Core Manager. This populates the `runName` field with a String representing the run's name, which can be used to create a unique resource name that won't clash with other runs.

The framework ensures that all correctly coded fields are filled before executing any test code, so test code doesn't need to bother itself with validating the fields themselves. If a field cannot be populated by a Manager, the test is marked either as `ENVIRONMENT_FAILURE`, or as `RESOURCE_EXHAUSTION` in which case an automated rerun is queued.

A few Managers provide direct access to themselves, for example the Core Manager:

```java
@CoreManager
public ICoreManager coreManager;
```

The Core Manager provides a method to obtain credentials via an ID - but the test may not know the ID before it runs, so it would be difficult to do this via an annotated field.

## Example tests

Review the SimBank sample tests to see how these principles are applied in practice. With one or two exceptions, these sample tests will be kept up to date with the latest specifications.

## Pitfalls and tips when writing test code

### Do not use <code>System.out</code> or <code>System.err</code>

Although using `System.out.println` may be satisfactory when running tests locally, these messages will be lost if running in automation. Use a logger instead. The Core Manager provides an annotated field to make this straightforward:

```java
@Logger
public Log Logger;
```

and then:

```java
logger.info("This is a message");
```

Any message written using this method is saved in the run log.

### Keep all test resources within the test project

When diagnosing or maintaining a test project, it is helpful if all the resources it consumes are within the same project - examples include JCL, or Db2 DDL. While it is possible to keep resources with the target environment, it makes portability an issue. If your test runs against, say, a remote server MVS1, and MVS1 contains some resources such as JCL, it is fine if you remain on MVS1 only. If you later decide to run the test on MVS2, you will need to copy all the resources across before you can run your test. A better solution is to keep the resources within the current test project and deploy them as part of the test run.

You can access resources in a test project using the Artifact Manager:

```java
@BundleResources
public IBundleResources bundleResources;

InputStream resource = bundleResources.retrieveFile("job.jcl");
```

### Discovering Manager resources

The best way to learn what fields, annotations and methods that a Manager provides is to use the Manager reference pages on this site. Auto-completion and Javadoc are also great resources when you know what you are looking for. The Manager reference pages contain code snippets and describe what fields and annotations apply to each Manager. The source code for the Managers can be found in the Github Managers repository - where you can also usually find an Installation Verification Test (VT) project for each Manager.

All Managers should comply with a naming standard, although it is not currently enforced. The Java bundle name tends to be `dev.galasa.managerid.manager` where `managerid` is the name of the Manager - for example `dev.galasa.core.manager`. Inside the bundle is the TPI (Tester Programming Interface) package, which tends to be named `dev.galasa.managerid` and includes all the annotations and interfaces a tester can employ to use the Manager. A test should never use any resource in a package with `internal` or `spi` in its name. By using these same naming conventions, you can use autocompletion and/or javadoc to locate Manager functionality across the project.

### JVM lifecycles

Galasa tests run in Java and use a JVM to execute. JVMs only exist for the life of the test - this is to avoid problems with memory leaks and resource locking between test runs. Because of this, a test can start threads, lock resources and so on without worrying too much about cleaning up.
