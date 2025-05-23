---
title: "Testing across environments"
---

Ideally, you want to avoid hard coding within your test. If you hard code target hostnames, port numbers and so on, you can't run the same test against different environments without changing the test code. 

Galasa Managers use attributes and their associated properties to bind a test to an environment at runtime. Using attributes to enable late binding of the test material to the system under test allows the same test to run against multiple environments without changing the test itself. 

## Using credentials properties

It is likely that a test will need to pass credentials to the application being tested. For example, as HTTP credentials or as username and password values entered onto a 3270 screen. 

Rather than hard code the credentials inside a test class, you can store the values in the credentials file when the test is run locally and in the ecosystem when the test is run remotely. The ability to get credentials from a credentials store means that you do not need to hard code these values inside a test, enabling the test to be run in different environments without changing a single line of code.  

You can extract credentials by using the Core Manager `getCredentials` method. The Core Manager uses the `getCredentials` method to retrieve a user id and password from the credentials store to use in your test. See the [Core Manager](../../docs/managers/core-manager) documentation for more information and code snippet examples.

If you want to mask the password, for example to prevent it from being displayed in recorded screens, you can use the Core Manager `registerConfidentialText` method. There is an example of this method in the _Running the SimBank Installation Verification Test_ documentation.

## An example using SimBank

In Galasa, when an annotation refers to a resource, for example a z/OS image, you can use the *imageTag* attribute to avoid hard coding the endpoint of that z/OS image in your test code. The properties that are associated with the attribute are stored in the CPS file. 

Let’s look at an example in the SimBank ```BatchAccountsOpenTest.java``` test code where the *imageTag* attribute is used to avoid hard coding an endpoint for a z/OS image.

The test declares that a z/OS image is required for the test to run and that this image is called *SIMBANK* by using the following test code: 
```
@ZosImage(imageTag="SIMBANK")
    public IZosImage image;
```

When the z/OS Manager reads the code, it creates a z/OS image by using the *imageTag* attribute to ascertain which set of properties from the `cps.properties` or `overrides.properties` file that it needs to load to create that image.

In this example, the following properties are associated with the *SIMBANK* z/OS image in the `overrides.properties` file, as described in the [Running the SimBank tests using the CLI](../running-simbank-tests/running-simbank-tests-cli) documentation:
```
zos.image.SIMBANK.ipv4.hostname=127.0.0.1
zos.image.SIMBANK.telnet.port=2023
zos.image.SIMBANK.telnet.tls=false
zos.image.SIMBANK.credentials=SIMBANK
```

## Building on the SimBank example

When you create your own test that runs against a z/OS image, you probably want to call that image something other than *SIMBANK*. For example, you might want to use an image called *zosImage1*.

You can do this by editing the CPS file to contain the properties that you want *zosImage1* to have and by declaring *zosImage1* in your test code, as per the following steps:

1.	Edit the CPS properties file (either locally or in your ecosystem) to define the properties of the image: 
```
zos.image.zosImage1.ipv4.hostname=127.0.0.1
zos.image.zosImage1.telnet.port=23
zos.image.zosImage1.telnet.tls=false
```

2.	In the test code, declare a z/OS image called *zosImage1*:
```
@ZosImage(imageTag="zosImage1")
public IZosImage image;
```
The z/OS Manager reads the test code and creates the image object by using the properties associated with *zosImage1* in the CPS file. 

## Clustering

What if you want to run your test against an image in a cluster? By editing the CPS properties file, you can define clusters containing images against which your test can run . Once defined, Galasa can dynamically select an image from that cluster at run time.

For example, you might want to define a cluster called *CLUSTER2* which contains two z/OS images called *IMAGEA* and *IMAGEB*. You can do this by udpating the CPS file to define *CLUSTER2* and the images that you want it to contain, along with the properties of those images, as per the following example:

```
# CLUSTER2
zos.cluster.CLUSTER2.images=IMAGEA,IMAGEB

zos.image.IMAGEA.ipv4.hostname=winmvs2a.example.com
zos.image.IMAGEA.telnet.port=23
zos.image.IMAGEA.telnet.tls=false
zos.image.IMAGEA.credentials=WINMVS2A
zos.image.IMAGEA.max.slots=1
zos.image.IMAGEA.clusterid=CLUSTER2
zos.tag.IMAGEA.clusterid=CLUSTER2

zos.image.IMAGEB.ipv4.hostname=winmvs2b.example.com
zos.image.IMAGEB.telnet.port=23
zos.image.IMAGEB.telnet.tls=false
zos.image.IMAGEB.credentials=WINMVS2A
zos.image.IMAGEB.max.slots=1
zos.image.IMAGEB.clusterid=CLUSTER2
zos.tag.IMAGEB.clusterid=CLUSTER2
```

The images are linked back to the *CLUSTER2* cluster by using  the following code: 

```
zos.image.IMAGEA.clusterid=CLUSTER2
zos.image.IMAGEB.clusterid=CLUSTER2
```  

In the test code, set the *imageTag* to *CLUSTER2*, as per the following example:

```
@ZosImage(imageTag="CLUSTER2")
   public IZosImage image;
```

When the test runs, Galasa dynamically selects either *IMAGEA* or *IMAGEB* from *CLUSTER2* at run time. 

For example, in the CPS file *IMAGEA* has a maximum number of slots set to ```1```. If that slot is in use, Galasa binds the test to *IMAGEB* at run time, if *IMAGEB* is available. Images can be added to *CLUSTER2* by updating the CPS file, without the need to recompile the test. 

In this way, Galasa manages the inbound workload of tests and splits them across LPARS. Each LPAR has a certain number of slots that are allocated to it. When the slots are used, no more tests can be allocated to that LPAR. If the LPAR is part of a cluster, then Galasa switches to another LPAR. Only when a test is told where it can run is it bound to that environment.


## Troubleshooting

If you see an error message in the run log indicating that a property is not found, check that the property is spelled correctly in your test and if so, check that the property is defined in the CPS properties file. 

If you are running test cases locally that target an LPAR that is offline, you might get an "Insufficient capacity for images..." error or similar. To resolve this problem, delete any failed test runs, clear the content of the DSS file in `~/.galasa/` and run the tests again.
