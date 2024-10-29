<details><summary>Request a zOS UNIX Command instance</summary>

The following snippet shows the code that is required to request a zOS UNIX Command instance in a Galasa test:

```
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosUNIXCommand(imageTag="A")
public IZosUNIXCommand unixCommand;
```

The code creates a zOS UNIX Command instance associated with the zOS Image allocated in the *zosImageA* field.
</details>

<details><summary>Issue a zOS UNIX Command and retrieve response</summary>

Issue the zOS UNIX `date` Command and retrieve the response:

```
String unixCommandString = "date";
String unixResponse = unixCommand.issueCommand(unixCommandString);
```

The String `unixResponse`  contains the output of the UNIX TIME command, e.g. 

```
Wed Apr 1 12:01:00 BST 2020
```
</details>