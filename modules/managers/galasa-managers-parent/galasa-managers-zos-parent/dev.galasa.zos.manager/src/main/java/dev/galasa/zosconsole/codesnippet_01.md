<details><summary>Request a zOS Console instance</summary>

The following snippet shows the code that is required to request a zOS Console instance in a Galasa test:

```
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosBatch(imageTag="A")
public IZosConsole zosConsole;
```

The code creates a zOS Console instance associated with the zOS Image allocated in the *zosImageA* field.
</details>

<details><summary>Issue a zOS Console command and retrieve the immediate response</summary>

Issue a zOS Console command and retrieve the immediate console command response:

```
String command = "D A,L";
IZosConsoleCommand consoleCommand = zosConsole.issueCommand(command);
String immediateResponse = consoleCommand.getResponse();

```
</details>


<details><summary>Issue a zOS Console command and retrieve the delayed response</summary>

Issue a zOS Console command and retrieve the delayed console command response:

```
String command = "D A,L";
IZosConsoleCommand consoleCommand = zosConsole.issueCommand(command);
String delayedResponse = consoleCommand.requestResponse();

```
</details>