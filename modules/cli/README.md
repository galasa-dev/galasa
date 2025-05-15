# Galasa CLI

The Galasa command line interface (Galasa CLI) is used to interact with the Galasa ecosystem or local development environment.

[![Main build](https://github.com/galasa-dev/cli/actions/workflows/build.yml/badge.svg)](https://github.com/galasa-dev/cli/actions/workflows/build.yml)

## Environment variables

### GALASA_BOOTSTRAP
Most commands will need a reference to the Galasa bootstrap file or url.  This can be provided with the `--bootstrap` flag or the `GALASA_BOOTSTRAP` environment variable. If both are provided, the explicit flag takes precedence.

### GALASA_HOME
`galasactl` commands assume that `${HOME}/.galasa` is a folder which is writeable, and contains property files, and storage for test results which are run using local JVMs.

This value can be overridden using the `GALASA_HOME` environment variable.

The `--galasahome` command-line flag can override the `GALASA_HOME` environment variable on a call-by-call basis.

Note: If you change this to a non-existent and/or non-initialised folder path, then 
you will have to create and re-initialise the folder using the `galasactl local init` command again. That command will respect the `GALASA_HOME` variable and will create the folder and initialise it were it not to exist.

### GALASA_TOKEN
In order to authenticate with a Galasa service, you will need to create a personal access token from the Galasa web user interface.

Once a personal access token has been created, you can either store the token in the galasactl.properties file within your Galasa home folder, or set the token as an environment variable named `GALASA_TOKEN`.


## Syntax
The syntax is documented in generated documentation [here](docs/generated/galasactl.md)


## Setting up your environment
Run this command to set up your environment for future calls to the galasactl tooling.
```
galasactl local init
```

If you wish to build content which depends on the very latest/bleeding-edge of galasa code, 
add the `--development` flag. This will set things up to include the maven repositories 
in any new {HOME}/.m2/settings.xml file, if that doesn't already exist.

This command respects the GALASA_HOME environment variable, and will set up a number of files
there, or in {HOME}/.galasa otherwise.


## Creating an example project

`galasactl` can be used to create near-empty test projects to lay-down an initial structure 
prior to fleshing out with more tests. This can provide a boost to productivity when 
starting a new OSGi bundle containing Galasa tests.

### Examples

Getting help:-

```
galasactl --help
galasactl project --help
galasactl project create --help
```

Create a folder tree which can be built into an OSGi bundle (without an OBR):
```
galasactl project create --package dev.galasa.example.banking
```

Create a folder tree which can be built into an OSGi bundle (with an OBR):
```
galasactl project create --package dev.galasa.example.banking --obr
```

Create a folder tree which has two bundles, each aiming to test different features of an application
(while also viewing the tooling log on the console)
```
galasactl project create --package dev.galasa.example.banking --features payee,account --obr --log -
```


### Building the example project

Maven and Gradle are both build tools, which read metadata from files which guide how the code within a module should be built. Maven and Gradle use different formats for these build files.

By default, the `galasactl project create` generates a project which includes a Maven build mechanism. The `--maven` flag being present also explicitly tells the tool to generate Maven build artifacts (`pom.xml` files).

To create a project which includes a Gradle build mechanism, add the `--gradle` flag. This tells the tool to add generated artifacts which direct a Gradle build.

You can use the `--maven` and `--gradle` flags together to produce a project which contains both Maven and Gradle build infrastructure files.

Create a folder tree which can be built using either Maven or Gradle:
```
galasactl project create --package dev.galasa.example.banking --features payee,account --obr --gradle --maven
```

To build a project with Maven artifacts, use `mvn clean install`

To build a project with Gradle artifacts, use `gradle build publishToMavenLocal`

If you wish the generated code to depend upon the very latest/bleeding-edge of galasa code, then add the `--development` flag. This will add extra settings to the `settings.gradle` file of the parent and the `build.gradle` file of any of the child test projects.

## auth login

Before interacting with a Galasa ecosystem using `galasactl`, you must be authenticated with it. The `auth login` command allows you to log in to an ecosystem provided by your `GALASA_BOOTSTRAP` environment variable or through the `--bootstrap` flag.

Prior to running this command, you must have a `galasactl.properties` file in your `GALASA_HOME` directory, which is automatically created when running `galasactl local init`, that contains a `GALASA_TOKEN` property with the following format:

```
GALASA_TOKEN=<your personal access token>
```

A value for the `GALASA_TOKEN` property can be retrieved by creating a new personal access token from a Galasa ecosystem's web user interface.

If you prefer, this property can be set as an environment variable instead of being read from this file.

On a successful login, a `bearer-token.json` file will be created in your `GALASA_HOME` directory. This file will contain a bearer token that `galasactl` will use to authenticate requests when communicating with a Galasa ecosystem.

If your bearer token expires, `galasactl` will automatically attempt to re-authenticate with your Galasa ecosystem. Alternatively, you can run the `auth login` command again to re-authenticate with your Galasa ecosystem.

### Examples

Logging in to an ecosystem:

```
galasactl auth login
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_auth_login.md).


## auth logout

To log out of a Galasa ecosystem using `galasactl`, you can use the `auth logout` command. If you run a `galasactl` command that interacts with an ecosystem while logged out, `galasactl` will attempt to automatically log in using the properties in your `galasactl.properties` file within your `GALASA_HOME` directory.

### Examples

Logging out of an ecosystem:

```
galasactl auth logout
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_auth_logout.md).


## auth tokens get
Tokens, auth tokens or personal access tokens, enable a user to be authenticated with a Galasa Ecosystem before interacting with it. This command allows a user to see details of all tokens authenticated with a Galasa Ecosystem.

Before running this command, it is advised to run the `auth tokens logout` and then `auth tokens login` commands (as seen above).

### Examples

```
> galasactl auth tokens get
tokenid                   created(YYYY-MM-DD) user     description
098234980123-1283182389   2023-12-03          mcobbett So I can access ecosystem1 from my laptop.
8218971d287s1-dhj32er2323 2024-03-03          mcobbett Automated build of example repo can change CPS properties
87a6sd87ahq2-2y8hqwdjj273 2023-08-04          savvas   CLI access from vscode

Total:3
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_auth_tokens_get.md).


## auth tokens delete

This command revokes a personal access token identified by the given token ID. This command is useful if you have lost access to your personal access token or if your token has been compromised, and you wish to prevent it from being used maliciously.

To retrieve a list of available personal access tokens that have been created and their token IDs, see [auth tokens get](#auth-tokens-get).

### Examples

Revoking a token with ID 'myId'

```
galasactl auth tokens delete --tokenid myId
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_auth_tokens_delete.md).



