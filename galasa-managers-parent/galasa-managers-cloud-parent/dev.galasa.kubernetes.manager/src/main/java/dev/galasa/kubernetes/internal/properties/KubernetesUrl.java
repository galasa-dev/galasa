/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.properties;

import java.net.MalformedURLException;
import java.net.URL;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.KubernetesClusterImpl;


/**
 * Kubernetes Cluster API URL CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name kubernetes.cluster.XXXX.url
 * 
 * @galasa.description The API URL of the Kubernetes Cluster
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid URL
 * 
 * @galasa.examples 
 * <code>kubernetes.cluster.K8S.url=http://cluster.org:8443</code>
 * 
 */
public class KubernetesUrl extends CpsProperties {

    public static @NotNull URL get(KubernetesClusterImpl cluster) throws KubernetesManagerException {
        try {
            String url = getStringNulled(KubernetesPropertiesSingleton.cps(), "cluster." + cluster.getId(), "url");
            if (url == null) {
                throw new KubernetesManagerException("Missing the API URL for Kubernetes Cluster " + cluster.getId() + ", set property kubernetes.cluster." + cluster.getId() + ".url");
            }
            return new URL(url) ;
        } catch (ConfigurationPropertyStoreException | MalformedURLException e) {
            throw new KubernetesManagerException("Problem retrieving the url for the cluster " + cluster.getId(), e);
        }
    }
}