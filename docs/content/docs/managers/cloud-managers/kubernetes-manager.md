---
title: "Kubernetes Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/kubernetes/package-summary.html){target="_blank"}.


## Overview

This Manager provides a test with a Kubernetes Namespace to utilize. The test will provide YAML representations of the resources that the test requires.

!!! warning

    The Kubernetes Manager does not create new namespaces. It allocates from a pool of pre-existing namespaces that must be created beforehand. After test completion, the manager **automatically deletes ALL resources** in allocated namespaces (Pods, Deployments, Services, ConfigMaps, Secrets, PVCs, etc.). Always use dedicated, empty namespaces for Galasa tests.

### Configuration Requirements

**Minimum required configuration:**
- `kubernetes.cluster.K8S.url` - The Kubernetes API server URL

**Authentication options:**
1. **Token-based authentication** (for remote clusters): Provide `secure.credentials.K8S.token` credential
2. **kubectl proxy mode** (for local testing): Use `http://localhost:8001` - no credentials required

The Kubernetes Manager supports Galasa Shared Environments. Shared environments provide the ability to create a test environment that can be shared across multiple test runs so you don't have to provision a test environment for each test.

## Limitations

The Manager only supports the following Kubernetes resources:-

- Deployment
- StatefulSet
- Service
- Secret
- ConfigMap
- PersistentVolumeClaim

If additional resources are required, please raise a GitHub issue.


## Annotations

The following annotations are available with the Kubernetes Manager


### Kubernetes Namespace

| Annotation: | Kubernetes Namespace |
| --------------------------------------- | :------------------------------------- |
| Name: | @KubernetesNamespace |
| Description: | The `@KubernetesNamespace` annotation requests the Kubernetes Manager to allocate a namespace on the infrastructure Kubernetes clusters.  The test can request as many namespaces as required so long as they  can be supported simultaneously by the Kubernetes Manager configuration. |
| Attribute: `kubernetesNamespaceTag` |  The `kubernetesNamespaceTag` identifies the Kubernetes names to other Managers or Shared Environments.  If a test is using multiple  Kubernetes namespace, each separate Kubernetes namespace must have a unique tag.  If more than one Kubernetes namespace use the same tag, they will refer to the  same Kubernetes namespace. |
| Syntax: | <pre lang="java">@KubernetesNamespace<br>public IKubernetesNamespace namespace;<br> </pre> |
| Notes: | The `IKubernetesNamespace` interface gives the test access to create and manage resources on the Kubernetes cluster.  See [KubernetesNamespace](../../reference/javadoc/dev/galasa/kubernetes/KubernetesNamespace.html){target="_blank"} and [IKubernetesNamespace](../../reference/javadoc/dev/galasa/kubernetes/IKubernetesNamespace.html){target="_blank"} to find out more. |


## Code snippets

Use the following code snippets to help you get started with the Kubernetes Manager.
 

### Setup for Local Testing with kubectl proxy

For local development and testing, you can use kubectl proxy to avoid certificate and authentication complexity:

```bash
# Start Minikube (or connect to your cluster)
minikube start

# Create namespaces for Galasa to use
kubectl create namespace galasa1
kubectl create namespace galasa2

# Start kubectl proxy (provides unauthenticated HTTP access)
kubectl proxy
```

Configure in `cps.properties`:
```properties
kubernetes.cluster.K8S.url=http://localhost:8001
kubernetes.cluster.K8S.namespaces=galasa1,galasa2
```

No credentials are required when using kubectl proxy at `http://localhost:8001`.

### Setup for Remote Kubernetes Clusters

For production or remote clusters, you need to configure authentication and RBAC:

**1. Find your cluster URL:**
```bash
kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}'
```

**2. Create namespaces:**
```bash
kubectl create namespace galasa1
kubectl create namespace galasa2
```

**3. Set up RBAC permissions:**

