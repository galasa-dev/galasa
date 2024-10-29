/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.properties;

import java.net.URL;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.KubernetesClusterImpl;


/**
 * Kubernetes Node Port Proxy Hostname CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name kubernetes.cluster.XXXX.nodeport.proxy.hostname
 * 
 * @galasa.description Provides the hostname that NodePorts can be accessed on.
 * 
 * @galasa.required No
 * 
 * @galasa.default The hostname as specified in the API URL
 * 
 * @galasa.valid_values A valid URL hostname
 * 
 * @galasa.examples 
 * <code>kubernetes.cluster.K8S.nodeport.proxy.hostname=cluster.org</code>
 * 
 */
public class KubernetesNodePortProxy extends CpsProperties {

    public static String get(KubernetesClusterImpl cluster) throws KubernetesManagerException {
        try {
            String hostname = getStringNulled(KubernetesPropertiesSingleton.cps(), "cluster." + cluster.getId(), "nodeport.proxy.hostname");
            if (hostname != null) {
                return hostname;
            }
            URL api = KubernetesUrl.get(cluster);
            return api.getHost();
        } catch (Exception e) {
            throw new KubernetesManagerException("Problem retrieving the nodeport proxy hostname for the cluster " + cluster.getId(), e);
        }
    }
}