---
title: "Running the SimBank tests using the CLI offline"
---

You can explore Galasa further with Galasa Simbank. Galasa Simbank is a simulated bank application that showcases Galasa's functionality within an application. Galasa SimBank comes with a selection of prepared Galasa tests:

- A basic Installation Verification Test (IVT) which logs on to SimBank  - `SimBankIVT.java`.
- A test that updates an account using web services and examines the changes with 3270 screens - `BasicAccountCreditTest.java`.
- A test that uses a provisioned account object to perform the same test as `BasicAccountCreditTest.java` in an improved test design - `ProvisionedAccountCreditTests.java`.
- A test that exercises the z/OS Batch Manager by simulating the submission of a JCL job to add a number of accounts to the SimBank system - `BatchAccountsOpenTest.java`.

The following sections explain how to run the `SimBankIVT` test class by using the CLI. Make sure that you have installed the Galasa CLI tool and Java version 11 JDK, and have set the JAVA_HOME environment variable, as described in the [CLI prerequisites offline](../cli-command-reference/zipped-prerequisites.md) documentation. 


## Updating the overrides and credentials property files


In order to run the Galasa SimBanks tests you need to add some configuration information in the `overrides.properties` and `credentials.properties` files that were created when you initialised your Galasa home folder by running the `galasactl local init` command. Complete the following steps to edit the files:
  

1. Edit a file called `overrides.properties` in your `.galasa` folder so that it contains the following configuration properties. Configuration properties held in this file are used by Galasa tests at runtime. You can change the value of the properties that are set in this file to enable you to run tests against different configurations without changing the test code. The following example configuration properties enable the provided Galasa SimBank tests to run on your machine:

    ```properties
    zos.dse.tag.SIMBANK.imageid=SIMBANK
    zos.dse.tag.SIMBANK.clusterid=SIMBANK

    simbank.dse.instance.name=SIMBANK
    simbank.instance.SIMBANK.zos.image=SIMBANK

    zos.image.SIMBANK.ipv4.hostname=127.0.0.1
    zos.image.SIMBANK.telnet.port=2023
    zos.image.SIMBANK.webnet.port=2080
    zos.image.SIMBANK.telnet.tls=false
    zos.image.SIMBANK.credentials=SIMBANK

    zosmf.image.SIMBANK.servers=SIMBANK
    zosmf.server.SIMBANK.image=SIMBANK
    zosmf.server.SIMBANK.port=2040
    zosmf.server.SIMBANK.https=false
    ```

1. Edit a file called `credentials.properties` in your `.galasa` folder. Credentials that are held in this file are used by Galasa tests, for example to pass credentials to the application being tested. Storing values in this file avoids the need to hard-code credentials inside a test class, enabling the test to run in different environments without changing any test code. The following example properties enable the provided Galasa SimBank tests to run on your machine:

    ```properties
    secure.credentials.SIMBANK.username=IBMUSER
    secure.credentials.SIMBANK.password=SYS1
    ```

## Running the SimBank IVT test class by using the CLI

The SimBank tests are located in the `maven` directory of the `isolated.zip` downloadable file. Complete the following steps to run the SimBankIVT test that is provided with Galasa. The following example uses SimBank OBR version `0.41.0`.

Remember to initialise your local environment by running the `galasactl local init` command and to start the SimPlatform server by running the `run-simplatform.sh` script, as described in the [Running Galasa SimBank using the CLI offline](./simbank-cli-offline.md) documentation.


You are now ready to run a local Galasa test offline with just the contents of the zipped distribution.

1. Open a terminal and run the SimBankIVT test locally by using the following example command, remembering to update the `--localMaven` flag value to the location of the `maven` directory that is provided as part of the _isolated.zip_  file that you downloaded:
    On Mac or Unix:

    ```shell
    galasactl runs submit local --log - \
    --obr mvn:dev.galasa/dev.galasa.simbank.obr/0.41.0/obr \
    --class dev.galasa.simbank.tests/dev.galasa.simbank.tests.SimBankIVT \
    --localMaven file:////Users/youruserid/Downloads/isolated/maven
    ```

    On Windows (Powershell):

    ```powershell
    galasactl runs submit local --log - `
    --obr mvn:dev.galasa/dev.galasa.simbank.obr/0.41.0/obr `
    --class dev.galasa.simbank.tests/dev.galasa.simbank.tests.SimBankIVT `
    --localMaven file:////Users/youruserid/Downloads/isolated/maven
    ```

    Note that the `--localMaven` flag refers to the `maven` directory inside the _isolated.zip_ as these are all the Maven artifacts that should be needed to run the test, including the `dev.galasa.simbank.obr` artifact which is passed to the `--obr` flag and the `SimBankIVT` test class which is passed to `class`.

1. The `SimBankIVT` test class runs, and the terminal displays its progress through to completion, with an Exit code of `0`.

1. View the results of the test runs in your terminal. You can also view results in the `run.log` file in the result archive store (RAS). 3270 terminal interactions can be viewed in the `artifacts` directory in the RAS. Find out more in the [Viewing test results locally](../cli-command-reference/viewing-test-results-cli.md) documentation. 


To run other SimBank tests, for example `BasicAccountCreditTest`, replace the test class name in the `--class` parameter. Remember to update the `--localMaven` flag value to the location of the `maven` directory as well. For example: 

On Mac or Unix:

```shell
galasactl runs submit local --log - \
--obr mvn:dev.galasa/dev.galasa.simbank.obr/0.41.0/obr \
--class dev.galasa.simbank.tests/dev.galasa.simbank.tests.BasicAccountCreditTest \
--localMaven file:////Users/youruserid/Downloads/isolated/maven
```

On Windows (Powershell):

```powershell
galasactl runs submit local --log - `
--obr mvn:dev.galasa/dev.galasa.simbank.obr/0.41.0/obr `
--class dev.galasa.simbank.tests/dev.galasa.simbank.tests.BasicAccountCreditTest `
--localMaven file:////Users/youruserid/Downloads/isolated/maven
```


## Next steps

Explore the SimBankIVT test and the other SimBank tests in the [Exploring Galasa SimBank](./index.md) sections. Follow the flow of logic in these classes and understand more about the Java that is used to create them, including how to use Galasa annotations and review documented test methods.
