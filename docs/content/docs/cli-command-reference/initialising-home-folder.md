---
title: "Initialising your local environment"
---

To start using Galasa tools, or running Galasa tests, you need to set up some basic file structures and files in your home directory. You can set up this structure easily by using the galasactl tool to initialise your local environment. Once your local environment is initialised, you can start running Galasa tests on your local JVM. 


## Setting the Galasa home folder (optional)

Galasa has a home folder that is set to `~/.galasa` by default. The home folder is a writable folder that is used by default to store test results and artifacts which are created when tests are run using local JVMs.

You do not need to change the default setting, but if you want to, the default value can be overridden by using the `GALASA_HOME` environment variable. Setting this variable means that you can control where files are created, where Galasa retrieves settings from, and where local test run results and artifacts are stored. This is useful if you need isolation between multiple Galasa local test environments, if you want to share your configuration and test run results with others, or if your `~/.galasa` folder is low on disk space.

For example, to set the `GALASA_HOME` environment variable to a folder called _mygalasatests_ in a directory called _temp_, use the following command:

On Mac or Unix:

```shell
export GALASA_HOME=/temp/mygalasatests
```

On Windows: 

```powershell
set GALASA_HOME=C:\temp\mygalasatests
```

You can override the value of the `GALASA_HOME` environment variable on a call-by-call basis by using the `--galasahome` command-line option.

If you change the `GALASA_HOME` variable to a non-existent or non-initialised folder path, you can create the folder and re-initialise the folder path by running the `galasactl local init` command.

## Setting up the Galasa file structure

You can initialise your local environment, and set up the files and file structure you need, by running the following command on a command line:

```shell
galasactl local init
```

This one-time command is run once per Galasa home directory and helps you to quickly create the folder and files that you need. If you already have a Galasa home folder set up, the folder and files are not created. 

The command creates the following file structure, with default contents in each of the properties files:

```
${HOME}/.galasa
├── bootstrap.properties
├── cps.properties
├── credentials.properties
├── dss.properties
├── overrides.properties
```

