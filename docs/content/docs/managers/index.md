---
title: "Managers"
---

You can find the links to the Javadoc API documentation for all the Galasa Managers on the [overview page](../reference/javadoc/overview-summary.html){target="_blank"}.


## Managers provided with the current Galasa distribution

!!! info "Key"

    Each Manager is tagged with one of the follow readiness indicators:

    ![alpha](../../assets/images/tags/alpha.svg)
    :   This Manager is being actively developed. It is subject to change and has not been extensively tested.

    ![beta](../../assets/images/tags/beta.svg)
    :   This Manager is almost ready. It has been tested and the TPI is stable, but there may be minor changes to come.

    ![release](../../assets/images/tags/release.svg)
    :   This Manager is feature complete, has passed all tests and is deemed release grade.


### CICS TS Managers

[CECI Manager](./cics-ts-managers/cics-ts-ceci-manager.md) ![release](../../assets/images/tags/release.svg){ valign=middle }
    
:   Provides CECI 3270 interaction - initially supporting containers and link programs.


CEDA Manager ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides CEDA 3270 interaction.


CEMT Manager ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides CEMT 3270 interaction.


[CICS TS Manager](./cics-ts-managers/cics-ts-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides configuration information for pre-existing CICS TS servers.
    Drives provisioning services from other managers, e.g. z/OS PT.


### IMS TM Managers

[IMS TM Manager](./ims-tm-managers/ims-tm-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides configuration information for pre-existing IMS TM systems.
    Drives provisioning services from other managers, e.g. z/OS 3270 Manager.


### Cloud Managers

[Docker Manager](./cloud-managers/docker-manager.md) ![release](../../assets/images/tags/release.svg){ valign=middle }

:   Enables containers to run on infrastructure Docker engines - either for testing directly or for assisting the testing process.


[Kubernetes Manager](./cloud-managers/kubernetes-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provisions Kubernetes namespaces for tests (or Managers) to use.


[OpenStack Manager](./cloud-managers/open-stack-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provisions Linux images on  servers within OpenStack.
    This Manager currently supports only Linux and provides the servers via the Linux Manager.


### Communications Managers

[HTTP Client Manager](./communications-managers/http-client-manager.md) ![release](../../assets/images/tags/release.svg){ valign=middle }

:   Provides a common setup of HTTP client operations for the test (or a Manager) to use.


[IP Network Manager](./communications-managers/ipnetwork-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides configuration information for IP-based servers.


[MQ Manager](./communications-managers/mq-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides the ability to connect to an existing Queue Manager and enables applications to read and write one or more messages to and from a queue.


### Core Managers

[Artifact Manager](./core-managers/artifact-manager.md) ![release](../../assets/images/tags/release.svg){ valign=middle }

:   Provides access to resources within a test bundle. It also provides templating services.


[Core Manager](./core-managers/core-manager.md) ![release](../../assets/images/tags/release.svg){ valign=middle }

:   The Core Manager provides tests with access to some of the core features within the Galasa Framework.
    The Core Manager is always initialised and included in a test run and contributes the logger, stored artefact root and test property annotations.


### Logging Managers

[ElasticLog Manager](./logging-managers/elasticlog-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Exports test results to ElasticSearch, which can be subsequently used within Kibana dashboards.


### Ecosystem Managers

[Galasa Ecosystem Manager](./ecosystem-managers/galasa-ecosystem-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Deploys an entire Galasa Ecosystem to Kubernetes to enable integration testing against Galasa.


### Test Tool Managers

[JMeter Manager](./test-tool-managers/jmeter-manager.md) ![beta](../../assets/images/tags/beta.svg){ valign=middle }

:   Configures and runs JMeter testing via Docker containers.


[SDV Manager](./test-tool-managers/sdv-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Create an automated test in Galasa and use the SDV Manager to record the security used during your test.


[Selenium Manager](./test-tool-managers/selenium-manager.md) ![beta](../../assets/images/tags/beta.svg){ valign=middle }

:   Allows tests to drive Web Browser testing using Selenium.


### Unix Managers

[Linux Manager](./unix-managers/linux-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Connect to a Linux image and run commands on that image via the command shell.
    Drive provisioning of images by using other Managers, for example, the OpenStack Manager.
    The Linux Manager depends on the IP Network Manager to provide the IP connection to the Linux image.


### Workflow Managers

[GitHub Manager](./workflow-managers/github-manager.md) ![release](../../assets/images/tags/release.svg){ valign=middle }

:   Use the GitHub Manager to help analyze test results.
    Identify test classes and test methods that have failed as the result of a known problem which is documented in an open GitHub issue, avoiding spending time investigating the cause of a failure of which the reason is known.


### z/OS Managers

[RSE API Manager](./zos-managers/rse-api-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides tests and Managers with access to RSE API functions.


[z/OS 3270 Manager](./zos-managers/zos3270terminal-manager.md) ![beta](../../assets/images/tags/beta.svg){ valign=middle }

:   Provides tests and Managers with a 3270 client.


[z/OS Batch z/OS MF Manager](./zos-managers/zos-batch-zos-mf-manager.md) ![beta](../../assets/images/tags/beta.svg){ valign=middle }

:   Provides the default implementation of the z/OS Batch Manager using z/OS MF.
    Can only be used via the z/OS Batch Manager interface.       


[z/OS Batch RSE API Manager](./zos-managers/zos-batch-rse-api-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides an implementation of the z/OS Batch Manager using the RSE API.
    Can only be used via the z/OS Batch Manager interface.


[z/OS Console oeconsol Manager](./zos-managers/zos-console-oeconsol-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides an implementation of the z/OS Console by using the z/OS UNIX oeconsol command.
    Can only be used via the z/OS Console Manager interface.                                                                               


[z/OS Console z/OS MF Manager](./zos-managers/zos-console-zos-mf-manager.md) ![beta](../../assets/images/tags/beta.svg){ valign=middle }

:   Provides the default implementation of the z/OS Console by using z/OS MF.
    Can only be used via the z/OS Console Manager interface.


[z/OS File RSE API Manager](./zos-managers/zos-file-rse-api-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides an implementation of the z/OS File Manager by using RSE API.
    Can only be used via the z/OS File Manager interface.


[z/OS File z/OS MF Manager](./zos-managers/zos-file-zos-mf-manager.md) ![beta](../../assets/images/tags/beta.svg){ valign=middle }

:   Provides the default implementation of the z/OS File Manager using z/OS MF.
    Can only be used via the z/OS File Manager interface.                        


[z/OS Manager](./zos-managers/zos-manager.md) ![beta](../../assets/images/tags/beta.svg){ valign=middle }

:   Provides tests and Managers with access to z/OS images, sysplexes and log information.
    Additionally, the z/OS Manager contributes annotations which allow you to run batch jobs, issue console commands, transfer files and securely access z/OS systems via TSO or UNIX commands.


[z/OS MF Manager](./zos-managers/zos-mf-manager.md) ![beta](../../assets/images/tags/beta.svg){ valign=middle }

:   Provides tests and Managers with access to z/OS MF functions.


[z/OS Program Manager](./zos-managers/zos-program-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Compiles test programs from source embedded in the Galasa test bundle at test start.
    The executable load module is then available for use in the test.


[z/OS TSO Command SSH Manager](./zos-managers/zos-tso-command-ssh-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides the default implementation of the z/OS TSO Command Manager using SSH.
    Can only be used via the z/OS TSO Command Manager interface.


[z/OS UNIX Command SSH Manager](./zos-managers/zos-unix-command-ssh-manager.md) ![alpha](../../assets/images/tags/alpha.svg){ valign=middle }

:   Provides the default implementation of the z/OS UNIX Command Manager using SSH.
    Can only be used via the z/OS UNIX Command Manager interface.
