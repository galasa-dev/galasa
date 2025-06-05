---
title: "Frequently asked questions"
---

Use the following sections to scan for questions and problems that have been raised previously, and how these have been answered and resolved.


## How can I generate screenshot output when running my tests locally?

You can generate screenshot output by setting the `zos3270.terminal.output` property in your `cps.properties` file or your `overrides.properties` file. For example, to generate screenshot output in json and png format, set the property to `zos3270.terminal.output=json,png`. 

If you used the `galasactl local init` command to initialise your environment, the following setting should be in the `cps.properties` file already, so you can un-comment the line `zos3270.terminal.output=json,png` by removing the hash symbol (`#`), as shown in the following example:

```properties
#-------------------------------------------------------------------------
# zos3270.terminal.output
#
# Controls which output format(s) any 3270 traffic uses when creating artifact files
# in the Result Archive Store (RAS).
#
# Supported values are 'json' or 'png' or both.
# 
# Default for a locally-launched JVM test is both.
#
# Example:
zos3270.terminal.output=json,png
```


## What function can I use to clear a CICS screen in a 3270 terminal?

If your terminal object is of type `ITerminal` you can use the `terminal.clear().waitForKeyboard()` method in your test code. 

If your terminal object is of type `ICicsTerminal` you can use the method `resetAndClear()`  as this method has a slightly different implementation and checks for a clear screen before beginning to type again.


## Are there any IVT test cases or Javadoc that I can look at for samples of test code / methods?

