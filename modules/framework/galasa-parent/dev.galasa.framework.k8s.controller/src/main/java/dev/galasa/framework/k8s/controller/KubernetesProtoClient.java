/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.io.IOException;

import io.kubernetes.client.ProtoClient;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.proto.V1.Namespace;

public class KubernetesProtoClient implements IKubernetesProtoClient {

    private ProtoClient client;

    public KubernetesProtoClient(ApiClient apiClient) {
        this.client = new ProtoClient(apiClient);
    }

    @Override
    public void deletePod(String namespace, String podName) throws ApiException, IOException {
        client.delete(Namespace.newBuilder(), "/api/v1/namespaces/" + namespace + "/pods/" + podName);
    }
}
