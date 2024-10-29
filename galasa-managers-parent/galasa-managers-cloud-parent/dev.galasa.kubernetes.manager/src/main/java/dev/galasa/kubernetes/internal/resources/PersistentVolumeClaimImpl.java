/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.resources;

import dev.galasa.kubernetes.IPersistentVolumeClaim;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.KubernetesNamespaceImpl;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.util.Yaml;

/**
 * PersistentVolumeClaim implementation
 * 
 *  
 *
 */
public class PersistentVolumeClaimImpl implements IPersistentVolumeClaim {
    
    private final V1PersistentVolumeClaim pvc;

    public PersistentVolumeClaimImpl(KubernetesNamespaceImpl namespace, V1PersistentVolumeClaim pvc) {
        this.pvc = pvc;
    }

    @Override
    public String getName() {
        return pvc.getMetadata().getName();
    }

    @Override
    public TYPE getType() {
        return TYPE.PersistentVolumeClaim;
    }

    @Override
    public String getYaml() {
        return Yaml.dump(this.pvc);
    }
    
    @Override
    public void refresh() throws KubernetesManagerException {
       throw new UnsupportedOperationException("Not developed yet"); //TODO
    }
}
