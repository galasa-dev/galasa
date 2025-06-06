---
title: "Retrying and cancelling tests"
---

Sometimes tests can become stuck in a loop and fail to finish running, for example, due to a lack of available resources, an environmental problem, or a timeout. You can retry a test to run again by using the `runs reset` command. If the test continues to fail to finish running, you can use the `runs cancel` command to cancel the test. 

Retrying a test sets the status of the test run in the DSS to `queued` status. Cancelling a test removes all entries in the DSS for that test run. For this reason it is preferable to retry a test rather than cancel a test. All information that is stored in the RAS about the test is kept and is not removed when either the `runs reset` or `runs cancel` command is run.


To view the full list of options that are available, see the [galasactl runs reset](../reference/cli-syntax/galasactl_runs_reset.md) and [galasactl runs cancel](../reference/cli-syntax/galasactl_runs_cancel.md) command references.


## Working with the `runs reset` command

The following example retrys a test run called _C1234_ by using the following command: 

```shell
galasactl runs reset --name C1234
```

### Output for test retries

When using the [galasactl runs download](./runs-download.md) command to view test results, if a test has run more than once, a number is added to the folder name to indicate the number of the retry, as shown in the following example:

```
C1234-1-2023-05-25_18:30:26
C1234-2-2023-05-25_18:30:26
C1234-3
```

In this example, the test _C1234_ tried to run twice unsuccessfully and completed on the third try. The numbers _1_ and _2_ in the folder names of the first two test run attempts indicate the retry order. The inclusion of the timestamp in folder name of the first two tries indicates that the test did not finish running. The third time the test finished running, so no timestamp is included as part of the name of the folder. 


## Working with the `runs cancel` command

The following example cancels a test run called _C1234_ by using the following command: 

```shell
galasactl runs cancel --name C1234
```

All information that is held in the DSS about the test run is removed. A message is returned in the log to say that the test run was lost and results are returned on the terminal in the following example format: 

```
2024/02/07 13:34:28 Run C1234 was lost - inttests/dev.galasa.inttests/dev.galasa.inttests.core.local.CoreLocalJava11Ubuntu
submitted-time(UTC) name requestor status result test-name

Total:1 Lost:1
2024/02/07 13:34:28 GAL1017E: Not all runs passed. 1 failed.
GAL1017E: Not all runs passed. 1 failed.
2024/02/07 13:34:28 Exit code is 2
```

