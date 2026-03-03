---
title: "Running tests in an Ecosystem"
---

The `runs submit` command submits and monitors tests in the Galasa Ecosystem. Tests can be input either from a portfolio or directly from a test package.

For information about creating a portfolio by using the Galasa CLI, see the documentation for the [runs prepare](./runs-prepare.md) command.


## Working with the `runs submit` command

The following section provides a subset of examples of how you can use the `runs submit` command to complete various tasks, for example, getting help, submitting tests, and setting overrides. The examples build on the Galasa SimBank tests, which you can run non-locally if you have an ecosystem that is running SimPlatform.


### Submitting tests to an ecosystem from a portfolio

The following example assumes that you have created a `my_portfolio.yaml` portfolio by using the [runs prepare](./runs-prepare.md) command. The command submits tests from the `my_portfolio.yaml` portfolio, and specifies the following settings.

On Mac or Unix:

```shell
galasactl runs submit \
    --bootstrap http://example.com:30960/bootstrap \
    --portfolio my_portfolio.yaml \
    --poll 5 \
    --progress 1 \
    --throttle 5 \
    --log -
```

On Windows (Powershell):

```powershell
galasactl runs submit `
    --bootstrap http://example.com:30960/bootstrap `
    --portfolio my_portfolio.yaml `
    --poll 5 `
    --progress 1 `
    --throttle 5 `
    --log -
```

where:

- `portfolio` specifies the portfolio that defines the tests that you want to run
- `poll` specifies the frequency in seconds that the CLI polls the Ecosystem for test run status.
- `progress` specifies the frequency in minutes that the CLI reports the overall progress of the test runs. A value of  `-1` or less disables progress reports.
- `throttle` specifies the number of test runs that can be submitted in parallel. A value of `0` or less  prevents throttling.
- `log` specifies that the progress log should be directed somewhere, and the `-` means that it should be sent to the console (stderr) so it is visible.


### Submitting tests without a portfolio

You can use test class names to submit test runs without using a portfolio.

The following command runs the `SimBankIVT` and `BasicAccountCreditTest` tests from the  `dev.galasa.simbank.tests` package.

On Mac or Unix:

```shell
galasactl runs submit \
    --bootstrap http://example.com:30960/bootstrap \
    --class dev.galasa.simbank.tests/SimBankIVT \
    --class dev.galasa.simbank.tests/BasicAccountCreditTest \
    --stream BestSoFar \
    --log -
```

On Windows (Powershell):

```powershell
galasactl runs submit `
    --bootstrap http://example.com:30960/bootstrap `
    --class dev.galasa.simbank.tests/SimBankIVT `
    --class dev.galasa.simbank.tests/BasicAccountCreditTest `
    --stream BestSoFar `
    --log -
```


### Setting overrides for all tests during a run

Specifying overrides is useful if you want to run a set of tests against a particular configuration without changing the test code. For example, you might have multiple versions of software that you need to test. How can you do that without changing the test code? The answer is to use override properties. If you are running tests locally, you can set overrides properties by editing your `Overrides Properties` file. If you are running tests in an ecosystem, you can use the `--override` parameter in the Galasa CLI. Note that overrides in the portfolio take precedence over the overrides on the `runs submit` command. This is so that you can set general overrides on the submit, but have specific class overrides in the portfolio.

The following command runs all the tests in the `my_portfolio.yaml` portfolio are on the z/OS LPAR `MYLPAR` in the `MYPLEX` cluster.

On Mac or Unix:

```shell
galasactl runs submit \
    --portfolio my_portfolio.yaml \
    --override zos.default.lpar=MYLPAR \
    --override zos.default.cluster=MYPLEX \
    --log -
```

On Windows (Powershell):

```powershell
galasactl runs submit `
    --portfolio my_portfolio.yaml `
    --override zos.default.lpar=MYLPAR `
    --override zos.default.cluster=MYPLEX `
    --log -
```


### Overriding the test run 'user'

When submitting runs to a Galasa ecosystem with `runs submit`, the run `requestor` will be set to the user who owns the personal access token that was used to authenticate to the Galasa ecosystem. If you wish to associate a different user with this batch of runs as the run `user`, you can use the `--user` flag. This is useful if you submit runs to a Galasa ecosystem in an automation tool or workflow and the `requestor` is a functional ID or bot account in the tool, but you wish to specify the actual user who triggered the automation, so they can query their runs later with the `runs get` command.

Only users with the `admin` or `owner` role can use the `--user` flag in their `runs submit` command, or the `user` will default to the authenticated requestor. The `user` specified in the command must be an existing system user, i.e., they must have access to the Galasa service and be a `requestor` of some existing test runs in the service. They must also have permission to submit test runs, i.e., have the `tester` role or more in the system.

The following example assumes that you have created a `my_portfolio.yaml` portfolio by using the [runs prepare](./runs-prepare.md) command. The command submits tests from the `my_portfolio.yaml` portfolio, and specifies the following settings.

```shell
galasactl runs submit \
    --bootstrap http://example.com:30960/bootstrap \
    --portfolio my_portfolio.yaml \
    --poll 5 \
    --progress 1 \
    --throttle 5 \
    --log - \
    --user my-tester-user
```

On Windows (Powershell):

```powershell
galasactl runs submit `
    --bootstrap http://example.com:30960/bootstrap `
    --portfolio my_portfolio.yaml `
    --poll 5 `
    --progress 1 `
    --throttle 5 `
    --log - \
    --user my-tester-user
```

## Controlling how long tests are allowed to run for

In some situations, tests might seem to stall or run indefinitely due to issues in the test code or wider environmental issues. To prevent tests from hanging indefinitely, you can configure a timeout for test runs using the `framework.test.run.timeout.minutes` CPS property. When a test exceeds the configured timeout period, it is automatically interrupted and assigned the `Hung` result.

For example, to configure a timeout of 30 minutes for test runs, you can set the `framework.test.run.timeout.minutes` CPS property using the Galasa CLI tool:

```shell
galasactl properties set --bootstrap http://example.com:30960/bootstrap --namespace framework --name test.run.timeout.minutes --value 30
```

This helps ensure that problematic tests do not consume resources indefinitely and allows you to identify and address issues more quickly.
