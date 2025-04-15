/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.mocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.k8s.controller.api.IKubernetesApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;

public class MockKubernetesApiClient implements IKubernetesApiClient {

    private List<V1Pod> mockPods = new ArrayList<>();

    public MockKubernetesApiClient(List<V1Pod> mockPods) {
        this.mockPods = mockPods;
    }

    @Override
    public void deletePod(String namespace, String podName) throws ApiException, IOException {
        V1Pod podToDelete = null;
        for (V1Pod pod : mockPods) {
            String currentPodName = pod.getMetadata().getName();
            if (podName.equals(currentPodName)) {
                podToDelete = pod;
                break;
            }
        }
        mockPods.remove(podToDelete);
    }

    public List<V1Pod> getMockPods() {
        return this.mockPods;
    }

    @Override
    public List<V1Pod> getPods(String namespace, String labelSelector) throws ApiException {
        return this.mockPods;
    }
    
}
