
# gherkin support

[Gherkin](https://cucumber.io/docs/gherkin/reference/) is a language popularised by the [Cucumber](https://cucumber.io/) tooling which provides an abstract high-level syntax for expressing test case steps and assertions, in support of the goals of [Behaviour-Driven Testing (BDD)](https://en.wikipedia.org/wiki/Behavior-driven_development).

Galasa offers limited support of the Gherkin syntax, with some off-the-shelf step definitions which are already implemented in the standard Galasa managers.

## Feature files and naming
A feature file is a file which describes the feature you are trying to test.
Normally, the file will have an extension of `.feature`

## A simple Gherkin example
```
Feature: FruitLogger
  Scenario: Log the cost of fruit
    THEN Write to log "An apple costs 1"
```

Above, you can see 
- The feature being named. This name is used as the test name when reporting the completed test run status,
so making it meaningfull which describes the feature of code being tested is helpful. You can only have one feature per file.
- A single Scenario being named. A scenario is similar to a `method` in a Java testcase.
- A step which prints "An apple costs 1 euro." to the test log. There would normally be multiple steps in a scenario, but we are keeping it simple in this first example. The list of available steps will be detailed below.

## Comments and indenting
```
Feature: FruitLogger
  # This is a simple feature which logs text to the test log.
  Scenario: Log the cost of fruit
    # We can log our favourite fruit.
    THEN Write to log "An apple costs 1"
```

Comments start with a `#` character, and are ignored by the test runner.

Indenting can be whatever you find easiest to read. Indent characters are stripped out and ignored during the processing of the feature file.

## Multiple scenarios per feature
You can add multiple scenarios in a single feature file.
For example:
```
Feature: FruitLogger
  Scenario: Log the cost of fruit-0
    THEN Write to log "An apple costs 1"
  Scenario: Log the cost of fruit-1
    THEN Write to log "An melon costs 2"
```

When executed, this test will run one test, `FruitLogger` which has two 'methods' called `Log the cost of fruit-0` and `Log the cost of fruit-1`

## Scenario outlines
Scenario outlines are a construct which provides a 'template' for what a scenario should be, but allows a table-driven mechanism to keep the feature brief, and re-use the scenario step list multiple times.

For example:
```
Feature: FruitLogger
  Scenario Outline: Log the cost of fruit
    THEN Write to log "An <fruit> costs <cost>"
  Examples:
    | fruit  | cost |
    | apple  | 1    |
    | melon  | 2    |
```

This feature will produce exactly the same as the previous example, in that:
- A data table is added to the bottom of the `Scenario Outline:` section, which has a first line defining varible names, 
and following lines detailing what the values of each variable should be.
- Steps can contain variable usages, where the variable name matches a column in the data table below, and are surrounded by `<` and `>` brackets.
- At run-time, the scenario outline causes an 'expansion' of the scenario outline into a number of scenarios, each being fed a single row of the associated data table.
- The expanded-out scenario outlines have a suffix of '-0' '-1' '-2' to indicate that the scenario was created using line 0, 1 or 2 of the data table. So failures in one of the generated scenarios can be associated with the data which caused the failure.

## Available step definitions

### 3270 Terminal manipulation steps

#### To allocate a terminal for the scenario to use.
- `GIVEN a terminal`
- `GIVEN a terminal with id of xxx`
- `GIVEN a terminal tagged yyy`
- `GIVEN a terminal with id of xxx tagged yyy`

An id of xxx allows the terminal to be named, so they can be distinguished in cases where two or more terminals are used in the same test scenario.

By default, the terminal id is `A` and it is using tag `PRIMARY` though the above allow you to specify other settings.

You can also set the desired terminal size using the following CPS properties:
- `zos3270.gherkin.terminal.rows` defaults to 24 if missing.
- `zos3270.gherkin.terminal.columns` defaults to 80 if missing.

These values can then be overridden on an individual test basis as required.

Or burn the terminal size into your scenario, which will take precedence on any CPS or override property value.
- `GIVEN a terminal with 48 rows and 160 columns`
- `Given a terminal B with 24 rows and 160 columns`
- `Given a terminal C tagged ABCD with 24 rows and 80 columns`

Default terminal size is 24 rows x 80 columns if not specified anywhere else.

This `PRIMARY` image tag 

#### To simulate the pressing of special Program Function (PF) keys 
- `AND press terminal key PF1`
- `AND press terminal key PF2`
- ...
- `AND press terminal key PF24`

Sends the desired PF key to the terminal.

If you have multiple terminals, and have allocated them using `GIVEN a terminal with id of A` for example:
- `AND press terminal A key PF1`
  
#### To send special character keys
- `AND press terminal key TAB`
- `AND press terminal key BACKTAB`
- `AND press terminal key ENTER`
- `AND press terminal key CLEAR`

or to a specific terminal `A` : `AND press terminal A key ENTER`

#### Send text to a terminal
- `AND type "xxx" on terminal in field labelled "yyy"` sends text `xxx` to the terminal in field `yyy`.
or
- `AND type "xxx" on terminal A in field labelled "yyy"` if you want to name the terminal to send the text to.
- `AND type "xxx" on terminal`

#### Logging into a terminal application
- 'AND type credentials MYCREDS1 username on terminal`
- 'AND type credentials MYCREDS1 password on terminal`
or
- 'AND type credentials MYCREDS1 username on terminal A`
- 'AND type credentials MYCREDS1 password on terminal A`

... where `MYCREDS1` is a variable, matching the name of a credential in the system. 

For example, within a local `credentials.properties` file it may look like this:
```
secure.credentials.MYCREDS1.username=myuserid
secure.credentials.MYCREDS1.password=mypassw0rd
```

#### Position the terminal
- `AND move terminal cursor to field "xxx"`


#### Waiting for responses
- `AND wait for terminal keyboard`
- `AND wait for terminal A keyboard`
- `THEN wait for "xxx" in any terminal field`
- `THEN wait for "xxx" in any terminal A field`

#### Checking terminal output
- `THEN check "xxx" appears only once on terminal`
- `THEN check "xxx" appears only once on terminal A`

#### Gathering a variable from the configuration property store
- `GIVEN <V> is test property namespace.prefix.infix.suffix`
- `GIVEN <FruitName> is test property fruit.name` Note: No 'test' namespace is required in the Gherkin file. That namespace prefix is assumed.

### Writing to a test log
- `THEN Write to log "xxx"`

## Putting it all together

Suppose I have :
- A partition `MYAPPLIID1` on `MYCLUSTER1` where no login is required
- Three transactions I can invoke. `GRK1` `GRK2` and `GRK3` each of which produce different messages

Then I can write a feature with a scenario which navigates to the partition and invokes each transaction in turn, 
checking the expected text against the text we received back.

```
Feature: Test 3270 interactions
 
  Scenario Outline: Run a named transaction and check the result
    Given a terminal
    
    # Login to zOS
    Then wait for "MYCLUSTER1 VAMP" in any terminal field
    And type "LOGON APPLID(MYAPPLID1)" on terminal
    And wait for terminal keyboard
    And press terminal key ENTER
    Then wait for "*****" in any terminal field
    And press terminal key CLEAR
    And wait for terminal keyboard

    # Run a transaction
    And type "<transaction>" on terminal
    And press terminal key ENTER
    Then wait for "<expectedMessage>" in any terminal field    

    Examples:
    | transaction | expectedMessage  |
    | GRK1        | TEST MESSAGE     |
    | GRK2        | SECOND TEST      |  
    | GRK3        | THRICE THIS      |
```

# How to run a feature locally

### Set up your galasa environment: 
```
galasactl local init
```

### Edit your CPS properties
Set the following into your `~/.galasa/cps.properties file:
```
# CPS properties to enable gherkin tests.

# Gherkin uses the 'PRIMARY' tag by default.
# The .imageid property value indicates what zos.image.{value}.ipv4.hostname will be used for example.
zos.dse.tag.PRIMARY.imageid=MYHOST
# The PLEXNAME below needs to change to match the name of your zos cluster.
zos.dse.tag.PRIMARY.clusterid=PLEXNAME
# The following PLEXNAME should be changed to the same value as above.
# The following MYHOSTIMAGE should be changed to be the name of the machine you want to access on the cluster. eg: MV2XX
zos.cluster.PLEXNAME.images=MYHOSTIMAGE

# The following MYHOST part of the property key needs to change to match the value of the zos.dse.tag.PRIMARY.imageid property.
# The following machine.hostname needs to change to be the dotted ip name which is resolvable via DNS
zos.image.MYHOST.default.hostname=machine.hostname
zos.image.MYHOST.ipv4.hostname=machine.hostname
# The PLEXNAME below must be changed to match the value of zos.dse.tag.PRIMARY.clusterid
zos.image.MYHOST.sysplex=PLEXNAME
zos.image.MYHOST.telnet.port=23
zos.image.MYHOST.telnet.tls=false
```

### Set a test gherkin feature into a file. Say ~/test1.feature
```
Feature: GherkinLog
  Scenario Outline: Log Example Statement
    THEN Write to log "Hello World"
```

### Run the gherkin test locally
Some examples:
- `galasactl runs submit local --gherkin file:///test1.feature` - A basic run of a feature test.
- `galasactl runs submit local --gherkin file:///test1.feature  --log -` - Viewing run logs also.
- `galasactl runs submit local --gherkin file:///test1.feature --overridefile my.override.properties --reportyaml results.yaml --log -` - Overriding some CPS properties and getting a test results summary.

## How to run the feature remotely


## Formal syntax supported
A [Bachus-Naur form](https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form) of the grammar supported by the Galasa tool is here:
```
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

