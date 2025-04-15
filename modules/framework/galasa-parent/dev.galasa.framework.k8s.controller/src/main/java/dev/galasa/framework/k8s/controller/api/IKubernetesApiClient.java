/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.api;

import java.io.IOException;
import java.util.List;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;

public interface IKubernetesApiClient {
    List<V1Pod> getPods(String namespace, String labelSelector) throws ApiException;

    void deletePod(String namespace, String podName) throws ApiException, IOException;
}
