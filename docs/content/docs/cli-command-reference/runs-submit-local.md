---
title: "Running a test locally"
---

# Running tests locally using the command line

The `galasactl runs submit local` command submits tests to run within the local JVM, rather than dynamically deploying the tests to a remotely deployed Galasa Ecosystem. 

You can submit a [Java test](#working-with-the-runs-submit-local-command) and a [Gherkin test](#running-a-gherkin-test-with-the-runs-submit-local-command) by using the command but must to specify different flags on the command line for each test type. Read on to find out more about how to submit each type of test on your local machine.

Running tests locally should only be used during test development to verify that the test is behaving as expected. 
Local runs do not benefit from the features that are provided when running tests within a Galasa Ecosystem. For example, resources are not cleaned-up in the event of a failure and scaling capabilities are limited by workstation resources. 


## Working with the `runs submit local` command

To use the `galasactl runs submit local` command, the `JAVA_HOME` environment variable must be set to reference the JVM in which you want the test to run, as described in the [CLI prerequisites online](./cli-prereqs.md) and [CLI prerequisites offline](./zipped-prerequisites.md) documentation. This is because the local java run-time environment is used to launch the test locally. To check that `JAVA_HOME` is set correctly, the tool checks that `$JAVA_HOME/bin/java` exists in Unix or Mac, and `%JAVA_HOME%\bin\java.exe` exists on Windows.

The level of Java must match the supported level of the Galasa version that is being launched. Use the `galasactl --version` command to find the galasactl tool version. We currently support Java version 11 to version 17 JDK. _Note:_ We do not currently support Java 21 or later.

You can view the full list of options that are available with the `galasactl runs submit local` command in the 
[Galasa cli repository](https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_runs_submit_local.md){target="_blank"}.

## Running a Java test with the `runs submit local` command

Use the following command to run a Java test in the local JVM.

On Mac or Unix:

```shell
galasactl runs submit local --log - \
--obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
--class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount
```

On Windows (Powershell):

```powershell
galasactl runs submit local --log - `
--obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr `
--class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount
```

where:

- `--log` specifies that debugging information is directed somewhere, and the `-` means that it is sent to the console (stderr).
- `--obr` specifies where the  CLI tool can find an OBR which refers to the bundle where the tests are stored. When running locally, all tests must exist in the OBR (or OBRs) that are passed to the tool. The `--obr` parameter specifies the Maven co-ordinates of the obr jar file, in the format `mvn:groupId/artifactId/version/classifier`.
- `--class` specifies which test class to run. The string is in the format of `<osgi-bundle-id>/<fully-qualified-java-class>`. All test methods within the class are run. Use multiple flags to test multiple classes.


## Running a Gherkin test with the `runs submit local` command

Use the following command to run a Gherkin test in the local JVM. Note that the `--gherkin` flag is specified and that the `--obr` or `--class` flags are not required. 

On Mac or Unix:

```shell
galasactl runs submit local --log - \
--gherkin file:///path/to/gherkin/file.feature
```


On Windows (Powershell):

```powershell
galasactl runs submit local --log - `   
--gherkin file:///path/to/gherkin/file.feature
```

where:

- `--log` specifies that debugging information is directed somewhere, and the `-` means that it is sent to the console (stderr).
- `--gherkin` specifies the path where the  CLI tool can find the Gherkin file containing the Gherkin tests. The path must be specified in a URL form, ending in a `.feature` extension. For example,`file:///Users/myuserid/gherkin/MyGherkinFile.feature` or `file:///C:/Users/myuserid/gherkin/MyGherkinFile.feature`.

For more information about the Gherkin support currently available, see the [Galasa CLI Gherkin documentation](https://github.com/galasa-dev/cli/blob/main/gherkin-docs.md){target="_blank"}.


## Overriding the path to the default local Maven repository

In order to run, tests require compiled artifacts to be hosted in a Maven repository. The artifacts must be bundled as an OSGI bundle. When you build a Galasa project locally, the built artifacts are placed by default in the `~/.m2/` repository in your home directory; the default location of the local Maven repository.  

If you want to use a non-standard location for your local Maven repository when running a test locally, rather than the default `~/.m2/` repository, you can specify the path to your non-standard local Maven repository folder when launching a test by setting the  `--localMaven` flag on the `galasactl runs submit local` command. The `--localMaven` parameter tells the CLI tool where Galasa bundles can be loaded from on your local file system. The parameter value must be given in a URL form, for example, `file:///Users/myuserid/mylocalrepository` or `file:///C:/Users/myuserid/mylocalrepository`.

*Note:* the repository that is referenced by the `--localMaven` flag must contain the test, Manager, and Galasa framework OBRs (OSGi Bundle Repositories) that the test needs in order to run. Galasa uses OBRs to locate tests in the specified Maven repository, along with all of the Managers that the test requires.


## Stopping a running test

Use `Ctrl-C` to stop the Galasa CLI, ending all test activity. Note that this might leave the system under test with resources that are not cleaned-up.


## Troubleshooting

If you have problems running the command, check that you have installed the correct version of Java installed and that you have set your JAVA_HOME environment variable, as described in the [CLI prerequisites](./cli-prereqs.md) and [CLI prerequisites offline](./zipped-prerequisites.md) documentation. Make sure you have added the Galasa CLI to your PATH and that you have [initialised your local environment](./initialising-home-folder.md) by running the `galasactl local init` command. Ensure that you have created and built the example project, as described in the [Creating a Galasa project](./setting-up-galasa-project.md) documentation. 


## Next steps

Once you have run a test, read the [Debugging a test locally](./runs-local-debug.md) documentation to find out how to connect your Galasa test with a Java debugger on a specified port, and configure your IDE to connect to that same port so that you can run your test locally in debug mode. 

