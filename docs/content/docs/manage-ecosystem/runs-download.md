---
title: "Downloading test artifacts"
---

You can download the artifacts that are associated with a test run from the RAS and store those artifacts in a directory by using the `runs download` command. Use this output to gather information about a test, help debug and diagnose test failure, share test run output, and make comparisons between test run results.

You can view the full list of options that are available with the `runs download` command in the [Galasa cli repository](https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_runs_download.md#galasactl-runs-download){target="_blank"}.

## Working with the `runs download` command

Downloaded artifacts are stored by default in a folder that is created within the current working directory from where the command is run. The name of the created folder corresponds to the test run-name that was provided. 

In the following example, the test run is called _C1234_. All artifacts for test run `C1234` are downloaded to a folder named `C1234` in the current working directory by using the following command:

```shell
galasactl runs download --name C1234
```

If the folder named `C1234` does not exist, it is created. If artifacts are already stored within an existing folder named `C1234`, the download fails. You can force artifacts stored within an existing folder to be overwritten by using the `--force` flag:

```shell
galasactl runs download --name C1234 –-force
```

Use the `--force` flag carefully to avoid artifacts being over-written, resulting in the loss of data.

### Specifying a folder for downloading artifacts

You can specify a location where you want the test artifacts downloaded by using the `--destination` option on the `runs download` command. This is useful if you do not want to use the default directory. 

The downloaded artifacts are organised within the specified directory in sub-folders that are based on the run-name. The file path of the folder can be relative to the current working directory, or absolute. For example, you can save artifacts from test run _C1234_ to a folder called _mytestruns_ in a directory called _temp_ by using the following command:

```shell
galasactl runs download --name C1234 --destination /temp/mytestruns
```

A message is displayed to confirm the location of the downloaded artifacts, for example:

```
GAL2501I: Downloaded 35 artifacts to '/temp/mytestruns/C1234' 
```

### Identifying output for incomplete test runs 

If you are investigating a problem, you might want to download artifacts that are associated with a test run which has not finished. If a test run is not finished when the `runs download` command is submitted, artifact collection might not be complete.

To identify such a scenario, a timestamp is appended to the folder name to indicate that the test did not finish running. The timestamp shows the time at which the `runs download` command is run. The time is displayed in Universal Time Coordinated (UTC) time. The date is displayed in the format _yyyy-mm-dd_. 

For example, if the `runs download` command is run on the 6th of May 2023 at 10.30 am, against test _U5432_ which is still running, the created folder name would look similar to the following example:

```
U5432-2023-05-06_10:30:15
```

A message is displayed to show the location of any downloaded artifacts from the incomplete run, for example:

```
GAL2501I: Downloaded 11 files to folder U5432_2023-05-06_10:30:15
```

### Identifying output for test retries

If a test is scheduled to run at a particular time but is unable to start, for example due to a lack of resources that are available in the environment, the Ecosystem might retry the test at a later time. When investigating problems with a test running in an Ecosystem, it is useful to download the artifacts that are associated with the retries of that test.

When using the `runs download` command, if a test has run more than once, a number is added to the folder name to indicate the number of the retry, as shown in the following example:

```
C1234-1-2023-05-25_18:30:26
C1234-2-2023-05-25_18:30:26
C1234-3
```

In this example, the test _C1234_ tried to run twice unsuccessfully and completed on the third try. The numbers _1_ and _2_ in the folder names of the first two test run attempts indicate the retry order. The inclusion of the timestamp in folder name of the first two tries indicates that the test did not finish running. The third time the test finished running, so no timestamp is included as part of the name of the folder. 


