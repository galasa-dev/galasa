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

## Testing locally
See [test-api-locally.md](./test-api-locally.md) for instructions on how to set up your environment to test the API locally.

## License
This code is under the [Eclipse Public License 2.0](https://github.com/galasa-dev/galasa/blob/main/LICENSE).

## Developer setup instructions
See the developer instructions [here](./dev-instructions.md)

## Design Notes on the Framework component
See [the framework design notes](./docs/design/design-intro.md).