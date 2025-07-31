---
title: "VTP Manager"
---

This Manager is at Release level.


## Overview
  
Create an automated integration test in Galasa and use the VTP Manager to transparently record the interactions between the test and your CICS programs. Play back the recorded file by using IBM Z Virtual Test Platform (ZVTP). Playing back the recording in ZVTP means that you can test a change to your application code without running the test inside CICS. Run the recorded tests as part of your regression testing strategy before and after code changes to detect any anomalies. 

The VTP Manager can configure ZVTP by using CICS transactions to start and stop recording on a CICS region. After all recordings are complete, the VTP recording is exported as a sequence of flat files to the specified HLQ.  

Recordings are saved in the format `<HLQ_FROM_CPS>.<RUNID>.R<number>` where `number` is the number of the recorded test method. For example `CTS.JBLOGGS.VTP.R1234.R1`.

The RAS is updated with a file that details the recordings that were made. For example:

```
Method: test1 exported as: CTS.JBLOGGS.VTP.L877.R1
Method: test2 exported as: CTS.JBLOGGS.VTP.L877.R2
```

## Prerequisites

IBM Virtual Test Platform must be installed and configured in the target CICS region. 
  
To use the VTP Manager, VTP recording must be enabled and the test class must contain @CicsRegion annotations. A high-level qualifier (HLQ) must be defined in the CPS in order to write the recording. 
  
To enable VTP recording, set `vtp.recording.enable` to _true_ in the CPS properties file.

If VTP recording is enabled but no fields annotated with @CICSRegion are contained in the test class, the following message is returned _VTP Recording enabled but test class contains no CICS TS fields - recording will not be attempted_.
  
  
## Configuration Properties
  
The following properties are used to configure the VTP Manager.
  

### VTP Playback HLQ CPS Property

| Property: | VTP Playback HLQ CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | vtp.playback.hlq |
| Description: | The HLQ that is used to create VTP recording files |
| Required:  | Yes |
| Default value: | None |
| Valid values: | VTP.RECORD |
| Examples: | `vtp.playback.hlq=VTP.RECORD` |


### VTP CICS Transactions CPS Property

| Property: | VTP CICS Transactions CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | vtp.cics.[instanceid].transactions |
| Description: | A list of transactions to record in this CICS region |
| Required:  | No |
| Default value: | None |
| Valid values: | trx1,trx2 |
| Examples: | `vtp.cics.PRIMARY.transactions=TSQT,TSQD` |


### Enable VTP Recording CPS Property

| Property: | Enable VTP Recording CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | vtp.recording.enable |
| Description: | Should the VTP Manager be activated for this run |
| Required:  | No |
| Default value: | false |
| Valid values: | true, false |
| Examples: | `vtp.recording.enable=true` |

