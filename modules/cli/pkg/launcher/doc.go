/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/*
Package launcher contains an abstraction of a launcher which can launch things.

There are two main implementations of launcher:
- The remoteLauncher which can launch a testRun inside a container on a remote Galasa server
- The jvmLauncher which can launch each testRun within a java virtual machine locally on this machine.

These launcher implementations share many aspects. Specifically:
- They are invoked by the same 'submitter' object.
- They all implement the launcher interface, allowing the launching of a test, and the monitoring of its'
status after the event.

This allows them to both produce results in the same format, so the same reporting routines can be used
for the various report formats.
*/
package launcher
