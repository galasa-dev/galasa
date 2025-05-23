---
title: "Viewing test results locally"
---

Test results from local test runs are stored in the result archive store (RAS) on the machine where the tests were run. The RAS directory contains all elements of a test, including the test results, run logs, and test artifacts. Inside the RAS directory you can find the `run.log` which stores event records from a test run on your local machine. You can also view the `images` folder in the `zos3270` directory to see the terminal interactions from a test run. 

## Viewing results in the run log

You can view the results of your test runs in the `run.log` file by completing the following steps:

1. Navigate to your ``.galasa`` directory 
1. Open the _run.log_ file by selecting _ras > results > run.log_
1. Expand the _run.log_ file to view a record of the events from the test run.

## Viewing 3270 terminal interactions

To view 3270 terminal screens and recorded web requests and responses that are generated from running tests you need to first update the `3270.terminal.output` property in the `cps.properties` file on your local machine. To do this, complete the following steps: 

1. Navigate to your ``.galasa`` directory 
1. Open the _cps.properties_ file, for example in VS Code.
1. Edit the _zos3270.terminal.output_ property to include ``.png`` files as well as `json` by adding the value `png` to the property. For example, ```zos3270.terminal.output=json,png```.

3270 terminal screens, and recorded web requests and responses for test runs are now stored as .png files in the _ras > results > artifacts > zos3270 > images_ folder.

You can then view the terminal interactions from your test runs in the `images` folder in the `zos3270` directory by completing the following steps:

1. Navigate to your ``.galasa`` directory 
1. Open the _images_ folder by selecting _ras > results > artifacts > zos3270 > images_
1. Expand the _images_ folder to view test artifacts stored from the run, for example, terminal screens, and recorded web requests and responses.

## Next steps

Take a look at the [Exploring Galasa SimBank](../running-simbank-tests/index.md) documentation. Galasa SimBank is a component that is distributed with Galasa. SimBank simulates a mainframe application and is designed to help you to learn Galasa's basic principles of operation before you start connecting Galasa to your own mainframe application-under-test.