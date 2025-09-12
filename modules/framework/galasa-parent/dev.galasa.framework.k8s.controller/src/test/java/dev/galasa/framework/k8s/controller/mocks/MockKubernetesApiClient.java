/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.mocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.galasa.framework.k8s.controller.api.IKubernetesApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Pod;

public class MockKubernetesApiClient implements IKubernetesApiClient {

    private List<V1Pod> mockPods = new ArrayList<>();
    public List<V1Pod> podsFailedToLaunch = new ArrayList<>();

    public List<V1Pod> podsLaunched = new ArrayList<>();

    // The mock fails to launch pods this number of times, before letting one launch.
    public int failToLaunchPodCount = 0;

    private V1ConfigMap configMap = null ;

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
        List<V1Pod> matchingPods = new ArrayList<>();

        // Label selectors can be in the form <key>=<value>
        // or <key1>=<value1>,<key2>=<value2> for multiple selectors
        Map<String, String> labelSelectorMap = new HashMap<>();
        String[] labelSelectorParts = labelSelector.split(",");

        for (String part : labelSelectorParts) {
            // Each part will now be in the form <key>=<value>
            String[] keyValueSelectorPair = part.split("=");
            
            String labelSelectorKey = keyValueSelectorPair[0];
            String labelSelectorValue = keyValueSelectorPair[1];

            labelSelectorMap.put(labelSelectorKey, labelSelectorValue);
        }

        for (V1Pod pod : mockPods) {
            Map<String, String> podLabels = pod.getMetadata().getLabels();
            boolean isMatchingLabelSelectors = true;

            for (Entry<String, String> selector : labelSelectorMap.entrySet()) {
                String podLabelValue = podLabels.get(selector.getKey());
                if (podLabelValue == null || !podLabelValue.equals(selector.getValue())) {
                    isMatchingLabelSelectors = false;
                    break;
                }
            }
            if (isMatchingLabelSelectors) {
                matchingPods.add(pod);
            }
        }
        return matchingPods;
    }



    @Override
    public V1Pod createNamespacedPod(String namespace, V1Pod newPodDefinition) throws ApiException {
        if (failToLaunchPodCount>0) {
            failToLaunchPodCount-=1;

            // Clone the pod using json, as it doesn't have a deep clone constructor.
            V1Pod podDefinitionClone = null;
            try {
                podDefinitionClone = V1Pod.fromJson(newPodDefinition.toJson());
            } catch( IOException ex ) {
                throw new RuntimeException("Failed to parse the json in a v1 pod!");
            }
            podsFailedToLaunch.add( podDefinitionClone );

            Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
            String responseBody = "AlreadyExists";
            throw new ApiException(1234, responseHeaders, responseBody);
        }
        podsLaunched.add(newPodDefinition);
        return newPodDefinition;
    }

    @Override
    public V1ConfigMap readNamespacedConfigMap(String configMapName, String namespace) throws ApiException {

        return this.configMap ;
    }

    public void setConfigMap(V1ConfigMap newConfigMap) {
        this.configMap = newConfigMap;
    }
    
}
