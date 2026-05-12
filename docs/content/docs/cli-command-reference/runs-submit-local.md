---
title: "Running a test locally"
---

# Running tests locally using the command line

The `galasactl runs submit local` command runs tests within your local JVM (Java Virtual Machine), rather than deploying them to a remote Galasa Ecosystem.

You can run both [Java tests](#running-a-java-test-with-the-runs-submit-local-command) and [Gherkin tests](#running-a-gherkin-test-with-the-runs-submit-local-command) using this command, but each test type requires different command-line flags.

**When to use local test runs:** Use local test runs during test development to verify that your test behaves as expected. Local runs do not provide the features available when running tests in a Galasa Ecosystem, such as automatic resource cleanup on failure and scaling capabilities.


## Prerequisites for the `runs submit local` command

Before running tests locally, you must set the `JAVA_HOME` environment variable to point to your JVM installation. See the [Prerequisites](./cli-prereqs.md) documentation for details.

The tool verifies that `JAVA_HOME` is set correctly by checking for:

- `$JAVA_HOME/bin/java` on Linux or macOS
- `%JAVA_HOME%\bin\java.exe` on Windows

**Java version requirements:** Your Java version must match the supported level for your Galasa version. Use `galasactl --version` to check your galasactl version. Currently, Galasa supports Java 17 JDK. Java 21 and later versions are not currently supported.

For all available command options, see the [galasactl runs submit local](../reference/cli-syntax/galasactl_runs_submit_local.md) reference.


## Running a Java test with the `runs submit local` command

Use this command to run a Java test in a local JVM:

=== "Linux or macOS"

    ```shell
    galasactl runs submit local --log - \
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount
    ```

=== "Windows (Powershell)"

    ```powershell
    galasactl runs submit local --log - `
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr `
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount
    ```

**Parameters explained:**

- `--log -`: Sends debugging information to the console (stderr). The `-` directs output to the console.

- `--obr`: Specifies where the CLI tool can find an OBR (OSGi Bundle Repository) that references the bundle containing your tests. When running locally, all tests must exist in the OBR (or OBRs) passed to the tool. The value uses Maven coordinates format: `mvn:groupId/artifactId/version/classifier`.

- `--class`: Specifies which test class to run. Format: `<osgi-bundle-id>/<fully-qualified-java-class>`. All test methods within the class are executed. Use multiple `--class` flags to run multiple test classes.

??? info "Overriding the default local Maven repository path"

    Tests require compiled artifacts hosted in a Maven repository. These artifacts must be bundled as OSGi bundles. When you build a Galasa project locally, the built artifacts are placed in `.m2/` in your user home directory by default.

    If you want to use a different location for your local Maven repository, specify the path using the `--localMaven` flag on the `galasactl runs submit local` command. This tells the CLI tool where to load Galasa bundles from on your local file system.

    The path must be in URL format:

    - Linux/macOS: `file:///Users/myuserid/mylocalrepository`
    - Windows: `file:///C:/Users/myuserid/mylocalrepository`

    **Important:** The repository referenced by `--localMaven` must contain all required OBRs (OSGi Bundle Repositories):

    - Test OBRs
    - Manager OBRs

    Galasa uses OBRs to locate tests and all required Managers in the specified Maven repository.


## Stopping a running test

Press `Ctrl-C` to stop the Galasa CLI and end all test activity. 

!!! warning

    This might leave the system under test with resources that are not cleaned up.
    
    See [How to clean up resources after local runs exit abnormally](../writing-own-tests/running-resource-cleanup-locally.md) for instructions on cleaning up resources.


## Troubleshooting

If you encounter problems running the command, verify the following:

1. **Java installation:** Check that you have the correct Java version installed and that your `JAVA_HOME` environment variable is set correctly. See [CLI prerequisites](./cli-prereqs.md).

2. **CLI setup:** Ensure that you have added the Galasa CLI to your PATH.

3. **Local environment:** Verify that you have initialized your local environment by running `galasactl local init`. See [Initialising your local environment](./initialising-home-folder.md).

4. **Project setup:** Confirm that you have created and built your example project. See [Creating a Galasa project](./setting-up-galasa-project.md).


## Next steps

After running a test, read [Debugging a test locally](./runs-local-debug.md) to learn how to:

- Connect your Galasa test with a Java debugger on a specified port
- Configure your IDE to connect to that port
- Run your test locally in debug mode
