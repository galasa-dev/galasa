---
title: "MQ Manager"
---

This Manager is at Alpha level. You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/mq/package-summary.html){target="_blank"}.


## Overview

This Manager provides the ability to connect a test to an existing IBM MQ queue manager, enabling one or more messages to be written to and read from existing queues.


## Annotations

The following annotations are available with the MQ Manager


### MQ Manager

| Annotation: |MQ Manager |
| --------------------------------------- | :------------------------------------- |
| Name: | @QueueManager |
| Description: | The `@QueueManager` annotation represents the name of the IBM MQ queue manager |
| Attribute: `tag` |  Specifies which queue manager to connect to. Default value is _PRIMARY_. |
| Syntax: | @QueueManager<br> public IMessageQueueManager qmgr;<br> |
| Notes: | The `IMessageQueueManager` interface enables connection to the IBM MQ queue manager.  |


### MQ Queue

| Annotation: |MQ Queue |
| --------------------------------------- | :------------------------------------- |
| Name: | @Queue |
| Description: | The `@Queue` annotation represents the name of the IBM MQ queue |
| Attribute: `queueMgrTag` |  Specifies which queue manager to connect to. Default value is _PRIMARY_.  |
| Attribute: `name` |  Specifies the name of the queue as it appears on the queue manager. Use the `name` attribute if the queue name never changes, regardless of environment.  |
| Attribute: `tag` |  The name of the queue. Use the `tag` attribute when the value of the queue name is defined by a property in the CPS.  |
| Notes: | You must specify either the `name` or the `tag` attribute but not both. If you specify both or neither, an exception is generated. |
| Attribute: `archive` |  Archive messages retrieved and sent messages from and to this queue into the RAS to aid debugging. Default value is _true_. Valid values are _true_ and _false_. |
| Syntax: | @Queue<br> public IMessageQueue queue;<br> |
| Notes: | The `IMessageQueue` interface enables the test to put the provided messages onto the IBM MQ queues and retrieve messages from the IBM MQ queues.  |


## Code snippets

Use the following code snippets to help you get started with the MQ Manager. Use the MQ Manager to connect a test to an existing IBM MQ queue manager and queue. Galasa does not provision new IBM MQ queue managers or queues. 


### Create an instance of an IBM MQ Manager

The following snippet shows the code that is required to create an instance of an IBM MQ Manager:

```java
@QueueManager
public IMessageQueueManager qmgr;
```


### Instantiate an IBM MQ queue

The following snippet shows the code that is required to instantiate an IBM MQ queue:

```java
@QueueManager()
public IMessageQueueManager qmgr;

@Queue(archive = true, name = "GALASA.INPUT.QUEUE")
public IMessageQueue queue;
```

You can just as simply instantiate multiple IBM MQ queues:

```java
@QueueManager()
public IMessageQueueManager qmgr;

@Queue(archive = true, name = "GALASA.INPUT.QUEUE")
public IMessageQueue queue;
	
@Queue(archive = false, name = "GALASA.INPUT.QUEUE2")
public IMessageQueue queue2;
	
@Queue(tag = "NEWQUEUE")
public IMessageQueue queue3;
```

You can also instantiate multiple IBM MQ queues on multiple queue managers:

```java
@QueueManager(tag = "DISTQMGR")
public IMessageQueueManager distributedQueueManager;

@QueueManager(tag = "CLOUDQMGR")
public IMessageQueueManager cloudQueueManager;
	
@Queue(archive = true, name = "GALASA.SEND.QUEUE1", queueMgrTag = "DISTQMGR")
public IMessageQueue queue1;

@Queue(archive = true, name = "GALASA.SEND.QUEUE2", queueMgrTag = "DISTQMGR")
public IMessageQueue queue2;

@Queue(archive = false, name = "GALASA.RECEIVE.QUEUE3", queueMgrTag = "CLOUDQMGR")
public IMessageQueue queue3;
	
@Queue(archive = true, name = "GALASA.RECEIVE.QUEUE4", queueMgrTag = "CLOUDQMGR")
public IMessageQueue queue4;
```


### Put messages to an IBM MQ queue

Multiple message objects can be passed to the _sendMessage_ method. The following snippets show the code required to create a text or binary message to put on an IBM MQ queue: 

```java
TextMessage tm = qmgr.createTextMessage(testData);
queue.sendMessage(tm);
```
```java
TextMessage tm = qmgr.createTextMessage(testData);
queue.sendMessage(tm,tm,tm,tm,tm,tm,tm,tm);
```
```java
byte[] input = {41,01,33,76};
BytesMessage m = qmgr.createBytesMessage(input);
queue.sendMessage(m);
```


### Read messages from an IBM MQ queue

The following snippet shows the code required to read a message from an existing IBM MQ queue:

```java
Message m = queue.getMessage();
String response = m.getBody(String.class);
```


### Clear messages from an IBM MQ queue

The following snippet shows the code required to clear messages from an IBM MQ queue:

```java
queue.clearQueue();
```


## Configuration Properties

The following are properties used to configure the MQ Manager:


### Instance ID for the tag CPS Property

| Property: | Instance ID for the tag CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | mq.tag.[tag].instanceid |
| Description: | The instance for the specified tag |
| Required:  | Yes |
| Default value: | None |
| Valid values: |  |
| Examples: | `mq.tag.[tag].instanceid=QUEUEMGR1` |

This property is associated with the _@QueueManager_ annotation attribute `tag`. You can use the `tag` attribute to avoid hardcoding in your test code. Because the properties that are associated with the attribute are stored in the CPS, the same test can run against a different queue manager without changing the test code. 


### Queue name for the tag CPS Property

| Property: | Queue name for the tag CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | mq.queue.[tag].queuename |
| Description: | The queue name for the specified tag |
| Required:  | Yes |
| Default value: | None |
| Valid values: |  |
| Examples: | `mq.queue.[tag].queuename=GALASA.INPUT.QUEUE` |

This property is associated with the _@Queue_ annotation attribute `tag`. You can use the `tag` attribute to avoid hardcoding in your test code. Because the properties that are associated with the attribute are stored in the CPS, the same test can run against a different queue without changing the test code. 


### Queue manager channel CPS Property

| Property: | Queue manager channel CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | mq.server.[instanceid].channel |
| Description: | The channel for the specified instance |
| Required:  | Yes |
| Default value: | None |
| Valid values: |  |
| Examples: | `mq.server.[instanceid].channel=DEV.APP.SVRCONN` |


### Queue manager credentials CPS Property

| Property: | Queue manager credentials CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | mq.server.[instanceid].credentials.id |
| Description: | The credentials for the queue Manager |
| Required:  | Yes |
| Default value: | None |
| Valid values: |  |
| Examples: | `mq.server.[instanceid].credentials.id=CRED1` |


### Queue manager host CPS Property

| Property: | Queue manager host CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | mq.server.[instanceid].host |
| Description: | The host for the specified instance |
| Required:  | Yes |
| Default value: | None |
| Valid values: |  |
| Examples: | `mq.server.[instanceid].host=127.0.0.1` |


### Queue manager name CPS Property

| Property: | Queue manager name CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | mq.server.[instanceid].name |
| Description: | The queue manager name for the specified instance |
| Required:  | Yes |
| Default value: | None |
| Valid values: |  |
| Examples: | `mq.server.[instanceid].name=QM1` |


### Queue manager port CPS Property

| Property: | Queue manager port CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | mq.server.[instanceid].port |
| Description: | The queue manager port for the specified instance |
| Required:  | Yes |
| Default value: | None |
| Valid values: |  |
| Examples: | `mq.server.[instanceid].port=1414` |

