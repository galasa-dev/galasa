<details><summary>Request a zOS TSO Command instance</summary>

The following snippet shows the code that is required to request a zOS TSO Command instance in a Galasa test:

```
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosTSOCommand(imageTag="A")
public IZosTSOCommand tsoCommand;
```

The code creates a zOS TSO Command instance associated with the zOS Image allocated in the *zosImageA* field.
</details>

<details><summary>Issue a zOS TSO Command and retrieve the immediate response</summary>

Issue the zOS TSO `TIME` Command and retrieve the response:

```
String tsoCommandString = "TIME";
String tsoResponse = tsoCommand.issueCommand(tsoCommandString);
```

The String `tsoResponse`  contains the output of the TSO TIME command, e.g. 

```
IKJ56650I TIME-12:01:00 PM. CPU-00:00:00 SERVICE-290 SESSION-00:00:00 APRIL 1,2020
```
</details>