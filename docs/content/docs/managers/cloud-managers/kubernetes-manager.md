---
path: "/docs/managers/kubernetes-manager"
title: "Kubernetes Manager"
---

This Manager is at Alpha level. You can view the <a href="https://javadoc.galasa.dev/dev/galasa/kubernetes/package-summary.html" target="_blank" rel="noopener noreferrer">Javadoc documentation for the Manager here</a>.<br>



[Overview](#overview)<br>
[Provided annotation](#annotations)<br>
[Code snippets and examples](#codesnippets)<br>


# <a name="overview"></a>Overview
This Manager provides a test with a Kubernetes Namespace to utilize. The test will provide YAML representations of the resources that the test requires. <br><br> As an absolute minimum, the CPS property <code>kubernetes.cluster.K8S.url</code> must be provided as well as a credential <code>secure.credentials.K8S.token</code> for the API token. <br><br> The Kubernetes Manager supports Galasa Shared Environments. Shared environments provide  the ability to create a test environment that can be shared across multiple test runs  so you don't have to provision a test environment for each test.

## Limitations
The Manager only supports the following Kubernetes resources:-<br> <br> - Deployment<br> - StatefulSet<br> - Service<br> - Secret<br> - ConfigMap<br> - PersistentVolumeClaim<br> <br> If additional resources are required, please raise a GitHub issue.<br><br> 


## <a name="annotations"></a>Annotations

The following annotations are available with the Kubernetes Manager
<details>
<summary>Kubernetes Namespace</summary>

| Annotation: | Kubernetes Namespace |
| --------------------------------------- | :------------------------------------- |
| Name: | @KubernetesNamespace |
| Description: | The <code>@KubernetesNamespace</code> annotation requests the Kubernetes Manager to allocate a namespace on the infrastructure Kubernetes clusters.  The test can request as many namespaces as required so long as they  can be supported simultaneously by the Kubernetes Manager configuration. |
| Attribute: `kubernetesNamespaceTag` |  The <code>kubernetesNamespaceTag</code> identifies the Kubernetes names to other Managers or Shared Environments.  If a test is using multiple  Kubernetes namespace, each separate Kubernetes namespace must have a unique tag.  If more than one Kubernetes namespace use the same tag, they will refer to the  same Kubernetes namespace. |
| Syntax: | <code>@KubernetesNamespace<br> public IKubernetesNamesapce namespace;<br> </code> |
| Notes: | The <code>IKubernetesNamespace</code> interface gives the test access to create and manage resources on the Kubernetes cluster.  See <a href="https://javadoc.galasa.dev/dev/galasa/kubernetes/KubernetesNamespace.html" target="_blank">KubernetesNamespace</a> and <a href="https://javadoc.galasa.dev/dev/galasa/kubernetes/IKubernetesNamespace.html" target="_blank">IKubernetesNamespace</a> to find out more. |

</details>



## <a name="codesnippets"></a>Code snippets

Use the following code snippets to help you get started with the Kubernetes Manager.
 
<details><summary>Create Kubernetes namespaces for the Kubernetes Manager to use</summary>

Note: Isolated namespaces must be provided for the Kubernetes Manager to use.  The Manager deletes any resources that 
exist on the namespace once a test has finished.

The following are example scripts and yaml files necessary to create namespaces:
1. [Namespace creation script](https://github.com/galasa-dev/managers/blob/main/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.kubernetes.manager/examples/namespaces.yaml)
1. [Create Service Account for the Manager to use (including api token)](https://github.com/galasa-dev/managers/blob/main/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.kubernetes.manager/examples/account.sh)
1. [The RBAC rules to be applied to each namespace](https://github.com/galasa-dev/managers/blob/main/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.kubernetes.manager/examples/rbac.yaml)
</details>

<details><summary>Obtain a Kubernetes Namespace</summary>

```java
@KubernetesNamespace()
public IKubernetesNamespace namespace;
```

This code requests the Kubernetes Manager to allocate a namespace for the test to use.

There is no limit in Galasa on how many Kubernetes Namespaces can be used within a single test. The only limit is the number of Kubernetes Namespaces that can be started in the Galasa Ecosystem. This limit is set by the Galasa Administrator and is typically set to the maximum number of namespaces defined in the Kubernetes cluster.  If there are not enough slots available for an automated run, the run is put back on the queue in waiting state to retry.  Local test runs fail if there are not enough container slots available.
</details>

<details><summary>Create a resource on the namespace</summary>

```java
@ArtifactManager
public IArtifactManager artifactManager

@KubernetesNamespace()
public IKubernetesNamespace namespace;

@Test
public void test() {
	IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
	
	String yaml = bundleResource.streamAsString(bundleResources.retrieveFile("/example.yaml"));
	
	IResource resource = namespace.createResource(yaml);
}

```

In this snippet, the test retrieves the contents of the `/example.yaml` resource file as a String.  The yaml file is passed the namespace for creation.  The yaml must contain only one Kubernetes resource.

The resource is created but is not checked to see if the resource has been started or allocated.
</details>

<details><summary>Retrieve a pod log</summary>

```java
IStatefulSet statefulSet = (IStatefulSet)namespace.createResource(yaml);

List<IPodLog> podLogs = statefulSet.getPodLogs("containername");

```

As Deployments and StatefulSets can have multiple pods and therefore containers with the same name,  a List is returned containing all the current logs for all the named containers.
</details>

## Configuration Properties

The following are properties used to configure the Kubernetes Manager.
 
<details>
<summary>Kubernetes Cluster IDs CPS Property</summary>

| Property: | Kubernetes Cluster IDs CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.ids |
| Description: | Provides a comma separated list of the active Kubernetes Clusters that are defined in the CPS |
| Required:  | No |
| Default value: | Defaults to a single cluster ID of K8S if the property is missing |
| Valid values: | A comma separated list of alphanumeric IDs.  Normally uppercased. |
| Examples: | <code>kubernetes.cluster.ids=K8S,ALTERNATE</code> |

</details>
 
<details>
<summary>Kubernetes Cluster Credentials CPS Property</summary>

| Property: | Kubernetes Cluster Credentials CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]credentials |
| Description: | Provides the Credentials ID for the token required to access the Kubernetes cluster |
| Required:  | No |
| Default value: | K8S |
| Valid values: | A valid credentials ID. Galasa convention states IDs should be uppercase |
| Examples: | <code>kubernetes.cluster.K8S.credentials=K8S<br> kubernetes.cluster.credentials=K8S</code> |

</details>
 
<details>
<summary>Maximum Slots on Cluster CPS Property</summary>

| Property: | Maximum Slots on Cluster CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]max.slots |
| Description: | Specifies the maximum number of slots(namespaces) that can be allocated at one time on the cluster |
| Required:  | No |
| Default value: | Defaults to 2 if not provided |
| Valid values: | Integer value.  A value <0 disables the cluster. |
| Examples: | <code>kubernetes.cluster.K8S.max.slots=5</code> |

</details>
 
<details>
<summary>Kubernetes Tag Shared Environment</summary>

| Property: | Kubernetes Tag Shared Environment |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.namespace.tag.XXXXXX.shared.environment |
| Description: | Tells the Kubernetes Manager which Shared Environment is assigned to a namespace tag |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid Shared Environment |
| Examples: | <code>kubernetes.namespace.tag.SHARED.shared.environment=M1</code> |

</details>
 
<details>
<summary>Kubernetes Namespace IDs CPS Property</summary>

| Property: | Kubernetes Namespace IDs CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]namespaces |
| Description: | Provides a comma separated list of the namespaces that are available on the cluster |
| Required:  | No |
| Default value: | Defaults to galasa{1-2} if not provided |
| Valid values: | A comma separated list of valid Kubernetes namespaces with resource pooling expanders |
| Examples: | <code>kubernetes.cluster.K8S.namespaces=galasa1,galasa{2-9}<br> kubebernetes.cluster.namespaces=bob1,bob2,bob3</code> |

</details>
 
<details>
<summary>Kubernetes Node Port Proxy Hostname CPS Property</summary>

| Property: | Kubernetes Node Port Proxy Hostname CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.XXXX.nodeport.proxy.hostname |
| Description: | Provides the hostname that NodePorts can be accessed on. |
| Required:  | No |
| Default value: | The hostname as specified in the API URL |
| Valid values: | A valid URL hostname |
| Examples: | <code>kubernetes.cluster.K8S.nodeport.proxy.hostname=cluster.org</code> |

</details>
 
<details>
<summary>Kubernetes Override Storage Class CPS Property</summary>

| Property: | Kubernetes Override Storage Class CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]override.storageclass |
| Description: | Provides a Kubernetes StorageClass that is set on all PersistentVolumeClaims that are created in the Kubernetes namespace.   The value of this property is set in the property *spec.storageClassName* |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid StorageClass that is defined in the Kubernetes cluster |
| Examples: | <code>kubernetes.cluster.K8S.override.storageclass=fast<br> kubernetes.cluster.override.storageclass=slow</code> |

</details>
 
<details>
<summary>Kubernetes Cluster API URL CPS Property</summary>

| Property: | Kubernetes Cluster API URL CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.XXXX.url |
| Description: | The API URL of the Kubernetes Cluster |
| Required:  | Yes |
| Default value: | None |
| Valid values: | A valid URL |
| Examples: | <code>kubernetes.cluster.K8S.url=http://cluster.org:8443</code> |

</details>
 
<details>
<summary>Kubernetes Validate Cluster Certificate CPS Property</summary>

| Property: | Kubernetes Validate Cluster Certificate CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]validate.certificate |
| Description: | Validates the Kubernetes Cluster API Certificate |
| Required:  | No |
| Default value: | true |
| Valid values: | true or false |
| Examples: | <code>kubernetes.cluster.K8S.validate.certificate=false<br> kubernetes.cluster.validate.certificate=true</code> |

</details>