## runs prepare

The purpose of `runs prepare` is to build a portfolio of tests, possibly from multiple test streams.  This portfolio can then be used in the `runs submit` command.

### Examples

Getting help:-

```
galasactl --help
galasactl runs --help
galasactl runs prepare --help
```

Finding out the version of the galasactl tool:-
```
galasactl --version
```

Will yield output such as this:
```
galasactl version 0.20.0-alpha-2305ba574524af5cd0fba59de18411582f470de5
```


Selecting tests from a test steam:-

```
galasactl runs prepare
          --portfolio test.yaml
          --stream inttests
          --package test.package.one
          --package test.package.two
```

Selecting tests using regex:-

```
galasactl runs prepare
          --portfolio test.yaml
          --stream inttests
          --package '.*age.*'
          --regex
```


Providing test specific overrides:-

```
galasactl runs prepare
          --portfolio test.yaml
          --stream inttests
          --package test.package.one
          --override zos.default.lpar=MYLPAR
          --override zos.default.cluster=MYPLEXCLUSTER
```

Building a portfolio over mulitple selections and overrides:-

```
galasactl runs prepare
          --portfolio test.yaml
          --stream inttests
          --package test.package.one
          --override zos.default.lpar=MYLPAR
          --override zos.default.cluster=MYPLEXCLUSTER

galasactl runs prepare
          --portfolio test.yaml
          --append
          --stream inttests
          --package test.package.two
          --override zos.default.lpar=MYLPARB
          --override zos.default.cluster=MYPLEXCLUSTER
```

