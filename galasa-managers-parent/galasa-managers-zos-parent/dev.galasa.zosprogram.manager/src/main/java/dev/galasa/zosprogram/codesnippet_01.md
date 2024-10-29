<details><summary>Compile and Link a COBOL program</summary>

The following snippet shows the code that is required to compile and link a *COBOL* program called *MYPROG* in a Galasa test:

```
@ZosProgram(name = "MYPROG",
        location = "source",
        language = Language.COBOL,
        imageTag = "A")
public IZosProgram myprog;
```

The program source is stored in a file named *MYPROG.cbl* in a folder named *source* in the test bundle resources folder. 
The manager builds the JCL to compile and link the source code and submits it on the zOS Image allocated in the *zosImageA* field.
</details>

<details><summary>Run the compiled program</summary>

The following snippet shows the code required to run the compiled program in a batch job:

```
@ZosImage(imageTag = "A")
public IZosImage image;

@ZosBatch(imageTag = "A")
public IZosBatch zosBatch;

...

StringBuilder jcl = new StringBuilder();
jcl.append("//STEP1   EXEC PGM=");
jcl.append(myprog.getName());
jcl.append("\n");
jcl.append("//STEPLIB DD DSN=");
jcl.append(myprog.getLoadlib().getName());
jcl.append(",DISP=SHR\n");
jcl.append("//SYSOUT  DD SYSOUT=*");
IZosBatchJob job = zosBatch.submitJob(jcl.toString(), null);
...
```

The manager created a load library for *MYPROG* because the *@ZosProgram* annotation did not specify one. The name of the library is obtained using the *getLoadlib()* method on the field so that it can be added to the *STEPLIB* in the JCL. 
</details>