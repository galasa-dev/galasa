---
title: "JMeter Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/jmeter/package-summary.html){target="_blank"}.


## Overview

This Manager enables JMeter performance tests to run within Galasa tests. The JMeter Manager supports two execution modes:

- **LOCAL mode (default)** - Runs JMeter using an external JMeter binary on the local machine. This mode is simpler to configure and faster to start, making it ideal for development and CI/CD environments.
- **DOCKER mode** - Runs JMeter inside a Docker container provisioned by the Docker Manager. This mode provides better isolation and consistency across different environments.

The test can access all JMeter-generated files (log files, JTL results files) without worrying about how JMeter is provisioned, maintained, or shut down at the end of the test.


## Execution Modes

### LOCAL Mode (Default)

LOCAL mode runs JMeter using an external JMeter binary. This is the recommended mode for most use cases.

**Configuration:**

Set the path to your JMeter binary in the Configuration Property Store (CPS):

```properties
jmeter.execution.mode=LOCAL
jmeter.binary.path=/path/to/apache-jmeter-5.6.3/bin/jmeter
```

The `jmeter.binary.path` must point directly to the JMeter binary file:
- Unix/Linux/Mac: `/path/to/apache-jmeter-x.x.x/bin/jmeter`
- Windows: `C:\path\to\apache-jmeter-x.x.x\bin\jmeter.bat`

**Advantages:**

- No Docker dependency required
- Faster startup time
- Simpler configuration
- Works in environments where Docker is not available

### DOCKER Mode

DOCKER mode runs JMeter in a Docker container. This mode requires the Docker Manager to be available.

**Configuration:**

=== "Local Docker Engine"
```properties
jmeter.execution.mode=DOCKER
docker.engine.PRIMARY.hostname=localhost
docker.engine.port=2375
```

=== "Remote Docker Engine"

```properties
jmeter.execution.mode=DOCKER
docker.default.engines=PRIMARY
docker.engine.PRIMARY.hostname=1.10.100.100
docker.engine.PRIMARY.port=2376
docker.engine.PRIMARY.max.slots=3
```

**Advantages:**

- Isolated execution environment
- Consistent JMeter version across environments

The number of concurrent JMeter sessions is limited by available Docker container slots (DOCKER mode) or system resources (LOCAL mode). For automated runs, if there are not enough resources available, the run is put back on the queue in *waiting* state to retry. Local test runs fail if there are not enough resources available.

## Limitations

JMeter tests cannot be run remotely on a target host.

## Code snippets

Use the following code snippets to help you get started with the JMeter Manager.
 
### Creating a JMeter session

The following snippet shows the minimum code that is required to request a JMeter session in a Galasa test:

```java
@JMeterSession(jmxPath="test.jmx")
public IJMeterSession session;
```

This code requests a JMeter session. In LOCAL mode, JMeter runs using your configured JMeter binary. In DOCKER mode, a container is provisioned with JMeter binaries installed. The container is discarded when the test finishes. You can also provision your JMX file via the Artifact Manager and point it to the bundle resources, the location of which is specified in the input stream of your JMX file.

The following snippet enables you to add a personal properties file to the test by pointing the Artifact Manager at the JMeter properties file.

```java
@JMeterSession(jmxPath="test.jmx", propPath="jmeter.properties")
public IJMeterSession session;
```

There is no limit in Galasa on the number of JMeter sessions that can be used within a single test. In DOCKER mode, the limit is the number of containers that can be started in the Galasa Ecosystem, which is set by the Galasa Administrator. In LOCAL mode, the limit is determined by available system resources. If there are not enough resources available for an automated run, the run is put back on the queue in *waiting* state to retry. Local test runs fail if there are not enough resources available.


### Setting a JMX file in a JMeter session by using the Artifact Manager

Use the following code to provision a JMX file by using the Artifact Manager.

```java
IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
InputStream jmxStream = bundleResources.retrieveFile("/test.jmx");
session2.setJmxFile(jmxStream);
```


### Setting the properties file in a JMeter session by using the Artifact Manager

Just as you would provision a JMX file via the Artifact Manager, you can use the following code to provision a personalized properties file that gets used by JMeter at runtime.

```java
IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
InputStream propStream = bundleResources.retrieveFile("/jmeter.properties");
session.applyProperties(propStream);
```


### Starting a JMeter session

You can set a timeout for a JMeter session or use the *default timeout of 60 seconds (60000ms)* for a JMeter session. To use this command, you must configure the JMX file correctly by using the `session.setJmxFile(inputStream)` method. *Timeout is in milliseconds.*

```java
session.startJmeter();         // Uses default 60 second (60000ms) timeout
//...
session.startJmeter(120000);   // Custom 120 second (120000ms) timeout
```


### Obtaining the JMX file from the JMeter-execution as a String

Use the following snippet to access the JMX file that was used in the JMeter session.

```java
session.getJmxFile();
```


### Obtaining the log file from the JMeter execution as a String

Use the following snippet to access the log file that is created when the JMX file that is running inside the container finishes running.

```java
session.getLogFile();
```


### Viewing the console output as a String

Use the following snippet to view any console output that is generated by the JMeter test run. In LOCAL mode, this captures the standard output from the JMeter process. In DOCKER mode, this captures the container's console output. Typically, there is no console output unless the JMX file itself is corrupt or written incorrectly. If a correctly written JMX file generates errors during execution, the errors are held in the log files or in the JTL file.

```java
session.getConsoleOutput();
```


### Obtaining a generated file from the JMeter-execution as a String

Use the following snippet to help you to access any file that is created after execution of a JMX file inside a container completes. In this example, the JTL file *test.jtl* is returned as a String containing the results of the test run which can be exported to a CSV file. The name of the JTL file has the same prefix as the JMX file.

```java
session.getListenerFile("test.jtl")
```


### Checking your test ran correctly

Use the following code to check that the test ran correctly. You can use the logs and JMX files for further investigation. If the JMX file has completed its function successfully, a boolean value of true is returned, otherwise a value of false is returned.

```java
session.isTestSuccessful();
```


### Stopping the JMeter test

Use the following code to stop the JMeter test and clean up resources.

```java
session.stopTest();
```