## runs submit

The purpose of `runs submit` is to submit and monitor tests in the Galasa ecosystem.  Tests can be input from a portfolio or using the same commands as the `runs prepare` command, but not both.

Note: The `--log -` directs logging information to the stderr console.
Omit this option if you do not want to see logging, or specify `--log myFileName.txt` if you wish 
to capture log information in a file.

### Examples

Getting help:-

```
galasactl --help
galasactl runs --help
galasactl runs runs --help
```

Running tests from a portfolio (and see the log on the console):-

```
galasactl runs submit --log -
          --portfolio test.yaml
          --poll 5
          --progress 1
          --throttle 5
```

Submitting tests without a portfolio (and see the log on the console) :-

```
galasactl runs submit --log -
          --class test.package.one/Test1
          --class test.package.one/Test2
```

Providing overrides for all tests during this run, note, overrides in the portfolio will take precedence over these
(and see the log on the console) :-

```
galasactl runs submit --log -
          --portfolio test.yaml
          --override zos.default.lpar=MYLPAR
          --override zos.default.cluster=MYPLEXCLUSTERA
```

## runs submit local

This command sequence causes the specified tests to be executed within the local JVM server.

Note: Runnning test running locally does not benefit from the features of running within a Galasa Ecosystem,
such as cleaning up resources when things fail and arbitrating contention for limited resources 
between competing tests. It should only be used during test development to verify that the test is 
behaving correctly.

### Example : Run a single test in the local JVM.
```
galasactl runs submit local --log -
          --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr
          --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount
```

### Example : Run a single Gherkin test in the local JVM.
```
galasactl runs submit local --log -
          --gherkin file:///path/to/gherkin/file.feature
```

- The --log - parameter indicates that debugging information should be sent to the console.
- The --obr indicates where galasactl can find an OBR which refers to the bundle where all the tests are housed.
- The --class parameter tells galasactl which test class to run. The string is in the format of `<osgi-bundle-id>/<fully-qualified-java-class>`. All the test methods within the class will be run. You can use multiple such flags to test multiple classes.
- The `JAVA_HOME` environment variable should be set to refer to the JVM to use in which the test will be launched.
- The --localmaven parameter tells galasactl where galasa bundles can be loaded from on your local file system. Defaults to your home .m2/repository file. Please note that this should be in a URL form e.g. `file:///Users/myuserid/.m2/repository`.

- The --gherkin parameter tells galasactl where gherkin files can be loaded from on your local file system. Please note that this should be in a URL form ending in a `.feature` extension e.g. `file:///Users/myuserid/gherkin/MyGherkinFile.feature`.

- The `--throttle 1` option would mean all your tests run sequentially. A higher throttle value means that local tests run in parallel.

To configure a JVM with special options, such as `-Xms20m` and other JVM options, you can set the optional parameter `framework.jvm.local.launch.options` in your bootstrap properties to hold a space-separated list of extra options which will be used when the JVM running your test in a local JVM is launched.

### Debugging a single test which runs in the local JVM
The `galasactl runs submit local` command has an option `--debug` which causes the test to be launched in 'debug mode'.
The test will attempt to connect with a JDB java debugger based on some configuration parameters.

The 'port' used to connect the testcase to the java debugger needs to be configured.
- The default value is 2970
- The above value can be overridden by adding the optional property `galasactl.jvm.local.launch.debug.port` into the `bootstrap.properties` file.
  - For example: `galasactl.jvm.local.launch.debug.port=2971`
  - This parameter is ignored if the `--debug` argument isn't supplied to the `galasactl runs submit local` command.
- The above value can be overridden by using the optional argument `--debugPort` to the `galasactl runs submit local` command.

The port value itself must be an unsigned integer.

Your IDE would typically need to be configured to connect to the same port the testcase is using.

