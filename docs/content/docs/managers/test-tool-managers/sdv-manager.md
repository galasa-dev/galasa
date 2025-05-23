---
path: "/docs/managers/sdv-manager"
title: "SDV Manager"
---


This Manager is at Alpha level.<br>


[Overview](#overview)<br>
[Configuring](#configuring)<br>


# <a name="overview"></a>Overview
  
Create an automated integration test in Galasa and use the **Security Definition Validation (SDV)** Manager to obtain a report of the Security Definitions that would have been required if **Resource Security** and **Command Security** were turned on.

To achieve this, your tests should be **role-based**. Users belonging to particular roles should be used throughout your tests. Their interactions on each unique CICS region under test and the resulting Security Definitions of those interactions, will be recorded.

The Security Definitions found in the test will be available as a report within the `sdv` folder of the test runs **Stored Artifacts**, in human-readable, **YAML format**. If no Security Definitions were witnessed in a test run, the report will contain a message indicating this. The reports will be grouped and named, firstly by the Java test package name, then secondly by each tested CICS region tag within the test class. For example:

```
dev.galasa.sdv.tests/dev.galasa.sdv.tests.featurea.TestClassOne.SDVCICSA.cics-security.yaml
dev.galasa.sdv.tests/dev.galasa.sdv.tests.featurea.TestClassOne.SDVCICSB.cics-security.yaml
dev.galasa.sdv.tests/dev.galasa.sdv.tests.featureb.TestClassTwo.SDVCICSA.cics-security.yaml
```

Users must be obtained for use within your test via the `@SdvUser` annotation. This annotation will allocate the test a user belonging to a role mapped to the provided `roleTag` (see **SDV Role CPS Property** below), for use on a provided CICS region via the provided `cicsTag`. All Security Definitions used by this user on the specified CICS region will be recorded. If the user interacts with a CICS region different from the stated one, those security accesses will not be recorded, a seperate `@SdvUser` would need to be declared to record those interactions. If a `cicsTag` is not provided, `PRIMARY` is assumed.  
Users are allocated from a pool of users. As an example, the following obtains a user which will conduct interactions on CICS Region tag `A` that belongs to the role mapped to the `R1` **roleTag** in the CPS properties:

```
@SdvUser(cicsTag = "A", roleTag = "R1")
public ISdvUser user1;
```

The SDV manager will ensure that a user is only allocated to one test, per CICS Region at any one time to avoid Security Definition "noise". When a test run ends, the user will be released back to the pool for the next test to use.
If there are no available users in the pool because they are all already allocated, the test will fail with an `ResourceUnavailableException`. The Galasa framework will then continue to re-attempt to run the test at fixed periods of time until a user is available and the test can run.

To ensure the Security Definitions are consistent for each run of a test, any resources that are created by the test should have a fixed name. As an example, if a datetime stamp is used in a resource naming strategy, this would be different each time the test is ran, therefore the Security YAML will be different each test run, which will be found if running any kind of before/after comparison.

This manager can be used within a **Continuous Integration** pipeline to identify, and validate changes in Security Definitions as a result of a request to change application, or test code. Contributing towards a larger DevSecOps strategy.  
<a href="https://www.ibm.com/docs/en/cics-ts/latest?topic=hiwztic-how-it-works-capturing-validating-security-definitions-during-development-process" target="_blank" >Visit the CICS TS documentation</a> for an introduction to the concept and for links to a demonstration of this in action in an example CI pipeline.

## Prerequisites

CICS feature, **Security Definition Capture (SDC)** must be active in each region under test for the SDV to function. SDC can be <a href="https://www.ibm.com/docs/en/cics-ts/latest?topic=region-configuring-security-definition-capture-sdc" target="_blank" rel="noopener noreferrer"> manually configured </a>before using the SDV manager or it can be automatically configured by the SDV manager before the test is ran using the relevant CPS properties in the [Configuring](#configuring) section.
  
To use the SDV Manager, the test class must contain at least one `@SdvUser` annotation. If one is not provided, the SDV Manager is not invoked and the Galasa test will run as normal without Security recording.
  
At least one z/OS user must exist for each role defined in your configuration. All pool users should be manually created in the z/OS system' Security DB, with membership to the correct roles, with the correct permissions.
  
All pool users must be added to the Galasa credentials store, assigned with a unique credentials tag.

All CICS regions under test must be provisioned to a started state before the SDV manager runs, and region must be running when test ends in-order to run job to gather Security reports. Exceptions will be encountered if either of these scenarios do not exist.

All CICS regions under test must have SIT parameter `SEC=YES`.
  
## <a name="configuring"></a>Configuring

### CPS properties
  
The following properties are used to configure the SDV Manager.
  
<details>
<summary>SDV Region HLQ CPS Property</summary>

| Property: | SDV Region HLQ CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | sdv.cicsTag.[cicsTag].hlq |
| Description: | The HLQ for each CICS region used in testing. Used to locate YAML job script. |
| Required:  | Yes |
| Default value: | None |
| Valid values: | CICS.INSTALL |
| Examples: | <code>sdv.cicsTag.A.hlq=CICS.INSTALL<br> </code> |

</details>
 
<details>
<summary>SDV Pool Users CPS Property</summary>

| Property: | SDV Pool Users CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | sdv.zosImage.[imageID].role.[Role].credTags |
| Description: | A list of Galasa Credential tags belonging to a role, for a given z/OS image. |
| Required:  | Yes |
| Default value: | None |
| Valid values: | USER1,USER2 |
| Examples: | <code>sdv.zosImage.IMAGEA.role.TELLER.credTags=USER1,USER2</code> |

</details>

<details>
<summary>SDV Port CPS Property</summary>

| Property: | SDV Port CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | sdv.cicsTag.[cicsTag].port |
| Description: | The port the SDC service listens on, on a given CICS region. |
| Required:  | Yes |
| Default value: | None |
| Valid values: | 32000 |
| Examples: | <code>sdv.cicsTag.A.port=32000</code> |

</details>

<details>
<summary>SDV Role CPS Property</summary>

| Property: | SDV Role CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | sdv.roleTag.[roleTag].role |
| Description: | The Role tag mapping to a role name. |
| Required:  | Yes |
| Default value: | None |
| Valid values: | TELLER |
| Examples: | <code>sdv.roleTag.A.role=TELLER</code> |

</details>

<details>
<summary>SDV SDC Activation CPS Property</summary>

| Property: | SDV SDC Activation CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | sdv.cicsTag.[cicsTag].SdcActivation |
| Description: | Should the SDV Manager configure and activate SDC, and all its pre-requisites?<br/>Done on a per-region basis and will remove all created resources at the end of the test (minus the SRR logstream).<br/>Will configure the SDC service to listen on the port specified via property `sdv.cicsTag.[cicsTag].port`.<br/>Should be considered when using temporarily provisioned CICS regions for test. |
| Required:  | No |
| Default value: | false |
| Valid values: | true, false |
| Examples: | <code>sdv.cicsTag.A.SdcActivation=true</code> |

</details>

<details>
<summary>SDV SRR Logstream Removal CPS Property</summary>

| Property: | SDV SRR Logstream Removal CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | sdv.cicsTag.[cicsTag].SrrLogstreamRemoval |
| Description: | Should the SDV Manager delete the SRR Journal and model at the end of a test?<br/>Done on a per-region basis. Should be considered when using temporarily provisioned CICS regions for test. |
| Required:  | No |
| Default value: | false |
| Valid values: | true, false |
| Examples: | <code>sdv.cicsTag.A.SrrLogstreamRemoval=true</code> |

</details>