Apply RBAC rules to each namespace - see [example RBAC configuration](https://github.com/galasa-dev/galasa/tree/main/modules/managers/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.kubernetes.manager/examples/rbac.yaml){target="_blank"}

**4. Create service account and token:**
```bash
# Create service account
kubectl -n galasa1 create serviceaccount galasa

# Generate token (Kubernetes 1.24+)
kubectl -n galasa1 create token galasa --duration=8760h
```

**5. Configure Galasa:**

In `cps.properties`:
```properties
kubernetes.cluster.K8S.url=https://your-cluster:6443
kubernetes.cluster.K8S.namespaces=galasa1,galasa2
```

In `credentials.properties`:
```properties
secure.credentials.K8S.token=<your-token-here>
```


### Obtain a Kubernetes Namespace

```java
@KubernetesNamespace()
public IKubernetesNamespace namespace;
```

This code requests the Kubernetes Manager to allocate a namespace for the test to use.

There is no limit in Galasa on how many Kubernetes Namespaces can be used within a single test. The only limit is the number of Kubernetes Namespaces that can be started in the Galasa Ecosystem. This limit is set by the Galasa Administrator and is typically set to the maximum number of namespaces defined in the Kubernetes cluster.  If there are not enough slots available for an automated run, the run is put back on the queue in waiting state to retry.  Local test runs fail if there are not enough container slots available.


### Create a resource on the namespace

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


### Retrieve a pod log

```java
IStatefulSet statefulSet = (IStatefulSet)namespace.createResource(yaml);

List<IPodLog> podLogs = statefulSet.getPodLogs("containername");

```

As Deployments and StatefulSets can have multiple pods and therefore containers with the same name, a List is returned containing all the current logs for all the named containers.


## Troubleshooting

### SSL Certificate Errors (PKIX path building failed)

If you encounter SSL certificate validation errors when connecting to remote Kubernetes clusters:

```
javax.net.ssl.SSLHandshakeException: PKIX path building failed:
unable to find valid certification path to requested target
```

**Solution 1: Add cluster certificate to Java truststore (Recommended)**

```bash
# Export cluster certificate
CLUSTER_URL=$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}')
CLUSTER_HOST=$(echo $CLUSTER_URL | sed 's|https://||' | cut -d: -f1)
CLUSTER_PORT=$(echo $CLUSTER_URL | sed 's|https://||' | cut -d: -f2)

openssl s_client -showcerts -connect ${CLUSTER_HOST}:${CLUSTER_PORT} </dev/null 2>/dev/null | \
  openssl x509 -outform PEM > cluster-cert.pem

# Import into Java truststore
JAVA_HOME=$(java -XshowSettings:properties -version 2>&1 | grep 'java.home' | awk '{print $3}')
sudo keytool -import -trustcacerts -alias k8s-cluster \
  -file cluster-cert.pem \
  -keystore $JAVA_HOME/lib/security/cacerts \
  -storepass changeit -noprompt
```

**Solution 2: Disable certificate validation (Testing only)**

Add to `cps.properties`:
```properties
kubernetes.cluster.K8S.validate.certificate=false
```

⚠️ **Warning:** Only use this for testing. It disables SSL validation and is not secure for production.

### Namespace Allocation Errors

If tests fail with "Unable to allocate a slot on any Kubernetes Cluster":

1. Verify namespaces exist: `kubectl get namespaces | grep galasa`
2. Check CPS configuration: `kubernetes.cluster.K8S.namespaces=galasa1,galasa2`
3. Ensure kubectl proxy is running (if using localhost:8001)
4. Increase max slots: `kubernetes.cluster.K8S.max.slots=5`

### Namespace Already in Use

If you see "The allocated namespace is dirty":

```bash
# Delete the galasa configmap
kubectl -n galasa1 delete configmap galasa

# Or recreate the namespace
kubectl delete namespace galasa1
kubectl create namespace galasa1
```


## Configuration Properties

The following are properties used to configure the Kubernetes Manager.
 

### Kubernetes Cluster IDs CPS Property

| Property: | Kubernetes Cluster IDs CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.ids |
| Description: | Provides a comma separated list of the active Kubernetes Clusters that are defined in the CPS |
| Required:  | No |
| Default value: | Defaults to a single cluster ID of K8S if the property is missing |
| Valid values: | A comma separated list of alphanumeric IDs.  Normally uppercased. |
| Examples: | `kubernetes.cluster.ids=K8S,ALTERNATE` |


### Kubernetes Cluster Credentials CPS Property

| Property: | Kubernetes Cluster Credentials CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]credentials |
| Description: | Provides the Credentials ID for the token required to access the Kubernetes cluster |
| Required:  | No |
| Default value: | K8S |
| Valid values: | A valid credentials ID. Galasa convention states IDs should be uppercase |
| Examples: | `kubernetes.cluster.K8S.credentials=K8S`<br> `kubernetes.cluster.credentials=K8S` |


