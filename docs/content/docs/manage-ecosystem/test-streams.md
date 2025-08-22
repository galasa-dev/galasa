---
title: "Test streams"
---

Galasa's Ecosystem organises tests into _streams_. Test streams give you more options when it comes to organising your test projects, how often they are built and where they are deployed.

A test stream is a group of tests that you want to run in automation, represented by a single OBR (OSGi Bundle Repository) and its equivalent test catalog. Galasa uses the OBR to locate your tests in a Maven repository, along with all of the Managers that a test project requires. A test catalog is generated directly from the test source, is always up to date, and is specified in the Galasa CLI to select tests to run in automation. Administrators can create few or many test streams, depending on testing requirements. Testers can contribute tests to one or more test streams.

## Test stream components

A test stream is made up of a name, description, OBR, location and repository components. 

- The OBR component is a URL that points to a list of Maven coodinates in the format `mvn:{grp}/{artifact id}/{version}/{obr}`. 
- The location component is a URL that points to the location of the test catalog that is associated with a particular OBR in the format `http://points-to-my-testcatalog`. A test catalog is an index of tests.  

- The repository component provides the location of the Maven repository where the test code and test catalog are deployed

The following diagram shows the relationship between the test code, test catalog, and test stream.

![test stream architecture:](test-streams-architecture.svg)


## Creating and retrieving a test stream

The components of the `test.stream` property are set by using `galasactl resources apply -f {yaml-filename}` command.
The `apply` sub-command will update the stream if it already exists, but you could use the `create` or `update` sub-command as an alternative.

See [the command reference](../reference/cli-syntax/galasactl_resources_apply.md) for specific syntax help.

Streams are explained in more detail, with an example [here](../ecosystem/ecosystem-manage-resources.md/#test-streams-as-galasastream-resources).

You can view all test streams in the `framework` namespace by using the `galasactl streams get` command, as shown in the following example:

```
galasactl streams get
name             state   description
integrationtests enabled Test stream for in-development integration tests
regressiontests  enabled Regression test suite

Total:2
```

The following table shows the available output formats that can be provided as part of the `galasactl streams get` command:

| Name |  Description  |
| :---- | :-------- | 
| `--format summary` | The default format is _summary_. Summary format is useful if you need a quick, high-level overview. If you omit the `--format` flag in the command, results are returned in summary format. You can set the summary format explicitly by setting the `--format summary` flag in the `galasactl streams get` command.   | 
| `--format yaml` |  The results from `galasactl streams get` are returned as GalasaStream resources in YAML format. This YAML content can then be used in `galasactl resources` commands to create, update, and delete secrets using a YAML file. See [Configuring an Ecosystem using resource files](./ecosystem-manage-resources.md) for more details.|

For example, you can use the following command to retrieve all test streams in `yaml` format:

```shell
galasactl streams get --format yaml
```

For a complete list of supported parameters, see the [galasactl streams get](../reference/cli-syntax/galasactl_streams_get.md) command reference.

## Organising test streams

There are many ways to organise test streams:

- **Single test stream**: A single test stream can contain all of your tests. This system is of most use when you are learning about Galasa and experimenting with the automation system. At some point, it is much more beneficial for you to move to a dual stream setup.

- **Dual test streams**: Often, you will need to run a _production_ test stream and a _best so far_ test stream. The production stream is used by the CI/CD pipeline to test the application, while the best so far stream will include tests under development that you wish to observe under automation.  
  The source code for tests in dual streams is usually from the same SCM (Source Code Management) area - in git terms, this would mean the same repository but in different branches. The best so far stream contains all of the production stream tests plus new or modified tests. When you are happy that a test has reached a good level of quality, you can promote it to the production stream and make it available to the CI/CD pipeline. Usually, you would organise things so that you have a Maven build per stream.

- **Many streams**: You might organise your streams so that you have one per team, or dual test streams per team. If you have lots of tests per application area, having a stream per area is also possible.

Build time and frequency usually dictates how streams are structured. If you are building your test projects frequently, and those build times are long, you might wish to split the test projects into streams. Alternatively, you could stick with a single stream, but split the projects into separate builds that all kick off the build of the test stream OBR when complete.
