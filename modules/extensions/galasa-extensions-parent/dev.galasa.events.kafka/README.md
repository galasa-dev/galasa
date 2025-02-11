# Galasa Kafka extension


This project contains the Galasa Kafka extension which is used to optionally produce events directly from Galasa to various topics in the configured Kafka cluster.


Currently, the extension only supports production of events for reporting. Consumption is planned for a later date.


## Required configuration to use the Kafka extension


1. The extension has already been added as an extra bundle in the bootstrap properties so no change required there.
1. You must provide your Kafka cluster's bootstrap servers with the CPS property `kafka.bootstrap.servers`. For example, `kafka.bootstrap.servers=host1:port1,host2:port2,host3:port3`.
1. You must provide a service credential token with write access to your Galasa ecosystem by creating a Secret called 'event-streams-token'. The Galasa ecosystem Helm chart will take the value from that Secret and provide it to the Pods that need it as an environment variable. Please see the [Helm documentation](https://github.com/galasa-dev/helm/blob/main/README.md) for more information.
1. To produce Framework events, the CPS property `framework.produce.events` must be set to `true`. If there is no value set in the CPS, it assumes the value of false, therefore event production must be explicitly opted in to. **Currently we only support Framework events but we are looking to grow this list - see the table below for current event types we support.**
1. You can opt in to the specific event types you want Galasa to produce to your cluster by specifying a topic name. If you have not specified a topic name that an event type should be sent to, that event won't be produced. For example, if you want your cluster to receive TestRunLifecycleStatusChangedEvents, you must set the CPS property: `kafka.testrunlifecyclestatuschangedevent`.


### Event types currently supported:
| Event Name | Description | Required CPS property | 
| --- | --- | --- |
| TestRunLifecycleStatusChangedEvent | A test run's lifecycle status has changed | The topic name you wish to publish this event to - for example `kafka.testrunlifecyclestatuschangedevent.topic.name=GalasaTests.StatusChangedEvents` |
| TestHeartbeatStoppedEvent | A test run's heartbeat has been stopped | The topic name you wish to publish this event to - for example `kafka.testheartbeatstoppedevent.topic.name=GalasaTests.HeartbeatStoppedEvents` |


## How to set up your Galasa ecosystem to use the Kafka extension


1. Provide your Kafka cluster's bootstrap servers: `galasactl properties set --bootstrap ${BOOTSTRAP} --namespace kafka --name bootstrap.servers --value host1:port1,host2:port2,host3:port3`
1. Create a Secret in your Galasa ecosystem called 'event-streams-token' so Galasa can authenticate to your cluster. See the [Helm documentation](https://github.com/galasa-dev/helm/blob/main/README.md) for instructions.
1. Activate event production. To activate Framework event production: `galasactl properties set --bootstrap ${BOOTSTRAP} --namespace framework --name produce.events --value true`
1. Provide the topic names in your Kafka cluster you want certain events to be published to.

    a. If you want Test Run Lifecycle Status Changed events: `galasactl properties set --bootstrap ${BOOTSTRAP} --namespace kafka --name testrunlifecyclestatuschangedevent.topic.name --value GalasaTests.StatusChangedEvents` 

    b. If you want Test Heartbeat Stopped events: `galasactl properties set --bootstrap ${BOOTSTRAP} --namespace kafka --name testheartbeatstopped.topic.name --value GalasaTests.HeartbeatStoppedEvents`

You should now be set up to receive events from Galasa directly into your configured Kafka topics on your cluster.


## What's next?

* Improve this extension and provide more configuration options to customise it for your use cases.
* Provide consumption of events in the extension in future for more advanced use cases such as triggering and scheduling tests based on events.


## More support


Questions related to the usage of the Galasa Kafka extension can be posted on the <a href="https://openmainframeproject.slack.com/archives/C061Q1CHV51" target="_blank"> galasa-help Slack channel</a>. If you're not a member of the Slack channel yet, you can <a href="https://openmainframeproject.slack.com/join/shared_invite/zt-2iicqylmu-B6N0ASxfP9Q5JDnv7FYFgw#/shared-invite/email" target="_blank"> register to join</a>.


## Contributing

If you are interested in contribution to the development of Galasa, take a look at the documentation and feel free to post a question on the [Galasa Slack channel](https://openmainframeproject.slack.com/archives/C061Q1CHV51) or raise new ideas, features or bugs as issues on [GitHub](https://github.com/galasa-dev/projectmanagement).

Take a look at the [contribution guidelines](https://github.com/galasa-dev/projectmanagement/blob/main/contributing.md).

## License

This code is under the [Eclipse Public License 2.0](https://github.com/galasa-dev/galasa/blob/main/LICENSE).
