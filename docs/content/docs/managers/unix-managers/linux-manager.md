---
title: "Linux Manager"
---

This Manager is at Alpha level. You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/linux/package-summary.html){target="_blank"}.


## Overview
This Manager provides the tester with the capability to connect to a Linux image as part of a test and to access the command shell. Linux commands can then be run on the image. 

The Linux Manager has a dependency on the IP Network Manager, which establishes an IP connection to the image.

A Linux image can be made available to a test from a Linux provisioner. There is either the Developer Supplied Environment (DSE) provisioner or the shared provisioner as part of the Linux Manager. Other Galasa Managers such as the [Openstack Manager](../cloud-managers/open-stack-manager.md) are also Linux provisioners.

The DSE provisioner provides a Galasa test with a Developer Supplied Environment, i.e., a specific Linux image you want your Galasa test to connect to. The shared provisioner however defines a pool of available Linux images known to Galasa, and then the test connects to one that is available that matches the requested capabilities and attributes specified in the CPS properties.


## Annotations

The following annotations are available with the Linux Manager.


### Linux Image

| Annotation: | Linux Image |
| --------------------------------------- | :------------------------------------- |
| Name: | `@LinuxImage` |
| Description: | The `@LinuxImage` annotation requests the Linux Manager to allocate a Linux image to the test. The test can then access the command shell and run Linux commands on the image. |
| Attribute: `imageTag` |  The `imageTag` is used to identify the Linux image to other Managers. If a test is using multiple Linux images, each separate Linux image must have a unique tag. If two Linux images use the same tag, they will refer to the same Linux image. |
| Attribute: `operatingSystem` |  The `operatingSystem` attribute provides the requested operating system of the image. |
| Attribute: `capabilities` |  The `capabilities` attribute specifies the capabilities required of the image, if any, in an array. |
| Syntax: | <pre lang="java">@LinuxImage(imageTag = "PRIMARY", operatingSystem = OperatingSystem.ubuntu)<br>public ILinuxImage linuxImage;<br></pre> |
| Notes: | The `ILinuxImage` interface gives the test access to the IPv4/6 address of the image. It also provides paths for the root, home, tmp, run directory and archives directory. A command shell on the image is also available through the interface.<br><br> See [LinuxImage](https://galasa.dev/docs/reference/javadoc/dev/galasa/linux/LinuxImage.html){target="_blank"} and [ILinuxImage](https://galasa.dev/docs/reference/javadoc/dev/galasa/linux/ILinuxImage.html){target="_blank"} to find out more. |


### Linux IP Host

| Annotation: | Linux IP Host |
| --------------------------------------- | :------------------------------------- |
| Name: | `@LinuxIpHost` |
| Description: | The `@LinuxIpHost` annotation represents an IP Host for a Linux image that has been provisioned for the test. |
| Attribute: `imageTag` |  The `imageTag` should match the `imageTag` of the `@LinuxImage` this variable is to be populated with. |
| Syntax: | <pre lang="java">@LinuxIpHost(imageTag = "PRIMARY")<br>public IIpHost linuxHost;<br></pre> |
| Notes: | The `IIpHost` interface gives the test access to the IPv4/6 address and information about the Telnet, FTP and SSH ports on the Linux image.<br><br> See [LinuxIpHost](https://galasa.dev/docs/reference/javadoc/dev/galasa/linux/LinuxIpHost.html){target="_blank"} and [IIpHost](https://galasa.dev/docs/reference/javadoc/dev/galasa/ipnetwork/IIpHost.html){target="_blank"} to find out more. |


## Configuration Properties

The following are properties used to configure the Linux Manager.


### Host ID of a DSE Linux image

If you wish to configure your Galasa tests to connect to a specific Linux image, then you should set this property. This gives Galasa a specific Linux image to connect to and it will only use this one.

The `dseimageid` tag in the property should refer to the `imageTag` specified in the `@LinuxImage` annotation.

| Property: | Host ID of a DSE Linux image |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.dse.tag.[dseimageid].hostid` |
| Description: | An ID identifying a specified Linux image that can be connected to in a Galasa test. This property should contain the tag that is used as the `imageid` values in the following properties. |
| Required:  | No |
| Default value: | N/A |
| Valid values: | A valid string that can be used as an `imageid` in the other CPS properties of the Linux Manager |
| Examples: | `linux.dse.tag.PRIMARY.hostid=DEVIMAGE1` |


### Shared Linux images

If you wish to provide Galasa with a list of possible Linux images to connect to, and it is not important which one is selected, then you should set this property. This gives Galasa a selection of available Linux images and it will connect to one available during provisioning.

| Property: | Shared Linux images |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.shared.images` |
| Description: | A comma-separated list of images that are available to allocate to Galasa tests. This property should contain the tags that are used as the `imageid` values in the following properties. |
| Required:  | Yes |
| Default value: | N/A |
| Valid values: | A valid string that can be used as an `imageid` in the other CPS properties of the Linux Manager |
| Examples: | `linux.shared.images=UBT,IMAGE2` |


### Shared Linux provisioner priority

| Property: | Shared Linux provisioner priority |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.shared.priority` |
| Description: | The importance of the shared Linux provisioner compared to other Linux provisioners. The larger the number the more important. |
| Required:  | No |
| Default value: | `1` |
| Valid values: | An integer value between 1 and 100 |
| Examples: | `linux.shared.priority=1`, `linux.shared.priority=100` |


**Note that the DSE provisioner is always the highest priority of all Linux provisioners. There is no CPS property to control this, this behaviour comes from the Linux Manager. This means that, if available, a DSE Linux image will always be selected over a shared Linux image.**


### Linux image hostname

| Property: | Linux image hostname |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].ipv4.hostname` |
| Description: | The location of the Linux image. This property must contain an `imageid` tag that should correspond to an `imageid` specified by either the `linux.dse.tag.[dseimageid].hostid` or `linux.shared.images` property. |
| Required:  | Yes |
| Default value: | N/A |
| Valid values: | A valid DNS name or IPv4/6 address |
| Examples: | `linux.image.UBT.ipv4.hostname=192.168.2.3`, `linux.image.DEVIMAGE1.ipv4.hostname=192.168.2.3` |


### Linux image credentials

| Property: | Linux image credentials |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].credentials` |
| Description: | The credentials tag that the username and password to access the Linux image are stored with in the Galasa Credentials Store. This property contains the `imageid` tag that should correspond to an `imageid` specified by either the `linux.dse.tag.[dseimageid].hostid` or `linux.shared.images` property. If no `imageid` is specified, this property value is used for any provisioned Linux image. |
| Required:  | Yes, if credentials are required for your Linux image |
| Default value: | N/A |
| Valid values: | A string |
| Examples: | `linux.image.UBT.credentials=UBTCREDS`, `linux.image.credentials.UBTCREDS` |


### Linux image operating system

| Property: | Linux image operating system |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].operating.system` |
| Description: | The operating system of the shared Linux image. This property contains the `imageid` tag that should correspond to an `imageid` specified by either the `linux.dse.tag.[dseimageid].hostid` or `linux.shared.images` property. If no `imageid` is specified, this property value is used for any provisioned Linux image. |
| Required:  | Yes |
| Default value: | N/A |
| Valid values: | `ubuntu` or `any` |
| Examples: | `linux.image.[imageid].operating.system=UBUNTU`, `linux.image.operating.system=UBUNTU` |


### Linux image capabilities

| Property: | Linux image capabilities |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].capabilities` |
| Description: | A comma-separated list of special capabilities of the Linux image, if any. When a `@LinuxImage` has one or more `capabilities` requested in the attribute of the annotation, the Linux Manager will find a Linux image that has the requested capabilities with this CPS property. This property contains the `imageid` tag that should correspond to an `imageid` specified by either the `linux.dse.tag.[dseimageid].hostid` or `linux.shared.images` property. If no `imageid` is specified, this property value is used for any provisioned Linux image. |
| Required:  | No |
| Default value: | Null, i.e., no special capabilities |
| Valid values: | A string |
| Examples: | `linux.image.UBT.capabilities=desktop,wmq` |


### Linux image maximum slots

| Property: | Linux image maximum slots |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].max.slots` |
| Description: | The maximum number of Galasa tests that can run on a Linux image at once. This property contains the `imageid` tag that should correspond to an `imageid` specified by either the `linux.dse.tag.[dseimageid].hostid` or `linux.shared.images` property. If no `imageid` is specified, this property value is used for any provisioned Linux image. |
| Required:  | No |
| Default value: | `2` |
| Valid values: | An integer value |
| Examples: | `linux.image.UBT.max.slots=9`, `linux.image.max.slots=9` |


