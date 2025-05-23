---
path: "/docs/managers/zos-tso-command-ssh-manager"
title: "zOS TSO Command SSH Manager"
---

This Manager is at Beta level. You can view the <a href="https://javadoc.galasa.dev/overview-summary.html" target="_blank" rel="noopener noreferrer">Javadoc documentation for the Manager here</a>.<br>


[Overview](#overview)<br>
[Configuring](#configuring)<br>


# <a name="overview"></a>Overview
This Manager is the internal implementation of the zOS TSO Command Manager using SSH. <br><br> See the <a href="/docs/managers/zos-manager">zOS Manager</a> for details of the z/OS TSO annotations and  code snippets.<br><br> 




## <a name="configuring"></a>Configuration Properties

The following are properties used to configure the zOS TSO Command SSH Manager.
 
<details>
<summary>The tsocmd path</summary>

| Property: | The tsocmd path |
| --------------------------------------- | :------------------------------------- |
| Name: | zostsocommand.[imageid].tsocmd.command.path |
| Description: | The path to the tsocmd command |
| Required:  | No |
| Default value: | tsocmd |
| Valid values: | A valid PATH environment variable or a full path name |
| Examples: | <code>zostsocommand.command.tsocmd.path=tsocmd</code><br> <code>zostsocommand.MFSYSA.tsocmd.command.path=/tools/tsocmd</code> |

</details>
