---
title: "Writing Gherkin tests"
---

This guide explains how to write Gherkin tests for Galasa, covering the syntax, structure, and best practices.

## Feature files

A feature file describes the feature you are testing. Feature files use the `.feature` extension and contain one or more test scenarios.

**Basic structure:**
```gherkin
Feature: Feature name
  Scenario: Scenario name
    GIVEN some precondition
    WHEN some action occurs
    THEN some expected result
```

## A simple example

Here is a minimal Gherkin test:

```gherkin
Feature: FruitLogger
  Scenario: Log the cost of fruit
    THEN Write to log "An apple costs 1"
```

**Key elements:**

- **Feature**: Names the feature being tested. This name appears in test reports, so make it meaningful. Each file can only have one feature.

- **Scenario**: Names a specific test case, similar to a test method in Java. A feature can contain multiple scenarios.

- **Step**: An individual action or assertion. Steps start with keywords: `GIVEN`, `WHEN`, `THEN`, or `AND`.

## Comments and formatting

You can add comments and format your feature files for readability:

```gherkin
Feature: FruitLogger
  # This is a simple feature which logs text to the test log.
  Scenario: Log the cost of fruit
    # We can log our favourite fruit.
    THEN Write to log "An apple costs 1"
```

**Formatting rules:**

- Comments start with `#` and are ignored by the test runner
- Indentation is flexible and ignored during processing
- Use indentation to improve readability

## Multiple scenarios

You can include multiple scenarios in a single feature file:

```gherkin
Feature: FruitLogger
  Scenario: Log the cost of fruit-0
    THEN Write to log "An apple costs 1"
  
  Scenario: Log the cost of fruit-1
    THEN Write to log "A melon costs 2"
```

When executed, this creates one test (`FruitLogger`) with two test methods (`Log the cost of fruit-0` and `Log the cost of fruit-1`).

## Scenario outlines

Scenario outlines provide a template for scenarios that can be run multiple times with different data. This keeps your feature files concise and supports data-driven testing.

**Example:**

```gherkin
Feature: FruitLogger
  Scenario Outline: Log the cost of fruit
    THEN Write to log "A <fruit> costs <cost>"
  
  Examples:
    | fruit  | cost |
    | apple  | 1    |
    | melon  | 2    |
```

**How it works:**

1. The `Examples:` section defines a data table with column headers (variable names) and data rows
2. Variables in steps are surrounded by `<` and `>` brackets (for example, `<fruit>`)
3. At runtime, the scenario outline expands into multiple scenarios, one for each data row
4. Each generated scenario gets a suffix (`-0`, `-1`, `-2`) indicating which data row was used

This example produces the same result as the multiple scenarios example above, but is more maintainable when you have many data variations.

## Step keywords

Gherkin uses four step keywords:

- **GIVEN**: Sets up preconditions or initial state
- **WHEN**: Describes an action or event
- **THEN**: Specifies expected outcomes or assertions
- **AND**: Continues the previous step type for readability

**Example showing all keywords:**

```gherkin
Feature: Terminal Login
  Scenario: Successful login
    GIVEN a terminal
    AND the terminal is connected
    WHEN I type my username
    AND I type my password
    THEN I should see the main menu
    AND the session should be active
```

While Galasa processes all keywords the same way, using them appropriately makes your tests more readable and follows BDD conventions.

## Variables from configuration

You can retrieve values from the Configuration Property Store (CPS) and use them in your tests:

```gherkin
Feature: Application Test
  Scenario: Use configured values
    GIVEN <FruitName> is test property fruit.name
    THEN Write to log "Testing with <FruitName>"
```

**Syntax:** `GIVEN <VariableName> is test property namespace.property.name`

The `test` namespace prefix is automatically added, so you only need to specify the property name after `test.`.

## Complete example

Here is a more complete example showing multiple features working together:

```gherkin
Feature: Test 3270 interactions
  
  Scenario Outline: Run a named transaction and check the result
    GIVEN a terminal
    
    # Login to zOS
    THEN wait for "MYCLUSTER1 VAMP" in any terminal field
    AND type "LOGON APPLID(MYAPPLID1)" on terminal
    AND wait for terminal keyboard
    AND press terminal key ENTER
    THEN wait for "*****" in any terminal field
    AND press terminal key CLEAR
    AND wait for terminal keyboard

    # Run a transaction
    AND type "<transaction>" on terminal
    AND press terminal key ENTER
    THEN wait for "<expectedMessage>" in any terminal field    

    Examples:
      | transaction | expectedMessage  |
      | GRK1        | TEST MESSAGE     |
      | GRK2        | SECOND TEST      |  
      | GRK3        | THRICE THIS      |
```

This example demonstrates:

- Terminal allocation
- Waiting for specific text
- Typing commands
- Pressing special keys
- Using scenario outlines with data tables
- Comments for clarity

## Best practices

1. **Use meaningful names**: Feature and scenario names should clearly describe what is being tested

2. **Keep scenarios focused**: Each scenario should test one specific behavior

3. **Use scenario outlines for data variations**: When testing the same behavior with different data, use scenario outlines instead of duplicating scenarios

4. **Add comments for clarity**: Use comments to explain complex steps or business logic

5. **Organize logically**: Group related scenarios in the same feature file

6. **Follow BDD conventions**: Use `GIVEN` for setup, `WHEN` for actions, and `THEN` for assertions
