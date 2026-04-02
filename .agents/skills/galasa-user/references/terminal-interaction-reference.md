---
name: terminal-interaction-reference
description: Detailed reference for 3270 terminal interaction timing, field navigation, and data extraction. Load only when working with terminal screens.
---

## When to Use This Reference

Load this file only when:
- Working with 3270 terminal screens (z/OS or CICS)
- Debugging terminal timing issues
- Implementing complex field navigation
- User mentions screens, fields, or terminal interaction problems

For basic terminal patterns, see the quick reference card.

## Understanding Keyboard Lock States

3270 terminals have two keyboard states:
- **Locked**: Keyboard is locked while the terminal processes a command or waits for the host to respond
- **Unlocked**: Keyboard is ready to accept input

**Key actions that lock the keyboard:**
- `enter()` - Submits data to the host
- `clear()` - Clears the screen
- `pf3()`, `pf5()`, `pf10()`, etc. - Function key presses
- Any action that sends data to the host

**Critical Rule:** Always call `waitForKeyboard()` after any action that locks the keyboard.

## Method Chaining Pattern

Always chain terminal method calls rather than invoking them on separate lines:

```java
// BAD - Don't do this
terminal.type("Hello, World!");
terminal.enter();
terminal.waitForKeyboard();

// GOOD - Chain the calls
terminal.type("Hello, World!").enter().waitForKeyboard();
```

## Timing Best Practices

### 1. Wait for Keyboard After Commands

Always use `waitForKeyboard()` after commands that lock the keyboard:

```java
// Clearing the screen
terminal.clear().waitForKeyboard();

// Submitting data
terminal.type("1").enter().waitForKeyboard();

// Pressing function keys
terminal.pf3().waitForKeyboard();
```

### 2. Wait for Screen Content to Load

`waitForKeyboard()` ensures the keyboard is unlocked, but the screen content may still be loading. Use `waitForTextInField()` to ensure specific content has appeared:

```java
// Navigate to a screen and wait for it to fully load
terminal.type("MENU")
    .enter()
    .waitForKeyboard()
    .waitForTextInField("Select an option");  // Wait for menu to appear
```

### 3. Common Timing Mistakes to Avoid

- ❌ Not waiting for keyboard after `enter()`, `clear()`, or PF keys
- ❌ Proceeding to next action before screen content loads
- ❌ Not verifying expected text appears before interacting with fields

```java
// WRONG - May fail if screen hasn't loaded
terminal.type("1").enter().waitForKeyboard();
terminal.type("1234").enter().waitForKeyboard();  // May type into wrong field!

// CORRECT - Verify screen loaded before proceeding
terminal.type("1").enter().waitForKeyboard()
    .waitForTextInField("CUSTOMER NUMBER");  // Ensure we're on the right screen
terminal.type("1234").enter().waitForKeyboard();
```

## Field Navigation and Interaction

### Moving Between Fields

```java
// Tab to next field
terminal.tab();

// Back tab to previous field
terminal.backTab();

// Position cursor at a specific field by its label
terminal.positionCursorToFieldContaining("Customer Number");
```

### Typical Field Interaction Pattern

```java
// Navigate to field, enter data, and submit
terminal.positionCursorToFieldContaining("Customer Number")
    .type("1234")
    .enter()
    .waitForKeyboard()
    .waitForTextInField("Customer found OK");
```

## Extracting Data from Screens

### CRITICAL: Capitalization Matters

When a user provides expected values or field names in quotes (like "Customer Number", "Sort Code", or "Mr Galasa Tester"), you **MUST** adhere to the exact capitalization of letters in the quotes. Incorrect capitalization will cause assertions to fail or field lookups to return incorrect results.

Examples:
- If the user says the field is called "Customer Number", use `"Customer Number"` (not `"customer number"` or `"CUSTOMER NUMBER"`)
- If the user says the expected value is "Sort Code", use `"Sort Code"` (not `"sort code"` or `"SORT CODE"`)
- If the user says the name should be "Mr Galasa Tester", use `"Mr Galasa Tester"` (not `"mr galasa tester"`)

### 1. Retrieving Labeled Field Values

