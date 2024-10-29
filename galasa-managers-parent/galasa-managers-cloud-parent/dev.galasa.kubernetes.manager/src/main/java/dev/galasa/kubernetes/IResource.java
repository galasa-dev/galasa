/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes;

/**
 * Abstracts a Kubernetes Resource, so that the test is not dependent of whatever Kubernetes client the 
 * Kubernetes Manager decides to use.
 * 
 *  
 *
 */
public interface IResource {

    public enum TYPE {
        Deployment,
        StatefulSet,
        Secret,
        Service,
        ConfigMap,
        PersistentVolumeClaim
    }
    
    /**
     * @return the name of the resource from metadata.name
     */
    public String getName();
    /**
     * @return The type of the resource as the Kubernetes Manager knows it
     */
    public TYPE getType();
    
    /**
     * @return Retrieve the raw YAML, this is populated on create resource, after the resource has been created, so should
     * have the status
     */
    public String getYaml();
    
    /**
     * refresh the raw YAML that is provided by getYaml()
     * 
     * @throws KubernetesManagerException If there is a comms problem to the Kubernetes Cluster
     */
    public void refresh() throws KubernetesManagerException;

}
