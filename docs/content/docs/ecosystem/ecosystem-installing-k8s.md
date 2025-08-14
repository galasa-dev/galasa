---
title: "Installing an Ecosystem using Helm"
---

If you want to run scalable, highly available testing for enterprise level workloads, you need to install your Galasa Ecosystem in a Kubernetes cluster. Running Galasa in a Kubernetes cluster means that you can run many tests in parallel on a resilient and scalable platform, where the clean-up of test resources can be managed, and test results can be centralised and gathered easily. 

Galasa provides a Galasa Ecosystem Helm chart to install a Galasa Ecosystem. You can install the chart into a Kubernetes cluster or minikube. However, note that minikube is not suitable for production purposes because it provides a single Kubernetes node and therefore does not scale well. Use minikube only for development and testing purposes.

The following sections explain how to install a Galasa Ecosystem on a Kubernetes cluster (and on minikube) by using Helm and validate that the Ecosystem is installed correctly. 

The [Galasa Helm repository](https://github.com/galasa-dev/helm){target="_blank"} contains the Galasa Ecosystem Helm chart that is referenced in the following sections.

_Note:_ The Galasa Ecosystem Helm chart currently supports only x86-64 systems. It cannot be installed on ARM64-based systems.


## Prerequisites

- [Helm](https://helm.sh){target="_blank"} must be installed to use the chart. See the [Helm documentation](https://helm.sh/docs/){target="_blank"} for installation instructions.

- The Kubernetes command-line tool *kubectl* must be installed on the machine that is used to run the commands and must be configured to point at your Kubernetes cluster. To find out more about Kubernetes, see the [Kubernetes Documentation](https://kubernetes.io/docs/home/){target="_blank"}.

- You must have a Kubernetes cluster at version 1.16 or higher. You can check the version number by running the `kubectl version` command.

- If you want to install the chart into minikube, ensure you have minikube installed and that it is running with `minikube status`. If minikube is not running, start it by running `minikube start`. Once minikube is running, follow the instructions in the following sections to install the Galasa Ecosystem Helm chart.


## Kubernetes role-based access control

If role-based access control (RBAC) is active on your Kubernetes cluster, a user with the `galasa-admin` role (or a role with equivalent permissions) is needed to run Helm commands on the cluster. The `galasa-admin` role allows assigned users to run the Helm install, upgrade, and delete commands to interact with the Helm chart. 

You can assign the `galasa-admin` role to a user by replacing the placeholder username in the [rbac-admin.yaml](https://github.com/galasa-dev/helm/blob/main/charts/ecosystem/rbac-admin.yaml){target="_blank"} file with a username that corresponds to a user with access to your cluster. If multiple users require admin privileges, you can assign the `galasa-admin` role to multiple groups, users, or service accounts by extending the `subjects` list. See the [Using RBAC Authorization Kubernetes documentation](https://kubernetes.io/docs/reference/access-authn-authz/rbac/){target="_blank"} for more information.

You also need a Galasa service account. The Galasa service account allows the API, Engine Controller, Metrics, and Resource Monitor to co-ordinate between themselves, and allows the Engine Controller to create and manage engine pods. 

For chart versions later than `0.23.0`, the Galasa service account is automatically created, if one does not already exist, when installing the Galasa Ecosystem Helm chart.  The Galasa service account enables the API, Engine Controller, Metrics, and Resource Monitor to co-ordinate, while allowing the Engine Controller to create and manage engine pods.

For chart versions `0.23.0` and earlier, you must create the Galasa service account manually by running the following command in the repository's [ecosystem](https://github.com/galasa-dev/helm/tree/main/charts/ecosystem){target="_blank"} directory:

```shell
kubectl apply -f \
https://raw.githubusercontent.com/galasa-dev/helm/ecosystem-0.23.0/charts/ecosystem/rbac.yaml
``` 


## Installing a Galasa Ecosystem 

Complete the following steps to install a Galasa Ecosystem by using Helm:

### Adding the Galasa repository

1.	Add or update the Galasa repository.

    - If the repository does not exist, add the repository by running the following command:

        ```shell
        helm repo add galasa https://galasa-dev.github.io/helm
        ```

    - If the repository exists, run the `helm repo update` command to get the latest versions of the packages and then run `helm search repo galasa` to see the available charts.    

    _Note:_ The Galasa Ecosystem Helm chart deploys three persistent volumes (PVs). If you need to provide a Kubernetes storage class for these PVs, download the `values.yaml` file for your chosen Galasa Ecosystem version from the [Galasa Helm repository's releases page](https://github.com/galasa-dev/helm/releases){target="_blank"} and update the `storageClass` value in the file with the name of a valid storage class on your cluster. If you are deploying to minikube, you can optionally use the standard storage class that is created for you by minikube, but this is not required.

2. Download the values.yaml file for your chosen Galasa Ecosystem version from the [Galasa Helm repository's releases page](https://github.com/galasa-dev/helm/releases){target="_blank"} if you have not done so already, and edit the values of the following properties:

    - Set `galasaVersion` to the version of Galasa that you want to run. (See the [Releases](../../releases/index.md) documentation for released versions). To ensure that each pod in the Ecosystem is running at the same level, do not use `latest` as the Galasa version.

    - Set `externalHostname` to the DNS hostname that you wish to use to access the Galasa Ecosystem services.

After updating the `galasaVersion` and `externalHostname` values, complete the following instructions to set up Ingress for your Ecosystem. 

### Configuring Ingress

The Galasa Ecosystem Helm chart uses Ingress to reach services that are running within a Kubernetes cluster. To learn more about Ingress, see the [Kubernetes Documentation](https://kubernetes.io/docs/concepts/services-networking/ingress/){target="_blank"}.

*Note:* If you are deploying to minikube, ensure the NGINX Ingress controller is enabled by running the `minikube addons enable ingress` command.

Assuming that your Ingress controller is set up on your Kubernetes cluster, configure the use of Ingress in your Ecosystem by completing the following updates to the values that are listed under the `ingress` section within your `values.yaml` file:

1. Replace the `ingressClassName` value with the name of the IngressClass that is configured in your cluster. By default, `nginx` is used.

2. If you are using HTTPS, add a `tls` configuration within the `ingress` section, specifying the `hosts` list and a `secretName` value corresponding to the name of the Kubernetes Secret that contains your TLS private key and certificate. See the [Kubernetes Documentation](https://kubernetes.io/docs/concepts/services-networking/ingress/#tls){target="_blank"} for information on how to set up TLS.

After updating the values under the `ingress` section of your `values.yaml` file, complete the following instructions to configure Dex in your Ecosystem.

### Configuring Dex 

For Galasa version 0.32.0 and later, Dex is used to authenticate users interacting with a Galasa Ecosystem.

To configure Dex in your Ecosystem, complete the following steps to update your `values.yaml` file:

1. Replace the hostname in your `issuer` value with the same hostname given in `externalHostname` and set the URI scheme to either `http` or `https`. For example:

    ```yaml
    issuer: http://<your-external-hostname>/dex
    ```

2. Optional. Update the `expiry` section to configure the expiry of JSON Web Tokens (JWTs) and refresh tokens issued by Dex. By default, JWTs expire 24 hours after being issued and refresh tokens remain valid unless they have not been used for one year. See the [Dex documentation on ID tokens](https://dexidp.io/docs/tokens){target="_blank"} for information and available expiry settings. 

You can now configure Dex to authenticate via a connector to authenticate with an upstream identity provider, for example, GitHub, Microsoft, or an LDAP server. For a full list of supported connectors, refer to the [Dex documentation](https://dexidp.io/docs/connectors){target="_blank"}. The following instructions explain how to configure Dex to authenticate through GitHub.


### Configuring authentication

Complete the following steps to configure Dex to authenticate through GitHub:


1. Register an OAuth application in [GitHub](https://github.com/settings/applications/new){target="_blank"}, ensuring the callback URL of the application is set to your Dex `issuer` value, followed by `/callback`. For example, if your `issuer` value is `https://prod-ecosystem.galasa.dev/dex`, then your callback URL is `https://prod-ecosystem.galasa.dev/dex/callback`.

2. Add a GitHub connector to your Dex configuration, providing the name of your GitHub organisation and any teams that you require users to be part of to be able to use your Ecosystem as follows:

    ```yaml
    dex:
      config:
        # Other Dex configuration values...

        connectors:
        - type: github
          id: github
          name: GitHub
          config:
            clientID: $GITHUB_CLIENT_ID
            clientSecret: $GITHUB_CLIENT_SECRET
            redirectURI: <your-dex-issuer-url>/callback
            orgs:
            - name: my-org
              teams:
              - my-team
    ```

    where `$GITHUB_CLIENT_ID` and `$GITHUB_CLIENT_SECRET` correspond to the registered OAuth application's client ID and secret. Ensure that the `redirectURI` value is the same value that you provided when setting up your GitHub OAuth application in step 1.

    If you want to pull the client ID and secret values of your OAuth application from a Kubernetes Secret, create a Secret by running the following `kubectl` command, ensuring that the Secret's keys match those given in the GitHub connector's `clientID` and `clientSecret` values without the leading `$` (i.e. `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` as shown in the following example):

    ```shell
    kubectl create secret generic my-github-oauth-app-credentials \
    --from-literal=GITHUB_CLIENT_ID="myclientid" \
    --from-literal=GITHUB_CLIENT_SECRET="myclientsecret"
    ```

    When your Kubernetes Secret is created, supply the name of the Secret by using the `envFrom` value in your `values.yaml` file to mount the Secret as shown in the following example:

    ```yaml
    dex:
      envFrom:
        - secretRef:
          name: my-github-oauth-app-credentials

      config:
        # Other Dex configuration values...

        connectors:
        - type: github
          id: github
          name: GitHub
          config:
            clientID: $GITHUB_CLIENT_ID
            clientSecret: $GITHUB_CLIENT_SECRET
            redirectURI: <your-dex-issuer-url>/callback
            orgs:
            - name: my-org
              teams:
              - my-team
    ```

By default, the Galasa Ecosystem Helm chart creates a Kubernetes Secret containing configuration details for Dex. 
If you want to apply your own Dex configuration as a Secret, your Dex configuration must be provided in a `config.yaml` key within the Secret, 
and the value of the `config.yaml` key must be a valid Dex configuration.

For more information on configuring Dex, see the [Dex documentation](https://dexidp.io/docs){target="_blank"}.


## Configure the default user role, and 'owner' of the Galasa service

When the Galasa service is first installed, users logging in will be assigned a role as dictated by the `galasaDefaultUserRole` Helm chart property. For example 'tester'.
This means nobody initially logging into the Galasa service will have administrator privileges.
We would discourage ever setting this property to `admin` as doing so would provide a less secure Galasa service, with any action in the system available to anyone on the system.

To obtain administration rights to the Galasa service, the kubernetes install must nominate one or more users as 'owners' of the service.
When a service owner next logs into the Galasa service, they will be granted a role of `owner`.
From this point on, that user can assign the 'admin' role to anyone else who needs it using the `galasactl` command line or by using the web user interface via a browser.

Once the Galasa service has one or more users with the 'admin' role, kubernetes can be updated so that the system doesn't have any "owner" if desired. 
The `owner` role exists solely to fix situations where there are no administrators on the Galasa service, such as when the Galasa service is initially installed, or when none of the
members in the organisation have the `admin` role.
If a user was a nominated "owner", performs some administration tasks, and is then removed from the list of "owners", their role on the Galasa service will revert to what it was initially.

To configure a list of owners, use the `galasaOwnersLoginIds` property of the `values.yaml` file and use Helm to deploy it.
You can set multiple owners by adding a comma-separated list of login-ids.

Each login-id must match the login-id allocated to the user by the authentication service to which Galasa connects.
If you are unsure what login-id to use, try setting up your system without an owner and logging into the Galasa service using a browser, then viewing the user profile page before returning to update the owner login-id in your `values.yaml` and deploying it to kubernetes using `helm upgrade`.

## Installing the chart

After configuring your `values.yaml` file, complete the following steps to install the Galasa Ecosystem Helm chart:

1.  Install the Galasa Ecosystem chart by running the following command:

    ```console
	helm install -f /path/to/values.yaml <release-name> galasa/ecosystem --wait
    ```

    where:

    - `/path/to/values.yaml` is the path to where the `values.yaml` file is downloaded and

    - `<release-name>` is the name that you want to give the Ecosystem.

    The `--wait` flag ensures that the chart installation completes before marking it as `Deployed`. During the installation, the API pod waits for the etcd and RAS pods to initialise while the Engine Controller, Metrics, and Resource Monitor pods wait for the API pod to initialise.

2.	View the status of the deployed pods by running `kubectl get pods` in another terminal. The returned results should look similar to the following example:

    ```console
    NAME                                      READY   STATUS     RESTARTS      AGE
    test-api-7945f959dd-v8tbs                 1/1     Running    0             65s
    test-engine-controller-56fb476f45-msj4x   1/1     Running    0             65s
    test-etcd-0                               1/1     Running    0             65s
    test-metrics-5fd9f687b6-rwcww             1/1     Running    0             65s
    test-ras-0                                1/1     Running    0             65s
    test-resource-monitor-778c647995-x75z9    1/1     Running    0             65s
    ```


## Verifying the installation

After the `helm install` command completes with a successful deployment message, run the following command to check that the Ecosystem can be accessed externally to Kubernetes so that a simple test engine can be run:

```shell
helm test <release-name>
```

where:

`<release-name>` is the name that you gave the Ecosystem during installation.

When the `helm test` command ends and displays a success message, the Ecosystem is set up correctly and is ready to be used.


## Accessing services

The URL of the Ecosystem bootstrap will be your external hostname, followed by `/api/bootstrap`.

For example, if the external hostname that you provided was `example.com` and you provided values for using TLS, the bootstrap URL would be `https://example.com/api/bootstrap`. This is the URL that you would enter into a galasactl command's `--bootstrap` option to interact with your Ecosystem.

If you are deploying to minikube, add an entry to your `/etc/hosts` file, ensuring the IP address matches the output of `minikube ip`. For example:

```console
192.168.49.2 example.com
```


## Running Galasa tests

Once you have successfully installed the Ecosystem, you can then deploy your Galasa tests to a Maven repository and set up a test stream. For more information on managing tests, see the [Managing tests in a Galasa Ecosystem](../manage-ecosystem/index.md) documentation.


## Upgrading the Galasa Ecosystem

Get the latest version of the Ecosystem chart and upgrade the Galasa Ecosystem to use the newer version of Galasa - for example version 0.44.0 - by running the following command:

On Mac or Unix:

```shell
helm repo update \
helm upgrade <release-name> galasa/ecosystem --reuse-values \
--set galasaVersion=0.44.0 --wait
```

On Windows (Powershell):

```powershell
helm repo update `
helm upgrade <release-name> galasa/ecosystem --reuse-values `
--set galasaVersion=0.44.0 --wait
```

where:

- `galasaVersion` is set to the version that you want to use and

- `<release-name>` is the name that you gave to the Ecosystem during installation


### Troubleshooting

- Check the logs by running the ```kubectl logs``` command. 
- Check that the Galasa version number in the sample is correct by running the ```kubectl describe pod <podname>``` command.  If the version number is not the latest one, update the version number in the sample and apply the update.
- If an 'unknown fields' error message is displayed, you can turn off validation by using the  ```--validate=false``` command. 

