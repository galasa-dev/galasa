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
 * Selenium Node Selector CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.kubernetes.node.selector
 * 
 * @galasa.description Node Selector tags to be added to the pod yaml that runs the Selenium Grid inside a k8's cluster. Multiple selectors can be passed comma seperated
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values Comma seperated list of any node selectors: beta.kubernetes.io/arch: amd64, platform: myplatform
 * 
 * @galasa.examples 
 * <code>selenium.kubernetes.node.selector=beta.kubernetes.io/arch: amd64</code>
 * 
 */
public class SeleniumKubernetesNodeSelector extends CpsProperties {
    
    public static String[] get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringWithDefault(SeleniumPropertiesSingleton.cps(), "", "kubernetes", "node.selectors").split(",");
    }
}