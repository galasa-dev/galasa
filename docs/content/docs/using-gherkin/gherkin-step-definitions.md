---
title: "Available step definitions"
---

This reference lists all pre-implemented step definitions available in Galasa for writing Gherkin tests.

## General steps

### Writing to the test log

Write messages to the test log for debugging or documentation purposes:

```gherkin
THEN Write to log "Your message here"
```

**Example:**
```gherkin
THEN Write to log "Starting test execution"
```

## 3270 Terminal steps

These steps allow you to interact with 3270 terminal applications.

### Allocating terminals

Allocate a terminal for your scenario to use:

```gherkin
GIVEN a terminal
GIVEN a terminal with id of xxx
GIVEN a terminal tagged yyy
GIVEN a terminal with id of xxx tagged yyy
```

**Parameters:**

- **id**: A name to identify the terminal when using multiple terminals in one scenario. Default is `A`.
- **tag**: Specifies which system to connect to. Default is `PRIMARY`.

**Examples:**
```gherkin
GIVEN a terminal
GIVEN a terminal with id of B
GIVEN a terminal tagged SECONDARY
GIVEN a terminal with id of C tagged BACKUP
```

### Specifying terminal size

You can specify the terminal dimensions in your scenario:

```gherkin
GIVEN a terminal with 48 rows and 160 columns
GIVEN a terminal B with 24 rows and 160 columns
GIVEN a terminal C tagged ABCD with 24 rows and 80 columns
```

**Default size:** 24 rows × 80 columns

**Configuration:** You can also set terminal size using CPS properties:

- `zos3270.gherkin.terminal.rows` (default: 24)
- `zos3270.gherkin.terminal.columns` (default: 80)

Priority order: Scenario specification > Override properties > CPS properties > Default values

### Pressing special keys

Send Program Function (PF) keys to the terminal:

```gherkin
AND press terminal key PF1
AND press terminal key PF2
...
AND press terminal key PF24
```

**For named terminals:**
```gherkin
AND press terminal A key PF1
```

### Pressing special character keys

Send special character keys to the terminal:

```gherkin
AND press terminal key TAB
AND press terminal key BACKTAB
AND press terminal key ENTER
AND press terminal key CLEAR
```

**For named terminals:**
```gherkin
AND press terminal A key ENTER
```

### Typing text

Send text to the terminal:

```gherkin
AND type "xxx" on terminal
AND type "xxx" on terminal in field labelled "yyy"
AND type "xxx" on terminal A in field labelled "yyy"
```

**Parameters:**

- **xxx**: The text to type
- **yyy**: The label of the field to type into
- **A**: The terminal ID (if using multiple terminals)

**Examples:**
```gherkin
AND type "LOGON" on terminal
AND type "myusername" on terminal in field labelled "User ID"
AND type "CICS" on terminal B in field labelled "Application"
```

### Using credentials

Type credentials from the credentials store:

```gherkin
AND type credentials MYCREDS1 username on terminal
AND type credentials MYCREDS1 password on terminal
AND type credentials MYCREDS1 username on terminal A
AND type credentials MYCREDS1 password on terminal A
```

**Parameters:**

- **MYCREDS1**: The credential name from your credentials store

**Credentials configuration example** (`credentials.properties`):
```properties
secure.credentials.MYCREDS1.username=myuserid
secure.credentials.MYCREDS1.password=mypassw0rd
```

**Example scenario:**
```gherkin
Scenario: Login with stored credentials
  GIVEN a terminal
  THEN wait for "Login" in any terminal field
  AND type credentials MAINFRAME username on terminal
  AND press terminal key TAB
  AND type credentials MAINFRAME password on terminal
  AND press terminal key ENTER
```

### Positioning the cursor

Move the terminal cursor to a specific field:

```gherkin
AND move terminal cursor to field "xxx"
```

**Example:**
```gherkin
AND move terminal cursor to field "Command"
```

### Waiting for responses

Wait for the terminal to be ready or for specific text to appear:

```gherkin
AND wait for terminal keyboard
AND wait for terminal A keyboard
THEN wait for "xxx" in any terminal field
THEN wait for "xxx" in any terminal A field
```

**Examples:**
```gherkin
AND wait for terminal keyboard
THEN wait for "Ready" in any terminal field
THEN wait for "CICS" in any terminal B field
```

### Checking terminal output

Verify that specific text appears on the terminal:

```gherkin
THEN check "xxx" appears only once on terminal
THEN check "xxx" appears only once on terminal A
```

**Examples:**
```gherkin
THEN check "Transaction complete" appears only once on terminal
THEN check "Error" appears only once on terminal B
```

## Configuration property steps

### Retrieving test properties

Get values from the Configuration Property Store (CPS):

```gherkin
GIVEN <VariableName> is test property namespace.property.name
```

**Note:** The `test` namespace prefix is automatically added, so you only specify the property name.

**Examples:**
```gherkin
GIVEN <FruitName> is test property fruit.name
GIVEN <ServerPort> is test property server.port
GIVEN <Timeout> is test property connection.timeout
```

**Using the variable:**
```gherkin
Feature: Configuration Test
  Scenario: Use configured fruit name
    GIVEN <FruitName> is test property fruit.name
    THEN Write to log "Testing with <FruitName>"
```

## Complete terminal interaction example

Here is a complete example showing multiple step definitions working together:

```gherkin
Feature: CICS Transaction Test
  
  Scenario: Execute CICS transaction
    # Allocate and configure terminal
    GIVEN a terminal with 24 rows and 80 columns
    
    # Login to system
    THEN wait for "VTAM" in any terminal field
    AND type "LOGON APPLID(CICSRGN1)" on terminal
    AND press terminal key ENTER
    AND wait for terminal keyboard
    
    # Enter credentials
    THEN wait for "User ID" in any terminal field
    AND type credentials CICS username on terminal
    AND press terminal key TAB
    AND type credentials CICS password on terminal
    AND press terminal key ENTER
    
    # Wait for CICS ready
    THEN wait for "CICS" in any terminal field
    AND press terminal key CLEAR
    AND wait for terminal keyboard
    
    # Execute transaction
    AND type "MYTR" on terminal
    AND press terminal key ENTER
    
    # Verify result
    THEN wait for "Transaction completed" in any terminal field
    THEN check "Success" appears only once on terminal
```

## Step definition syntax patterns

When writing steps, follow these patterns:

**Terminal allocation:**

- `GIVEN a terminal [with id of <id>] [tagged <tag>] [with <rows> rows and <columns> columns]`

**Typing:**

- `AND type "<text>" on terminal [<id>] [in field labelled "<label>"]`
- `AND type credentials <credname> <username|password> on terminal [<id>]`

**Key presses:**

- `AND press terminal [<id>] key <keyname>`

**Waiting:**

- `AND wait for terminal [<id>] keyboard`
- `THEN wait for "<text>" in any terminal [<id>] field`

**Checking:**

- `THEN check "<text>" appears only once on terminal [<id>]`

**Configuration:**

- `GIVEN <<variable>> is test property <property.name>`

**Logging:**

- `THEN Write to log "<message>"`
