---
title: "Zos3270Terminal Manager"
---

This Manager is at Beta level. You can view the [Javadoc documentation for the Manager](https://javadoc.galasa.dev/dev/galasa/zos3270/package-summary.html){target="_blank"}.


## Overview

The zos3270Terminal Manager enables 3270 terminal interactions with back-end application programs and subsystems. 

Colour and highlight validation is supported. Use the `retrieveHighlightAtCursor` method to check that a field is highlighted as expected. Use the `retrieveColourAtCursor` method to check that the text colour in a specified field is as expected. You can check for the following colours: blue, red, pink, green, turquoise, yellow, neutral, and default. Use the `terminal.reportExtendedScreen` method to send colour output to the log. Support is also provided for diffent screen sizes. Screen sizes can be specified on the `@Zos3270Terminal` annotation.

The `ConfidentialTextFiltering` service enables confidential information such as passwords to be replaced with a numbered shield in these generated logs. 

Examples of using colour support and screen sizing are available in the [Code snippets and examples](#code-snippets-and-examples) section.

When running a Galasa test with the Galasa CLI, terminal images are logged to the run log and PNG representations of the terminal screens can also be saved to the Result Archive Store (RAS) as the outputs are now controlled by the `zos3270.terminal.output` CPS property.

The zos3270Terminal Manager supports [Gherkin keywords](https://github.com/galasa-dev/cli/blob/main/gherkin-docs.md#3270-terminal-manipulation-steps){target="_blank"}. 

*Note:* The feature for saving PNG representations of the terminal screens to the RAS is available in the current release as experimental code only.


## Including the Manager in a test

To use the Zos3270Terminal Manager in a test you must import the _@Zos3270Terminal_ annotation into the test, as shown in the following example: 


```
@Zos3270Terminal(imageTag = "PRIMARY")
public ITerminal terminal;
```


To use the colour and highlight features in a test, import the following components into the test:

```
import dev.galasa.zos3270.spi.Colour;
import dev.galasa.zos3270.spi.Highlight;
```

You also need to add the Manager dependency into the pom.xml file if you are using Maven, or into the build.gradle file if you are using Gradle. 

If you are using Maven, add the following dependencies into the _pom.xml_ in the _dependencies_ section:

```
<dependency>
<groupId>dev.galasa</groupId>
<artifactId>dev.galasa.zos3270.manager</artifactId>
</dependency>
```

If you are using Gradle, add the following dependencies into ```build.gradle``` in the _dependencies_ closure:

```
dependencies {
compileOnly 'dev.galasa:dev.galasa.zos3270.manager'
}
```


## Configuration Properties

The following properties are used to configure the Zos3270Terminal Manager:


### Apply Confidential Text Filtering to screen records

| Property: | ConfidentialTextFiltering CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.apply.ctf |
| Description: | Logs and screen recordings are passed through the Confidential Text Filtering services, to hide text, for example, passwords  |
| Required:  | No |
| Default value: | true |
| Valid values: | true, false |
| Examples: | `zos3270.apply.ctf=true` |


### Extra bundles required to implement the CICS TS Manager

| Property: | ExtraBundles CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | cicsts.extra.bundles |
| Description: | The symbolic names of any bundles that need to be loaded with the CICS TS Manager  |
| Required:  | No |
| Default value: |  dev.galasa.cicsts.ceci.manager, dev.galasa.cicsts.ceda.manager, dev.galasa.cicsts.cemt.manager  |
| Valid values: | Bundle-symbolic names in a comma separated list  |
| Examples: | `cicsts.extra.bundles=org.example.cicsts.provisioning` |


### Select the HTTP server to view live updates

| Property: | LiveTerminalUrl CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.live.terminal.images |
| Description: |  Set the URL to send live terminal updates for displaying in Eclipse. Eclipse sets this property in the overrides to indicate that the z/OS 3270 is to place the terminal images ready for live viewing in the Eclipse UI|
| Required:  | No |
| Default value: |  There is no default, an empty value means that no live recording is done |
| Valid values: | A valid URL |
| Examples: | `zos3270.console.terminal.images=example.url` |


### Send terminal images to the console or run log

| Property: | LogConsoleTerminals CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.console.terminal.images|
| Description: | Enables terminal images to be logged to the console or run log |
| Required:  | No |
| Default value: |  true |
| Valid values: | true, false |
| Examples: | `zos3270.console.terminal.images=true` |


### Add custom 3270 device types

| Property: | 3270DeviceTypes CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.image.IMAGEID.device.types |
| Description: | Allows for custom terminal device types |
| Required:  | No |
| Default value: | IBM-DYNAMIC, IBM-3278-2 |
| Valid values: | Valid 3270 device types in a comma separated list |
| Examples: | `zos3270.image.IMAGE_A.device.types=IBM-DYNAMIC,IBM-3278-2` |


### Add a custom 3270 device name

| Property: | 3270DeviceName CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.image.IMAGEID.device.name |
| Description: | Allows for a custom 3270 device name to be requested when connecting to a server |
| Required:  | No |
| Examples: | `zos3270.image.IMAGE_A.device.name=IYCQTC57` |


### Select 3270 terminal outputs

| Property: | 3270TerminalOutput CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.terminal.output |
| Description: | Experimental: Selects the representations of 3270 terminal screens to be saved to the RAS |
| Required:  | No |
| Default value: |  JSON  |
| Valid values: | JSON, PNG  |
| Examples: | `zos3270.terminal.output=json,png` |


## Annotations provided by the Manager

The following annotations are provided by the Zos3270Terminal Manager:


### z/OS 3270 Terminal

| Annotation: | z/OS 3270 Terminal |
| --------------------------------------- | :------------------------------------- |
| Name: | @Zos3270Terminal |
| Description: | The `@Zos3270Terminal` annotation requests the z/OS 3270 Terminal Manager to provide a 3270 terminal associated with a z/OS image. |
| Attribute: `imageTag` |  The `imageTag` is used to identify the z/OS image. Optional. The default value is "primary".|
| Attribute: `autoConnect` |  Allows a user to choose if the terminal automatically connects in the provision start stage. Optional. The default value is true.|
| Syntax: | <pre lang="java">@ZosImage(imageTag="A")<br>public IZosImage zosImageA;<br><br>@Zos3270Terminal(imageTag="A")<br>public ITerminal zosTerminalA;<br></pre> |
| Notes: | The `ITerminal` interface has a number of methods to issue commands to the 3270 client. See [ITerminal](https://javadoc.galasa.dev/dev/galasa/zos3270/ITerminal.html){target="_blank"} to find out more. |


## Code snippets and examples

### Check that the correct screen is displayed and available

The following example checks that the logon screen is displayed and that the keyboard is available for input: 

```java
terminal.waitForKeyboard().waitForTextInField("SIMPLATFORM LOGON SCREEN");
```


### Log on to the system

The following example positions the cursor on the correct field and logs on to the system with User ID 'TESTER1' and password 'SYS1': 

```java
terminal.positionCursorToFieldContaining("Userid").tab().type("TESTER1")
        .positionCursorToFieldContaining("Password").tab().type("SYS1").enter();
```


### Select an application

The following example checks that the expected text "SIMBANK MAIN MENU" is displayed, positions the cursor to the correct field, and selects the "BANKTEST" application : 

```java
terminal.waitForKeyboard().waitForTextInField("SIMBANK MAIN MENU").positionCursorToFieldContaining("===>")
        .tab().type("BANKTEST").enter();
```


### Check that value of a field is displayed in the expected colour

The following example checks that the value in the customer number field is the colour turquoise: 

```java
terminal.positionCursorToFieldContaining("CUSTOMER NUMBER").cursorRight();
assertThat(terminal.retrieveColourAtCursor()).isEqualTo(Colour.TURQUOISE);
```


### Check that value in a specified screen position is in the expected colour

The following example checks that the text in a specified screen position is the colour blue: 

```java
assertThat(terminal.retrieveColourAtPosition(5, 3)).isEqualTo(Colour.BLUE);
```


### Customise screen size

You can define your terminal size in your test code by setting the primary rows and columns:

```java
@Zos3270Terminal(primaryColumns = 80, primaryRows = 24)
        public ITerminal t2;
```


### Customise logging

The following example sends all field attributes to the log: 

```java
terminal.reportExtendedScreen(true, true, true, true, true, true, true);
```

where the attributes are printCursor, printColour, printHighlight, printIntensity, printProtected, printNumeric, and printModified.

