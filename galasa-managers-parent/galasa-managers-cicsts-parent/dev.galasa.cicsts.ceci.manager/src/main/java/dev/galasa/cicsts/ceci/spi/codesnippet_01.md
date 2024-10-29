<details><summary>Request a CECI instance</summary>

The following snippet shows the code that is required to request a CECI instance in a Galasa test:

```
@ZosImage(imageTag = "A")
public IZosImage image;
    
@CicsRegion()
public ICicsRegion cicsRegion;

@CicsTerminal()
public ICicsTerminal cicsTerminal;

...

ICeci ceci = cicsRegion.ceci();
```

The code creates a CICS/TS CECI instance. The CECI instance will also require a 3270 terminal instance. 
The 3270 terminal is associated with the zOS Image allocated in the *zosImageA* field.


</details>

<details><summary>Issue a basic CECI command</summary>

The following snippet shows the code required to issue the a basic CECI command. In this case, the test will write a message to the operator console:

```
String ceciCommand = "EXEC CICS WRITE OPERATOR TEXT('About to execute Galasa Test...')";
ICeciIResponse resp = cicsRegion.ceci().issueCommand(cicsTerminal, ceciCommand);
if (!resp.isNormal() {
    ...
}
```
</details>


<details><summary>Link to program with container</summary>

Create a CONTAINER on a CHANNEL, EXEC CICS LINK to a PROGRAM with the CHANNEL and get the returned CONTAINER data.

Create the input CONATINER called "MY-CONTAINER-IN" on CHANNEL "MY-CHANNEL" with the data "My_Contaier_Data". The CONTAINER will default to TEXT with no code page conversion:

```
ICeciIResponse resp = cicsRegion.ceci().putContainer(ceciTerminal, "MY-CHANNEL", "MY-CONTAINER-IN", "My_Contaier_Data", null, null, null);
if (!resp.isNormal()) {
    ...
}
```
Link to PROGRAM "MYPROG" with the CHANNEL "MY-CHANNEL":

```
eib = cicsRegion.ceci().linkProgramWithChannel(ceciTerminal, "MYPROG", "MY-CHANNEL", null, null, false);
if (!resp.isNormal()) {
    ...
}
```
Get the content of the CONTAINER "MY-CONTAINER-OUT" from CHANNEL "MY-CHANNEL" into the CECI variable "&DATAOUT" and retrieve the variable data into a String:

```
eib = cicsRegion.ceci().getContainer(ceciTerminal, "MY-CHANNEL", "MY-CONTAINER-OUT", "&DATAOUT", null, null);
if (!resp.isNormal()) {
    ...
}
String dataOut = cicsRegion.ceci().retrieveVariableText(ceciTerminal, "&DATAOUT");
```
</details>

<details><summary>Write binary data to a temporary storage queue</summary>

Use the following code to write binary data to TS QUEUE 

Create a binary CECI variable:

```
char[] data = {0x0C7, 0x081, 0x093, 0x081, 0x0A2, 0x081, 0x040, 0x0C4, 0x081, 0x0A3, 0x081};
cicsRegion.ceci().defineVariableBinary(ceciTerminal, "&BINDATA", data);
```
Write the binary variable to a TS QUEUE called "MYQUEUE": 

```
String command = "WRITEQ TS QUEUE('MYQUEUE') FROM(&BINDATA)";
ICeciIResponse resp = cicsRegion.ceci().issueCommand(ceciTerminal, command);
if (!resp.isNormal()) {
    ...
}

```

The "MYQUEUE" now contains the following data:

```
Galasa Data
```
</details>

<details><summary>Confirm the signed on userid</summary> 

Use the following code to issue the CICS ASSIGN API and retrieve the signed on userid from the response: 


```
String command = "ASSIGN";
ICeciIResponse resp = cicsRegion.ceci().issueCommand(ceciTerminal, command);
String userid = resp.getResponseOutputValues().get("USERID").getTextValue();

```

Alternatively, issue ASSIGN and assign the userid value to a variable:

```
String command = "ASSIGN USERID(&USERID)";
ICeciIResponse resp = cicsRegion.ceci().issueCommand(ceciTerminal, command);
String userid = cicsRegion.ceci().retrieveVariableText("&USERID");

```
</details>