/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.selenium.SeleniumManagerException;

/**
 * Selenium Kubernetes Namespace
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.kubernetes.namespace
 * 
 * @galasa.description Provides the name of the namespace for the nodes to be provisioned on
 * 
 * @galasa.required Yes
 * 
 * @galasa.valid_values A valid String representation an available namespace on your k8's cluster
 * 
 * @galasa.examples 
 * <code>selenium.kubernetes.namespace=galasa</code>
 * 
 */
public class SeleniumKubernetesNamespace extends CpsProperties {

    public static String get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        String namespace = getStringNulled(SeleniumPropertiesSingleton.cps(),"kubernetes","namespace");
        if (namespace == null) {
            throw new SeleniumManagerException("No kubernetes namespace provided");
        }
        return namespace;
    }

}