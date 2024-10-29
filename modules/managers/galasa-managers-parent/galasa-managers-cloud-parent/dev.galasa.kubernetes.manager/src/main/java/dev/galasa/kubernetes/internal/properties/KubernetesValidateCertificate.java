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
 * Kubernetes Validate Cluster Certificate CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name kubernetes.cluster.[XXXX.]validate.certificate
 * 
 * @galasa.description Validates the Kubernetes Cluster API Certificate 
 * 
 * @galasa.required No
 * 
 * @galasa.default true
 * 
 * @galasa.valid_values true or false
 * 
 * @galasa.examples 
 * <code>kubernetes.cluster.K8S.validate.certificate=false<br>
 * kubernetes.cluster.validate.certificate=true</code>
 * 
 */
public class KubernetesValidateCertificate extends CpsProperties {

    public static boolean get(KubernetesClusterImpl cluster) throws KubernetesManagerException {
       return Boolean.parseBoolean(getStringWithDefault(KubernetesPropertiesSingleton.cps(), "true", "cluster", "validate.certificate", cluster.getId()));
    }
}