The 'mode' used to control the connection from the local JVM to the debugger can be `listen` or `attach`.
- `listen` configured into the galasactl configuration means that the JVM launching opens a port and pauses to listen for traffic on that port, 
waiting for a JDB debugger to connect to that port.
- `attach` configured into the galasactl configuration means that the JVM launching attempts to attach to the debug port, which has previously been opened
by the JDB debugger.

Configure the debug mode like this:
- The default value is `listen`.
- The above default value can be overridden by adding the optional property `galasactl.jvm.local.launch.debug.mode` into the `bootstrap.properties` file.
  - For example: `galasactl.jvm.local.launch.debug.mode=attach`
- The above value can be overridden by using the optional argument `--debugMode` to the `galasactl runs submit local` command.

Your IDE would typically need to be configured with the opposite type of connection mode in order to attach the JDB debugger to the running Galasa test.
For example: If your `galasactl` is configured to `listen`, then start the test first, and configure your IDE to attach to the same port.
If your `galasactl` is configured to `attach`, then start your JDB debugger first, so it is there waiting for the testcase to attach to the debug port when 
the testcase is launched.

To configure the different IDEs to connect to a local testcase in `--debug` mode, follow these instructions:
- For Microsoft vscode see [here](./docs/vscode/debug_in_vscode.md)
- For IntelliJ see [here](./docs/intellij/debug_in_intellij.md)
- For Eclipse see [here](./docs/eclipse/debug_in_eclipse.md)


## runs get
This command retrieves information about a historic run on an ecosystem.
Several formats are supported including: 'summary', 'details', 'raw' 
```
galasactl runs get --name C1234 --format details
```
For a complete list of supported formatters try running the command with a known to be bad formatter name. For example:
```
galasactl runs get --name C1234 --format badFormatterName
```
For a complete list of supported parameters see [here](./docs/generated/galasactl_runs_get.md).

## runs delete

This command deletes a test run from an ecosystem's RAS. The name of the test run to delete can be provided to delete it along with any associated artifacts that have been stored.

### Examples

A run named "C1234" can be deleted using the following command:

```
galasactl runs delete --name C1234
```

A complete list of supported parameters for the `runs delete` command is available [here](./docs/generated/galasactl_runs_delete.md)

## runs download

This command downloads all artifacts for a test run that are stored in an ecosystem's RAS.
The artifacts are stored in a directory within the working directory where the command is executed. The name of the created directory corresponds to the run name that was provided (e.g. `./C123/...`).

### Examples

All artifacts for a run named "C1234" can be downloaded to a directory named "C1234" in the current working directory using the following command:

```
galasactl runs download --name C1234
```

If a run directory named "C1234" already exists, artifacts stored within the directory can be overwritten with the `--force` flag as follows:

```
galasactl runs download --name C1234 --force
```

By default, all directories containing test run artifacts will be created as children of the current directory ('.'), 
this can be overridden using the `--destination` option.
For example: The command below downloads artifacts and places them in folder `/Users/me/my/folder/C1234`
```
galasactl runs download --name C1234 --destination /Users/me/my/folder
```


A complete list of supported parameters for the `runs download` command is available [here](./docs/generated/galasactl_runs_download.md).


## runs reset

This command will reset a running test in the Ecosystem that is either stuck in a timeout condition or looping, by requeing the test. Note: The reset command does not wait for the server to complete the act of resetting the test, but if the command succeeds, then the server has accepted the request to reset the test.


## Example

The run "C1234" can be reset using the following command:

```
galasactl runs reset --name C1234
```


## runs cancel

If after running `runs reset` the test is still not able to run through successfully, it can be abandoned with `runs cancel`.

This command will cancel a running test in the Ecosystem. It will not delete any information that is already stored in the RAS about the test, it will only cancel the execution of the test. Note: The cancel command does not wait for the server to complete the act of cancelling the test, but if the command succeeds, then the server has accepted the request to cancel the test.

## Example

The run "C1234" can be cancelled using the following command:

