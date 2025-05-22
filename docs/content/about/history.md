---
title: "A brief history of Galasa"
---

## The Problem

Galasa started life as JAT (JUnit Automated Testing) - an internal IBM tool extensively used for integration testing within IBM z/OS teams. It was created to help eliminate repetitive manual and semi-automated testing. These manual tests could be complex and take a long time to set up and run, increasing the risk of introducing human-error into the process. It was also time-consuming to identify the cause of failures, as diagnostics were stored in different repositories, including logs and program compilation output on z/OS, and test script log output on laptops. Changes to IBM CICS could take many weeks to reach Beta as tests took too long to execute, increasing the time required to deliver. 


## The Solution

JAT was created to solve these problems. The framework was inspired by JUnit but with a focus on functional and integration testing. JAT is written in Java - in part because of Java's run-anywhere capability, huge number of utility libraries and its extensive ecosystem. However, the team found that Java expertise is not essential to use it. 

Executing a test was made much easier and quicker as test code could be run at the push of a single button. Automated scheduling of test runs enabled the continuous testing of new code in a timely and cost-effective manner.

In JAT, all aspects of a test run are automatically recorded and stored in a single repository. If a test fails in an automated pipeline, simplified access to logs, screenshots and data makes it quick and easy to diagnose the problem. The system is highly scalable, allowing a large number of tests to be run in parallel against many releases of CICS, enabling IBM to deliver a consistent, reliable product.


## The Result

After JAT was rolled out across CICS, the team fully automated their tests, including tasks such as creating test data, running batch jobs, invoking RESTful interfaces, interacting with 3270 terminals, and provisioning and de-provisioning test environments. Manual regression was virtually eliminated, giving the team more time to focus on exploratory testing to root out hard-to-find defects. Repeatable, scalable and reliable integration tests running as part of the DevOps pipeline helped to reduce the delivery of a new CICS feature from 18 to 3 months without any loss of confidence in quality. In fact, JAT was so successful, that it was adopted by other teams across IBM. 


## The Future
The success of JAT inspired us to perfect the architecture and release our exciting new framework as Galasa. 

Galasa is a type of Snout Moth and a moth was attached to the first "bug" report by Grace Hopper where she reported the "first actual case of a bug being found" as a moth had flown into a relay. While we do not know if it was a Galasa moth in the relay, we quite like the name.

We’d love you to get involved with the project and help us establish a collaborative community where we can share great ideas and build new, cool Galasa features together.
