---
title: "Galasa Ecosystem Manager"
---

This Manager is at Alpha level. You can view the <a href="https://javadoc.galasa.dev/dev/galasa/galasaecosystem/package-summary.html" target="_blank" rel="noopener noreferrer">Javadoc documentation for the Manager here</a>.<br>



[Overview](#overview)<br>
[Provided annotation](#annotations)<br>
[Code snippets and examples](#codesnippets)<br>


# <a name="overview"></a>Overview
This Manager provides the test with a fully provisioned Galasa Ecosystem on which to test.  When the test starts running the test can be assured that all the services are up and working. <br><br> The Galasa Ecosystem Manager supports Galasa Shared Environments. Shared Environments provide  the ability to create a test environment that can be shared across multiple test runs  so you don't have to provision a test environment for each test.

## Limitations
The Manager only supports the following platforms:<br> - Kubernetes Namespace<br> <br> In the near future, this Manager will be able to provision ecosystems in Docker and on a plain Linux server.<br><br> 

## <a name="annotations"></a>Annotations

The following annotations are available with the Galasa Ecosystem Manager
<details>
<summary>Kubernetes Ecosystem</summary>

| Annotation: | Kubernetes Ecosystem |
| --------------------------------------- | :------------------------------------- |
| Name: | @KubernetesEcosystem |
| Description: | The <code>@KubernetesEcosystem</code> annotation requests the Galasa Ecosystem Manager to provision a Galasa Ecosystem within a Kubernetes Namespace. |
| Attribute: `ecosystemNamespaceTag` |  The <code>ecosystemNamespaceTag</code> is used to identify the Ecosystem to other Managers or Shared Environments.  If a test is using multiple  Ecosystems, each separate Ecosystem must have a unique tag.  If two Ecosystems use the same tag, they refer to the  same Ecosystem. |
| Attribute: `kubernetesNamespaceTag` |  The <code>kubernetesNamespaceTag</code> identifies which tagged Kubernetes Namespace is to be used to deploy the Galasa Ecosystem into. |
| Attribute: `yamlDirectory` |  The <code>yamlDirectory</code> points to a resource directory within the test bundle that contains a set of override yaml files to use when creating the  ecosystem.  Each file must end with .yaml to be found.  If a directory or resource is not provided, the stable yaml files within the Galasa Ecosystem Manager will be used. |
| Syntax: | <code>@KubernetesEcosystem<br> public IKubernetesEcosystem ecosystem;<br> <br> @KubernetesEcosystem(yamlDirectory="/k8syaml"<br> public IKubernetesEcosystem ecosystem;<br> </code> |
| Notes: | The <code>IKubernetesEcosystem</code> interface gives the test access to the URLs of the services and API endpoints within the Ecosystem. When the test runs all the services that are required by the test are known to be up and working.<br> <br> The test must provide a @KubernetesNamespace IKubernetesNamespace annotation, as this is where the Ecosystem is provisioned in.  In the future, Docker and Linux will be options. <br> The Galasa Ecosystem has its own stable versions of the Kubernetes yaml files that are needed to create the entire Ecosystem.  If you want to override those and use your own yaml files, then use the yamlDirectory attribute.  If a resource is missing in the test's set, then the stable version is used. |

</details>

<details>
<summary>Local Ecosystem</summary>

| Annotation: | Local Ecosystem |
| --------------------------------------- | :------------------------------------- |
| Name: | @LocalEcosystem |
| Description: | The <code>@LocalEcosystem</code> annotation requests the Galasa Ecosystem Manager to provision a local running environment on a Linux or Windows instance.   The Local running environment will  use the FPF file configuration and not run any of the servers by default. |
| Attribute: `ecosystemTag` |  The <code>ecosystemNamespaceTag</code> is used to identify the Ecosystem to other Managers or Shared Environments.  If a test is using multiple  Ecosystems, each separate Ecosystem must have a unique tag.  If two Ecosystems use the same tag, they refer to the  same Ecosystem. |
| Attribute: `linuxImageTag` |  The <code>linuxImageTag</code> identifies which tagged Linux image is to be used to deploy the Galasa Ecosystem into. |
| Attribute: `windowsImageTag` |  The <code>windowsImageTag</code> identifies which tagged Windows image is to be used to deploy the Galasa Ecosystem into. |
| Attribute: `javaInstallationTag` |  The <code>javaInstallationTag</code> to which Java installation on the image is to be used to run the Galasa tests and services. |
| Attribute: `isolationInstallation` |  |
| Syntax: | <code>@LocalEcosystem(linuxImageTag="PRIMARY")<br> public ILocalEcosystem ecosystem;<br> <br> @LocalEcosystem(windowsImageTag="PRIMARY")<br> public ILocalEcosystem ecosystem;<br> </code> |
| Notes: | The <code>ILocalEcosystem</code> interface gives the test access FPF services and the ability to run tests from the commandline. The Manager will pre-configure the CPS, DSS and CREDS before the test begins.<br> <br> The test must provide a @LocalNamespace ILocalNamespace annotation, as this is where the Ecosystem is provisioned in. <br> The annotation must provide either a Windows or Linux image tag, but not both and must provide a @JavaInstallation tag. |

</details>



## <a name="codesnippets"></a>Code snippets

Use the following code snippets to help you get started with the Galasa Ecosystem Manager.
 
<details><summary>Obtain a Kubernetes Namespace</summary>

```java
@KubernetesEcosystem
public IKubernetesEcosystem ecosystem;
    
@KubernetesNamespace
public IKubernetesNamespace namespace;
```

This code requests that the Galasa Ecosystem be provisioned in a Kubernetes Namespace. The default tag for both of them is 
PRIMARY.
</details>

<details><summary>Retrieve the RAS Endpoint</summary>

```java
@KubernetesEcosystem
public IKubernetesEcosystem ecosystem;

URI ras = ecosystem.getEndpoint(EcosystemEndpoint.RAS);

```

This snippet demonstrates how to retrieve the Result Archive Store (RAS) endpoint. Be aware, that the URI is 
prefixed with the store type, e.g. couchdb:http://couchdb.server:5984. This is the same for the CPS, DSS and CREDS.
</details>

<details><summary>Set and retrieve a CPS property</summary>

```java
ecosystem.setCpsProperty("bob", "hello");

String value = ecosystem.getCpsProperty("bob")
```

Sets the CPS property `bob` with the value `hello` and retrieves it again.
</details>

## Configuration Properties

The following are properties used to configure the Galasa Ecosystem Manager.
 
<details>
<summary>Docker Registry</summary>

| Property: | Docker Registry |
| --------------------------------------- | :------------------------------------- |
| Name: | galasaecosystem.docker.registry |
| Description: | The registry that contains the Docker images |
| Required:  | No |
| Default value: | docker.io |
| Valid values: | a valid hostname |
| Examples: | <code>galasaecosystem.docker.registry=docker.io</code> |

</details>
 
<details>
<summary>Docker Image Version</summary>

| Property: | Docker Image Version |
| --------------------------------------- | :------------------------------------- |
| Name: | galasaecosystem.docker.version |
| Description: | The versions of the Docker images to be used with the Ecosystem |
| Required:  | Yes |
| Default value: | The setting of galasaecosystem.maven.version |
| Valid values: | A valid Docker version literial |
| Examples: | <code>galasaecosystem.docker.version=0.4.0</code> |

</details>
 
<details>
<summary>Isolated Full zip location</summary>

| Property: | Isolated Full zip location |
| --------------------------------------- | :------------------------------------- |
| Name: | galasaecosystem.isolated.full.zip |
| Description: | The location of the isolated zip for the full distribution |
| Required:  | No |
| Default value: | None |
| Valid values: | Valid URL |
| Examples: | <code>galasaecosystem.isolated.full.zip=https://resources.galasa.dev/isolated.zip</code> |

</details>
 
<details>
<summary>Isolated MVP zip location</summary>

| Property: | Isolated MVP zip location |
| --------------------------------------- | :------------------------------------- |
| Name: | galasaecosystem.isolated.mvp.zip |
| Description: | The location of the isolated zip for the mvp distribution |
| Required:  | No |
| Default value: | None |
| Valid values: | Valid URL |
| Examples: | <code>galasaecosystem.isolated.mvp.zip=https://github.com/galasa-dev/isolated/releases/download/v0.41.0/galasa-isolated-mvp-0.41.0.zip</code> |

</details>
 
<details>
<summary>Kubernetes Ecosystem Tag Shared Environment</summary>

| Property: | Kubernetes Ecosystem Tag Shared Environment |
| --------------------------------------- | :------------------------------------- |
| Name: | galasaecosystem.ecosystem.tag.XXXXXX.shared.environment |
| Description: | Tells the Galasa Ecosystem Manager which Shared Environment is assigned to an Ecosystem Tag |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid Shared Environment |
| Examples: | <code>galasaecosystem.ecosystem.tag.SHARED.shared.environment=M1</code> |

</details>
 
<details>
<summary>Maven Repository URL</summary>

| Property: | Maven Repository URL |
| --------------------------------------- | :------------------------------------- |
| Name: | galasaecosystem.maven.repository |
| Description: | The location of the Maven Repository all artifacts will be downloaded from |
| Required:  | Yes |
| Default value: | None |
| Valid values: | Value URL |
| Examples: | <code>galasaecosystem.maven.repository=https://nexus.galasa.dev/repository/maven-development</code> |

</details>
 
<details>
<summary>Maven use default local repository</summary>

| Property: | Maven use default local repository |
| --------------------------------------- | :------------------------------------- |
| Name: | galasaecosystem.maven.use.default.local.repository |
| Description: | The Local ecosystems will use a dedicated local repository, however, this slows the installation, so setting this property to true will use the normal ~/.m2/repository so downloads happen only once per day, useful for rapid development and testings |
| Required:  | No |
| Default value: | false |
| Valid values: | true or false |
| Examples: | <code>galasaecosystem.maven.use.default.local.repository=true</code> |

</details>
 
<details>
<summary>Maven Artifact Version</summary>

| Property: | Maven Artifact Version |
| --------------------------------------- | :------------------------------------- |
| Name: | galasaecosystem.maven.version |
| Description: | The versions of the Maven artifacts to be used with the Ecosystem |
| Required:  | Yes |
| Default value: | None |
| Valid values: | A valid maven version literial |
| Examples: | <code>galasaecosystem.maven.version=0.4.0</code> |

</details>
