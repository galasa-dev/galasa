# Galasa Framework
This repository contains the code for Galasa's core framework. The framework orchestrates all component activities, and co-ordinates with the test runner to execute your tests. 
Code that is required for the lifecycle of a test, including framework initialisation steps to bring up appropriate Managers and the test runner is stored here. The repository also contains the k8s controller which is used to run tests in automation on a Kubernetes cluster.
It is unlikely that you will need to change the framework during the normal range of testing activities.

## Documentation

More information can be found on the [Galasa Homepage](https://galasa.dev). Questions related to the usage of Galasa can be posted on the <a href="https://galasa.slack.com" target="_blank"> Galasa Slack channel</a>. If you're not a member of the Slack channel yet, you can <a href="https://join.slack.com/t/galasa/shared_invite/zt-ele2ic8x-VepEO1o13t4Jtb3ZuM4RUA" target="_blank"> register to join</a>.

## Where can I get the latest release?

Find out how to install the Galasa Eclipse plug-in from our [Installing the Galasa plug-in](https://galasa.dev/docs/getting-started/installing) documentation.

Other repositories are available via [GitHub](https://github.com/galasa-dev). 

## Contributing

If you are interested in the development of Galasa, take a look at the documentation and feel free to post a question on [Galasa Slack channel](https://galasa.slack.com) or raise new ideas / features / bugs etc. as issues on [GitHub](https://github.com/galasa-dev/projectmanagement).

Take a look at the [contribution guidelines](https://github.com/galasa-dev/projectmanagement/blob/main/contributing.md) and the [Contributor's Guide](https://github.com/galasa-dev/galasa/blob/main/CONTRIBUTING.md).

## Build locally
Use the `build-locally.sh` script. 
See the comments at the top of the script for options you can use and a list of environment variables you can override.

## Configuration of the framework component
When the framework runs, it requires some level of configuration to run.

### Environment Variables
Environment variables are set in several ways. In unix systems use `export X=Y`. In Windows use `set X=Y` or use the user interface to set values. 
Here are the environment variables used by the framework component:
- `GALASA_HOME` - holds the path which should be used in preference to the `${HOME}/.galasa` location. Optional. This setting is overridden by the system property of the same name. Defaults to `${HOME}/.galasa` if not specified. For example: /mygalasahome

### System Properties
System properties are passed to the framework when the JVM is invoked using the `-D{NAME}={VALUE}` syntax. 
Here are the system properties which the framework understands:

- `GALASA_HOME` - holds the path which should be used in preference to the `${HOME}/.galasa` location. Optional. This setting overrides 
the environment variable of the same name, which in turn overrides the default of `${HOME}/.galasa` if not specified. 

## Galasa Boot

Galasa Boot is a JAR file that can be used to launch Galasa in various modes. See below for the syntax and available options for invoking this JAR file.

After building the framework module locally, the boot.jar will be located in `galasa/modules/framework/galasa-parent/galasa-boot/build/libs/galasa-boot-{galasa-version}.jar`

### Syntax

To run Galasa Boot, you must have Java 11 or above installed.

```
java -jar /path/to/galasa/galasa-boot-{version}.jar [OPTIONS]
```
See below for the available flags that can be passed in to the Galasa Boot JAR:

| Option | Description |
|--------|-------------|
| --obr |  The OBR for Galasa to load in the format `mvn:/{group-id}/{artifact-id}/{version}/obr`. |
| --bootstrap | A bootstrap properties URL. Should start with 'http://' or 'file://'. If omitted, no bootstrap properties will be loaded from an external source. |
| --overrides | An overrides properties URL used when running a Galasa test. Should start with 'http://' or 'file://'. If omitted, no override properties will be loaded. |
| --resourcemanagement | Starts the Galasa system resource monitor service. |
| --k8scontroller | Starts the Galasa Kubernetes-based engine controller service. |
| --api | Starts the Galasa REST API server. |
| --metricserver | Starts the Galasa metrics service. |
| --test | A test for Galasa to run. The format is {osgi-bundle-name}/{java-class-name}. Java class names are fully qualified. No .class suffix is needed. |
| --run | The run name that should be associated with the test being run. |
| --gherkin | A Gherkin test for Galasa to run. Should start with 'file://'. |
| --bundle | Extra bundles to load. Can be provided multiple times to load multiple extra bundles. |
| --metrics | The port the metrics server will open, 0 to disable. |
| --health | The port the health server will open, 0 to disable. |
| --localmaven | The local maven repository URL, defaults to ~/.m2/repository. |
| --remotemaven | Remote maven repository URLs, defaults to Maven central. |
| --trace | Enables TRACE logging. |
| --file | File for data input/output. |
| --dryrun | Perform a dry-run of the specified actions. Can be combined with --file. |
| --setupeco | Sets up the Galasa Service. |
| --validateeco | Checks that the Galasa Service is set up correctly by submitting a CoreManagerIVT test run to the service. |

## Testing locally
See [test-api-locally.md](./test-api-locally.md) for instructions on how to set up your environment to test the API locally.

## License
This code is under the [Eclipse Public License 2.0](https://github.com/galasa-dev/galasa/blob/main/LICENSE).

## Developer setup instructions
See the developer instructions [here](./dev-instructions.md)

## Design Notes on the Framework component
See [the framework design notes](./docs/design/design-intro.md).