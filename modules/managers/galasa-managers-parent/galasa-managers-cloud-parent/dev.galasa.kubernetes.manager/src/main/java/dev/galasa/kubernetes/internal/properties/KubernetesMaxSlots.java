/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.KubernetesClusterImpl;


/**
 * Maximum Slots on Cluster CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name kubernetes.cluster.[XXXX.]max.slots
 * 
 * @galasa.description Specifies the maximum number of slots(namespaces) that can be allocated at one time on the cluster
 * 
 * @galasa.required No
 * 
 * @galasa.default Defaults to 2 if not provided
 * 
 * @galasa.valid_values Integer value.  A value <0 disables the cluster.
 * 
 * @galasa.examples 
 * <code>kubernetes.cluster.K8S.max.slots=5</code>
 * 
 */
public class KubernetesMaxSlots extends CpsProperties {

    public static int get(KubernetesClusterImpl cluster) throws KubernetesManagerException {
        return getIntWithDefault(KubernetesPropertiesSingleton.cps(), 2, "cluster", "max.slots", cluster.getId()) ;
    }
}