---
title: "Artifact Manager"
---

This Manager is at Release level. You can view the [Javadoc documentation for the Manager](https://javadoc.galasa.dev/dev/galasa/artifact/package-summary.html){target="_blank"}.


## Overview

This Manager allows a test to access files that are stored within the same bundle as the test code itself.  Helper methods are provided to stream and substitute symbolics within the loaded file.


## Code snippets

Use the following code snippets to help you get started with the Artifact Manager.
 

### Create the Artifact Manager

The ArtifactManager allows you to stream resources from within the same bundles as your test class.  The easiest way to accomplish this is to use the BundleResources annotation:

```java
@BundleResources
public IBundleResources resources;
```

The resources object now references the list of resources that are held within the test bundle under */src/main/resources/*.  These resources can now be accessed directly by the test code, by calling methods on the resources object.

Alternatively, if the resources are within another class, you can use the ArtifactManager annotation to address the Manager directly:

```java
@ArtifactManager
public IArtifactManager artifacts;
```

This will provision an instance of the Artifact Manager.  In order to obtain a IBundleResources object to reference the content of the resources within a test bundle you will need to use the API:

```java
IBundleResources resources = artifacts.getBundleResources(this.getClass());
```


### Retrieving the content of a file as a string

The simplest thing that a test might want to do is retrieve the content of a file as a string.  First, an input stream to the file location is constructed and then a helper method is invoked to stream the content as a string.  However, if you wanted to read from the input stream yourself, then that option is available to you.

```java
InputStream is = resources.retrieveFile("input/hobbit.txt");
String textContext = resources.streamAsString(is);
```

In this case the file hobbit.txt would be in the location */src/main/resources/input/hobbit.txt*.

You can stream a string content directly by using the following code:

```java
String textContent = resources.retrieveFileAsString("/textFiles/test1.txt");
```

This uses the same methods as the previous example but is one less line of code you have to write.


### Substituting variables into a file

As you read a file from the local bundle you might want to substitute values into the file before retrieving it.  This is called within Galasa as retrieving a skeleton file from the bundle.  The first step is to create a HashMap containing the name of the variables that you want to be substituted in the target file, as well as the value that should be used.

```java
HashMap<String, Object> parameters = new HashMap<String, Object>();
parameters.put("ACCOUNT_NUMBER", "123456789");
parameters.put("AMOUNT", "50.05");
```

In this case we are adding two items to the HashMap, setting the substitute values for both ACCOUNT_NUMBER and AMOUNT.  This HashMap is then passed to the retrieveSkeletonFile method.

```java
InputStream is = resources.retrieveSkeletonFile("input/hobbit.txt", parameters);
String textContext = resources.streamAsString(is);
```

When the file is now retrieved from the local bundle all references to ++ACCOUNT_NUMBER++ and ++AMOUNT++ will be substituted for "123456789" and "50.05" respectively.  Note that these substitutions are for the instance of the test that is running.  The actual copy of the file in the bundle is unchanged and cannot affect any other instances of the test that are running at the same time.

From Galasa 0.8.0 this has been updated to allow you to retrieve the content of a file directly as a string and still perform variable substitution as the following example shows:

```java
String textContext = resources.retrieveSkeletonFile("input/hobbit.txt", parameters);
```