```
galasactl runs cancel --name C1234
```

## monitors set

This command can be used to update a monitor in the Galasa service. The name of the monitor to be enabled must be provided using the `--name` flag.

### Examples

To enable a monitor named "myCustomMonitor":

```
galasactl monitors set --name myCustomMonitor --is-enabled true
```

To disable a monitor named "myCustomMonitor":

```
galasactl monitors set --name myCustomMonitor --is-enabled false
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_monitors_set.md).

## monitors get

This command can be used to get the details of monitors, like resource cleanup monitors, that are available in the Galasa service.

By default, the output of `monitors get` will be in a "summary" format. To change the output format, you can supply the `--format` flag followed by the format that you wish the output to be displayed in. For a list of supported formats, view the command's help information by running `galasactl monitors get --help`.

### Examples

To get a list of all monitors that are currently available in the Galasa service, run the following command:

```
galasactl monitors get 
```

If you would like to get a specific monitor, you can supply the name of the monitor with the `--name` flag. For example, the following command can be used to get a monitor named "myCustomMonitor":

```
galasactl monitors get --name myCustomMonitor
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_monitors_get.md).

## properties get
This command retrieves details of properties in a namespace.

Properties in a namespace can be filtered out by using `--prefix`, `--infix` and/or `--suffix`, or `--name`.
The formats supported are: 'summary', 'raw', and 'yaml'. The default is 'summary'.

For a complete list of supported formatters try running the command with a known to be bad formatter name. For example:
```
galasactl properties get --namespace framework --format badFormatterName
```
### Examples
`--prefix`, `--infix` and `--suffix` can be used together or separately to get all properties with a matching prefix, infix and/or suffix.
```
galasactl properties get --namespace framework --prefix test
```
```
galasactl properties get --namespace framework --infix galasa --suffix test
```
```
galasactl properties get --namespace framework --prefix test --infix galasa --suffix stream
```

`--name` is used to get a singular property
```
galasactl properties get --namespace framework --name propertyName
```

For a complete list of supported formatters try running the command with a known to be bad formatter name. For example:
```
galasactl properties get --name propertyName --format badFormatterName
```
For a complete list of supported parameters see [here](./docs/generated/galasactl_properties_get.md).

`--format` is used to modify the output table
```
> galasactl properties get --namespace framework --format summary
namespace name          value
framework propertyName0 value0
framework propertyName1 value1
>
```
```
> galasactl properties get --namespace framework --format raw
framework|propertyName0|value0
framework|propertyName1|value1
>
```
```
> galasactl properties get --namespace framework --format yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: propertyName0
data:
    value: value0
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: validNamespace
    name: propertyName1
data:
    value: value1
>
```

## properties set
This command attempts to update the value of a property in a namespace, but if the property does not exist in that namespace, it creates the property.

The property to be set is supplied through `--name` and its value through `--value`.
### Examples
```
galasactl properties set --namespace framework --name propertyName --value propertyValue
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_properties_set.md).


## properties delete
This command deletes a property in a namespace.

The property to be deleted is supplied through `--name`.
### Examples
```
galasactl properties delete --namespace framework --name propertyName
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_properties_delete.md).


## properties namespaces get
This command retrieves details of namespaces in the CPS.

The formats supported are: 'summary' and 'raw'. The default is 'summary'.

For a complete list of supported formatters try running the command with a known to be bad formatter name. For example:
```
galasactl properties namespaces get --format badFormatterName
```

### Examples

```
galasactl namespaces properties get
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_properties_namespaces_get.md).

`--format` is used to modify the output table
```
> galasactl namespaces properties get --format summary
namespace name          value
framework propertyName0 value0
framework propertyName1 value1
>
```
```
> galasactl namespaces properties get --format raw
framework|propertyName0|value0
framework|propertyName1|value1
>
```



## resources apply
This command creates or updates resources in the Galasa Ecosystem

For each resource provided in the yaml file, it is either created, if it doesn't already exist, or updated if it already exists. A compiled list of errors is returned if any error occurs for any resource during the process.

### Examples
```
galasactl resources apply -f my_resources.yaml
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_resources_apply.md).

