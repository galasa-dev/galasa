/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.resources;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.kubernetes.IPodLog;
import dev.galasa.kubernetes.KubernetesManagerException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;

/**
 * ReplicaSet type resource implementation, ie Deployment/StatefulSet
 * 
 *  
 *
 */
public abstract class ReplicaSetHolder {

    /**
     * Retrieve all the pod logs for a deployment/statefulset
     * 
     * @param apiClient the api client
     * @param labelSelector The label selector to find the pods
     * @param namespace the namespace id
     * @param container the container name
     * @return A list of pod logs, never null 
     * @throws KubernetesManagerException A lot could go wrong
     */
    @NotNull
    protected static List<IPodLog> getPodLogs(ApiClient apiClient, V1LabelSelector labelSelector, String namespace, String container) throws KubernetesManagerException {   
        ArrayList<IPodLog> podLogs = new ArrayList<>();
        //*** Find all the pods belong to the StatefulSet
        
        try {
            CoreV1Api coreApi = new CoreV1Api(apiClient);
            
            String convertedLabelSelector = Utility.convertLabelSelector(labelSelector);
            
            V1PodList pods = coreApi.listNamespacedPod(namespace, null, null, null, null, convertedLabelSelector, null, null, null, null, null);
            for(V1Pod pod : pods.getItems()) {
                String name = pod.getMetadata().getName();
                String log = null;
                
                try {
                    log = coreApi.readNamespacedPodLog(name, namespace, container, null, null, null, null, null, null, null, null);
                } catch(ApiException e) {
                }
                
                podLogs.add(new PodLogImpl(name, log));
            }
            
            return podLogs;
        } catch(Exception e) {
            throw new KubernetesManagerException("Problem obtaining pod logs", e);
        }
    }

}
