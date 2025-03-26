/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.api.monitors.internal.IKubernetesApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;

public class MockKubernetesApiClient implements IKubernetesApiClient {

    private List<V1Deployment> mockDeployments = new ArrayList<>();

    private boolean isThrowErrorEnabled = false;

    public void addMockDeployment(V1Deployment deployment) {
        mockDeployments.add(deployment);
    }

    @Override
    public List<V1Deployment> getNamespacedDeployments(String namespace, String labelSelector) throws ApiException {
        throwApiExceptionIfEnabled();
        return mockDeployments;
    }

    public void setThrowErrorEnabled(boolean isThrowErrorEnabled) {
        this.isThrowErrorEnabled = isThrowErrorEnabled;
    }

    private void throwApiExceptionIfEnabled() throws ApiException {
        if (isThrowErrorEnabled) {
            throw new ApiException("simulating an error from kubernetes");
        }
    }

    @Override
    public V1Deployment getDeploymentByName(String name, String namespace) throws ApiException {
        throwApiExceptionIfEnabled();

        V1Deployment matchingDeployment = null;
        for (V1Deployment deployment : mockDeployments) {
            if (deployment.getMetadata().getName().equals(name)) {
                matchingDeployment = deployment;
                break;
            }
        }
        return matchingDeployment;
    }
}
