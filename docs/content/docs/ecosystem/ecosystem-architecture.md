---
title: "Ecosystem Architecture"
---

The following diagram provides a high-level representation of the Galasa Ecosystem architecture:<br><br> ![Galasa ecosystem architecture:](ecosystem-overview-new.svg)


<br>The Ecosystem is made up of a collection of microservices for orchestrating runtimes, monitoring tests and resources, and providing clean-up of those resources if required. The Ecosystem provides a centralized store for run configurations, a single location for storing all test results and test artifacts, and utilizes a REST endpoint that can be called from any IDE or pipeline. 


### Key components:

The following diagram highlights some of the key components that make up the Galasa Ecosystem and examples of external resources with which Galasa can interact: ![Galasa ecosystem:](galasa-ecosystem-architecture-new.svg)

<br>Expand the following sections to find out more about the key components of the Galasa Ecosystem:

<details>
<summary>Galasa Stores</summary>

|                                   |                                                                                                                          |
| ------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------- |
| **Configuration Property Store**                       | The configuration property store (CPS) defines object properties, topologies, system configurations, and definitions which instruct the way in which a Galasa test runs. For example, properties for endpoints, ports and timeouts. When running in an ecosystem, all Galasa tests will use the same CPS configuration, unless any overrides are passed at submission. It is the CPS and the configurational properties that enable tests to run against multiple environments, without changing the code inside the test. *Note:* As IP addresses and ports of test machines are stored within the CPS on a users system, we recommend that hard drive encryption is turned on in the operating system where possible.        |
| **Dynamic Status Store**               | The dynamic status store (DSS) provides status information about the ecosystem and the tests that are running. The DSS is used by the resource manager and engine controller to ensure the limits that are set in the CPS configuration are not exceeded. DSS property values change dynamically as tests are run, showing the resources that are currently being used, shared or locked by a test, so that workloads can be limited to avoid throttling. When running in automation, the DSS is shared by every instance of the framework.                                                             |
| **Result Archive Store**                      | The result archive store (RAS) is a single database which stores all elements of a test, including the test results, run logs, and test artifacts. These elements can be used to help diagnose the cause of any failures encountered as a result of running a test, or to gather information about a test.  Storing all of this information in one place makes it simple for entire teams to view results.                              |
| **Credentials Store**                   | The credentials store (CREDs) securely provides the credentials, for example, password, username and personal access token that are required for a test to run in automation. The CREDs is hosted in the etcd server.    |
| **etcd**                       | The etcd server is a highly available key-value pair store which hosts the Configuration Property Store (CPS), the Dynamic Status Store (DSS) and the Credentials Store (CREDs). The etcd server stores and maintains a single, consistent source of the truth about the status of the ecosystem at any given point in time.      |
| **CouchDB**                       | This database runs inside Docker container or Kubernetes pod and contains the Result Archive Store (RAS).        |


</details>


<details>
<summary>Galasa Servers</summary>

|                                   |                                                                                                                          |
| ------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------- |
| **Engine controller**                       | The engine controller enables tests to run at scale within the ecosystem. This service is responsible for instantiating individual test engines by creating Docker containers or Kubernetes pods to manage and execute individual Galasa automation test runs. The engine controller allocates a test engine to a test submission if the required resources for the test are available. Otherwise, the test is put into a waiting state to be run at an appropriate time.        |
| **Resource Management**                       | The resource management service monitors running tests and resources that have been marked as in use. If a test case becomes stale or is ended manually, this service performs clean up actions to ensure that the resources are entered back into the pool for another test to use. This service can perform large pieces of work, including the de-provisioning of an environment.   |
| **Metrics Server**                       | The metrics server indicates the health of the ecosystem, for example, providing metrics on the number of successful test runs.        |
| **API Server**                       |  The API server acts as a central point from which to control the Galasa Ecosystem and is used by Galasa as an endpoint with which IDEs and pipelines interact for submitting tests and retrieving results. The API server hosts the bootstrap server.       |
| **Bootstrap Server**                       | The bootstrap server is part of the API server. The bootstrap is an endpoint that is provided by the API server to store the initial configuration required to instantiate a Galasa framework. When setting up the Galasa Ecosystem, the IDE must be updated to point to the bootstrap that is configured to use the ecosystem.      |
| **Galasa Web UI**                       | The Galasa Web UI is currently under construction and planned for a future release. Use the WebUI to see a dashboard overview of the current and historical health of the Galasa framework. The UI can also run, schedule or reschedule tests, be used to analyse output from failed test runs, and manage the configuration needed to customise the framework and  tests for maximum throughput, resilience and flexibility.        |
| **Dex**                       | The Galasa Ecosystem Helm chart's use of Dex is under development and is subject to change. In a future release, Dex will be used to authenticate users interacting with a Galasa Ecosystem.      |

</details>

<details>
<summary>Code Deployment</summary>

|                                   |                                                                                                                          |
| ------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------- |
| **Maven Repositories and OBRs**                       | For tests to run in the ecosystem they require compiled artifacts to be hosted in a Maven repository. The artifacts must be bundled as an OSGI bundle -  Galasa provides a Maven plug-in to create these bundles.       |
| **Nexus**                       | A Nexus server enables deployment of Maven artifacts to the ecosystem and can be used to host Docker images. If you have an alternative internal artifact repository, you can use that instead of Nexus.     |

</details>

<details>
<summary>Optional reporting servers</summary>

|                                   |                                                                                                                          |
| ------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------- |
| **Prometheus**                       | The Prometheus server scrapes and stores metrics from defined endpoints from the metrics server. These metrics provide information on the rate of  throughput of tests in the ecosystem.   |
| **Grafana**                       | A dashboard for visualizing Prometheus metrics. Data can be explored through queries and drilldown.       |
| **Elasticsearch**                       | Provides an Elastic search instance to record the results of automated test runs.        |
| **Kabana**                       | A dashboard for visualizing Elasticsearch metrics. Data can be explored through queries and drilldown.       |

</details>

### Key concepts:

These concepts are relevant when running tests locally, or when submitting tests to a remote Galasa service:
- A **test case** - A piece of test logic which can be compiled or translated into something which is runnable by the Galasa framework. This may be a piece of Java code for example, or a Gherkin test feature.
- A **test run** - Also known as a 'run' - An execution of the testcase which was started at a certain point in time, executed logic steps, and is still running, or has finished with a test status and result. For example: Passed or Failed.

Some concepts are only relevant on a Galasa service:
- A **user** - The authorised person manipulating the Galasa system in some way.
- **Role based access control (RBAC)** - The mechanism where the Galasa service will allow some users to perform any available task, but limit what other users can do. This is to help isolate some system capabilities for safety or for security. Read more about the RBAC in the [role-based access](./ecosystem-role-based-access.md) section.

