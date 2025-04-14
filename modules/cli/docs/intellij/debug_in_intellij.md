# Debug a local Galasa test within IntelliJ

- Load the testcase code into your workspace.
- Set a breakpoint
- Launch the `galasactl runs submit local --debug ...` command.
  - The JVM containing the test should launch and stop at the breakpoint, awaiting a java debugger to attach.
- From the top menu, "Run" use the "attach to process" item.
- A dialog appears, asking you to select from the java processes which are currently waiting. Select your process based on the testcase name you are trying to debug. The selection causes the dialog to disappear.
- Intellij launches your debugger, at the breakpoint you set.
  - This assumes the breakpoint you set is reachable code in the normal running of the test.


## Notes
If you wish to launch multiple processes/test cases in debug sessions, you will have to add an explicit `--debugPort` parameter onto the `galasactl runs submit local` command, so that each port is only used by one test/debugger pair at once.

If you wish to use the configuration where the IntelliJ debugger listens on the
debug port, and the testcase connects to it, use the `--debugPort` and `--debugMode` options to pair up with an IntelliJ debug configuration.
Remember that the `--debugMode` needs to be the opposite to the "Debugger Mode" within IntelliJ.
For example: If the `galasactl` tool is using a "listen" mode, then the debugger has to "attach", and vice versa.


