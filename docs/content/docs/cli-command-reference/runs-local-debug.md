---
title: "Debugging a test locally"
---

The following section describes how to connect your Galasa test with a Java debugger on a specified port, and then configure your IDE (IDE options covered in this topic are Microsoft VSCode, IntelliJ, and Eclipse) to connect to that same port so that you can run your test locally in debug mode.


## Using the debug option

You can run a test locally in debug mode by using the galasactl `galasactl runs submit local` command with the `--debug` option specified. To run in debug mode, the Galasa test connects with a Java debugger on a specified port and the IDE being used is configured to connect to the same port. The test can then be launched in the Java debugger. 

The default value of the port is `2970`, but you can override this value adding an optional property, `galasactl.jvm.local.launch.debug.port`, into the `bootstrap.properties` file. For example, `galasactl.jvm.local.launch.debug.port=2971`. This property is ignored if the `--debug` option is not supplied to the `galasactl runs submit local` command.

If you need to override the value of the port that is set in the bootstrap, you can do so by using the `--debugPort` option on the `runs submit local` command. The port value itself must be an unsigned integer.

To launch multiple testcases in debug mode, add an explicit `--debugPort` option on the `galasactl runs submit local` command, so that each port is only used by one test/debugger pair at a time.

You can view the full list of options that are available with the `galasactl runs submit local` command in the 
[Galasa cli repository](https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_runs_submit_local.md){target="_blank"}.


## Connection modes

The mode that is used to control the connection from the local JVM to the Java debugger can be set to `listen` or `attach`. The default value is `listen`. 

In `listen` mode, the JVM that launches the testcase opens a debug port and pauses to listen for traffic on that port, waiting for a Java debugger to connect. In `attach` mode, the JVM that launches the testcase attaches to the debug port that has already been opened by the Java debugger.

You can override the default value of `listen` by adding the optional property `galasactl.jvm.local.launch.debug.mode` into the `bootstrap.properties` file. For example, `galasactl.jvm.local.launch.debug.mode=attach`.

If you need to override the mode that is set in the bootstrap, you can do so by using the `--debugMode` option on the `galasactl runs submit local` command. 


## Configuring the IDE

Your IDE typically needs configuring to connect to the same port that the testcase is using. The configuration used depends on the mode that is used to control the connection from the local JVM to the Java debugger. 

The IDE needs to be configured with the opposite type of connection mode in order to attach the Java debugger to the running Galasa test. For example, if your galasactl is configured to `listen` mode, then start the test first, and configure your IDE to attach to the same port. If your galasactl is configured to `attach` mode, then start your Java debugger first, so that it is there waiting for the testcase to attach to the debug port when the testcase is launched.

Information on how to configure different IDEs to connect to a local testcase in debug mode, can be found in the following sections. The examples that are used in the sections assume that the connection mode for the IDE is `attach` and the connection mode for galasactl is `listen`. If your settings are the other way around, launch the IDE first, and then launch the Galasa test.

- [Microsoft VSCode](#debugging-a-local-galasa-test-within-microsoft-vscode)
- [IntelliJ](#debugging-a-local-galasa-test-within-intellij)
- [Eclipse](#debugging-a-local-galasa-test-within-eclipse)


## Debugging a local Galasa test within Microsoft VSCode

Complete the following steps to run a local test in debug mode using VSCode:


### Prepare to launch the Java debugger

1. Install the Microsoft [Debugger for Java](https://github.com/microsoft/vscode-java-debug){target="_blank"} plugin. 
2. Add the following text to your _${workspace}/.vscode/_. You can add the text manually, or by selecting _Run and Debug_ from the side menu in Visual Studio Code and choosing _create a launch.json file_.
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Debug (Attach to Galasa test on 2970)",
            "projectName": "banking",
            "request": "attach",
            "hostName": "localhost",
            "port": 2970
        }
    ]
}
```
3. Check that the port number specified, so that when you configure the IDE the port used by the IDE matches the port number that is used by the testcase. In this example, `port` is set to `2970`, which is the default value.<br>
4. Check the value of the `request` field. The request field with a value of `attach` can be paired with the default of `listen` that is used by the `galasactl runs submit local --debug` command. Each tool must be configured with the opposite value, so one listens to the port, and the other attaches to it.
5. Make a note of the `name` that is specified so that you select this configuration to launch the test in the Java debugger. 


### Launch a Galasa test in the Java debugger


1. Load the test code source into the VSCode workspace and set a breakpoint within the test code that you want to debug.
2. Launch the Galasa test by running the following command. The Galasa test waits for the debugger to connect to the port that it is listening on.
```
galasactl runs submit local --debug
```
3. Launch the debugger within the IDE by selectin _Run and Debug_ from the side menu provided by the Java Debugging plugin.
4. From the drop down menu, select the launch configuration that matches the launch configuration of the `galasactl` tool. In this example, the configuration is called `Debug (Attach to Galasa test on 2970)`.
5. Click the `Run` symbol. The JVM resumes execution, and runs up to the point where your breakpoint is set, and then stops.
6. Check the IDE to see the variables in use, and the line of code where execution has reached. You can then control the execution of the testcase by using the IDE-provided debugger controls. 


## Debugging a local Galasa test within IntelliJ

Complete the following steps to run a local test in debug mode using IntelliJ:


1. Load the testcase code into your IntelliJ workspace and set a breakpoint within the test code that you want to debug.
2. Launch the Galasa test by running the following command. The Galasa test waits for the debugger to connect to the port that it is listening on.
```
galasactl runs submit local --debug
```
3. Launch the debugger. 
4. From the top menu, select _Run_ and use the _attach to process_ item. A dialog appears, asking you to select from the Java processes which are currently waiting. 
5. Select your process based on the name of the testcase that you are trying to debug. In this example, the name is `Debug (Attach to Galasa test on 2970)`. Once selected, the dialog disappears and IntelliJ launches your debugger at the breakpoint that you set.

To use the configuration where the IntelliJ debugger listens on the debug port, and the testcase connects to it, use the `--debugPort` and `--debugMode` options to pair up with an IntelliJ debug configuration. Remember that the `--debugMode` must be the opposite to the `Debugger Mode` within IntelliJ. For example, if the galasactl tool is using a `listen` mode, then the debugger has to `attach` to the port, and vice versa.

## Debugging a local Galasa test within Eclipse

Complete the following steps to run a local test in debug mode using Eclipse:

1. Import the testcase projects into your workspace and set a breakpoint within your test code that you want to debug.
2. Launch the Galasa test by using the following command. The JVM running the Galasa test launches, and pauses, listening on a port. 
```
galasactl runs submit local --debug
``` 
3. Create a debug configuration of the type _Remote Java Application_.
4. Set the _port number_ field to correspond to the port that your testcase is configured to use. In this example, the port is `2970`.
5. Set the _connection type_ field to be the opposite of what your testcase JVM is configured to use. By default, galasactl assumes a `listen` mode, so in that case the connection type field should be set to `attach`.
6. Save the debug configuration.
7. Launch the debug configuration. The debugger connects to the JVM running your Galasa testcase and starts a debugging session from the initial start point of the breakpoint that you set.


## Next steps

Read the [Viewing test results locally](./viewing-test-results-cli.md) documentation to learn how to view the output of your test runs in your local environment. 

Take a look at the [Exploring Galasa SimBank](../running-simbank-tests/index.md) documentation. Galasa SimBank is a component that is distributed with Galasa. SimBank simulates a mainframe application and is designed to help you to learn Galasa's basic principles of operation before you start connecting Galasa to your own mainframe application-under-test.