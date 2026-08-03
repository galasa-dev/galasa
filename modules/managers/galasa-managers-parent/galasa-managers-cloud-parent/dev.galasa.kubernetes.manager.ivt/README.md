## Kubernetes Manager IVT

This is an Installation Verification Test (IVT) for the Kubernetes Manager, which validates that the manager can successfully allocate pre-existing namespaces and manipulate Kubernetes resources within them.

**⚠️ IMPORTANT WARNINGS:**

1. **The Kubernetes Manager does not create new namespaces.** It allocates from a pool of pre-existing namespaces that you must create beforehand.

2. **The manager DELETES ALL RESOURCES in allocated namespaces after test completion.** This includes:
   - Pods, Deployments, StatefulSets
   - Services, ConfigMaps, Secrets
   - PersistentVolumeClaims
   - Any other resources in the namespace

   **⚠️ DO NOT use namespaces that contain resources you want to keep!** Always use dedicated, empty namespaces for Galasa tests. The cleanup process is automatic and currently cannot be disabled.

## Prerequisites

- [Minikube](https://minikube.sigs.k8s.io/docs/start) or access to a Kubernetes cluster
- `kubectl` command-line tool installed
- Galasa CLI (`galasactl`) installed

## Running the IVT Locally with Minikube

### Step 1: Start Minikube

```bash
minikube start
```

### Step 2: Create Namespaces

The Kubernetes Manager requires pre-existing namespaces to allocate to tests. Create them with:

```bash
kubectl create namespace galasa1
kubectl create namespace galasa2
```

Verify the namespaces were created:

```bash
kubectl get namespaces | grep galasa
```

### Step 3: Start kubectl Proxy

The kubectl proxy provides an unauthenticated HTTP endpoint to the Kubernetes API, which simplifies local testing:

```bash
kubectl proxy
```

This will start a proxy server at `http://localhost:8001`. Leave this running in a separate terminal.

### Step 4: Configure Galasa Properties

Add the following to your `cps.properties` file:

```properties
kubernetes.cluster.K8S.url=http://localhost:8001
kubernetes.cluster.K8S.namespaces=galasa1,galasa2
kubernetes.cluster.K8S.max.slots=2
```

**Note:** When using `kubectl proxy` at `http://localhost:8001`, credentials are NOT required. The manager will automatically detect this and use unauthenticated mode.

### Step 5: Run the IVT

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa/dev.galasa.uber.obr/1.0.1/obr \
  --class dev.galasa.kubernetes.manager.ivt/dev.galasa.kubernetes.manager.ivt.KubernetesManagerIVT \
  --log -
```

## Running Against a Real Kubernetes Cluster

If you're testing against a real Kubernetes cluster (not kubectl proxy), you'll need to configure authentication.

### Step 1: Create Namespaces

```bash
kubectl create namespace galasa1
kubectl create namespace galasa2
```

### Step 2: Set Up RBAC (Role-Based Access Control)

Apply the RBAC configuration to each namespace:

```bash
kubectl apply -f ../dev.galasa.kubernetes.manager.ivt/examples/rbac.yaml -n galasa1
kubectl apply -f ../dev.galasa.kubernetes.manager.ivt/examples/rbac.yaml -n galasa2
```

This creates a `galasa-admin` role with permissions to manage pods, services, deployments, etc.

### Step 3: Create Service Account and Get Token

```bash
# Create service account in galasa1 namespace
kubectl -n galasa1 create serviceaccount galasa

# Generate a token for the service account (Kubernetes 1.24+)
kubectl -n galasa1 create token galasa --duration=8760h
```

Copy the token output - you'll need it for the next step.

### Step 4: Configure Galasa Properties

Add to your `cps.properties`:

```properties
kubernetes.cluster.K8S.url=https://your-cluster-api-server:6443
kubernetes.cluster.K8S.namespaces=galasa1,galasa2
kubernetes.cluster.K8S.max.slots=2
```

Add to your `credentials.properties`:

```properties
secure.credentials.K8S.token=<paste-your-token-here>
```

### Step 5: Run the IVT

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa/dev.galasa.uber.obr/1.0.1/obr \
  --class dev.galasa.kubernetes.manager.ivt/dev.galasa.kubernetes.manager.ivt.KubernetesManagerIVT \
  --log -
```

## Troubleshooting

### Error: "Unable to allocate a slot on any Kubernetes Cluster"

This error means the manager couldn't find any available namespaces. Check:

1. **Namespaces exist in Kubernetes:**
   ```bash
   kubectl get namespaces | grep galasa
   ```

2. **Namespaces are configured in CPS:**
   ```properties
   kubernetes.cluster.K8S.namespaces=galasa1,galasa2
   ```

3. **kubectl proxy is running** (if using localhost:8001):
   ```bash
   # Should show: Starting to serve on 127.0.0.1:8001
   kubectl proxy
   ```

4. **All slots aren't in use:**
   - Increase `kubernetes.cluster.K8S.max.slots=5`
   - Or add more namespaces

### Error: "Credentials K8S are missing"

This error occurs when:
- Using a real cluster URL (not localhost:8001) without credentials
- Solution: Add token to `credentials.properties` as shown above
- Or use `kubectl proxy` at `http://localhost:8001` (no credentials needed)

### Error: "Unable to initialise the Kubernetes API Client"

Check:
- Cluster URL is correct and accessible
- For kubectl proxy: ensure it's running on port 8001
- For real clusters: verify token is valid and not expired
- Network connectivity to the cluster

### SSL Certificate Error: "PKIX path building failed"

This error occurs when connecting to Kubernetes clusters with certificates not trusted by Java's default truststore (common with private CAs, self-signed certificates, or cloud providers like IBM Cloud).

**Error message:**
```
javax.net.ssl.SSLHandshakeException: PKIX path building failed:
sun.security.provider.certpath.SunCertPathBuilderException:
unable to find valid certification path to requested target
```

**Solution1: Add Certificates to Java Truststore (Recommended for Production)**

1. **Export the cluster's certificate chain:**
   ```bash
   # Get the cluster URL
   CLUSTER_URL=$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}')
   
   # Extract hostname and port
   CLUSTER_HOST=$(echo $CLUSTER_URL | sed 's|https://||' | cut -d: -f1)
   CLUSTER_PORT=$(echo $CLUSTER_URL | sed 's|https://||' | cut -d: -f2)
   
   # Download the certificate chain
   openssl s_client -showcerts -connect ${CLUSTER_HOST}:${CLUSTER_PORT} </dev/null 2>/dev/null | \
     openssl x509 -outform PEM > cluster-cert.pem
   ```

2. **Import certificate into Java's truststore:**
   ```bash
   # Find Java installation
   JAVA_HOME=$(java -XshowSettings:properties -version 2>&1 | grep 'java.home' | awk '{print $3}')
   
   # Import certificate (requires sudo)
   sudo keytool -import -trustcacerts -alias k8s-cluster \
     -file cluster-cert.pem \
     -keystore $JAVA_HOME/lib/security/cacerts \
     -storepass changeit -noprompt
   
   # Verify import
   keytool -list -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit | grep k8s-cluster
   ```

**Solution 2: Create Custom Truststore (Better for Testing/Development)**

1. **Create a custom truststore with the cluster certificate:**
   ```bash
   # Export cluster certificate (as above)
   openssl s_client -showcerts -connect ${CLUSTER_HOST}:${CLUSTER_PORT} </dev/null 2>/dev/null | \
     openssl x509 -outform PEM > cluster-cert.pem
   
   # Create custom truststore
   keytool -import -trustcacerts -alias k8s-cluster \
     -file cluster-cert.pem \
     -keystore ./k8s-truststore.jks \
     -storepass changeit -noprompt
   ```

2. **Configure Galasa to use the custom truststore:**
   
   Add to your test run command:
   ```bash
   galasactl runs submit local \
     --obr mvn:dev.galasa/dev.galasa.uber.obr/1.0.1/obr \
     --class dev.galasa.kubernetes.manager.ivt/dev.galasa.kubernetes.manager.ivt.KubernetesManagerIVT \
     --log - \
     --jvm "-Djavax.net.ssl.trustStore=./k8s-truststore.jks -Djavax.net.ssl.trustStorePassword=changeit"
   ```

**Solution 3: Disable Certificate Validation (Testing Only - NOT Secure)**

Add to `cps.properties`:
```properties
kubernetes.cluster.K8S.validate.certificate=false
```

**⚠️ Warning:** Only use this for testing in non-production environments. It disables SSL certificate validation, making connections vulnerable to man-in-the-middle attacks.

### Namespace Already in Use

If you see "The allocated namespace galasa1 on cluster K8S is dirty", it means a previous test didn't clean up properly:

```bash
# Delete the galasa configmap
kubectl -n galasa1 delete configmap galasa

# Or delete and recreate the namespace
kubectl delete namespace galasa1
kubectl create namespace galasa1
```

## Configuration Reference

### Required CPS Properties

| Property | Description | Example |
|----------|-------------|---------|
| `kubernetes.cluster.K8S.url` | Kubernetes API server URL | `http://localhost:8001` |
| `kubernetes.cluster.K8S.namespaces` | Comma-separated list of namespaces | `galasa1,galasa2` |

### Optional CPS Properties

| Property | Default | Description |
|----------|---------|-------------|
| `kubernetes.cluster.K8S.max.slots` | `2` | Maximum concurrent namespace allocations |
| `kubernetes.cluster.K8S.credentials` | `K8S` | Credentials ID for authentication |
| `kubernetes.cluster.K8S.validate.certificate` | `true` | Whether to validate SSL certificates |
| `kubernetes.cluster.ids` | `K8S` | Comma-separated list of cluster IDs |

### Credentials (Only for Non-Proxy Clusters)

| Property | Description |
|----------|-------------|
| `secure.credentials.K8S.token` | Service account token for authentication |

## What the IVT Tests

The IVT validates the following Kubernetes Manager capabilities:

1. **Namespace Allocation** - Can provision an isolated namespace
2. **ConfigMap Creation** - Can create and retrieve ConfigMaps
3. **Secret Creation** - Can create and manage Secrets
4. **PVC Creation** - Can create PersistentVolumeClaims
5. **Deployment Creation** - Can deploy applications
6. **StatefulSet Creation** - Can create StatefulSets
7. **Service Creation** - Can expose services

## Additional Resources

- [Kubernetes Manager Documentation](https://galasa.dev/docs/managers/cloud-managers/kubernetes-manager)
- [Example YAML Files](../dev.galasa.kubernetes.manager/examples/) - Sample configurations for namespaces and RBAC
- [Minikube Documentation](https://minikube.sigs.k8s.io/docs/)
