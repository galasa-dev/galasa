<details><summary>Create the Artifact Manager</summary>

The ArtifactManager allows you to stream resources from within the same bundles as your test class.  The easiest way to accomplish this is to use the BundleResources annotation:

```
@BundleResources
public IBundleResources resources;
```
The resources object now references the list of resources that are held within the test bundle under */src/main/resources/*.  These resources can now be accessed directly by the test code, by calling methods on the resources object.

Alternatively, if the resources are within another class, you can use the ArtifactManager annotation to address the Manager directly:

```
@ArtifactManager
public IArtifactManager artifacts;
```

This will provision an instance of the Artifact Manager.  In order to obtain a IBundleResources object to reference the content of the resources within a test bundle you will need to use the API:

```
IBundleResources resources = artifacts.getBundleResources(this.getClass());
```

</details>

<details><summary>Retrieving the content of a file as a string</summary> 

The simplest thing that a test might want to do is retrieve the content of a file as a string.  First, an input stream to the file location is constructed and then a helper method is invoked to stream the content as a string.  However, if you wanted to read from the input stream yourself, then that option is available to you.

```
InputStream is = resources.retrieveFile("input/hobbit.txt");
String textContext = resources.streamAsString(is);
```

From Galasa 0.8.0 you can stream a string content directly by using the following code:

```
String textContent = resources.retrieveFileAsString("/resources/textFiles/test1.txt");
```

This uses the same methods as the previous example but is one less line of code you have to write.
</details>

<details><summary>Substituting variables into a file</summary>

As you read a file from the local bundle you might want to substitute values into the file before retrieving it.  This is called within Galasa as retrieving a skeleton file from the bundle.  The first step is to create a HashMap containing the name of the variables that you want to be substituted in the target file, as well as the value that should be used.

```
HashMap<String, Object> parameters = new HashMap<String, Object>();
parameters.put("ACCOUNT_NUMBER", "123456789");
parameters.put("AMOUNT", "50.05");
```

In this case we are adding two items to the HashMap, setting the substitute values for both ACCOUNT_NUMBER and AMOUNT.  This HashMap is then passed to the retrieveSkeletonFile method.

```
InputStream is = resources.retrieveSkeletonFile("input/hobbit.txt", parameters);
String textContext = resources.streamAsString(is);
```

When the file is now retrieved from the local bundle all references to ++ACCOUNT_NUMBER++ and ++AMOUNT++ will be substituted for "123456789" and "50.05" respectively.  Note that these substitutions are for the instance of the test that is running.  The actual copy of the file in the bundle is unchanged and cannot affect any other instances of the test that are running at the same time.

From Galasa 0.8.0 this has been updated to allow you to retrieve the content of a file directly as a string and still perform variable substitution as the following example shows:

```
String textContext = resources.retrieveSkeletonFile("input/hobbit.txt", parameters);
```

</details>
