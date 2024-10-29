<details><summary>Request a zOS File Handler instance</summary>

The following snippet shows the code that is required to request a zOS File Handler instance in a Galasa test:

```
@ZosFileHandler
public IZosFileHandler zosFileHandler;
```
</details>

<details><summary>Read the content of an existing sequential data set</summary>

Create a new *IZosDataset* object representing an existing sequential data set. If the data set exists, retrieve the content in text mode:

```
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.EXISTING.DATASET.SEQ", zosImage);
if (dataSet.exists()) {
    String content = dataSet.retrieveAsText();
    ...
}
```
</details>


<details><summary>Read the content of an existing partitioned data set member</summary>

Create a new *IZosDataset* object representing an existing partitioned data set (PDS). If the PDS exists, check if the member exists and retrieve it's content in text mode:

```
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.EXISTING.DATASET.SEQ, zosImage);
    String memberName = "MEMBER1";
    if (dataSet.exists() && dataSet.memberExists(memberName)) {
        String content = dataSet.memberRetrieveAsText(memberName);
        ...
    }
```
</details>


<details><summary>Create a new sequential data set</summary>

Create a new *IZosDataset* object representing a new sequential data set. If the data set does not exist, allocate the data set with attributes to the equivalent of the following JCL:

```
//NEWDS    DD DSN=GALASA.NEW.DATASET.SEQ,DISP=(NEW,CATLG),
//            DSORG=PS,RECFM=FB,LRECL=80,BLKSIZE=32720,
//            UNIT=SYSDA,SPACE=(TRK,(1,1))
```
Finally, content is written to the data set in text mode:


```
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.NEW.DATASET.SEQ", zosImage);
    if (!dataSet.exists()) {
        dataSet.setDatasetOrganization(DatasetOrganization.SEQUENTIAL);
        dataSet.setRecordFormat(RecordFormat.FIXED_BLOCKED);
        dataSet.setRecordlength(80);
        dataSet.setBlockSize(32720);
        dataSet.setUnit("SYSDA");
        dataSet.setSpace(SpaceUnit.TRACKS, 1, 1);
        dataSet.create();
    }
    List<String> records = new ArrayList<>();
    records.add("RECORD 1");
    records.add("RECORD 2");
    records.add("RECORD 3");
    dataSet.storeText(String.join("\n", records));
```
</details>

<details><summary>Create a new partitioned data set member</summary>

Create a new *IZosDataset* object representing a new partitioned data (PDS) set member. If the data set does not exist, allocate the PDS with attributes to the equivalent of the following JCL:

```
//NEWPDS   DD DSN=GALASA.NEW.DATASET.PDS,DISP=(NEW,CATLG),
//            DSORG=PS,RECFM=FB,LRECL=80,BLKSIZE=32720,
//            UNIT=SYSDA,SPACE=(TRK,(1,1,15))
```
Finally, content is written to a member in the PDS in text mode:


```
@ZosImage(imageTag="A")
public IZosImage zosImage;

@ZosFileHandler
public IZosFileHandler zosFileHandler;
...
IZosDataset dataSet = zosFileHandler.newDataset("GALASA.NEW.DATASET.PDS", zosImage);
if (!dataSet.exists()) {
    dataSet.setDatasetOrganization(DatasetOrganization.SEQUENTIAL);
    dataSet.setRecordFormat(RecordFormat.FIXED_BLOCKED);
    dataSet.setRecordlength(80);
    dataSet.setBlockSize(32720);
    dataSet.setUnit("SYSDA");
    dataSet.setSpace(SpaceUnit.TRACKS, 1, 1);
    dataSet.setDirectoryBlocks(15);
    dataSet.create();
}
String memberName = "MEMBER1";
List<String> records = new ArrayList<>();
    records.add("RECORD 1");
    records.add("RECORD 2");
    records.add("RECORD 3");
    dataSet.memberStoreText(memberName, String.join("\n", records));
}
```
To create a PDS/E, i.e. the JCL equivalent of

```
DSNTYPE=LIBRARY
```
use

```
dataSet.setDatasetType(DSType.LIBRARY);
```
instead of setting the number of directory blocks.
</details>

<details><summary>Create a new VSAM KSDS</summary>

Create a new *IZosVSAMDataset* object representing a new VSAM KSDS data set. If the data set is allocated with a minimum set of attributes:

```
IZosVSAMDataset vsamDataSet = zosFileHandler.newVSAMDataset("ROBERTD.GALASA.TEST.DS.ANOTHER.KSDS", zosImage);
vsamDataSet.setSpace(VSAMSpaceUnit.CYLINDERS, 1, 1);
vsamDataSet.setRecordSize(50, 101);
vsamDataSet.create();
```
</details>

<details><summary>Read the contents of a zOS UNIX File</summary>

Create a new *IZosDataset* object representing a UNIX file. If the file exists, retrieve the content in text mode:

```
IZosUNIXFile unixFile = zosFileHandler.newUNIXFile("/tmp/Galasa/existingFile", zosImage);
if (unixFile.exists()) {
    unixFile.setDataType(UNIXFileDataType.TEXT);
    String content = unixFile.retrieveAsText();
}
```

</details>

<details><summary>Read the contents of a zOS UNIX File</summary>

Create a new *IZosDataset* object representing a new UNIX file. If UNIX file does not exist, create it. Write to the file in binary mode:

```
IZosUNIXFile unixFile = zosFileHandler.newUNIXFile("/tmp/Galasa/newFile", zosImage);
if (!unixFile.exists()) {
    unixFile.create();
}
List<String> properties = new ArrayList<>();
properties.add("dev.galasa.property1=value1");
properties.add("dev.galasa.property2=value2");
properties.add("dev.galasa.property3=value3");
unixFile.setDataType(UNIXFileDataType.BINARY);
unixFile.storeBinary(String.join("\n", properties).getBytes());
```

</details>

<details><summary>List the contents of a zOS UNIX Directory</summary>

Create a new *IZosDataset* object representing a new UNIX directory. If UNIX directory exists, list its contents:

```
IZosUNIXFile unixDirectory = zosFileHandler.newUNIXFile("/tmp/Galasa/", zosImage);
if (unixDirectory.exists())
{
    Map<String, String> dir = unixDirectory.directoryListRecursive();
    for (Map.Entry<String, String> entry : dir.entrySet()) {
        logger.info(String.format("%2$-9s: %1$s", entry.getKey(), entry.getValue()));
   }
}
```

Example output:

```
directory: /tmp/Galasa/dira
file     : /tmp/Galasa/dira/file1
file     : /tmp/Galasa/dira/file2
file     : /tmp/Galasa/existingFile
file     : /tmp/Galasa/newFile
```

</details>