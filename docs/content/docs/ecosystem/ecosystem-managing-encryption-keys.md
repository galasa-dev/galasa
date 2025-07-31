---
title: "Managing Ecosystem encryption keys"
---

To maintain the security of your Galasa Ecosystem, you may wish to replace the encryption key being used to encrypt credentials in the Galasa Ecosystem's credentials store with a new encryption key, and re-encrypt all your existing credentials using the new key.

!!! warning "Backup existing credentials before proceeding"

    Before proceeding to rotate encryption keys, it is highly recommended to make a backup of the existing credentials stored in your Galasa Ecosystem by running the following command using the Galasa CLI tool:

    ```console
    galasactl secrets get --format yaml > /path/to/backup/file.yaml
    ```

    where `/path/to/backup/file.yaml` is either an absolute or relative path of your choice to a file where the backup will be stored.

## Prerequisites

The following command-line utilities must be installed:

- [kubectl](https://kubernetes.io/docs/tasks/tools) (v1.30.3 or later){target="_blank"}

- [galasactl](../cli-command-reference/installing-cli-tool.md){target="_blank"} (0.38.0 or later)

- openssl (3.3.2 or later)

You must have the correct permissions to read and update Kubernetes Secrets in the Kubernetes namespace that you have installed your Galasa Ecosystem within.

You must also have a valid personal access token for your Galasa Ecosystem set on your machine so that you can retrieve and update secrets with `galasactl`.

## Automated steps

For Linux and macOS, you can download the rotate-encryption-keys.sh script for your installed Galasa Ecosystem version from the [Galasa Helm repository's releases page](https://github.com/galasa-dev/helm/releases){target="_blank"} and run the script via the command-line to simplify the process of rotating encryption keys and re-encrypting credentials.

The following flags can be supplied when running the script:

- `--release-name <name>` **Required**. The Helm release name provided when installing the Galasa Ecosystem Helm chart (see [Installing your Galasa Ecosystem](./ecosystem-installing-k8s.md))

- `--namespace <namespace>` Optional. The Kubernetes namespace where your Galasa Ecosystem is installed

- `--bootstrap <bootstrap-url>` Optional. The bootstrap URL of the Galasa Ecosystem that is being serviced. Not required if the `GALASA_BOOTSTRAP` environment variable is set and is pointing to the correct bootstrap URL. Overrides the existing `GALASA_BOOTSTRAP` environment variable value if set

For example:

```shell
./rotate-encryption-keys.sh --release-name example --namespace galasa-dev
```

The `rotate-encryption-keys.sh` script will automatically update the current encryption key with a new one, and then restart your Galasa Ecosystem's API and engine controller pods so that they can pick up the new encryption key. After rotating the encryption keys, the script will re-encrypt the existing secrets in your Galasa Ecosystem using the newly activated encryption key.

Once the encryption keys have been rotated and the existing secrets have been re-encrypted, the script will clear the fallback decryption keys list and restart the API and engine controller pods for a final time to keep the Galasa services in sync with the contents of the encryption keys secret.


## Manual steps

If the `rotate-encryption-keys.sh` script fails or if you are on Windows and cannot run the script, you can manually rotate encryption keys by performing the following steps.

This guide assumes `kubectl` is configured to manage resources in the namespace that your Galasa Ecosystem is installed within, so the `--namespace` flag is not required when running `kubectl` commands.

This guide also assumes that the `GALASA_BOOTSTRAP` environment variable is set, so the `--bootstrap` flag is not required when running `galasactl` commands.

1. Get all the existing secrets from your Galasa Ecosystem by running:

    ```shell
    galasactl secrets get --format yaml
    ```

    Store these secrets in a `.yaml` or `.yml` file so that you can re-encrypt them after rotating the encryption keys.

2. Find the name of the Kubernetes Secret containing your Galasa Ecosystem's encryption keys by running:

    ```shell
    kubectl get secrets
    ```

    The secret's name should be of the form `{release-name}-encryption-secret`, where `{release-name}` is the Helm release name provided when installing the Galasa Ecosystem Helm chart.

3. Get the existing encryption keys data for your Galasa Ecosystem by running:

    ```shell
    kubectl get secret {encryption-secret-name} --output jsonpath='{ .data.encryption-keys\.yaml }' | openssl base64 -d -A
    ```

    where `{encryption-secret-name}` is the name of the Kubernetes secret retrieved in step 2. The output should look like the following:

    ```yaml
    encryptionKey: <existing-encryption-key>
    fallbackDecryptionKeys: []
    ```

    Place the output into a file.

4. Generate a new encryption key by running:

    ```shell
    openssl rand -base64 32
    ```

5. In the file created at the end of step 3, move the existing `encryptionKey` value into the `fallbackDecryptionKeys` list and place the newly generated encryption key into the `encryptionKey` field. The file should now look like this:

    ```yaml
    encryptionKey: <newly-generated-encryption-key>
    fallbackDecryptionKeys:
    - <existing-encryption-key>
    ```

    where `<newly-generated-encryption-key>` is the new encryption key generated in step 5 and `<existing-encryption-key>` is the old encryption key retrieved in step 3.

6. Base64-encode the file contents by running the following command:

    ```shell
    openssl base64 -in <encryption-keys-file>
    ```
    
    where `<encryption-keys-file>` is an absolute or relative path to the file created in step 3.

    Record the base64-encoded output, making sure there are no spaces or line breaks in the recorded output.

7. Update the existing Kubernetes Secret with the rotated keys by running:

    ```shell
    kubectl patch secret {encryption-secret-name} --type='json' -p="[{'op': 'replace', 'path': '/data/encryption-keys.yaml', 'value': '<base64-encoded-encryption-keys>'}]"
    ```

    where `{encryption-secret-name}` is the name of the Kubernetes secret retrieved in step 2, and `<base64-encoded-encryption-keys>` is the output recorded in step 6.

8. Restart the Galasa Ecosystem's API server deployment by running:

    ```shell
    kubectl rollout restart deployment {release-name}-api
    kubectl rollout status deployment {release-name}-api
    ```

    where `{release-name}` is the name of the Helm release provided when installing the Galasa Ecosystem Helm chart.

9. Restart the Galasa Ecosystem's engine controller deployment by running:

    ```shell
    kubectl rollout restart deployment {release-name}-engine-controller
    kubectl rollout status deployment {release-name}-engine-controller
    ```

    where `{release-name}` is the name of the Helm release provided when installing the Galasa Ecosystem Helm chart.

10. Once both the API server and engine controller have been restarted successfully, you can re-encrypt your existing secrets using the YAML file you created in step 1, by running:

    ```shell
    galasactl resources apply -f <secrets-yaml-file>
    ```

    where `<secrets-yaml-file>` is an absolute or relative path to the YAML file created at the end of step 1.

Your Galasa Ecosystem will now use the newly generated encryption key to encrypt and decrypt secrets until the next time it is rotated.

To verify that your secrets can still be read correctly, you can run `galasactl secrets get --format yaml` again and compare the YAML output with the content of YAML file that you applied in step 10. If the output is the same, then the secrets have been re-encrypted successfully.
