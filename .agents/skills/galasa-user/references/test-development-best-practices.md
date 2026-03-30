---
name: test-development-best-practices
description: Key principles and best practices for writing Galasa tests that scale and run in automation.
---

## Overview

These guidelines help you develop tests that can run in automation environments targeting multiple application instances simultaneously. Follow these principles even when starting with local, sequential testing to develop good habits for testing at scale.

## Design for Parallel Execution

### Expect tests to run multiple times in parallel

**Why it matters:**
- In CI/CD pipelines, automated tests often run against different target environments simultaneously
- Tests must not interfere with each other

**Best practices:**
- Ensure remote resource usage won't clash with other test instances
- Use Galasa framework to identify which test instance you're testing against
- Never hard code locations, ports, or names - these might change
- Don't assume a specific number of test environments - they may scale dynamically

## Avoid Hard-Coded Values

### Do not hard code resource names

**Why it matters:**
- Hard-coded values make tests non-portable
- Cannot run same test against development one day and QA the next
- Builds up technical debt in test code

**Best practices:**
- Use test properties to pass resource names to tests
- If no application Manager exists, use CPS properties for configuration
- Avoid hard-coding: application IDs, target hostnames, ports, credentials, file paths

**Example of what NOT to do:**
```java
// BAD - Hard-coded values
String hostname = "system.example.com";
int port = 992;
String username = "MYUSER";
```

**Example of what TO do:**
```java
// GOOD - Use injected configuration
@ZosImage(imageTag = "PRIMARY")
public IZosImage zosImage;

// Configuration comes from CPS properties
```

## Test Granularity

### Many short, sharp tests are better than few long tests

**Why it matters:**
- More parallelism = faster feedback in CI/CD
- Galasa ecosystem designed for scale - can handle thousands of parallel tests
- Single hour-long test with six methods can run in 10 minutes if split into six parallel tests

**Best practices:**
- Break long tests into smaller, focused tests
- Each test should validate one specific function
- Maximize parallel execution opportunities
- Reduce overall test suite execution time

## Leverage Managers

### Use Manager facilities instead of writing custom code

**Why it matters:**
- Manager code uses best practices and is battle-proven
- Automatic benefits when Managers improve
- Reduces technical debt in test code

**Best practices:**
- Check if a Manager exists for your needs before writing custom code
- Use Manager methods instead of reimplementing functionality
- Abstract common code into Application Managers as patterns emerge
- Expect some refactoring as test functionality settles

## Testing vs. Exercising

### Understand the difference between testing and exercising code

**Testing a function:**
- Examine all aspects: UI, API, logging, audit messages
- Verify all parts work correctly
- Comprehensive validation

**Exercising a function:**
- Use the function as part of testing another function
- Don't examine all parts - just accept it works or not
- Speeds up tests by reducing unnecessary validation

**Example:**
- When testing login functionality: verify all UI elements, error messages, audit logs
- When exercising login to test another feature: just login and proceed

## Lifecycle Management

### Use `@Before` and `@After` for state management

**Purpose:**
- `@Before`: Runs before each test method - reset resources before tests
- `@After`: Runs after each test method - cleanup resources after tests
- `@BeforeClass`: One-time setup before all tests in class
- `@AfterClass`: One-time cleanup after all tests in class

**Use cases:**
- Reset HTTP clients between tests
- Clear terminal screens
- Initialize test data
- Release resources

**Example:**
```java
@BeforeClass
public void setupOnce() throws Exception {
    // One-time setup for all tests
}

@Before
public void setupEachTest() throws Exception {
    // Reset state before each test
}

@Test
public void testSomething() throws Exception {
    // Test logic
}

@After
public void cleanupEachTest() throws Exception {
    // Cleanup after each test
}

@AfterClass
public void cleanupOnce() throws Exception {
    // One-time cleanup after all tests
}
```

## Test Organization

### Understand your meta-information

**Why it matters:**
- Meta-information builds test catalog
- Enables test selection and reporting
- Provides structure to test suite

**Best practices:**
- Use annotations to categorize tests
- Divide tests by: functional area, test type, priority, etc.
- Add structure early - harder to retrofit later
- Use `@Summary` annotation to describe test purpose

**Example:**
```java
@Summary("Verify customer account creation")
@Test
public class TestAccountCreation {
    // Test methods
}
```

### Scope tests appropriately

**Why it matters:**
- Well-scoped tests scale better
- Easier to maintain and debug
- Clearer test purpose and results

**Best practices:**
- Each test should validate a single application function
- Avoid testing multiple functions in one test class
- Keep test classes focused and manageable
- Reduce information overload

## Debugging and Diagnostics

### Make tests easy to debug

**Use logging effectively:**
```java
@Logger
public Log logger;

@Test
public void testSomething() throws Exception {
    logger.info("Starting test with customer ID: " + customerId);
    // Test logic
    logger.info("Retrieved account balance: " + balance);
}
```

**Best practices:**
- Log test state and pertinent variables
- Helps align run log with test class during analysis
- Be explicit about errors: "Expected message-A but saw message-B" not "Did not see expected result"

## Test Code Quality

### Your test code is a valuable asset

**Why it matters:**
- Test code is as important as application code
- Key indicator of application quality
- Poor test code jeopardizes quality assessment

**Best practices:**
- Source control manage all test code
- Use static code analyzers
- Perform buddy checks/code reviews
- Maintain high quality standards
- Follow coding standards and conventions
