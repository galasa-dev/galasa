/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal;

import java.io.IOException;
import java.util.List;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.util.Config;

public class KubernetesApiClient implements IKubernetesApiClient {

    private AppsV1Api kubeApiClient;

    public KubernetesApiClient() {
        try {
            ApiClient client = Config.defaultClient();
            this.kubeApiClient = new AppsV1Api(client);
        } catch (IOException e) {
            this.kubeApiClient = new AppsV1Api();
        }
    }

    @Override
    public List<V1Deployment> getNamespacedDeployments(String namespace, String labelSelector) throws ApiException {
        return kubeApiClient.listNamespacedDeployment(namespace)
            .labelSelector(labelSelector)
            .execute()
            .getItems();
    }

    @Override
    public V1Deployment getDeploymentByName(String name, String namespace) throws ApiException {
        return kubeApiClient.readNamespacedDeployment(name, namespace).execute();
    }
}
