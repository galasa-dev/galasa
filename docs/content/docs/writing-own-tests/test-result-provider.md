---
title: "Controlling code execution based on test results so far"
---

As a tester, you may want the ability to control code execution in a Galasa test based on its progress so far, for example, to run additional diagnostics or begin clean up steps if a failure occurs.

The `ITestResultProvider` can be used to give a test access to:

- The **overall test class result** so far (e.g., Passed or Failed).
- The **result of individual methods** that have already run.
- The **exception** thrown by any failed method.

This lets you adapt the behaviour of non-test code depending on the state of your test run.

## How it works

- The `@TestResultProvider` annotation injects an `ITestResultProvider` into your test class.
- After each `@BeforeClass`, `@Before`, `@Test`, `@After`, and `@AfterClass` method, the Core Manager updates this provider with:
  - The current **test class result**.
  - An updated **list of method results**.

To use this capability, include the lines of code below in your test:

```java
@TestResultProvider
public ITestResultProvider testResultProvider;
```

## Using the overall test class result

In this example, an `@AfterClass` method uses the `testResultProvider` to check whether the test class has failed. If it failed, it runs the method `myCustomCleanupMethod` that gathers extra diagnostic data and cleans up resources.

```java
@AfterClass
public void afterClassMethod() throws FrameworkException {
    if (testResultProvider.getResult().isFailed()) {
        myCustomCleanupMethod();
    }
}

private void myCustomCleanupMethod() {
    try {
        // Custom diagnostic collection and cleanup logic for failures.
    } catch (Exception ex) {
        logger.error("Error while cleaning up in myCustomCleanupMethod()");
        // Ignore the problem.
    }
}
```

## Using the result of the last executed test method

When a test class is annotated with `@ContinueOnTestFailure`, it keeps running even after a test method fails.

The example below retrieves the most recently executed test method result from the `testResultProvider` in an `@After` method.

If that method failed with a `FrameworkException`, it runs the method `myCustomCleanupMethod`.

```java
@After
public void afterMethod() throws FrameworkException {
    List<ITestMethodResult> methodResults = testResultProvider.getTestMethodResults();
    ITestMethodResult lastMethodRan = methodResults.get(methodResults.size() - 1);

    if (lastMethodRan.isFailed() && lastMethodRan.getFailureReason() instanceof FrameworkException) {
        myCustomCleanupMethod();
    }
}

private void myCustomCleanupMethod() {
    try {
        // Custom diagnostic collection and cleanup logic for failures.
    } catch (Exception ex) {
        logger.error("Error while cleaning up in startServerDump()");
        // Ignore the problem.
    }
}
```

## Summary

The `ITestResultProvider` is useful when you need to:
* Make decisions based on whether the whole Galasa test has failed.
* Respond to failures in specific test methods.
* Gather diagnostics only when needed, without impacting passing runs.