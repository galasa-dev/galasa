---
title: "zOS Console oeconsol Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/zosconsole/oeconsol/manager/package-summary.html){target="_blank"}.


## Overview

This Manager is the internal implementation of the z/OS Console Manager using **oeconsol**. The **oeconsol** z/OS Console Manager is used in conjunction  with the z/OS UNIX Command Manager. The z/OS Manager provides the interface for the z/OS console function and pulls in the **oeconsol** Console Manager  to provide the implementation of the interface. If your test needs to request a z/OS console instance, issue a console command or retrieve the console  command, you can call the z/OS Manager in your test code and the z/OS Manager will call the **oeconsol** Console Manager to provide the implementation  via the z/OS console function. Multiple z/OS console images can be requested by a test.

See the [zOS Manager](./zos-manager.md) for details of the z/OS Console Annotations.

This implementation is less rich than the zOS/MF implementation due to the restricted functionallity of **oeconsol**:

- **oeconsol** does not directly support console name.

    Console name can be used to avoid clashes with other consoles that the user has open, e.g. in another Galsa test, in a TSO or SDSF session. When supplying console name via `IZosConsole#issueCommand(String command, String consoleName)` the Manager attempts to obtain credentials from the CPS, i.e. `secure.credentials.[consoleName].username`. The credentials are used to logon to z/OS UNIX and execute **oeconsol** with the supplied command.

- **oeconsol** does not support retrieving delayed responses.

    A `ZosConsoleException` is will be thrown when the `IZosConsoleCommand#requestResponse()` method is called.

See [oeconsol](https://github.com/IBM/IBM-Z-zOS/tree/main/zOS-Tools-and-Toys/oeconsol){target="_blank"} for documentation and download.


## Configuration Properties

The following are properties used to configure the zOS Console oeconsol Manager.
 

### The oeconsol path

| Property: | The oeconsol path |
| --------------------------------------- | :------------------------------------- |
| Name: | zosconsole.oeconsole.[imageid].command.path |
| Description: | The path to the oeconsol command |
| Required:  | No |
| Default value: | oeconsol |
| Valid values: | A valid PATH environment variable or a full path name |
| Examples: | `zosconsole.oeconsole.command.path=oeconsol`<br>`zosconsole.MFSYSA.oeconsol.command.path=/tools/oeconsol`<br>where `/tools/oeconsol` is the locations of the oeconsol executable|

