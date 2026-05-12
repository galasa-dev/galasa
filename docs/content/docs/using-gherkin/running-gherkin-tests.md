---
title: "Running Gherkin tests"
---

This guide explains how to run Gherkin tests locally and remotely using the Galasa CLI.

## Prerequisites

Before running Gherkin tests, ensure you have:

1. **Initialized your Galasa environment:**
   ```shell
   galasactl local init
   ```

2. **Set up Java:** Ensure `JAVA_HOME` is set and points to a Java 17 JDK installation

3. **Created a feature file:** Write your test in a `.feature` file

## Running tests locally

Use the `galasactl runs submit local` command with the `--gherkin` flag to run Gherkin tests on your local machine.

```shell
galasactl runs submit local --gherkin file:///path/to/test.feature --log -
```

**Path format:** The path must be in URL format:

- Linux/macOS: `file:///Users/username/tests/test.feature`
- Windows: `file:///C:/Users/username/tests/test.feature`

The `--log -` flag sends logging output to the console (stderr).

## Configuring your environment

### Setting up CPS properties

For tests that interact with z/OS systems, configure your `.galasa/cps.properties` file:

```properties
# Gherkin uses the 'PRIMARY' tag by default
# The imageid property indicates which zos.image will be used
zos.dse.tag.PRIMARY.imageid=MYHOST

# The cluster name for your z/OS system
zos.dse.tag.PRIMARY.clusterid=PLEXNAME

# Images in the cluster (change MYHOSTIMAGE to your system name, e.g., MV2XX)
zos.cluster.PLEXNAME.images=MYHOSTIMAGE

# Hostname configuration (change MYHOST to match zos.dse.tag.PRIMARY.imageid)
# Change machine.hostname to your actual hostname or IP address
zos.image.MYHOST.default.hostname=machine.hostname
zos.image.MYHOST.ipv4.hostname=machine.hostname

# Sysplex configuration (change PLEXNAME to match zos.dse.tag.PRIMARY.clusterid)
zos.image.MYHOST.sysplex=PLEXNAME

# Telnet configuration
zos.image.MYHOST.telnet.port=23
zos.image.MYHOST.telnet.tls=false
```

**Configuration steps:**

1. Replace `MYHOST` with your chosen image identifier
2. Replace `PLEXNAME` with your z/OS cluster name
3. Replace `MYHOSTIMAGE` with your actual system name
4. Replace `machine.hostname` with your system's hostname or IP address

### Terminal size configuration

You can set default terminal dimensions using CPS properties:

```properties
# Default terminal size for Gherkin tests
zos3270.gherkin.terminal.rows=24
zos3270.gherkin.terminal.columns=80
```

These values can be overridden in your feature file or using override properties.

## Example: Running a simple test

### Create a feature file

Create a file named `test1.feature`:

```gherkin
Feature: GherkinLog
  Scenario: Log Example Statement
    THEN Write to log "Hello World"
```

### Run the test

```shell
galasactl runs submit local --gherkin file:///test1.feature --log -
```

## Troubleshooting

### Common issues

**Issue:** `JAVA_HOME not set`

- **Solution:** Set the `JAVA_HOME` environment variable to your Java 17 JDK installation

**Issue:** `Feature file not found`

- **Solution:** Ensure the path is in URL format and the file exists at that location

**Issue:** `Connection timeout to z/OS system`

- **Solution:** Verify your CPS properties are correct and the system is accessible

**Issue:** `Step definition not found`

- **Solution:** Check that you are using a supported step definition from the [Available step definitions](./gherkin-step-definitions.md) list

### Getting help

If you encounter issues:

1. Check the [CLI prerequisites](../cli-command-reference/cli-prereqs.md)
2. Verify your [local environment initialization](../cli-command-reference/initialising-home-folder.md)
3. Review the [Available step definitions](./gherkin-step-definitions.md)
4. Check the Galasa logs for detailed error messages

## Command reference

For complete command syntax and all available options, see [galasactl runs submit local](../reference/cli-syntax/galasactl_runs_submit_local.md)
