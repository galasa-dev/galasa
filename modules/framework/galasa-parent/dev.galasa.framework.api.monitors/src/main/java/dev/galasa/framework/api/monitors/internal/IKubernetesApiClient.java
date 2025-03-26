/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal;

import java.util.List;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;

public interface IKubernetesApiClient {

    List<V1Deployment> getNamespacedDeployments(String namespace, String labelSelector) throws ApiException;

    V1Deployment getDeploymentByName(String name, String namespace) throws ApiException;
}
