---
title: "Initialising your local environment"
---

Before you can run Galasa tests, you need to set up a basic folder structure on your computer. The `galasactl` tool makes this easy with a single command.


??? note "Setting the Galasa home folder (optional)"

    By default, Galasa uses a folder called `~/.galasa` in your home directory to store test results and configuration files.

    You can change this location by setting the `GALASA_HOME` environment variable. This is useful if you:
    - Need multiple separate Galasa environments
    - Want to share configuration with others
    - Are running low on disk space in your home directory

    To set a custom location, use:

    === "Linux or macOS"

        ```shell
        export GALASA_HOME=/temp/mygalasatests
        ```

    === "Windows (Powershell)"

        ```powershell
        set GALASA_HOME=C:\temp\mygalasatests
        ```

    You can also override this setting for a single command using the `--galasahome` option.

    If you change `GALASA_HOME` to a new location, run `galasactl local init` to set up the folder structure there.

## Setting up the Galasa file structure

Run this command to create the necessary folders and files:

```shell
galasactl local init
```

This command only needs to be run once for each Galasa home directory. If the folder structure already exists, nothing will be changed.

The command creates these files:

```
${HOME}/.galasa
├── bootstrap.properties
├── cps.properties
├── credentials.properties
├── dss.properties
├── overrides.properties
```

See [About the properties files](#about-the-properties-files) for details on what each file does.

The command also creates a `~/.m2/settings.xml` file for Maven configuration. This file tells Maven where to find the libraries needed to run Galasa tests.

### Verifying the setup

Check that the setup worked by looking for these folders in your home directory:

- `.galasa` folder
- `.m2` folder

**Note:** Files and folders starting with `.` are hidden by default. You may need to enable "Show hidden files" in your file browser.

Your home directory location:

- **Windows:** `C:\Users\<username>`
- **macOS or Linux:** `/Users/<username>`
- **Linux:** `/home/<username>`

!!! note "If you already have a settings.xml file"
    If `~/.m2/settings.xml` already exists, the init command won't overwrite it. Check that it contains the required Maven repositories. It should look like this:

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

For more command options, see the [galasactl local init](../reference/cli-syntax/galasactl_local_init.md) reference.

## About the properties files

The following table explains a bit more about the purpose of the properties files that are created in the `.galasa` folder after running the `galasactl local init` command: 

| File |  Purpose  |
| :---- | :-------- | 
| **bootstrap.properties**  | Contains the connection details for a Galasa Ecosystem (a shared test environment). This file is empty when running tests locally without shared configuration. |
| **cps.properties** | Stores configuration settings for your remote test systems, such as endpoints, ports, and timeouts. These settings let you run the same test against different environments without changing your test code. Use the `@TestProperty` annotation in your tests to read values from this file. | 
| **credentials.properties** | Stores usernames, passwords, and other credentials needed by your tests. Keeping credentials here (instead of in your test code) means you can run tests in different environments without code changes. Use the `getCredentials` method in your tests to read these values. |
| **dss.properties**  | Tracks which test resources are currently in use (like ports or connections). This prevents tests from trying to use resources that aren't available. If tests fail unexpectedly, this file might show resources as "in use" when they are actually free. If no tests are running, you can delete this file to reset all counters. **Note:** Deleting this file will also reset test run numbers. | 
| **overrides.properties** | Lets you temporarily change configuration settings without editing your test code or the main configuration. Useful for testing against different software versions or environments. | 

## Next steps

You are now ready to [create a Galasa project](./setting-up-galasa-project.md).
