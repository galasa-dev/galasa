---
title: "Selecting tests to run in an Ecosystem"
---

If you have a group of tests that you want to run in batch, the simplest way to do this is by defining these tests inside a portfolio. You can create a portfolio of tests by using the `runs prepare` command. You can build the portfolio from single or multiple [test streams](/docs/manage-ecosystem/test-streams).

This portfolio of tests can then be run by using the [runs submit command](/docs/manage-ecosystem/ecosystem-cli-runs-submit). When you submit the tests to run, you need specify only the portfolio name on the command, rather than listing all of the classes.

## Working with the `runs prepare` command

The following section provides a subset of examples that use the `runs prepare` command to complete various tasks, for example, getting help, selecting tests, and setting overrides. The examples build on the Galasa SimBank tests, which you can run non-locally if you have an ecosystem that is running SimPlatform.


### Selecting tests from a test steam

The following example command selects tests from a test stream called `BestSoFar` for inclusion in a portfolio called `my_portfolio.yaml`. All tests in the `dev.galasa.simbank.tests`  package within the `BestSoFar` test stream are added to the `my_portfolio.yaml` portfolio. Packages are selected if the name contains a specified string or matches the regex if --regex is specified.  

On Mac or Unix:

```
galasactl runs prepare \
                        --bootstrap http://example.com:30960/bootstrap \
                        --portfolio my_portfolio.yaml \
                        --stream BestSoFar \
                        --package dev.galasa.simbank.tests
```

On Windows (Powershell):

```
galasactl runs prepare `
                        --bootstrap http://example.com:30960/bootstrap `
                        --portfolio my_portfolio.yaml `
                        --stream BestSoFar `
                        --package dev.galasa.simbank.tests
```

### Building a portfolio for multiple test packages

You can build a portfolio for multiple test package by using a comma separated list.

The following example command selects tests from the `dev.galasa.simbank.tests` and `dev.galasa.example.banking` test packages and adds these tests to the `my_portfolio.yaml` portfolio.

On Mac or Unix:

```
galasactl runs prepare \
                    --bootstrap http://example.com:30960/bootstrap \
                    --portfolio my_portfolio.yaml \
                    --stream BestSoFar \
                    --package dev.galasa.simbank.tests,dev.galasa.example.banking
```

On Windows (Powershell):

```
galasactl runs prepare `
                    --bootstrap http://example.com:30960/bootstrap `
                    --portfolio my_portfolio.yaml `
                    --stream BestSoFar `
                    --package dev.galasa.simbank.tests,dev.galasa.example.banking
```

### Selecting tests without a test stream

You can use test class names to build a portfolio when a test stream or test catalog is not available. 

The following example command selects specified tests (`SimBankIVT` and `BasicAccountCreditTest`) from the `dev.galasa.simbank.tests` test package and adds these tests to the `my_portfolio.yaml` portfolio. 

On Mac or Unix:

```
galasactl runs prepare \
                        --bootstrap http://example.com:30960/bootstrap \
                        --portfolio my_portfolio.yaml \
                        --class dev.galasa.simbank.tests/SimBankIVT \
                        --class dev.galasa.simbank.tests/BasicAccountCreditTest
```

On Windows (Powershell):

```
galasactl runs prepare `
                        --bootstrap http://example.com:30960/bootstrap `
                        --portfolio my_portfolio.yaml `
                        --class dev.galasa.simbank.tests/SimBankIVT `
                        --class dev.galasa.simbank.tests/BasicAccountCreditTest
```


### Setting test-specific overrides

Specifying overrides is useful if you want to run a set of tests against a particular configuration without changing the test code. For example, you might have multiple versions of software that you need to test. How can you do that without changing the test code? The answer is to use override properties. If you are running tests locally, you can set overrides properties by editing your `Overrides Properties` file. If you are running tests in an ecosystem, you can use the `--override` parameter in the Galasa CLI. Note that overrides in the portfolio take precedence over the overrides on the `runs submit` command. This is so you can set general overrides on the submit, but have specific class overrides in the portfolio.

The following example creates a portfolio called `my_portfolio.yaml` that contains tests from the `BestSoFar` test stream where the test package is `dev.galasa.simbank.tests`.

All the tests in the `my_portfolio.yaml` portfolio will run on the z/OS LPAR `MYLPAR` in the `MYPLEX` cluster when the `galasactl runs submit --portfolio my_portfolio.yaml` command is run, regardless of the LPAR and cluster that is specified in the CPS properties file.

On Mac or Unix:

```
galasactl runs prepare \
                        --bootstrap http://example.com:30960/bootstrap \
                        --portfolio my_portfolio.yaml \
                        --stream BestSoFar \
                        --package dev.galasa.simbank.tests \
                        --override zos.default.lpar=MYLPAR \
                        --override zos.default.cluster=MYPLEX
```

On Windows (Powershell):

```
galasactl runs prepare `
                        --bootstrap http://example.com:30960/bootstrap `
                        --portfolio my_portfolio.yaml `
                        --stream BestSoFar `
                        --package dev.galasa.simbank.tests `
                        --override zos.default.lpar=MYLPAR `
                        --override zos.default.cluster=MYPLEX
```


### Building a portfolio over multiple selections and overrides

You can select tests by using "package" OR "bundle" OR "test". If a test is selected multiple times, it will only be added once. Duplicate tests are appended, enabling the same test to be selected multiple times with different overrides.

In the following example, the first command creates a portfolio called `my_portfolio.yaml` that contains tests from the `BestSoFar` test stream where the test package name is `dev.galasa.simbank.tests`. The second command adds more tests to the `my_portfolio.yaml` portfolio from the `BestSoFar` test stream where the test package name is `dev.galasa.simbank.tests.two`. 

All the tests in the `my_portfolio.yaml` portfolio from the `dev.galasa.simbank.tests` package will run on the z/OS LPAR `MYLPAR` in the `MYPLEX` cluster. The tests from the `dev.galasa.simbank.tests.two` package will run on the z/OS LPAR `MYLPAR2` in the `MYPLEX` cluster when the `galasactl runs submit --portfolio my_portfolio.yaml` command is run.

On Mac or Unix:

```
galasactl runs prepare \
                        --bootstrap http://example.com:30960/bootstrap \
                        --portfolio my_portfolio.yaml \
                        --stream BestSoFar \
                        --package dev.galasa.simbank.tests \
                        --override zos.default.lpar=MYLPAR \
                        --override zos.default.cluster=MYPLEX \
galasactl runs prepare \
                        --bootstrap http://example.com:30960/bootstrap \
                        --portfolio my_portfolio.yaml \
                        --append \
                        --stream BestSoFar \
                        --package dev.galasa.simbank.tests.two \
                        --override zos.default.lpar=MYLPAR2 \
                        --override zos.default.cluster=MYPLEX
```

On Windows (Powershell):

```
galasactl runs prepare `
                        --bootstrap http://example.com:30960/bootstrap `
                        --portfolio my_portfolio.yaml `
                        --stream BestSoFar `
                        --package dev.galasa.simbank.tests `
                        --override zos.default.lpar=MYLPAR `
                        --override zos.default.cluster=MYPLEX `
galasactl runs prepare `
                        --bootstrap http://example.com:30960/bootstrap `
                        --portfolio my_portfolio.yaml `
                        --append `
                        --stream BestSoFar `
                        --package dev.galasa.simbank.tests.two `
                        --override zos.default.lpar=MYLPAR2 `
                        --override zos.default.cluster=MYPLEX
```

You can now run the tests in your portfolio by using the `runs submit` command.
