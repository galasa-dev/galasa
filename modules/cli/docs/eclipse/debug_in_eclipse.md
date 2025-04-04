# Debug a local Galasa test within Eclipse

- Import the testcase projects into your workspace
- Set a breakpoint somewhere within your testcase code
- Launch the test using the `galasactl runs submit local --debug ...` command
  - The JVM running the Galasa test will launch, and pause, listening on a port.
    Control the port used with the `--debugPort` parameter or `galasactl.jvm.local.launch.debug.port` property in your `bootstrap.properties` file. The default is 2970.
  - The `--debugMode` can be set to `attach` instead of the default `listen` if you wish to connect to a listening debugger instead. By default the testcase JVM will listen on the debug port.
- Create a debug configuration of the type "Remote Java Application"
  - Set the "port number" field to correspond to the port your testcase is configured to use.
  - Set the "connection type" field to be the opposite of what your testcase JVM is configured to use. By default, `galasactl` assumes a 'listen' mode, so in that case the connection type field should be set to 'attach'
- Save the debug configuration
- Launch the debug configuration
  - The debugger will connect to the JVM running your Galasa testcase and start a debugging session from the initial start point of the breakpoint you set earlier.



