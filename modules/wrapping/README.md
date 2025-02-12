# Wrapping
This repository wraps Galasa dependencies that are not in an OSGI bundle into OSGI bundles which can be run in Galasa. 

## Documentation

More information can be found on the [Galasa Homepage](https://galasa.dev). Questions related to the usage of Galasa can be posted on [Galasa Slack channel](https://galasa.slack.com)

## Where can I get the latest release?

Find out how to install the Galasa Eclipse plug-in from our [Installing the Galasa plug-in](https://galasa.dev/docs/getting-started/installing) documentation.

Other repositories are available via [GitHub](https://github.com/galasa-dev). 

## Contributing

If you are interested in the development of Galasa, take a look at the documentation and feel free to post a question on [Galasa Slack channel](https://galasa.slack.com) or raise new ideas / features / bugs etc. as issues on [GitHub](https://github.com/galasa-dev/projectmanagement).

Take a look at the [contribution guidelines](https://github.com/galasa-dev/projectmanagement/blob/main/contributing.md).

## License

This code is under the [Eclipse Public License 2.0](https://github.com/galasa-dev/galasa/blob/main/LICENSE).

## Building locally
To build locally, you will need to generate a GPG key to sign the articacts with, so that you can prove 
the source of the artifacts are from you. The Private part of the GPG public-private keypair is used to 
do the signing. The GPG key is typically installed into your keyring/certificate store, and a passphrase/alias is 
used to address the private key. 
When we build artifacts, we sign them prior to deploying them to any external repository, so others can 
tell that the artifacts haven't been tampered-with, and that the source of the artifacts is genuine.
This is known as non-repudiation.

To build locally, use the `./build-locally.sh --help` command to see what other parameters are needed.

See the [documentation for the maven gpg plugin](https://maven.apache.org/plugins/maven-gpg-plugin/usage.html)
for information on other ways of specifying the GPG key to the build process.
