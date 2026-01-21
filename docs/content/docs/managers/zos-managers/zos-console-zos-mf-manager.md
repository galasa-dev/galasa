---
title: "zOS Console zOS MF Manager"
---

This Manager is at Beta level. You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/zosconsole/package-summary.html){target="_blank"}.

## Overview

This Manager is the internal implementation of the z/OS Console Manager using zOS/MF. The z/OS MF Console Manager is used in conjunction with the z/OS Manager. The z/OS Manager provides the interface for the z/OS console function and pulls in the z/OS MF Console Manager to provide the implementation of the interface. If your test needs to request a z/OS console instance, issue a console command or retrieve the console command, you can call the z/OS Manager in your test code and the z/OS Manager will call the z/OS MF Console Manager to provide the implementation via the z/OS console function. Multiple z/OS console images can be requested by a test.

See the [zOS Manager](./zos-manager.md) for details of the z/OS Console Annotations.