## resources create
This command creates resources in the Galasa Ecosystem

Each resource provided in the yaml file is created. A compiled list of errors is returned if any error occurs for any resource during the process.

### Examples
```
galasactl resources create -f my_resources.yaml
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_resources_create.md).

## resources update
This command updates resources in the Galasa Ecosystem

Each resource provided in the yaml file is updated. A compiled list of errors is returned if any error occurs for any resource during the process.

### Examples
```
galasactl resources update -f my_resources.yaml
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_resources_update.md).

## resources delete
This command deletes resources in the Galasa Ecosystem

Each resource provided in the yaml file is deleted. A compiled list of errors is returned if any error occurs for any resource during the process.

### Examples
```
galasactl resources delete -f my_resources.yaml
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_resources_delete.md).

## secrets get

This command retrieves a list of secrets stored in the Galasa Ecosystem's credentials store. The retrieved secrets can be displayed in different formats, including `summary` and `yaml` formats, based on the value provided by the `--format` flag. If `--format` is not provided, secrets will be displayed in the `summary` format by default.

### Examples

All secrets stored in a Galasa Ecosystem can be retrieved using the following command:

```
galasactl secrets get
```

To get a specific secret named `SYSTEM1`, the `--name` flag can be provided as follows:

```
galasactl secrets get --name SYSTEM1
```

To display a secret in a different format, like YAML, the `--format` flag can be provided:

```
galasactl secrets get --name SYSTEM1 --format yaml
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_secrets_get.md).

## secrets set

This command can be used to create and update secrets in the Galasa Ecosystem. These secrets can then be used in Galasa tests to authenticate with test systems and perform other secure operations. The name of a secret to create or update must be provided using the `--name` flag.

### Examples

The `--username`, `--password`, and `--token` flags can be used in different combinations to create different types of secret.

For example, a UsernamePassword secret can be created by supplying `--username` and `--password`:

```
galasactl secrets set --name SYSTEM1 --username "my-username" --password "my-password"
```

A UsernameToken secret can be created by supplying `--username` and `--token`:

```
galasactl secrets set --name SYSTEM1 --username "my-username" --token "my-token"
```

A Token secret can be created by supplying `--token` on its own:
```
galasactl secrets set --name SYSTEM1 --token "my-token"
```

A Username secret can be created by supplying `--username` on its own:

```
galasactl secrets set --name SYSTEM1 --username "my-username"
```

Base64-encoded credentials can be supplied using the `--base64-username`, `--base64-password`, and `--base64-token` flags.

For example, to create a UsernamePassword secret where both the username and password are base64-encoded:

```
galasactl secrets set --name SYSTEM1 --base64-username "my-base64-username" --base64-password "my-base64-password"
```

It is also possible to mix these flags with their non-encoded variants discussed previously. For example, to create a UsernameToken secret where only the token is base64-encoded:

```
galasactl secrets set --name SYSTEM1 --username "my-base64-username" --base64-token "my-base64-token"
```

Once a secret has been created, you can change the type of the secret by supplying your desired secret type using the `--type` flag. When supplying the `--type` flag, all credentials for the new secret type must be provided. To find out what secret types are supported, run `galasactl secrets set --help`.

For example, to create a UsernamePassword secret and then change it to a Token secret:

```
galasactl secrets set --name SYSTEM1 --username "my-username" --password "my-password"
galasactl secrets set --name SYSTEM1 --token "my-token" --type Token
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_secrets_set.md).

## secrets delete

This command deletes a secret with the given name from the Galasa Ecosystem's credentials store. The name of the secret to be deleted must be provided using the `--name` flag.

### Examples

To delete a secret named `SYSTEM1`, run the following command:

```
galasactl secrets delete --name SYSTEM1
```

For a complete list of supported parameters see [here](./docs/generated/galasactl_secrets_delete.md).

## roles get
To list the roles which are available on a Galasa service.

Note: Roles are currently read-only and cannot be used in conjunction with the `galasactl resources apply -f` or similar commands at this time.

### Examples
```
> galasactl roles get
name        description
admin       Administrator access
deactivated User has no access
tester      Test developer and runner

