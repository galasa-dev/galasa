---
title: "zOS File zOS MF Manager"
---

This Manager is at Beta level. You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/zosfile/package-summary.html){target="_blank"}.


## Overview

This Manager is the internal implementation of the zOS File Manager using zOS/MF. The z/OS MF File Manager is used in conjunction with the z/OS Manager. The z/OS Manager provides the interface for the z/OS file function and pulls in the z/OS MF File Manager to provide the implementation of the interface. If your test needs to instantiate a UNIX file, dataset, or VSAM data set, write and retrieve content from it, or configure and manipulate it then you can call the z/OS Manager in your test code and the z/OS Manager will call the z/OS MF File Manager to provide the implementation via the z/OS file function.

See the [zOS Manager](./zos-manager.md) for details of the z/OS File Annotations.
