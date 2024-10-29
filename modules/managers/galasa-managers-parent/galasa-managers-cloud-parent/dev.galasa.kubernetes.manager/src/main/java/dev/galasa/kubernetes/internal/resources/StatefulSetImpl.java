/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.resources;

import java.util.List;

import dev.galasa.kubernetes.IPodLog;
import dev.galasa.kubernetes.IStatefulSet;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.KubernetesNamespaceImpl;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.util.Yaml;

/**
 * StatefulSet implementation
 * 
 *  
 *
 */
public class StatefulSetImpl extends ReplicaSetHolder implements IStatefulSet {
    
    private final KubernetesNamespaceImpl namespace;
    private V1StatefulSet statefulSet;

    public StatefulSetImpl(KubernetesNamespaceImpl namespace, V1StatefulSet deployment) {
        this.namespace   = namespace;
        this.statefulSet = deployment;
    }

    @Override
    public String getName() {
        return statefulSet.getMetadata().getName();
    }

    @Override
    public TYPE getType() {
        return TYPE.StatefulSet;
    }

    @Override
    public String getYaml() {
        return Yaml.dump(this.statefulSet);
    }
    
    @Override
    public void refresh() throws KubernetesManagerException {
       try {
           AppsV1Api api = new AppsV1Api(namespace.getCluster().getApi());
           
           V1StatefulSet newSet = api.readNamespacedStatefulSet(getName(), namespace.getId(), null);
           
           this.statefulSet = newSet;
       } catch(Exception e) {
           throw new KubernetesManagerException("Problem refreshing the resource YAML", e);
       }
    }

    @Override
    public List<IPodLog> getPodLogs(String container) throws KubernetesManagerException {
        if (statefulSet.getSpec() == null || statefulSet.getSpec().getSelector() == null) {
            throw new KubernetesManagerException("Missing Selector");
        }

        return getPodLogs(this.namespace.getCluster().getApi(), this.statefulSet.getSpec().getSelector(), this.namespace.getId(), container);
    }

}