### Maximum Slots on Cluster CPS Property

| Property: | Maximum Slots on Cluster CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]max.slots |
| Description: | Specifies the maximum number of slots(namespaces) that can be allocated at one time on the cluster |
| Required:  | No |
| Default value: | Defaults to 2 if not provided |
| Valid values: | Integer value.  A value <0 disables the cluster. |
| Examples: | `kubernetes.cluster.K8S.max.slots=5` |


### Kubernetes Tag Shared Environment

| Property: | Kubernetes Tag Shared Environment |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.namespace.tag.XXXXXX.shared.environment |
| Description: | Tells the Kubernetes Manager which Shared Environment is assigned to a namespace tag |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid Shared Environment |
| Examples: | `kubernetes.namespace.tag.SHARED.shared.environment=M1` |


### Kubernetes Namespace IDs CPS Property

| Property: | Kubernetes Namespace IDs CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]namespaces |
| Description: | Provides a comma separated list of the namespaces that are available on the cluster |
| Required:  | No |
| Default value: | Defaults to galasa{1-2} if not provided |
| Valid values: | A comma separated list of valid Kubernetes namespaces with resource pooling expanders |
| Examples: | `kubernetes.cluster.K8S.namespaces=galasa1,galasa{2-9}`<br> `kubebernetes.cluster.namespaces=bob1,bob2,bob3` |


### Kubernetes Node Port Proxy Hostname CPS Property

| Property: | Kubernetes Node Port Proxy Hostname CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.XXXX.nodeport.proxy.hostname |
| Description: | Provides the hostname that NodePorts can be accessed on. |
| Required:  | No |
| Default value: | The hostname as specified in the API URL |
| Valid values: | A valid URL hostname |
| Examples: | `kubernetes.cluster.K8S.nodeport.proxy.hostname=cluster.org` |


### Kubernetes Override Storage Class CPS Property

| Property: | Kubernetes Override Storage Class CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]override.storageclass |
| Description: | Provides a Kubernetes StorageClass that is set on all PersistentVolumeClaims that are created in the Kubernetes namespace.   The value of this property is set in the property *spec.storageClassName* |
| Required:  | No |
| Default value: | None |
| Valid values: | A valid StorageClass that is defined in the Kubernetes cluster |
| Examples: | `kubernetes.cluster.K8S.override.storageclass=fast`<br> `kubernetes.cluster.override.storageclass=slow` |


### Kubernetes Cluster API URL CPS Property

| Property: | Kubernetes Cluster API URL CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.XXXX.url |
| Description: | The API URL of the Kubernetes Cluster |
| Required:  | Yes |
| Default value: | None |
| Valid values: | A valid URL |
| Examples: | `kubernetes.cluster.K8S.url=http://cluster.org:8443` |


### Kubernetes Validate Cluster Certificate CPS Property

| Property: | Kubernetes Validate Cluster Certificate CPS Property |
| --------------------------------------- | :------------------------------------- |
| Name: | kubernetes.cluster.[XXXX.]validate.certificate |
| Description: | Validates the Kubernetes Cluster API Certificate |
| Required:  | No |
| Default value: | true |
| Valid values: | true or false |
| Examples: | `kubernetes.cluster.K8S.validate.certificate=false`<br> `kubernetes.cluster.validate.certificate=true` |

