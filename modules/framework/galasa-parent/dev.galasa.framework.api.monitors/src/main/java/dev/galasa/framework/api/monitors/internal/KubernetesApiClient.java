/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal;

import java.util.List;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;

public class KubernetesApiClient implements IKubernetesApiClient {

    private AppsV1Api kubeApiClient;

    public KubernetesApiClient() {
        this.kubeApiClient = new AppsV1Api();
    }

    @Override
    public List<V1Deployment> getNamespacedDeployments(String namespace, String labelSelector) throws ApiException {
        return kubeApiClient.listNamespacedDeployment(namespace)
            .labelSelector(labelSelector)
            .execute()
            .getItems();
    }
}