### Linux image archives directory

| Property: | Linux image archives directory |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].archives.directory` |
| Description: | The location the archives are stored on this Linux image. This property contains the `imageid` tag that should correspond to an `imageid` specified by either the `linux.dse.tag.[dseimageid].hostid` or `linux.shared.images` property. If no `imageid` is specified, this property value is used for any provisioned Linux image. |
| Required:  | No |
| Default value: | `/opt/archives` |
| Valid values: | A valid fully-qualified path |
| Examples: | `linux.image.UBT.archives.directory=/opt/archives`, `linux.image.archives.directory=/opt/archives` |


### Linux image username pool

| Property: | Linux image username pool |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].username.pool` |
| Description: | A set of static usernames or resource string patterns that can be used to allocate usernames, that can be used on the Linux image. This property contains the `imageid` tag that should correspond to an `imageid` specified by either the `linux.dse.tag.[dseimageid].hostid` or `linux.shared.images` property. If no `imageid` is specified, this property value is used for any provisioned Linux image. |
| Required:  | No |
| Default value: | `galasa{0-9}{0-9}` |
| Valid values: | A comma-separated list of static usernames, or resource string patterns that can be used to allocate usernames. A resource string pattern can consist of a constant like `galasa`, and variables like `{0-9}` that expand into a character, that when combined make the resource strings like `galasa1` or `galasa9` |
| Examples: | `linux.image.UBT.username.pool=galasa{0-9}{0-9}`, `linux.image.UBT.username.pool=BOB1,BOB2` |

 
### Retain run directory on Linux image

| Property: | Retain run directory on Linux image |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.image.[imageid].retain.run.directory` |
| Description: | Informs the Linux Manager if you would like the retain the run directory on the image after the test run is complete. This property contains the `imageid` tag that should correspond to an `imageid` specified by either the `linux.dse.tag.[dseimageid].hostid` or `linux.shared.images` property. If no `imageid` is specified, this property value is used for any provisioned Linux image. |
| Required:  | No |
| Default value: | `false` |
| Valid values: | `true` or `false` |
| Examples: | `linux.image.UBT.retain.run.directory=true` |


### Linux Manager extra bundles

| Property: | Linux Manager extra bundles |
| --------------------------------------- | :------------------------------------- |
| Name: | `linux.bundle.extra.managers` |
| Description: | Extra Galasa Managers that may be required to enable the Linux Manager. This may be required if your Linux images are stored in a platform that provides Infrastructure as a Service. If you require multiple extra managers, these should be provided in a comma-separated list. |
| Required:  | No |
| Default value: | N/A |
| Valid values: | A valid Galasa Manager package name or comma-separated list of Galasa Manager package names |
| Examples: | `linux.bundle.extra.managers=dev.galasa.openstack.manager` |