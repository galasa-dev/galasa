---
title: "Exploring Galasa SimBank"
---

Distributed with Galasa, SimBank is a component that simulates a mainframe application. It sits above another component called SimPlatform. As delivered, SimBank implements a sample banking application against which you can configure and run a set of provided tests in preparation for running your own tests against an *actual* mainframe application. You can also practice writing some new tests to run against the SimBank banking application.

By exercising the Galasa framework against SimBank, you can pre-empt a lot (but not all) of the work and learning necessary to eventually hook your own tests up with a genuine mainframe environment. If the provided SimBank tests do not work, then it is unlikely that you will be able to run your own tests on a mainframe application. In summary, SimBank helps you to understand Galasa's basic principles of operation before you learn how to connect Galasa to your own mainframe application-under-test. Start by installing Galasa Simbank, either using the 
[Running Galasa SimBank online](simbank-cli) documentation if you are working with the Galasa CLI repository in GitHub, or the [Running Galasa SimBank offline](simbank-cli-offline) documentation if you are using the Galasa zipped distribution.

Galasa SimBank comes with a selection of prepared Galasa tests. You can find out more about these tests in the following sections. Follow the flow of logic in these classes and understand more about the Java that is used to create them, including how to use Galasa annotations and review documented test methods. A good place to start is with the [SimBank IVT](simbank-IVT) documentation. You can then move on to look at the other tests that are provided with Galasa. Sample SimBank tests are available in the <a href="https://github.com/galasa-dev/simplatform/tree/main/galasa-simbank-tests/dev.galasa.simbank.tests/src/main/java/dev/galasa/simbank/tests" target="_blank"> Galasa simplatform repository</a> in GitHub. 