Total:3
```

To get a named role in yaml format
```
>galasactl roles get --name admin --format yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaRole
metadata:
    id: "2"
    name: admin
    description: Administrator access
    url: http://prod1-galasa-dev.cicsk8s.hursley.ibm.com/rbac/roles/2
data:
    actions:
        - GENERAL_API_ACCESS
        - SECRETS_GET
        - USER_ROLE_UPDATE_ANY
```

## users
A deployed Galasa service has a number of users on the system. These can be queried:

```
> galasactl users get 
login-id               role   web-last-login(UTC) rest-api-last-login(UTC)
user.one@mydomain.com  tester 2025-01-13 15:33
Jade@mydomain.com      admin  2025-01-13 15:33    2025-01-16 10:47
mikec@mydomain.com     admin  2025-01-13 15:33    2025-01-16 16:20

Total:3
```

If you only want get details about a single user:
```
> galasactl users get --login-id mikec@mydomain.com
login-id               role   web-last-login(UTC) rest-api-last-login(UTC)
mikec@mydomain.com     admin  2025-01-13 15:33    2025-01-16 16:20

Total:1
```

An administrator can change the role of a user:
```
> galasactl users set --login-id user.one@mydomain.com --role tester
```

## Reference Material

### Syntax
Full syntax, with descriptions of every parameter is available [here](./docs/generated/galasactl.md)

### Errors
See [here](./docs/generated/errors-list.md) for a list of error messages the `galasactl` tool can output.

### Known limitations
- Go programs can sometimes struggle to resolve DNS names, especially when a working over a virtual private network (VPN). In such situations, you may notice that a bootstrap file cannot be found with `galasactl` but can be found by a desktop browser, or curl command.
In such situations we recommend that you manually add the host detail to the `/etc/hosts` file, 
to avoid DNS being involved in the resolution mechanism.

## Building locally
To build the cli tools locally, use the `./build-locally.sh --help` script for instructions.

## Built artifacts
Download built artifacts:

Built artifacts include:

- galasactl-darwin-x86_64 
- galasactl-darwin-arm64
- galasactl-linux-x86_64 
- galasactl-linux-s390x 
- galasactl-windows-x86_64.exe

Browse the following web site and download whichever built binary files you wish:

- Latest (and previous) stable releases: https://github.com/galasa-dev/cli/releases
- Bleeding edge/Unstable : https://development.galasa.dev/main/binary/cli/

## Docker images containing the command-line tools
The build process builds some docker images with the command-line tools installed.
This could be useful when wishing to embed a usage of the command-line within a build process which can use a docker image.

- Bleeding edge/Unstable : `docker pull harbor.galasa.dev/galasadev/galasa-cli-amd64:main`

### How to use the docker image
The docker image has the `galasactl` tool on the path of the docker image when it starts up.
So, invoke the `galasactl` without installing on your local machine, using the docker image like this:
```
docker run harbor.galasa.dev/galasadev/galasa-cli-amd64:main galasactl --version
```

## Running a test locally, but using shared configuration properties on a remote Galasa server
This configuration is supported. An ecosystem can be set up with CPS (configuration properties store) properties.

The galasactl tool can be configured to communicate with that CPS.

To do this, assuming `https://myhost/api/bootstrap` can be used to 
communicate with the remote server, add the following to your `bootstrap.properties` file, 
```
# Tell the galasactl tool that local tests should use the REST API to get shared configuration properties from a remote server.
# https://myhost/api is the location of the Galasa REST API endpoints.
framework.config.store=galasacps://myhost/api
framework.extra.bundles=dev.galasa.cps.rest
```

The user must perform a `galasactl auth login` to the same ecosystem before trying to launch a local test.

# Gherkin Support
Gherkin support is described [here](./gherkin-docs.md)