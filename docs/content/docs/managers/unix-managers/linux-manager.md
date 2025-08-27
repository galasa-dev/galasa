---
title: "Linux Manager"
---

This Manager is at Alpha level. You can view the [Javadoc documentation for the Manager](https://javadoc.galasa.dev/dev/galasa/linux/package-summary.html){target="_blank"}.


## Overview
This Manager provides the tester with the capability to connect to a Linux image as part of a test and to access the command shell.
Standard commands can then be run on the image.
The Linux Manager has a dependency on the IP Network Manager, which establishes an IP connection to the image.


## Configuration Properties

The following are properties used to configure the Linux Manager.


### Linux Manager extra bundles

| Property: | Linux Manager extra bundles |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.bundle.extra.managers` |
| Description: | Extra Galasa Managers that may be required to enable the Linux Manager. This may be required if your Linux images are stored in a platform that provides Infrastructure as a Service. |
| Required:  | No |
| Default value: | N/A |
| Valid values: | A valid Galasa Manager package name |
| Examples: | `linux.bundle.extra.managers=dev.galasa.openstack.manager` |


### Shared Linux images

| Property: | Linux image hostname |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.shared.images` |
| Description: | A comma separated list of what images are available to allocate to tests. This property contains the tags that are used as the `imageid` values in the following properties. |
| Required:  | Yes |
| Default value: | N/A |
| Valid values: | A valid string that can be used as an `imageid` in other CPS properties of the Linux Manager |
| Examples: | `linux.shared.images=UBT,MYIMAGE` |


### Shared Linux provisioner priority

| Property: | Linux image hostname |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.shared.priority` |
| Description: | The importance of this provisioner compared to other provisioners. The larger the number the more important. |
| Required:  | No |
| Default value: | 1 |
| Valid values: | An integer value between 1 and 100 |
| Examples: | `linux.shared.priority=1`, `linux.shared.priority=100` |


### Linux image hostname

| Property: | Linux image hostname |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].ipv4.hostname` |
| Description: | The location of the Linux image. |
| Required:  | Yes |
| Default value: | N/A |
| Valid values: | A valid DNS name or IPv4/6 address |
| Examples: | `linux.image.UBT.ipv4.hostname=192.168.2.3` |


### Linux image credentials

| Property: | Linux image hostname |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].credentials` |
| Description: | The credentials tag that the username and password to access the Linux image are stored with in the Galasa Credentials Store. |
| Required:  | Yes, if credentials are required for your Linux image |
| Default value: | N/A |
| Valid values: | A string |
| Examples: | `linux.image.UBT.credentials=UBTCREDS` |


### Linux image operating system

| Property: | Linux image hostname |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].operating.system` |
| Description: | The operating system of the shared Linux image. |
| Required:  | Yes |
| Default value: | N/A |
| Valid values: | A valid operating system `ubuntu` or `windows` |
| Examples: | `linux.image.[imageid].operating.system=UBUNTU`, `linux.image.operating.system=UBUNTU` |


### Linux image capabilities

| Property: | Linux image capabilities |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].capabilities` |
| Description: | The special capabilities of the Linux image. |
| Required:  | No |
| Default value: | Empty, i.e., no special capabilities |
| Valid values: | A string |
| Examples: | `linux.image.UBT.capabilities=desktop,wmq`, `linux.image.capabilities=desktop,wmq` |


### Linux image maximum slots

| Property: | Linux image maximum slots |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].max.slots` |
| Description: | The maximum slots, i.e., the maximum amount of Galasa tests that can run on a Linux image at once. |
| Required:  | No |
| Default value: | 2 |
| Valid values: | An integer value |
| Examples: | `linux.image.UBT.max.slots=9`, `linux.image.max.slots=9` |


### Linux image archives directory

| Property: | Linux image archives directory |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].archives.directory` |
| Description: | The location the archives are stored on this Linux image. |
| Required:  | No |
| Default value: | `/opt/archives` |
| Valid values: | A valid fully-qualified path |
| Examples: | `linux.image.UBT.archives.directory=/opt/archives`, `linux.image.archives.directory=/opt/archives` |


### Linux image username pool

| Property: | Linux image username pool |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].username.pool` |
| Description: | The username patterns that can be used on the Linux image. |
| Required:  | No |
| Default value: | `galasa{0-9}{0-9}` |
| Valid values: | A comma separated list of static or generated names or a valid string pattern template |
| Examples: | `linux.image.UBT.username.pool=galasa{0-9}{0-9}`, `linux.image.UBT.username.pool=BOB1,BOB2` |

 
### Retain run directory

| Property: | Retain run directory |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].retain.run.directory` |
| Description: | Informs the Linux Manager if you would like the retain the run directory on the image after the test run is complete. |
| Required:  | No |
| Default value: | false |
| Valid values: | true or false |
| Examples: | `linux.image.UBT.retain.run.directory=true` |

