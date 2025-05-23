---
path: "/docs/managers/linux-manager"
title: "Linux Manager"
---

This Manager is at Alpha level. You can view the <a href="https://javadoc.galasa.dev/dev/galasa/linux/package-summary.html" target="_blank" rel="noopener noreferrer">Javadoc documentation for the Manager here</a>.<br>


[Overview](#overview)<br>
[Configuring](#configuring)<br>


# <a name="overview"></a>Overview
This Manager provides the tester with the capability to                      connect to a Linux image as part of a test and to access                      the command shell. Standard commands can then be run on                      the image. The Linux Manager has a dependency on the IP Network                      Manager, which establishes an IP connection to the image.                                                                                    <br>                     <br>





## <a name="configuring"></a>Configuration Properties

The following are properties used to configure the Linux Manager.
 
<details>
<summary>Retain the run directory after the test is complete, for diagnostic purposes</summary>

| Property: | Retain the run directory after the test is complete, for diagnostic purposes |
| --------------------------------------- | :------------------------------------- |
| Name: | linux.image.[imageid].retain.run.directory |
| Description: | Informs the Linux Manager that you would like the retain the run directory after the test run is complete |
| Required:  | No |
| Default value: | false |
| Valid values: | true or false |
| Examples: | <code>linux.image.UBT.retain.run.directory=true</code> |

</details>
