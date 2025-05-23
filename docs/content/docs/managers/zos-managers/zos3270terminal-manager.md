---
path: "/docs/managers/zos3270terminal-manager"
title: "Zos3270Terminal Manager"
---


This Manager is at Beta level. You can view the <a href="https://javadoc.galasa.dev/dev/galasa/zos3270/package-summary.html">Javadoc documentation for the Manager here</a>.<br>


[Overview](#overview)<br>
[Configuring](#configuring)<br>
[Provided annotation](#annotations)<br>
[Code snippets and examples](#codesnippets)<br>

# <a name="overview"></a>Overview

The zos3270Terminal Manager enables 3270 terminal interactions with back-end application programs and subsystems. 

Colour and highlight validation is supported. Use the <code>retrieveHighlightAtCursor</code> method to check that a field is highlighted as expected. Use the <code>retrieveColourAtCursor</code> method to check that the text colour in a specified field is as expected. You can check for the following colours: blue, red, pink, green, turquoise, yellow, neutral, and default. Use the <code>terminal.reportExtendedScreen</code> method to send colour output to the log. Support is also provided for diffent screen sizes. Screen sizes can be specified on the `@Zos3270Terminal` annotation.

The <code>ConfidentialTextFiltering</code> service enables confidential information such as passwords to be replaced with a numbered shield in these generated logs. 

Examples of using colour support and screen sizing are available in the [Code snippets and examples](#codesnippets) section.

When running a Galasa test with the Galasa CLI, terminal images are logged to the run log and PNG representations of the terminal screens can also be saved to the Result Archive Store (RAS) as the outputs are now controlled by the `zos3270.terminal.output` CPS property.

The zos3270Terminal Manager supports <a href="https://github.com/galasa-dev/cli/blob/main/gherkin-docs.md#3270-terminal-manipulation-steps">Gherkin keywords</a>. 

*Note:* The feature for saving PNG representations of the terminal screens to the RAS is available in the current release as experimental code only.



## <a name="dependencies"></a>Including the Manager in a test

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

# <a name="configuring"></a>Configuring 


The following properties are used to configure the Zos3270Terminal Manager:

## <a name="cps"></a>Configuration Properties

<details><summary>Apply Confidential Text Filtering to screen records</summary>

| Property: | ConfidentialTextFiltering CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.apply.ctf |
| Description: | Logs and screen recordings are passed through the Confidential Text Filtering services, to hide text, for example, passwords  |
| Required:  | No |
| Default value: | true |
| Valid values: | true, false |
| Examples: | <code>zos3270.apply.ctf=true<br></code> |

</details>


<details>
<summary>Extra bundles required to implement the CICS TS Manager</summary>

| Property: | ExtraBundles CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | cicsts.extra.bundles |
| Description: | The symbolic names of any bundles that need to be loaded with the CICS TS Manager  |
| Required:  | No |
| Default value: |  dev.galasa.cicsts.ceci.manager, dev.galasa.cicsts.ceda.manager, dev.galasa.cicsts.cemt.manager  |
| Valid values: | Bundle-symbolic names in a comma separated list  |
| Examples: | <code>cicsts.extra.bundles=org.example.cicsts.provisioning<br></code> |

</details>

<details><summary>Select the HTTP server to view live updates</summary>

| Property: | LiveTerminalUrl CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.live.terminal.images |
| Description: |  Set the URL to send live terminal updates for displaying in Eclipse. Eclipse sets this property in the overrides to indicate that the z/OS 3270 is to place the terminal images ready for live viewing in the Eclipse UI|
| Required:  | No |
| Default value: |  There is no default, an empty value means that no live recording is done |
| Valid values: | A valid URL |
| Examples: | <code>zos3270.console.terminal.images=example.url<br></code> |

</details>

<details>
<summary>Send terminal images to the console or run log</summary>

| Property: | LogConsoleTerminals CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.console.terminal.images|
| Description: | Enables terminal images to be logged to the console or run log |
| Required:  | No |
| Default value: |  true |
| Valid values: | true, false |
| Examples: | <code>zos3270.console.terminal.images=true<br></code> |

</details>

<details>
<summary>Add custom 3270 device types</summary>

| Property: | 3270DeviceTypes CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.image.xxxxxx.device.types |
| Description: | Allows for custom terminal device types |
| Required:  | No |
| Default value: | IBM-DYNAMIC, IBM-3278-2 |
| Valid values: | Valid 3270 device types in a comma separated list |
| Examples: | <code>zos3270.image.custom.device.types=IBM-DYNAMIC,IBM-3278-2<br></code> |

</details>

<details>
<summary>Select 3270 terminal outputs</summary>

| Property: | 3270TerminalOutput CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | zos3270.terminal.output |
| Description: | Experimental: Selects the representations of 3270 terminal screens to be saved to the RAS |
| Required:  | No |
| Default value: |  JSON  |
| Valid values: | JSON, PNG  |
| Examples: | <code>zos3270.terminal.output=json,png<br></code> |

</details>


# <a name="annotations"></a>Annotations provided by the Manager


The following annotations are provided by the Zos3270Terminal Manager:


<details>
<summary>z/OS 3270 Terminal</summary>

| Annotation: | z/OS 3270 Terminal |
| --------------------------------------- | :------------------------------------- |
| Name: | @Zos3270Terminal |
| Description: | The <code>@Zos3270Terminal</code> annotation requests the z/OS 3270 Terminal Manager to provide a 3270 terminal associated with a z/OS image. |
| Attribute: `imageTag` |  The <code>imageTag</code> is used to identify the z/OS image. Optional. The default value is "primary".|
| Attribute: `autoConnect` |  Allows a user to choose if the terminal automatically connects in the provision start stage. Optional. The default value is true.|
| Syntax: | @ZosImage(imageTag="A")<br> public IZosImage zosImageA;<br> @Zos3270Terminal(imageTag="A")<br> public ITerminal zosTerminalA;<br></code> |
| Notes: | The <code>ITerminal</code> interface has a number of methods to issue commands to the 3270 client. See <a href="https://javadoc.galasa.dev/dev/galasa/zos3270/ITerminal.html" target="_blank">ITerminal</a> to find out more. |

</details>

# <a name="codesnippets"></a>Code snippets and examples

<details><summary>Check that the correct screen is displayed and available</summary>

The following example checks that the logon screen is displayed and that the keyboard is available for input: 

```
terminal.waitForKeyboard().waitForTextInField("SIMPLATFORM LOGON SCREEN");
```

</details>

<details><summary>Log on to the system</summary>

The following example positions the cursor on the correct field and logs on to the system with User ID 'TESTER1' and password 'SYS1': 

```
terminal.positionCursorToFieldContaining("Userid").tab().type("TESTER1")
        .positionCursorToFieldContaining("Password").tab().type("SYS1").enter();
```

</details>

<details><summary>Select an application</summary>

The following example checks that the expected text "SIMBANK MAIN MENU" is displayed, positions the cursor to the correct field, and selects the "BANKTEST" application : 

```
terminal.waitForKeyboard().waitForTextInField("SIMBANK MAIN MENU").positionCursorToFieldContaining("===>")
        .tab().type("BANKTEST").enter();
```

</details>

<details><summary>Check that value of a field is displayed in the expected colour</summary>

The following example checks that the value in the customer number field is the colour turquoise: 

```
terminal.positionCursorToFieldContaining("CUSTOMER NUMBER").cursorRight();
assertThat(terminal.retrieveColourAtCursor()).isEqualTo(Colour.TURQUOISE);
```

</details>


<details><summary>Check that value in a specified screen position is in the expected colour</summary>

The following example checks that the text in a specified screen position is the colour blue: 

```
assertThat(terminal.retrieveColourAtPosition(5, 3)).isEqualTo(Colour.BLUE);
```

</details>

<details><summary>Customise screen size</summary>

You can define your terminal size in your test code by setting the primary rows and columns:

```
@Zos3270Terminal(primaryColumns = 80, primaryRows = 24)
        public ITerminal t2;
```

</details>

<details><summary>Customise logging</summary>

The following example sends all field attributes to the log: 

```
terminal.reportExtendedScreen(true, true, true, true, true, true, true);
```

where the attributes are printCursor, printColour, printHighlight, printIntensity, printProtected, printNumeric, and printModified.

</details>
