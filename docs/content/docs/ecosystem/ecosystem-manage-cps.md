---
title: "Managing configuration properties"
---

[Retrieving namespaces](#retrievingnamespaces)<br>
[Retrieving properties](#retrieving)<br>
[Setting properties](#setting)<br>
[Deleting properties](#deleting)<br>


Integration testing can be complicated. Tests often require configuration parameters to bind to a specific test environment. Galasa Managers require configuration so that systems under test can be contacted. Credentials for systems need to be supplied to tests and Managers to enable connections. The organisation of these parameters must be centralised if tests are to be run in automation. 

To address these challenges, Galasa uses a Configuration Property Store (CPS) as part of the core Galasa framework. The CPS holds all the configuration values, so that the Galasa framework, Ecosystem, Managers, and individual tests can use the CPS to retrieve configuration information.

A video -  <a href="https://www.youtube.com/watch?v=d_mufWVa31U" target="_blank">The Galasa Configuration Property Store </a> - is available on the Galasa YouTube channel. Watch the video to find out more about the features of the Galasa CPS, and to see a demonstration on how to manipulate CPS property values by using the Galasa command line tool.

## Simplifying test configuration with the CPS

When running Galasa tests locally using the command line tool  the CPS is stored in the flat file `{GALASA_HOME}/cps.properties`. 

When running tests in the Galasa Ecosystem, where tests are run on a Kubernetes cluster, a REST service hosts the storage of the CPS properties in a central location. A centralised CPS means that configuration information can be supplied to all automated tests running in a Galasa Ecosystem, allowing multiple tests to run using the same shared configuration information. 

You can set, retrieve and delete properties that are held in the CPS by using the `galasasctl properties` commands. The ability to manage these properties directly makes it easier for testers to set parameters and credentials on the Ecosystem for tests to read and use at runtime. System administrators can use the CLI to set Ecosystem-wide configuration properties after Ecosystem installation.

By default, properties are are always retrieved from the REST service individually during test class runs.
If the amount of network requests to retrieve properties from the CPS causes any problems, you can enable a cache by setting the `framework.cps.rest.cache.is.enabled` property to `true`.
When the cache is enabled, all CPS properties are loaded at the start of a test class run.


## About the Configuration Properties Store 

The CPS is a key-value pair store. Properties in the CPS are dot-separated values with lower and upper-case segments that define, for example, endpoint, port, and repository properties. These properties control how a Galasa test runs.  It is the CPS and the configuration properties that enable tests to run against multiple environments, without changing the code inside the test. 

Naming conventions are used to maintain order in the properties which are stored in the CPS. If a property in the CPS consists of a prefix, suffix, and a variable infix, then the prefix and suffix are lower-case, and the infix part of the property name is upper-case, to indicate that it is variable in nature. This convention allows the CPS to group names together and for easy searching and lookup by tests, Managers and users alike. Properties can be searched by using the prefix, suffix and a list of possible infixes.

Namespaces are used to group properties together within the CPS. Namespaces help to restrict the values that can be drawn from the CPS. For example, test cases draw values only from the `test` namespace. The Galasa framework draws values from the `framework` namespace, for example, the location of the Credentials Store and the Dynamic Status Store. Managers also provide their own configuration properties, for example, the configuration properties of the Docker Manager are held in the `docker` namespace. 

Namespaces are either defined as `normal` or `secure` types. Returned values that are associated with properties in a `secure` namespace type are redacted, so property values are not displayed in returned results. All other namespaces are classed as `normal` type and these types of namespaces do display property values. The `--namespace` flag is mandatory for all `galasasctl properties` commands. 


## <a name="retrievingnamespaces"></a>Retrieving namespaces from an Ecosystem

Use the `galasactl properties namespaces get` command to retrieve a list of all namespaces that are in an Ecosystem. 
You can use the appropriate namespace in the `galasasctl properties get`, `galasasctl properties set`, and `galasasctl properties delete` commands to view, create, update, or delete properties within that namespace. Namespaces are returned in either summary format, by setting the `--format summary` flag, or in raw format, by setting the `--format raw` flag on the `galasactl properties namespaces get` command. 

<b>Table 1:</b> The following table shows the options that you can set on the `galasactl properties namespaces get` command to retrieve namespaces in different formats:

| Name |  Description  |
| :---- | :-------- | 
| `--format summary` | The default format is _summary_. Summary format is useful if you need a quick, high-level view. If you omit the `--format` flag in the command, results are returned in summary format. You can set the summary format explicitly by setting the `--format summary` flag in the `galasactl properties namespaces get` command.   | 
| `--format raw` |  The output from `galasactl properties namespaces get` is returned in a form that makes it easy for scripting to digest the individual pieces of data available. Returned values are separated by pipes, without formatting or header information.| 



Use the following command to retrieve all namespaces in an Ecosystem `summary` format:

```
galasactl properties namespaces get
```

The following example shows namespaces returned in summary format:

```
name       type
docker     NORMAL
framework  NORMAL
secure     SECURE
Total: 3
```

Use the following command to retrieve all namespaces in an Ecosystem `raw` format:

```
galasactl properties namespaces get --format raw
```

The following example shows namespaces returned in the `raw` format of `namespace|type|`, with pipe delimiters:

```
docker|NORMAL
framework|NORMAL
secure|SECURE
```

Returned properties are sorted in alphabetical order based on the name of the namespace. 


## Managing CPS properties

Use the `galasasctl properties get`, `galasasctl properties set`, and `galasasctl properties delete` commands to dynamically manage CPS properties. These Galasa CLI commands make it easy to ensure that the appropriate properties and credentials are installed in the Ecosystem for automated tests to query and use. 

The example commands that are provided in the following sections assume that the `GALASA_BOOTSTRAP` environment variable is set, so the `--bootstrap` flag is not required in the command. 

## <a name="retrieving"></a>Retrieving properties from a namespace 

Use the `galasactl properties get` command to read CPS properties and values from a specified namespace in the Galasa Ecosystem to verify that the properties exist and are set correctly. You can filter the properties that are returned by using the property name (to return a single property), or by using the prefix, suffix, and infix flags to return a subset of properties that match the provided criteria. 

Property values that are returned from secure namespace types are redacted, so property values are not displayed. Namespaces that are classed as normal type do display property values.

<b>Table 2:</b> The following table shows the options that you can set on the `galasactl properties get` command to retrieve property results in different formats:

| Name |  Description  |
| :---- | :-------- | 
| `--format summary` | The default format is _summary_. Summary format is useful if you need a quick, high-level view. You can set the summary format explicitly by setting the `--format summary` flag in the `galasactl properties get` command. If you omit the `--format` flag in the command, results are returned in summary format.  | 
| `--format yaml` |  If you want to use galasactl to extract a yaml file which describes a property's values, you can set the `--format yaml` flag in the command. This is useful if you want to update a number properties with different values by using a single command.  | 
| `--format raw` |  The output from `galasactl properties get` is returned in a form that makes it easy for scripting to digest the individual pieces of data available. Returned values are separated by pipes, without formatting or header information.| 


### Retrieve properties from a namespace

To retrieve properties that are stored in the `framework` namespace in summary format, use the following command:<br><br>
`galasactl properties get --namespace framework`


### Retrieve a single property from a namespace

To retrieve a specific property from the `framework` namespace, specify the property name in the command by using the `-–name` flag. The following example retrives the `resultarchive.store` property from the framework `namespace` in raw format.

On Mac or Unix:

```
galasactl properties get --namespace framework \
--name resultarchive.store \
--format raw
```

On Windows (Powershell):

```
galasactl properties get --namespace framework `
--name resultarchive.store `
--format raw
```
 

*Note:* The `-–name` flag cannot be used in conjunction with the `-–prefix`, `--suffix`, or `-–infix` flags.

### Retrieve a subset of properties in a namespace

To filter the properties that are returned, without specifying the property name, use the `–-prefix`, `–-suffix`, and ``--infix`` flags. You can specify more than one `--infix` value by using a comma separated list. For example, to return properties in the `docker` namespace that start with `engine`, end with `hostname`, and contain `LOCAL` or `REMOTE` use the following command, which returns the results in a yaml file by specifying the `--format yaml` flag in the command: 


On Mac and Unix:

```
galasactl properties get \
--namespace docker --prefix engine --suffix hostname --infix LOCAL,REMOTE \
--format yaml
```

On Windows (Powershell):

```
galasactl properties get `
--namespace docker --prefix engine --suffix hostname --infix LOCAL,REMOTE `
--format yaml
```


The `--prefix`, `--suffix` and `-–infix` flags can be used together or separately to retrieve all properties that match the provided criteria.


For a complete list of supported parameters see the <a href="https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_properties_get.md" target="_blank"> galasactl properties get</a> documentation in the cli repository.


### Returned properties

Properties are returned in either `summary`, `yaml`, or `raw` format. 

The default format of returned properties is summary format. For example:

```
namespace name                    value
docker    engine.LOCAL.hostname   127.0.0.1
docker    engine.REMOTE.hostname  103.67.89.6

Total: 2
```


The following example shows the format of properties that are returned in a yaml file. As more than one property is returned, the properties are separated in the file by three dashes, `---`, as shown in the following example:

```
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: engine.LOCAL.hostname
  namespace: docker
data:
  value: 127.0.0.1
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: engine.REMOTE.hostname
  namespace: docker
data:
  value: 103.67.89.6
```

The following example shows properties that are returned in `raw` format (`namespace | name | value`):

```
docker|engine.LOCAL.hostname|127.0.0.1|docker|engine.REMOTE.hostname|103.67.89.6|
```

The following example shows a property in a secure namespace type returned in summary format. The returned property value is masked:

```
namespace name               value
secure    property.example   ********

Total: 1
```

## <a name="setting"></a>Setting properties in a namespace

You can update a property and its value in a namespace by using the `galasactl properties set` command. If the property does not exist in that namespace, the command creates the property. You must provide the namespace, the name of the property, and the value of the property in the command in the following example format:

```galasactl properties set --namespace myNamespace --name myName --value myValue```

where: 

 - `namespace` is the namespace in which the property is stored
 - `name` is the name of the property 
 - `value` is the property value 

A success message is displayed when the property is updated or created.

For a complete list of supported parameters see the <a href="https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_properties_set.md" target="_blank"> galasactl properties set</a> documentation in the cli repository.

### Examples of setting properties in a namespace

The [WebAppIntegrationTest](../running-simbank-tests/web-app-integration-test) documentation is designed to help you to understand how to set the properties that enable a local test to run in a Galasa Ecosystem.

For example, the following CPS properties are set for the Docker Manager when running the `WebAppIntegrationTest` locally:

```
docker.dse.engine.PRIMARY=LOCAL
docker.default.engines=LOCAL
docker.engine.LOCAL.hostname=127.0.0.1
docker.engine.LOCAL.port=2375
docker.engine.LOCAL.max.slots=10
```

The first component, `docker` indicates the namespace. The upper-case components are used to show which part of the property is dynamic, while the lower case is used to define the static parts.

You can use the `galasactl properties set` command to update these properties so that the test can run in the Ecosystem. To use a remote server as a Docker Engine, simply change the default engine from LOCAL to REMOTE and specify the appropriate connection details. No change is required to the test code, only to the CPS properties file.

For example, to change the value of the properties in the `docker` namespace, use the following example commands:

```
galasactl properties set --namespace docker --name dse.engine.PRIMARY --value REMOTE
galasactl properties set --namespace docker --name default.engines --value REMOTE
galasactl properties set --namespace docker --name engine.REMOTE.hostname --value 103.67.89.6
galasactl properties set --namespace docker --name engine.REMOTE.port --value 2987
galasactl properties set --namespace docker --name engine.REMOTE.max.slots --value 20
```


## <a name="deleting"></a>Deleting properties from a namespace

You can delete a property and its associated value in a namespace by using the `galasactl properties delete` command. You must provide the namespace and the name of the property that you want to delete.  For example:

```galasactl properties delete --namespace myNamespace --name myName```

where: 

 - `namespace` is the namespace that contains the property to delete
 - `name` is the name of the property you want to delete



A success message is displayed when the property is deleted.

For a complete list of supported parameters see the <a href="https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_properties_delete.md" target="_blank"> galasactl properties delete</a> documentation in the cli repository.



