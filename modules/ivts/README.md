# IVTs

This module contains the Galasa Installation Verification Tests (IVTs) that are written to test the installation and functionality of different Galasa Managers. For example, the `CoreManagerIVT` tests the Galasa Core Manager. Each IVT is itself a Galasa test that uses a Manager to do certain things, then tests that the expected action was completed successfully.

## IVTs in this module

The IVTs are currently in the process of being migrated from within the Managers module to this module. IVTs were previously stored in their own bundles within the Managers module in the same subdirectory as the Manager bundle they tested. However, they are now being moved to this module and added as subprojects of the 'dev.galasa.ivts' bundle to simplify the project's testing.

IVTs that have been moved over and are within this module are:
* CoreManagerIVT: Tests the Core Manager
* ArtifactManagerIVT: Tests the Artifact Manager
* HttpManagerIVT: Tests the HTTP Manager
* DockerManagerIVT: Tests the Docker Manager

## How this module is used

The IVTs contained in this module are built as part of the GitHub Actions build process which builds the test cases, OBR and a test catalog, which altogether represent a test stream called 'ivts'. See the build process in the [.github directory](../../.github/workflows/ivts.yaml).

This test stream is referenced in the CPS properties of a Galasa service which can then run tests from the stream remotely in the Galasa service with `galasactl runs submit`. These tests are run on a daily basis to test the functionality of the Managers and check for any regressions.

## Building locally

To build this module locally, use the `./build-locally` script. This will build the IVT test cases, OBR and test catalog and publish them to your local Maven repository.

## Running locally

Any IVT in this module can also be run locally with `galasactl runs submit local` after building the module locally. You will need to set up certain CPS properties in your local Galasa environment to do this, as the IVTs initialise and use Galasa Managers as part of the test case, which require CPS properties to be set up. 

To find out how to initialise your local environment, see [Initialising your local environment](https://galasa.dev/docs/cli-command-reference/initialising-home-folder) on our website. To learn what CPS properties each Manager needs, see the individual Manager documentation on our [website](https://galasa.dev/docs/managers).

As an example, to run the `CoreManagerIVT` locally, run the following command:
```
galasactl runs submit local \
--obr mvn:dev.galasa/dev.galasa.ivts.obr/0.40.0/obr \
--class dev.galasa.ivts/dev.galasa.ivts.core.CoreManagerIVT \
--log -
```

## Further documentation

For more information on Test streams, see the [Test streams page](https://galasa.dev/docs/manage-ecosystem/test-streams) on our website.

For more information on running tests locally or remotely with `galasactl`, see our [command-line interface documentation](https://github.com/galasa-dev/cli/blob/main/README.md) on GitHub.