# Maven Plugin for building Galasa

This repository is an extension to Maven2 and includes goals that are used to build OSGI bundles and test catalogs for Galasa. The OSGI bundle repositories contain all the project information, configuration details and dependencies that are needed for building and running Galasa projects. The test catalog is used to manage Galasa test cases - attributes associated with test cases that are held in the catalog can used to schedule test runs. 

## Documentation

More information can be found on the [Galasa Homepage](https://galasa.dev). Questions related to the usage of Galasa can be posted on [Galasa Slack channel](https://galasa.slack.com).

## Where can I get the latest release?

Find out how to install the Galasa Eclipse plug-in from our [Installing the Galasa plug-in](https://galasa.dev/docs/getting-started/installing) documentation.

Other repositories are available via [GitHub](https://github.com/galasa-dev). 

## Contributing

If you are interested in the development of Galasa, take a look at the documentation and feel free to post a question on [Galasa Slack channel](https://galasa.slack.com) or raise new ideas / features / bugs etc. as issues on [GitHub](https://github.com/galasa-dev/projectmanagement).

Take a look at the [contribution guidelines](https://github.com/galasa-dev/projectmanagement/blob/main/contributing.md) and the [Contributor's Guide](https://github.com/galasa-dev/galasa/blob/main/CONTRIBUTING.md).

## How to build locally
Use the `build-locally.sh` script to build this code locally.

Environment variable over-rides:

- `LOGS_DIR` - Optional. Where logs are placed. Defaults to creating a temporary directory.
- `SOURCE_MAVEN` - Optional. Where a maven repository is from which the build will draw artifacts.
- `DEBUG` - Optional. Defaults to 0 (off)
- `GPG_PASSPHRASE` - Used to sign and verify artifacts during the build


## How to use the plugin
Here we discuss how to use the maven plugin when building Galasa test projects.

### Building a test catalog for a Java bundle

This goal causes a test catalog to be constructed for all the tests in the child bundles of this maven project.

Goal: `bundletestcat`

Phase: `package`

Input Parameters/Properties:
- `galasa.skip.bundletestcatalog` required.

Output:
A test catalog file is generated holding references to all the test classes.

Example:
```
<plugin>
    <groupId>dev.galasa</groupId>
    <artifactId>galasa-maven-plugin</artifactId>
    <extensions>true</extensions>
    <executions>
        <execution>
        <id>build-testcatalog</id>
        <phase>package</phase>
        <goals>
            <goal>bundletestcat</goal>
        </goals>
        </execution>
    </executions>
</plugin>
```

### Building an OBR resource

Input Parameters/Properties:
- `galasa.obr.url.type` property = "obrUrlType" optional
- `includeSelf` optional. Default value is `false`

### Publishing a test catalog to the Galasa ecosystem/server

Goal: `deploytestcat`

Phase: `deploy`

Input Parameters/Properties:
- `galasa.test.stream` required. A string.
- `galasa.token` optional. An access token for the galasa ecosystem, if that ecosytem is using authentication.
- `galasa.bootstrap` required. A URL to the ecosystem.
- `galasa.skip.bundletestcatalog` optional. A boolean. Controls whether the test catalog build is skipped. If set to true then no test catalog is built, so the test catalog is not deployed to the Galasa server.
- `galasa.skip.deploytestcatalog` optional. A boolean. If set to true, the test catalog is not deployed to the Galasa server.

For example:
```
<plugin>
    <groupId>dev.galasa</groupId>
    <artifactId>galasa-maven-plugin</artifactId>
    <extensions>true</extensions>
    <executions>
        ...
        <execution>
            <id>deploy-testcatalog</id>
            <phase>deploy</phase>
            <goals>
                <goal>deploytestcat</goal>
            </goals>
        </execution>
        ...
    </executions>
</plugin>
```

#### Passing a secret token into a maven plugin. Method 1: using ${GALASA_TOKEN}

The `galasa.token` maven property is used by this plugin. 
You can set it using the following in your pom.xml like this:

```
<properties>
    ...
    <galasa.token>${GALASA_TOKEN}</galasa.token>
    ...
</properties>
```

This allows you to call maven and pass the value from the command-line
```
mvn clean install deploy "-DGALASA_TOKEN=${GALASA_TOKEN}"
```

This assumes you have `GALASA_TOKEN` set in your environment.

Note: This method allows the caller of the command-line to pass in 
whatever value they want, from an environment variable (`GALASA_TOKEN` in this case)
or from any other value.

This may be useful if you are deploying to multiple Galasa server environments, 
or switching between tokens used to contact the Galasa Ecosystem.

#### Passing a secret token into a maven plugin. Method 2: using ${env.GALASA_TOKEN}
The `galasa.token` maven property is used by this plugin. 
You can set it using the following in your pom.xml like this:

```
<properties>
    ...
    <galasa.token>${env.GALASA_TOKEN}</galasa.token>
    ...
</properties>
```

This allows you to set the GALASA_TOKEN as an environment variable, and 
the maven plugin for Galasa can pick up the value from the environment.

Note: This causes a tighter 'binding' between your environment and the maven 
build, so all parties using this code need to use the same environment variable
name.

### Supressing the deploy of the test catalog
Use the `galasa.skip.bundletestcatalog` or `galasa.skip.deploytestcatalog` to control whether
the deploy of the test catalog is skipped.

If either of these flags is true, then the publication of the test catalog to the Galasa
ecosystem will be supressed.

For example, to skip both the building of the test catalog and the deployment of it, you can 
add this to your pom.xml:
```
<properties>
    ...
	<galasa.skip.bundletestcatalog>true</galasa.skip.bundletestcatalog>
	<galasa.skip.deploytestcatalog>true</galasa.skip.deploytestcatalog>
    ...
</properties>
```

Or you could pass a `-D` parameter on the command-line:
```
mvn deploy -Dgalasa.skip.deploytestcatalog=true
```

### Merging two test catalogs

Input Parameters/Properties:
- `galasa.skip.bundletestcatalog` optional. A boolean.
- `galasa.build.job` optional. A string.

### Building a gherkin test catalog for Gherkin features

Input Parameters/Properties:
- `galasa.skip.gherkintestcatalog` required. A boolean.


### Building a .zip of gherkin tests

This goal builds a zip file containing all the gherkin feature files.

Goal: `gherkinzip`

Phase: `package`

Input Parameters/Properties:
- `galasa.skip.gherkinzip` required. A boolean.


### Calculating a git commit hash

For example:
```
<plugin>
    <groupId>dev.galasa</groupId>
    <artifactId>galasa-maven-plugin</artifactId>
    <extensions>true</extensions>
    <executions>
        <execution>
            <id>process-resources</id>
            <goals>
                <goal>gitcommithash</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## License

This code is under the [Eclipse Public License 2.0](https://github.com/galasa-dev/galasa/blob/main/LICENSE).