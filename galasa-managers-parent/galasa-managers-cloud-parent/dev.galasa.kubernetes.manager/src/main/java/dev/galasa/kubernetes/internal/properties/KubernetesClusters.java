/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.kubernetes.KubernetesManagerException;


/**
 * Kubernetes Cluster IDs CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name kubernetes.cluster.ids
 * 
 * @galasa.description Provides a comma separated list of the active Kubernetes Clusters that are defined in the CPS
 * 
 * @galasa.required No
 * 
 * @galasa.default Defaults to a single cluster ID of K8S if the property is missing
 * 
 * @galasa.valid_values A comma separated list of alphanumeric IDs.  Normally uppercased. 
 * 
 * @galasa.examples 
 * <code>kubernetes.cluster.ids=K8S,ALTERNATE</code>
 * 
 */
public class KubernetesClusters extends CpsProperties {

    public static List<String> get() throws KubernetesManagerException {
        return getStringListWithDefault(KubernetesPropertiesSingleton.cps(), "K8S", "cluster", "ids");
    }
}