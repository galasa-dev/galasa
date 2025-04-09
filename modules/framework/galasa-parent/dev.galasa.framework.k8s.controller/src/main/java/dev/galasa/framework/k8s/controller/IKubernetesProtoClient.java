/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.io.IOException;

import io.kubernetes.client.openapi.ApiException;

public interface IKubernetesProtoClient {
    void deletePod(String namespace, String podName) throws ApiException, IOException;
}