For more information about these files and what they are used for, see the [About the properties files](#about-the-properties-files) section. 

Running the `galasactl local init` command also creates an `/.m2` directory, containing a `settings.xml` file. The `settings.xml` file enables you to set configurations for Maven to use during test runs. For example, you can set local and remote repository locations, credentials for private repositories, and Maven profile settings. 

You can validate the set up by locating your user home directory and confirming that it contains a `.galasa` and a `.m2` folder. On Windows, the user home directory resembles: `C:\Users\<username>`, on MacOS it will be `/Users/<username>` and on Linux `/home/<username>`. Note that any file or folder beginning with a `.` is a hidden folder, so you might need to change the settings on your file browser user interface to show hidden files.


!!! note "Note - online installation only"
    If an `.~/.m2/settings.xml` file already exists, the `galasactl local init` command does not update it. View the file contents and check that it has the required `maven.central` and `plugin` repositories configured. The file should contain the following content:


```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <pluginGroups>
        <pluginGroup>dev.galasa</pluginGroup>
    </pluginGroups>
     
    <profiles>
        <profile>
            <id>galasa</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <repositories>
                <repository>
                    <id>maven.central</id>
                    <url>https://repo.maven.apache.org/maven2/</url>
                </repository>
                <!-- To use the bleeding edge version of galasa, use the development obr
                <repository>
                    <id>galasa.repo</id>
                    <url>https://development.galasa.dev/main/maven-repo/obr</url> 
                </repository>
                -->
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <id>maven.central</id>
                    <url>https://repo.maven.apache.org/maven2/</url>
                </pluginRepository>
                <!-- To use the bleeding edge version of galasa, use the development obr
                <pluginRepository>
                    <id>galasa.repo</id>    
                    <url>https://development.galasa.dev/main/maven-repo/obr</url> 
                </pluginRepository>
                -->
             </pluginRepositories>
         </profile>
     </profiles>
</settings>
```

If you have access to GitHub, you can view the full list of options (flags) that are available with the `galasactl local init` command in the [Galasa CLI repository](https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_local_init.md){target="_blank"}.

## About the properties files

The following table explains a bit more about the purpose of the properties files that are created in the `.galasa` folder after running the `galasactl local init` command: 

| Name |  Description  |
| :---- | :-------- | 
| bootstrap.properties  | The bootstrap contains the information that Galasa needs to bring up a framework in the environment to connect to the Galasa Ecosystem. If you are running a test on your local machine, the bootstrap file is blank as it does not need to refer to the Ecosystem. |
| cps.properties | The configuration property store (CPS) defines object properties, topologies, system configurations, and definitions which instruct the way in which a Galasa test runs. For example, properties for endpoints, ports and timeouts. When running in an Ecosystem, all Galasa tests will use the same CPS configuration, unless any overrides are passed at submission. It is the CPS and the configurational properties that enable tests to run against multiple environments, without changing the code inside the test. You can extract a test property value from the CPS file for use in a test by using the `@TestProperty` annotation that is provided by the Core Manager. | 
| credentials.properties | It is likely that a test will need to pass credentials to the application being tested. For example, as HTTP credentials or as username and password values entered onto a 3270 screen. Rather than hard code the credentials inside a test class, you can store the values in the credentials.properties file when the test is run locally. The ability to get credentials from a credentials store means that you do not need to hard code these values inside a test, enabling the test to be run in different environments without changing a single line of code. You can extract credentials to use in your test by using the `getCredentials` method that is provided by the Core Manager. |
| dss.properties  | In a local environment, or when running tests locally within a pipeline, the Dynamic Status Store (DSS) is stored in a file. In an Ecosystem, the DSS is hosted on the `etcd` server. The DSS contains a number of settings, and counters that Managers put there, to indicate what resources are currently in-use so far. If a z/OS machine only has 10 ports configured for example, the counter reaches 10, which will prevent any more tests trying to allocate a port for them to use. There is a danger if tests are stopped, that the proper clean-up is skipped, and the port number doesn't decrease when a test owning that port stops abnormally. Normal operation of an Ecosystem includes a resource monitor which looks for this situation and releases resources that a failed test owns. In a local run that isn't present, so the DSS might not accurately reflect the number of resources that are available. To resolve, if no tests are running, you can clear out the `dss.properties` files in your pipeline, which steps all the counters down to 0.  Note that if you always clear out the dss file, the `run name` of tests will always start from the same number, as the `latest test number allocated` counter is also stored in the DSS. | 
| overrides.properties | Specifying overrides is useful if you want to run a set of tests against a particular configuration without changing the test code. For example, you might have multiple versions of software that you need to test. How can you do that without changing the test code? The answer is to use override properties. If you are running tests locally, you can set overrides properties by editing your overrides properties file.  | 



## Setting the Galasa bootstrap 


The bootstrap is a simple properties file that contains the information that Galasa needs to bring up a framework to connect to an Ecosystem. If you are running a test remotely in a Galasa Ecosystem, or if you are running a test locally but using a shared configuration that is hosted by the Galasa Ecosystem, the Galasa bootstrap is set to the URL of the Galasa Ecosystem in which you want to run your test or in which the shared configuration is stored. The bootstrap is blank only when a test is run locally, without using shared configuration. For more information about the modes in which you can run a Galasa test, see the [Running a Galasa test](../writing-own-tests/running-test-modes.md) documentation. 

If you are planning to only run tests locally, with everything running on the local machine, you do not need to set the Galasa bootstrap. Otherwise, you can set the bootstrap either by using the `--bootstrap` option on the CLI command or by setting the `GALASA_BOOTSTRAP` environment variable. If both are provided, the `--bootstrap` option takes precedence.

You can set environment variables on a terminal by using the `export` (if you are on Mac or Linux) or `set` (if you are on Windows) command. For example, to set `GALASA_BOOTSTRAP` to `http://galasa-bootstrap.url.com`, use the following command:

On Mac or Unix:

```shell
export GALASA_BOOTSTRAP=http://galasa-bootstrap.url.com/bootstrap
```

On Windows: 

```powershell
set GALASA_BOOTSTRAP=http://galasa-bootstrap.url.com/bootstrap
```

where:
`http://galasa-bootstrap.url.com` is the URL of the Galasa Ecosystem in which you want to run your test or in which the shared configuration is stored.



## Next steps

You can now start [creating a project using the CLI](./setting-up-galasa-project.md) to accommodate your Galasa tests in your local storage.



