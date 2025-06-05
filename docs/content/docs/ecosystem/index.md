---
title: "The Galasa Ecosystem"
---

To realise the full power of Galasa, you need to run your tests inside a Galasa Ecosystem. The ecosystem enables you to run automated testing away from your workstation, outside of a local JVM. 

The SimBank tests showcase how the Galasa framework can run inside a locally hosted JVM. Local runs use their own Galasa property files to act as configurational services, making it easy to instantiate and run tests.

However, there are limitations to taking a local-only approach:

- Configuration settings, test results and test artifacts are stored locally, so cannot be easily shared across teams using Galasa tools
- Tests cannot be run headlessly, so the workstation must be kept active
- Scaling capabilities are limited by workstation resources
- Monitoring and Management features are not available, for example, test streams and the test catalog and dashboarding capabilities


## Running tests in the Galasa Ecosystem

The Galasa Ecosystem is a cloud native application which exposes a sequence of microservices that are used to manage the set of running tests. Running tests inside the ecosystem provides a number of benefits:

- **Sharing tests across an enterprise**  
When tests run inside the ecosystem, the Galasa framework provides the ability to scale horizontally to run large numbers of tests in parallel - enabling more testing to complete in a shorter timeframe. The ability to run tests at scale is one of the key features that differentiates Galasa from other test frameworks. Data is locked whilst in use, preventing cross contamination with other running tests. 

- **Re-usability**  
 With Galasa, one person can complete the configurations for use across all test runs. Galasa configurations are maintained in a single location - the Configuration Property Store (CPS) and so can be shared across an organization for use by other tests.  Setting these properties centrally establishes a single source of truth and means that testers do not need to know about or configure these properties each time they write a new test. Test run results, run logs and artifacts are also stored in one central location, again enabling sharing across teams.
 
 - **Testing as a service**  
 With an established Galasa Ecosystem you can run your testing as a service; regression tests, application tests, system verification tests and time-consuming adhoc tests can be run on demand by using Galasa as part of your DevOps pipeline and in a cloud environment. Workload is directed away from the CI pipeline and is run in its own dedicated environment, preventing computational resources from being diverted away from other jobs that are running within the pipeline. 

- **Automated test runs**  
The Galasa test catalog can be used as part of the ecosystem to run automated testing away from a workstation without the need for local test material. The catalog can be configured to store related tests within a shared test catalog, enabling tests to be automatically selected to run for any given change set. The test catalog uses the latest version of test cases, so you know the tests that you're running are up-to-date.


