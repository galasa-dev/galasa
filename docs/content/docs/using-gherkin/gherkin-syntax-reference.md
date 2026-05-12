---
title: "Gherkin syntax reference"
---

This page provides the formal grammar specification for Gherkin syntax supported by Galasa.

## Formal grammar

The following Backus-Naur Form (BNF) defines the grammar supported by the Galasa Gherkin parser:

```bnf
<feature> ::= 'Feature:' <scenarioPartList> END_OF_FILE

<scenarioPartList> ::= null
                    | <scenarioPart> <scenarioPartList>

<scenarioPart> ::= <scenarioOutline>
                | <scenario>

<scenario> ::= 'Scenario:' <stepList>

<scenarioOutline> ::= 'Scenario Outline:' <stepList> 'Examples:' <dataTable>

<stepList> ::= null
            | <step> <stepList>

<dataTable> ::= <dataHeaderLine> <dataValuesLineList>

<dataTableHeader> ::= <dataLine>

<dataTableValuesLineList> ::= null
                            | <dataLine> <dataTableValuesLineList>

<step> ::= <stepKeyword> <text>

<stepKeyword> ::= "GIVEN" | "THEN" | "WHEN" | "AND"
```

## Grammar explanation

### Feature

A feature file must start with the `Feature:` keyword followed by one or more scenarios or scenario outlines.

**Syntax:**
```
Feature: <feature name>
  <scenarios>
```

### Scenario

A scenario represents a single test case and contains a list of steps.

**Syntax:**
```
Scenario: <scenario name>
  <steps>
```

### Scenario Outline

A scenario outline is a template for scenarios that can be executed multiple times with different data from an examples table.

**Syntax:**
```
Scenario Outline: <scenario name>
  <steps with variables>
  Examples:
    | <column headers> |
    | <data row 1>     |
    | <data row 2>     |
```

### Steps

Steps are individual actions or assertions within a scenario. Each step starts with a keyword.

**Syntax:**
```
<keyword> <step text>
```

**Keywords:**

- `GIVEN` - Preconditions or setup
- `WHEN` - Actions or events
- `THEN` - Expected outcomes or assertions
- `AND` - Continuation of the previous step type

### Data Tables

Data tables in scenario outlines provide test data for parameterized scenarios.

**Syntax:**
```
Examples:
  | column1 | column2 | column3 |
  | value1  | value2  | value3  |
  | value4  | value5  | value6  |
```

**Rules:**

- First row contains column headers (variable names)
- Subsequent rows contain data values
- Columns are separated by `|` characters
- Variable names in steps are surrounded by `<` and `>`

## Syntax rules

### Case sensitivity

- Keywords (`Feature:`, `Scenario:`, `Scenario Outline:`, `Examples:`, `GIVEN`, `WHEN`, `THEN`, `AND`) are case-sensitive
- Step text is case-sensitive
- Variable names in scenario outlines are case-sensitive

### Whitespace and indentation

- Indentation is optional and ignored by the parser
- Use indentation for readability
- Leading and trailing whitespace in step text is trimmed

### Comments

- Comments start with `#`
- Comments can appear on their own line or at the end of a line
- Comments are ignored by the parser

**Example:**
```gherkin
# This is a comment
Feature: Example  # This is also a comment
  Scenario: Test
    GIVEN a terminal  # Comment after a step
```

### Line endings

- Both Unix (`\n`) and Windows (`\r\n`) line endings are supported
- Line endings are normalized during parsing

## Limitations

Galasa's Gherkin implementation has the following limitations compared to full Cucumber:

1. **No Background sections:** Background steps that run before each scenario are not supported

2. **No Tags:** Scenario and feature tags (like `@smoke`, `@regression`) are not supported

3. **No Doc Strings:** Multi-line string arguments (using `"""`) are not supported

4. **No Data Tables in steps:** Data tables as step arguments are not supported

5. **Limited step definitions:** Only pre-implemented step definitions from Galasa Managers are available. Custom step definition implementation is not supported

6. **No Hooks:** Before and After hooks are not supported

7. **No Step Arguments:** Steps cannot have additional arguments beyond the step text

## Valid examples

### Minimal feature

```gherkin
Feature: Minimal
  Scenario: Simple
    THEN Write to log "Hello"
```

### Feature with multiple scenarios

```gherkin
Feature: Multiple Scenarios
  Scenario: First
    THEN Write to log "First"
  
  Scenario: Second
    THEN Write to log "Second"
```

### Feature with scenario outline

```gherkin
Feature: Data Driven
  Scenario Outline: Test with data
    THEN Write to log "<message>"
  
  Examples:
    | message |
    | First   |
    | Second  |
```

### Feature with comments

```gherkin
# This feature tests logging
Feature: Logging Test
  # This scenario logs a message
  Scenario: Log message
    # Write to the log
    THEN Write to log "Test"
```

### Complex feature

```gherkin
Feature: Terminal Test
  # Test basic terminal operations
  Scenario: Allocate and use terminal
    GIVEN a terminal
    AND wait for terminal keyboard
    THEN Write to log "Terminal ready"
  
  # Test with different terminal sizes
  Scenario Outline: Terminal with size
    GIVEN a terminal with <rows> rows and <columns> columns
    THEN Write to log "Terminal size: <rows>x<columns>"
  
  Examples:
    | rows | columns |
    | 24   | 80      |
    | 48   | 160     |
```

## Invalid examples

### Missing Feature keyword

```gherkin
# INVALID: No Feature keyword
Scenario: Test
  THEN Write to log "Test"
```

### Scenario Outline without Examples

```gherkin
# INVALID: Scenario Outline requires Examples
Feature: Invalid
  Scenario Outline: Test
    THEN Write to log "<message>"
```

### Invalid step keyword

```gherkin
# INVALID: 'BUT' is not a supported keyword
Feature: Invalid
  Scenario: Test
    BUT Write to log "Test"
```
