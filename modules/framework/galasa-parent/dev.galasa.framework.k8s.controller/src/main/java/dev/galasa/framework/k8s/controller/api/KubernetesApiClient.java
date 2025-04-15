/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.api;

import java.io.IOException;
import java.util.List;

import io.kubernetes.client.ProtoClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.proto.V1.Namespace;

public class KubernetesApiClient implements IKubernetesApiClient {

    private CoreV1Api api;
    private ProtoClient protoClient;

    public KubernetesApiClient(CoreV1Api api, ProtoClient protoClient) {
        this.api = api;
        this.protoClient = protoClient;
    }

    @Override
    public List<V1Pod> getPods(String namespace, String labelSelector) throws ApiException {
        V1PodList podList = api.listNamespacedPod(namespace)
            .labelSelector(labelSelector)
            .execute();

        return podList.getItems();
    }

    @Override
    public void deletePod(String namespace, String podName) throws ApiException, IOException {
        protoClient.delete(Namespace.newBuilder(), "/api/v1/namespaces/" + namespace + "/pods/" + podName);
    }
}
