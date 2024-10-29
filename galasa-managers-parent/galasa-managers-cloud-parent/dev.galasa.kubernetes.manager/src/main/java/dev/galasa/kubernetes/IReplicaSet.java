/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes;

import java.util.List;

/**
 * Represents a resource that utilises ReplicatSets,  ie Deployment and StatefulSet
 * 
 *  
 *
 */
public interface IReplicaSet extends IResource {

    public List<IPodLog> getPodLogs(String container) throws KubernetesManagerException;

}
