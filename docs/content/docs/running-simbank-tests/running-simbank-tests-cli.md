---
title: "Running the SimBank tests using the CLI"
---

You can explore Galasa further with Galasa Simbank. Galasa Simbank is a simulated bank application that showcases Galasa's functionality within an application. Galasa SimBank comes with a selection of prepared Galasa tests:

- A basic Installation Verification Test (IVT) which logs on to SimBank  - `SimBankIVT.java`.
- A test that updates an account using web services and examines the changes with 3270 screens - `BasicAccountCreditTest.java`.
- A test that uses a provisioned account object to perform the same test as `BasicAccountCreditTest.java` in an improved test design - `ProvisionedAccountCreditTests.java`.
- A test that exercises the z/OS Batch Manager by simulating the submission of a JCL job to add a number of accounts to the SimBank system - `BatchAccountsOpenTest.java`.

The following sections explain how to run the `SimBankIVT` test class by using the CLI. Make sure that you have installed the Galasa CLI tool and Java version 11 JDK, and have set the JAVA_HOME environment variable, as described in the [CLI prerequisites online](../cli-command-reference/cli-prereqs.md) documentation. 


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

The SimBank tests are held in the [Galasa simplatform repository](https://github.com/galasa-dev/simplatform){target="_blank"} in GitHub. To start running the tests you need to clone the repository, if you have not already done so. To find out how to clone the cli repository, follow the instruction in the [Running Galasa SimBank online](../running-simbank-tests/simbank-cli.md) documentation.

After cloning the repository, complete the following steps to run the SimBankIVT test that is provided with Galasa. The following example uses SimBank OBR version `0.43.0` and Galasa uber OBR version `0.43.0`.

You can find the version of the `dev.galasa.simbank.obr` that you are using by looking in the `pom.xml` file in the `dev.galasa.simbank.obr` folder. The `dev.galasa.uber.obr` is the OBR that contains all the bundles that are needed for Galasa to work including Managers, any required dependencies, the framework, etc. The version of the `dev.galasa.uber.obr` depends on which version of Galasa you have installed.

Remember to initialise your local environment by running the `galasactl local init` command. Note that the the following section describes how to run the `./build-locally.sh` and  `./run-locally.sh --server` scripts only in a Mac or Unix environment.


1. Open a terminal, navigate to your `simplatform` directory and run the `./build-locally.sh` script to build the code in both the simbank-tests and the simplaform-application directories.

2. In another terminal run the `./run-locally.sh --server` script to start the simbank server inside a inside a local JVM. In a few seconds, the terminal responds with a series of initialization messages, which on Windows looks like:

    ```
    2019-10-21 14:24:35 INFO dev.galasa.simplatform.main.Simplatform main Starting Simplatform ...
    2019-10-21 14:24:35 INFO dev.galasa.simplatform.db.Database setDerbyHome Setting Derby home to C:\Users\<username>\AppData\Local\Temp\galasaSimplatform1440125512154994774
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.saf.SecurityAuthorizationFacility <init> Creating SAF service
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.application.Bank accountExists Checking if account: 123456789 exists
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.application.Bank accountExists Account doesn't exist
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.application.Bank openAccount Creating account: 123456789
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.application.Bank accountExists Checking if account: 987654321 exists
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.application.Bank accountExists Account doesn't exist
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.application.Bank openAccount Creating account: 987654321
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.saf.SecurityAuthorizationFacility addUser Added user: IBMUSER
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.main.Simplatform main Loading services...
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.listener.Listener <init> Loading service: dev.galasa.simplatform.listener.WebServiceListener listening on port: 2080
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.listener.Listener <init> Loading service: dev.galasa.simplatform.listener.TelnetServiceListener listening on port: 2023
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.main.Simplatform main ... services loaded
    2019-10-21 14:24:36 INFO dev.galasa.simplatform.main.Simplatform main Starting Derby Network server....
    2019-10-21 14:24:37 INFO dev.galasa.simplatform.main.Simplatform main ... Derby Network server started on port 2027
    2019-10-21 14:24:37 INFO dev.galasa.simplatform.main.Simplatform main ... Simplatform started
    ```
    If you are a Mac or Linux user, the messages will be almost identical.

3. In the terminal in which you ran the `./build-locally.sh` script, run the following command to run the SimBankIVT test:
    On Mac or Unix:

    ```shell
    galasactl runs submit local --log - \
    --obr mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr \
    --class dev.galasa.simbank.tests/dev.galasa.simbank.tests.SimBankIVT 
    ```

    On Windows (Powershell):

    ```powershell
    galasactl runs submit local --log - `
    --obr mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr `
    --class dev.galasa.simbank.tests/dev.galasa.simbank.tests.SimBankIVT
    ```

    where:

    - `--log` specifies that debugging information is directed somewhere, and the `-` means that it is sent to the console (stderr).
    - `--obr`  The `--obr` parameter specifies the Maven co-ordinates of the OBR jar file, in the format `mvn:groupId/artifactId/version/classifier`. The first instance specifies the OBR where the  CLI tool can find an OBR which refers to the bundle where the tests are stored. When running locally, all tests must exist in the OBR (or OBRs) that are passed to the tool. The second instance specifies the OBR containing  the bundles that are needed for Galasa to work (for example, Galasa Managers, required dependencies, and the Galasa framework).
    - `--class` specifies which test class to run. The string is in the format of `<osgi-bundle-id>/<fully-qualified-java-class>`.

4. The `SimBankIVT` test class runs, and the terminal displays its progress through to completion, with an Exit code of `0`.

5. View the results of the test runs in your terminal. You can also view results in the `run.log` file in the result archive store (RAS). 3270 terminal interactions can be viewed in the `artifacts` directory in the RAS. Find out more in the [Viewing test results locally](../cli-command-reference/viewing-test-results-cli.md) documentation. 

To run other SimBank tests, for example `BasicAccountCreditTest`, replace the test class name in the `--class` parameter. For example: 

On Mac or Unix:

```shell
galasactl runs submit local --log - \
--obr mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr \
--class dev.galasa.simbank.tests/dev.galasa.simbank.tests.BasicAccountCreditTest 
```

On Windows (Powershell):

```powershell
galasactl runs submit local --log - `
--obr mvn:dev.galasa/dev.galasa.simbank.obr/0.43.0/obr `
--class dev.galasa.simbank.tests/dev.galasa.simbank.tests.BasicAccountCreditTest
```



## Next steps

Explore the SimBankIVT test and the other SimBank tests in the [Exploring Galasa SimBank](./index.md) sections. Follow the flow of logic in these classes and understand more about the Java that is used to create them, including how to use Galasa annotations and review documented test methods.