Use `retrieveFieldTextAfterFieldWithString()` to get the value of a labeled field:

```java
// Get the value after a label (works even if on different lines)
String id = terminal.retrieveFieldTextAfterFieldWithString("ID");
assertThat(id.trim()).as("ID should match").isEqualTo("987654");

String name = terminal.retrieveFieldTextAfterFieldWithString("Customer Name");
assertThat(name.trim()).as("Name should match").isEqualTo("Galasa Tester");
```

**Important:** Always use `.trim()` when comparing field values, as they may contain leading/trailing whitespace.

### 2. Checking for Messages or Unlabeled Text

Use `retrieveScreen()` to get the entire screen content as a string:

```java
// Check for success/error messages
String screen = terminal.retrieveScreen();
assertThat(screen).as("Success message should appear").contains("Customer found OK");

// Check for error messages
assertThat(screen).as("Should not show error").doesNotContain("ERROR");
```

### 3. Verifying Screen State

Before interacting with a screen, verify you're on the expected screen:

```java
// Verify we're on the customer display screen
terminal.waitForTextInField("Display Customer");

// Or check for specific prompt text
terminal.waitForTextInField("Provide a Customer number");
```

## Complete Interaction Example

Here's a complete example showing proper timing and verification:

```java
@Test
public void testCustomerLookup() throws Exception {
    // Navigate to customer display screen
    terminal.type("LOOKUP CUSTOMER")                              // Run a 'LOOKUP CUSTOMER' command
        .enter()                                    // Submit
        .waitForKeyboard()                          // Wait for keyboard to unlock
        .waitForTextInField("CUSTOMER NUMBER");     // Verify screen loaded
    
    // Enter customer number
    terminal.type("1234")                           // Type customer number
        .enter()                                    // Submit
        .waitForKeyboard()                          // Wait for keyboard to unlock
        .waitForTextInField("Customer found OK");  // Verify success
    
    // Extract and verify field values
    String sortCode = terminal.retrieveFieldTextAfterFieldWithString("Sort code");
    assertThat(sortCode.trim()).as("Sort code should match").isEqualTo("112233");
    
    String customerName = terminal.retrieveFieldTextAfterFieldWithString("Customer name");
    assertThat(customerName.trim()).as("Customer name should match").isEqualTo("Mr Michael Z Barker");
}
```

## Error Detection

Error messages vary by application. When checking for errors:

1. Ask the user what specific error text to expect
2. Use `retrieveScreen()` to get full screen content
3. Check for the specific error text in assertions

```java
// Check for specific error message
String screen = terminal.retrieveScreen();
assertThat(screen).as("Should show invalid customer error")
    .contains("Customer not found");
```

## Terminal Size Configuration

Default terminal size is 24 rows × 80 columns. To use a different size:

```java
@Zos3270Terminal(imageTag = "PRIMARY", primaryColumns = 100, primaryRows = 30)
public ITerminal largeTerminal;
```

The framework handles different screen sizes automatically, so you typically don't need to worry about this unless your application requires a specific size.

## Common Function Keys

- `pf3()` - Exit/Return to previous screen or main menu
- `pf5()` - Delete (when viewing a customer or account)
- `pf10()` - Update (when viewing a customer or account)
- `pf12()` - Cancel current operation

Always use `terminal.pf3()` (not `terminal.type("F3")`) to press function keys.

## Troubleshooting Terminal Issues

### Screen Not Loading
- Ensure `waitForKeyboard()` is called after every keyboard-locking action
- Add `waitForTextInField()` to verify screen content has appeared
- Check that expected text matches exact capitalization

### Wrong Field Being Updated
- Verify screen loaded with `waitForTextInField()` before typing
- Use `positionCursorToFieldContaining()` to explicitly position cursor
- Check field labels match exact capitalization

### Timing Failures
- Increase wait time by chaining multiple `waitForKeyboard()` calls if needed
- Use `waitForTextInField()` instead of fixed delays
- Verify keyboard state before each interaction

### Assertion Failures
- Always use `.trim()` when comparing field values
- Match exact capitalization of expected values
- Use `retrieveScreen()` to debug what's actually on screen