Yes, you can view the [installation verification tests (IVTs)](https://github.com/galasa-dev/managers/tree/main/galasa-managers-parent){target="_blank"} for the Managers in GitHub.

You can find the links to the Javadoc API documentation for all the Galasa Managers on the [overview page](https://javadoc.galasa.dev/){target="_blank"}.


## Can I generate run reports with logs whist running Galasa tests? 

If you are using the galasactl command-line tool to launch tests, you can specify the `--reportjson`, `--reportjunit` , or `--reportyaml` flags to get a `json`, `junit` or `yaml` report. Run log output is always captured in the `{GALASA_HOME}/ras/{run-id}`, and you can turn on debug-output to the command-line tool using `--log -` (if you want to see it on the screen), or `--log {filename}` if you want the log information captured into a file.


## Does Galasa support multiple terminals?

Yes, you can declare the number of terminals you need in the test class and use different tags to hook them up to different z/OS images.


## What is the purpose of `imageTag`?

The `imageTag` is a symbolic label in your testcase source code which maps onto some configuration. `imageTag` is useful because if the configuration needs to change, it means that you don't have to go back and change the source code and re-build/re-distribute it. You can simply change the configuration it uses when running.

The purpose an imageTag is to give the Galasa framework a label that it can use to tie-up with the CPS properties that are used by the Manager. For example, the Terminal Manager. Your CPS properties have a mapping from the `imageTag` label to the actual system that is used. When you generate a project with the `galasactl project create` command, it creates a `cps.properties` file as shown in the following example:

```properties
# An example of properties required to identify and configue a z/OS resource
# In this case, our z/OS system is on a cluster, which has two images.
# Here we define a user-tag called CLUSTERA.
# This defines a user-tag of IMAGEA and IMAGEB.
# The names are chosen to be meaningful, and to give us a way of linking 
# related properties using parts of the name.

# zos.cluster.CLUSTERA.images=IMAGEA,IMAGEB

## Here are some physical values which can be looked up for the IMAGEA system:
# zos.image.IMAGEA.default.hostname=dev.galasa.system1
# zos.image.IMAGEA.ipv4.hostname=dev.galasa.system1
# zos.image.IMAGEA.telnet.port=992
# zos.image.IMAGEA.telnet.tls=true
# zos.image.IMAGEA.credentials=MY_KEY_INTO_GALASA_CREDENTIALS_STORE
# zos.image.IMAGEA.max.slots=4

## Here are some physical values which can be looked up for the IMAGEB system
# zos.image.IMAGEB.default.hostname=dev.galasa.system2
# zos.image.IMAGEB.ipv4.hostname=dev.galasa.system2
# zos.image.IMAGEB.telnet.port=992
# zos.image.IMAGEB.telnet.tls=true
# zos.image.IMAGEB.credentials=MY_KEY_INTO_GALASA_CREDENTIALS_STORE
# zos.image.IMAGEB.max.slots=4

## For our systems, we might use zosmf to communicate to the z/OS systems...
# zosmf.server.CLUSTERA.images=IMAGE1
# zosmf.server.MVLA.hostname=dev.galasa.system1
# zosmf.server.MVLA.port=32070
```

So the `zos.cluster.CLUSTERA.images` means that you could use the `CLUSTERA` as a label in the Galasa annotations, and the Galasa framework would be allowed to decide which actual image is used (IMAGEA or IMAGEB). If you actually want to use `IMAGEA` in your source code you can, but it's better to use this symbolic abstraction via the `zos.cluster.images`.

In the documented Simbank example, we have this:

```properties
zos.dse.tag.SIMBANK.imageid=SIMBANK
zos.dse.tag.SIMBANK.clusterid=SIMBANK

simbank.dse.instance.name=SIMBANK
simbank.instance.SIMBANK.zos.image=SIMBANK

zos.image.SIMBANK.ipv4.hostname=127.0.0.1
zos.image.SIMBANK.telnet.port=2023
zos.image.SIMBANK.webnet.port=2080
zos.image.SIMBANK.telnet.tls=false
zos.image.SIMBANK.credentials=SIMBANK

zosmf.image.SIMBANK.servers=SIMBANK
zosmf.server.SIMBANK.image=SIMBANK
zosmf.server.SIMBANK.port=2040
zosmf.server.SIMBANK.https=false
```

which means there is only a single imageTag of "SIMBANK" being mapped to the "SIMBANK" image, for which there are only a single set of properties.

If you had a CPS property of `zos.cluster.CLUSTERA.images=IMAGEA`, then `CLUSTERA` should be set in your Java code annotations, with something similar to the following example in your `cps.properties` file: 

```properties
zos.image.IMAGEA.default.hostname=dev.galasa.system1
zos.image.IMAGEA.ipv4.hostname=dev.galasa.system1
zos.image.IMAGEA.telnet.port=992
zos.image.IMAGEA.telnet.tls=true
zos.image.IMAGEA.credentials=MY_KEY_INTO_GALASA_CREDENTIALS_STORE
zos.image.IMAGEA.max.slots=4
```

but with the values customized to match the name and location of your z/OS system.


## What is the connection between z/OS terminal and application?

The `ITerminal` object allows communication to the remote z/OS system. See the [Javadoc for ITerminal](https://javadoc.galasa.dev/dev/galasa/zos3270/ITerminal.html){target="_blank"} for more information. 


## Why is my test (which is running on a local LPAR) failing at the Provision Generate phase with the error "Caused by: dev.galasa.zos.ZosManagerException: Insufficent capacity for images in cluster..."?
 
Try clearing out the `dss.properties` file on your local machine. The DSS (Dynamic Status Store) is a component that is provided in Galasa. In a local environment, or when running tests locally within a pipeline, it is in a file. The `dss.properties` file contains lots of settings, and counters that Managers put there, to indicate what resources are currently in-use so far. So, if a z/OS machine only has ten ports configured for example, and the counter reaches ten, this prevents any more tests trying to allocate a port for them to use. There is a danger if tests are dropped, that the proper cleanup is skipped, and the port number doesn't decrease when a test owning that port stops abnormally.

Normal operation of an Ecosystem includes a resource monitor which looks for this situation and releases resources that a failed test owns. In a local run the resource monitor isn't present, so the DSS might not accurately reflect how much resource is available. In this example, a test is run, the 3270 port counter increases up to one, the test fails, and the DSS is not cleaned up but gets re-used in the next pipeline run. The counter then starts at one and increases to two and so forth until the limit is reached and a test is no longer able to get access to the resources it needs. The same situation might occur with counters of images on a cluster.

Clearing out the `dss.properties` file resets the counters down to zero again. It is safe to do this providing that no tests are running. In principle, if you run multiple tests in parallel, you can see some of this limiting safety feature start failing tests even if you did not clean out the DSS. It is worth noting that if you always clear out the `dss.properties` file, the run name of tests always start from the same number, as the latest test number allocated counter is also kept there.


## I have a testcase that needs to connect to an external resource with SSL. Is there a preferred way to do this with Galasa if I do not have access to the default truststore? 

For local tests, in the `bootstrap.properties` file, you can use `galasactl.jvm.local.launch.options=-Xmx80m -Xms20m`.
When the galasactl program launches the JVM it will add those options to the command-line.
