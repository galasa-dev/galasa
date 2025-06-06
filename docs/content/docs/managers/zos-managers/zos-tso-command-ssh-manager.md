---
title: "zOS TSO Command SSH Manager"
---

This Manager is at Beta level. You can view the [Javadoc documentation for the Manager](https://javadoc.galasa.dev/overview-summary.html){target="_blank"}.


## Overview

This Manager is the internal implementation of the zOS TSO Command Manager using SSH.

See the [zOS Manager](./zos-manager.md) for details of the z/OS TSO annotations and code snippets.


## Configuration Properties

The following are properties used to configure the zOS TSO Command SSH Manager.
 

### The tsocmd path

| Property: | The tsocmd path |
| --------------------------------------- | :------------------------------------- |
| Name: | zostsocommand.[imageid].tsocmd.command.path |
| Description: | The path to the tsocmd command |
| Required:  | No |
| Default value: | tsocmd |
| Valid values: | A valid PATH environment variable or a full path name |
| Examples: | `zostsocommand.command.tsocmd.path=tsocmd`<br>`zostsocommand.MFSYSA.tsocmd.command.path=/tools/tsocmd` |
