---
title: "Installing an Ecosystem using Helm"
---

If you want to run scalable, highly available testing for enterprise level workloads, you need to install your Galasa Ecosystem in a Kubernetes cluster. Running Galasa in a Kubernetes cluster means that you can run many tests in parallel on a resilient and scalable platform, where the clean-up of test resources can be managed, and test results can be centralised and gathered easily.

Galasa provides a Galasa Ecosystem Helm chart to install a Galasa Ecosystem. You can install the chart into a Kubernetes cluster or minikube. However, note that minikube is not suitable for production purposes because it provides a single Kubernetes node and therefore does not scale well. Use minikube only for development and testing purposes.

The [Galasa Helm repository](https://github.com/galasa-dev/helm){target="_blank"} contains the Galasa Ecosystem Helm chart that is referenced in the following sections.

_Note:_ The Galasa Ecosystem Helm chart currently supports only x86-64 systems. It cannot be installed on ARM64-based systems.

## Quick Start

### Prerequisites

- [Helm 3 or later](https://helm.sh/docs/intro/install/){target="_blank"} installed
- Access to a Kubernetes cluster (v1.28 or later). See [Supported Kubernetes Versions](#supported-kubernetes-versions) for compatibility details.
- `kubectl` configured to access your cluster

### Basic Installation Steps

1\. **Add the Galasa Helm repository:**

   ```bash
   helm repo add galasa https://galasa-dev.github.io/helm
   helm repo update
   ```

2\. **Download and configure values:**

   Download the `values.yaml` file for your chosen Galasa version from the [Galasa Helm repository's releases page](https://github.com/galasa-dev/helm/releases){target="_blank"}:

=== "macOS or Linux"

    ```bash
    curl -O https://raw.githubusercontent.com/galasa-dev/helm/main/charts/ecosystem/values.yaml
    ```

=== "Windows (PowerShell)"

    ```powershell
    Invoke-WebRequest -Uri https://raw.githubusercontent.com/galasa-dev/helm/main/charts/ecosystem/values.yaml -OutFile values.yaml
    ```

Edit `values.yaml` and set:

- `galasaVersion`: Your desired Galasa version (see [Releases](../../releases/index.md) documentation). Do not use `latest` to ensure all pods run at the same level.
- `externalHostname`: The hostname for accessing your ecosystem (e.g., `galasa.example.com`)

3\. **Configure network access:**

   Choose either Ingress (default) or Gateway API. For Ingress, update:
   ```yaml
   ingress:
     enabled: true
     ingressClassName: nginx  # Change to your IngressClass
   ```

   For Gateway API or HTTPS setup, see [Network Access Configuration](#network-access-configuration).

4\. **Configure authentication (Dex):**

   Update the `dex` section in `values.yaml`:
   ```yaml
   dex:
     config:
       issuer: https://galasa.example.com/dex  # Use your hostname
       connectors:
       - type: github  # Or another supported connector
         id: github
         name: GitHub
         config:
           clientID: $GITHUB_CLIENT_ID
           clientSecret: $GITHUB_CLIENT_SECRET
           redirectURI: https://galasa.example.com/dex/callback
   ```

   See [Configuring Authentication](#configuring-authentication) for detailed setup.

5\. **Install the ecosystem:**

   ```bash
   helm install my-galasa galasa/ecosystem -f values.yaml --wait
   ```

   where `my-galasa` is the name you want to give the Ecosystem.

6\. **Verify the installation:**

   ```bash
   helm test my-galasa
   ```

Your Galasa Ecosystem is now accessible at `https://galasa.example.com/api/bootstrap`

## Supported Kubernetes Versions

The following table shows the minimum and maximum Kubernetes versions supported for each Galasa release:

| **Galasa Version** | **Minimum Kubernetes Version** | **Maximum Kubernetes Version** |
|--------|------|------|
| 0.48.0 | 1.28 | 1.34 |
| 0.47.0 | 1.28 | 1.34 |
| 0.46.1 | 1.28 | 1.34 |
| 0.46.0 | 1.28 | 1.34 |
| 0.45.0 | 1.28 | 1.34 |
| 0.44.0 | 1.28 | 1.34 |
| 0.43.0 | 1.28 | 1.33 |
| 0.42.0 | 1.28 | 1.33 |
| 0.41.0 | 1.28 | 1.33 |
| 0.40.0 | 1.28 | 1.33 |

**Notes:**

- Kubernetes versions outside these ranges may work but are **not officially supported**.
- Only **x86-64 architectures** are supported. ARM64-based systems are not currently supported.
- Ensure your Helm version is **3.x or later** for all Galasa service releases.

You can check your Kubernetes version by running:
```bash
kubectl version
```

## Configuration Guide

### RBAC Setup

If role-based access control (RBAC) is active on your Kubernetes cluster, configure user permissions:

**For chart versions after 0.23.0:** RBAC is configured automatically during installation.

**For chart version 0.23.0 and earlier:** Apply RBAC manually:
```bash
kubectl apply -f https://raw.githubusercontent.com/galasa-dev/helm/ecosystem-0.23.0/charts/ecosystem/rbac.yaml
```

**Admin access:** To grant users the `galasa-admin` role for managing the Helm chart, update the [`rbac-admin.yaml`](https://github.com/galasa-dev/helm/blob/main/charts/ecosystem/rbac-admin.yaml){target="_blank"} file. Replace the placeholder username with actual usernames or add multiple subjects as needed. See the [Using RBAC Authorization Kubernetes documentation](https://kubernetes.io/docs/reference/access-authn-authz/rbac/){target="_blank"} for more information.

### Network Access Configuration

Choose one method to expose your Galasa services:

#### Option 1: Ingress (Default)

Most common for production deployments. Configure in `values.yaml`:

```yaml
ingress:
  enabled: true
  ingressClassName: nginx  # Change to your IngressClass
```

**For HTTPS**, add a TLS configuration:
```yaml
ingress:
  enabled: true
  ingressClassName: nginx
  tls:
    - hosts:
        - galasa.example.com
      secretName: galasa-tls-secret
```

See the [Kubernetes Ingress documentation](https://kubernetes.io/docs/concepts/services-networking/ingress/#tls){target="_blank"} for information on how to set up TLS.

#### Option 2: Gateway API (v0.47.0+)

**Prerequisites:**

- [Gateway Controller](https://gateway-api.sigs.k8s.io/guides/getting-started/#installing-a-gateway-controller){target="_blank"} installed
- [Gateway API CRDs](https://gateway-api.sigs.k8s.io/guides/getting-started/#installing-gateway-api){target="_blank"} installed

For clusters with Gateway API support, configure in `values.yaml`:

```yaml
gateway:
  enabled: true
  gatewayClassName: my-gateway-class
```

**For HTTPS**, add certificate references:
```yaml
gateway:
  enabled: true
  gatewayClassName: my-gateway-class
  tls:
    certificateRefs:
      - kind: Secret
        group: ""
        name: my-certificate-secret
```

### Configuring Authentication

Galasa uses [Dex](https://dexidp.io){target="_blank"} for authentication (version 0.32.0 and later). Configure a connector to your identity provider.

#### GitHub Example

1. **Create a GitHub OAuth App:**
   - Go to [GitHub OAuth Apps](https://github.com/settings/applications/new){target="_blank"}
   - Set **Homepage URL** to your external hostname (e.g., `https://galasa.example.com`)
   - Set **Callback URL** to `https://<your-external-hostname>/dex/callback` (e.g., `https://galasa.example.com/dex/callback`)
   - Generate a client secret and save both the client ID and secret

2. **Configure Dex in values.yaml:**

   ```yaml
   dex:
     config:
       issuer: https://galasa.example.com/dex
       
       connectors:
       - type: github
         id: github
         name: GitHub
         config:
           clientID: $GITHUB_CLIENT_ID
           clientSecret: $GITHUB_CLIENT_SECRET
           redirectURI: https://galasa.example.com/dex/callback
           # Optional: Restrict to organization/team
           orgs:
           - name: my-org
             teams:
             - my-team
   ```

3. **Store credentials securely (recommended):**

   Create a Kubernetes Secret with your OAuth credentials:

=== "macOS or Linux"

    ```bash
    kubectl create secret generic github-oauth-credentials \
      --from-literal=GITHUB_CLIENT_ID="your-client-id" \
      --from-literal=GITHUB_CLIENT_SECRET="your-client-secret"
    ```

=== "Windows (PowerShell)"

    ```powershell
    kubectl create secret generic github-oauth-credentials `
      --from-literal=GITHUB_CLIENT_ID="your-client-id" `
      --from-literal=GITHUB_CLIENT_SECRET="your-client-secret"
    ```

Then reference the secret in `values.yaml`:
   ```yaml
   dex:
     envFrom:
       - secretRef:
           name: github-oauth-credentials
     
     config:
       issuer: https://galasa.example.com/dex
       
       connectors:
       - type: github
         id: github
         name: GitHub
         config:
           clientID: $GITHUB_CLIENT_ID
           clientSecret: $GITHUB_CLIENT_SECRET
           redirectURI: https://galasa.example.com/dex/callback
           orgs:
           - name: my-org
             teams:
             - my-team
   ```

#### Other Identity Providers

Dex supports many connectors including Microsoft, LDAP, OIDC, and more. See the [Dex connectors documentation](https://dexidp.io/docs/connectors){target="_blank"} for configuration examples.

#### Optional: Configure Token Expiry

Update the `expiry` section to configure the expiry of JSON Web Tokens (JWTs) and refresh tokens issued by Dex. By default, JWTs expire 24 hours after being issued and refresh tokens remain valid unless they have not been used for one year. See the [Dex documentation on ID tokens](https://dexidp.io/docs/tokens){target="_blank"} for information and available expiry settings.

### Configuring User Roles and Owners

When the Galasa Ecosystem is first installed, users logging in are assigned a default role as specified by the `galasaDefaultUserRole` property (e.g., `tester`). This means no user initially has administrator privileges.

To grant administration rights, nominate one or more users as 'owners' of the service using the `galasaOwnersLoginIds` property in your `values.yaml` file:

```yaml
galasaOwnersLoginIds: "user1,user2,user3"
```

When a nominated owner logs into the Galasa Ecosystem, they are granted the `owner` role. They can then assign the `admin` role to other users using the `galasactl` command line or the web user interface.

**Important notes:**

- Each login-id must match the login-id allocated to the user by the authentication service (e.g., Dex).
- If you are unsure what login-id to use, log into the Galasa Ecosystem using a browser and view the user profile page.
- Once you have users with the `admin` role, you can remove the `owner` designation if desired.
- The `owner` role exists solely to fix situations where there are no administrators on the Galasa Ecosystem.

### Optional Configurations

#### Istio Service Mesh

The Galasa service supports integration with [Istio](https://istio.io) service mesh to automatically encrypt all pod-to-pod traffic using mutual TLS (mTLS).

**Prerequisites:**
- Istio 1.29+ installed in your Kubernetes cluster
- See [Istio installation guide](https://istio.io/latest/docs/setup/getting-started/)

**Basic Configuration (Internal Traffic Only):**

To enable mTLS for internal pod-to-pod traffic:

```yaml
istio:
  enabled: true
  mtls:
    mode: "STRICT"  # Recommended for production
```

**External Traffic Routing:**

Istio can also handle external traffic routing. Choose one option:

**Option 1: Istio with Kubernetes Gateway API (Recommended)**

```yaml
istio:
  enabled: true
  mtls:
    mode: "STRICT"

gatewayApi:
  enabled: true
  gatewayClassName: "istio"  # Use Istio's Gateway implementation

```

**Option 2: Istio with Kubernetes Ingress**

First, create an Istio IngressClass:

```yaml
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  name: istio
spec:
  controller: istio.io/ingress-controller
EOF
```

Then configure the chart's values:

```yaml
istio:
  enabled: true
  mtls:
    mode: "STRICT"

ingress:
  enabled: true
  ingressClassName: "istio"  # Use Istio's Ingress controller

```

**Note:** When using Istio for external traffic, Istio handles both external ingress and internal mTLS encryption.

**Configuration Options:**

- `istio.enabled`: Enable or disable Istio integration (default: `false`)
- `istio.mtls.mode`: mTLS enforcement mode
  - `STRICT`: Only mTLS traffic allowed (recommended for production)
  - `PERMISSIVE`: Both mTLS and plaintext allowed (useful for migration)
  - `DISABLE`: mTLS disabled

**How It Works:**

When Istio is enabled:
1. All Galasa service pods receive an Istio sidecar proxy
2. The sidecar automatically encrypts all pod-to-pod traffic using mTLS
3. Application code continues to use HTTP URLs - encryption is transparent
4. Test pods launched by the Engine Controller also receive Istio sidecars

**Migration Strategy:**

For existing deployments, use a gradual migration approach:

1. **Enable with PERMISSIVE mode:**
   ```yaml
   istio:
     enabled: true
     mtls:
       mode: "PERMISSIVE"
   ```

2. **Upgrade your deployment:**
   ```bash
   helm upgrade my-galasa galasa/ecosystem -f values.yaml --wait
   ```

3. **Verify all services are working:**
   ```bash
   # Check that all pods have Istio sidecars (should show 2 containers per pod)
   kubectl get pods
   
   # Verify mTLS is enabled by checking Istio proxy config
   istioctl proxy-status 
   ```

4. **Switch to STRICT mode:**
   ```yaml
   istio:
     enabled: true
     mtls:
       mode: "STRICT"
   ```

5. **Upgrade again:**
   ```bash
   helm upgrade my-galasa galasa/ecosystem -f values.yaml --wait
   ```

**Troubleshooting:**

- **Pods not getting sidecars:** Verify Istio is installed with `kubectl get pods -n istio-system`
- **Connection failures:** Use PERMISSIVE mode during migration, then switch to STRICT
- **Check Istio proxy logs:** `kubectl logs <pod-name> -c istio-proxy`

#### Storage Class

If your cluster requires a specific StorageClass for persistent volumes:

```yaml
storageClass: my-storage-class
```

If you are deploying to minikube, you can optionally use the standard storage class that is created for you by minikube, but this is not required and can be left empty.

#### Custom Logging (Log4j2)

Customize log format by setting `log4j2Properties` in `values.yaml`:

```yaml
log4j2Properties: |
  status = error
  name = Default

  appender.console.type = Console
  appender.console.name = stdout
  appender.console.layout.type = PatternLayout
  appender.console.layout.pattern = %d{dd/MM/yyyy HH:mm:ss.SSS} %-5p %c{1.} - %m%n

  rootLogger.level = debug
  rootLogger.appenderRef.stdout.ref = stdout
```

For JSON templates, create a ConfigMap and reference it:

=== "macOS or Linux"

    ```bash
    kubectl create configmap my-json-layouts --from-file=/path/to/MyLayout.json
    ```

=== "Windows (PowerShell)"

    ```powershell
    kubectl create configmap my-json-layouts --from-file=C:\path\to\MyLayout.json
    ```

```yaml
log4jJsonTemplatesConfigMapName: my-json-layouts
```

The ConfigMap is mounted into the Galasa service pods in the `/log4j-config` directory. Refer to the [Log4j documentation](https://logging.apache.org/log4j/2.x/manual/configuration.html){target="_blank"} for available properties.

#### Internal Certificates

For connecting to servers with internal or corporate certificates:

1\. Create a ConfigMap with your certificates:

=== "macOS or Linux"

    ```bash
    kubectl create configmap my-certificates \
      --from-file=/path/to/certificate1.pem \
      --from-file=/path/to/certificate2.pem
    ```

=== "Windows (PowerShell)"

    ```powershell
    kubectl create configmap my-certificates `
      --from-file=C:\path\to\certificate1.pem `
      --from-file=C:\path\to\certificate2.pem
    ```

2\. Reference it in `values.yaml`:
   ```yaml
   certificatesConfigMapName: my-certificates
   ```

## Installing on Minikube

⚠️ **Minikube is for development/testing only, not production use.**

### Prerequisites

- [Minikube](https://minikube.sigs.k8s.io/docs/start/){target="_blank"} installed and running
- Verify with: `minikube status`
- If minikube is not running, start it: `minikube start`

### Installation Steps

1\. **Enable Ingress:**
   ```bash
   minikube addons enable ingress
   ```

2\. **Configure hosts file:**
   
   Add an entry to your hosts file, ensuring the IP address matches the output of `minikube ip`:

=== "Mac or Linux"

    ```bash
    # Get the minikube IP
    minikube ip
    
    # Add this line to /etc/hosts (replace IP with your minikube IP)
    192.168.49.2 galasa.local
    ```

=== "Windows (run as Administrator)"

    ```powershell
    # Get the minikube IP
    minikube ip
    
    # Add this line to C:\Windows\System32\drivers\etc\hosts (replace IP with your minikube IP)
    192.168.49.2 galasa.local
    ```

3\. **Configure values.yaml:**
   - Set `externalHostname: galasa.local`
   - Configure Dex and Ingress as described in the [Configuration Guide](#configuration-guide)

4\. **Install:**
   ```bash
   helm install my-galasa galasa/ecosystem -f values.yaml --wait
   ```

5\. **Verify:**
   ```bash
   kubectl get pods  # Wait for all pods to be Ready
   helm test my-galasa
   ```

Your Galasa Ecosystem is now accessible at `http://galasa.local/api/bootstrap`

## Verifying the Installation

After the `helm install` command completes, verify your ecosystem is working:

```bash
helm test <release-name>
```

Expected output:
```
TEST SUITE:     my-galasa-validate
Last Started:   Mon Mar  3 11:44:24 2025
Last Completed: Mon Mar  3 11:45:45 2025
Phase:          Succeeded
```

**Access your ecosystem:**

- Bootstrap URL: `https://<your-hostname>/api/bootstrap`
- Web UI: `https://<your-hostname>`

This is the URL that you would enter into a galasactl command's `--bootstrap` option to interact with your Ecosystem.

**Monitor pods:**
```bash
kubectl get pods
```

All pods should show `Running` status and `1/1` ready. Example output:
```console
NAME                                      READY   STATUS     RESTARTS      AGE
test-api-7945f959dd-v8tbs                 1/1     Running    0             65s
test-engine-controller-56fb476f45-msj4x   1/1     Running    0             65s
test-etcd-0                               1/1     Running    0             65s
test-metrics-5fd9f687b6-rwcww             1/1     Running    0             65s
test-ras-0                                1/1     Running    0             65s
test-resource-monitor-778c647995-x75z9    1/1     Running    0             65s
```

## Running Galasa Tests

Once you have successfully installed the Ecosystem, you can deploy your Galasa tests to a Maven repository and set up a test stream. For more information on managing tests, see the [Managing tests in a Galasa Ecosystem](../manage-ecosystem/index.md) documentation.

## Upgrading the Galasa Ecosystem

To upgrade to a newer Galasa version:

=== "macOS or Linux"

    ```bash
    helm repo update
    helm upgrade <release-name> galasa/ecosystem \
      --reuse-values \
      --set galasaVersion=0.46.1 \
      --wait
    ```

=== "Windows (PowerShell)"

    ```powershell
    helm repo update
    helm upgrade <release-name> galasa/ecosystem `
      --reuse-values `
      --set galasaVersion=0.46.1 `
      --wait
    ```

Or update your `values.yaml` and run:

```bash
helm upgrade <release-name> galasa/ecosystem -f values.yaml --wait
```

where:

- `galasaVersion` is set to the version that you want to use
- `<release-name>` is the name that you gave to the Ecosystem during installation

## Uninstalling the Galasa Ecosystem

To uninstall the Galasa Ecosystem:

```bash
helm uninstall <release-name>
```

This removes all Kubernetes resources created by the chart.

## Troubleshooting

- **Check pod logs:** Run `kubectl logs <pod-name>` to view logs for a specific pod
- **Check pod details:** Run `kubectl describe pod <pod-name>` to see detailed information about a pod
- **Verify Galasa version:** Ensure the version number in your `values.yaml` matches your intended version
- **Validation errors:** If an 'unknown fields' error message is displayed, you can turn off validation by using the `--validate=false` flag with kubectl commands
