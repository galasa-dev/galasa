/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.java.JavaManagerException;

/**
 * Java jacoco agent download location
 * 
 * @galasa.cps.property
 * 
 * @galasa.name java.jacoco.agent.location
 * 
 * @galasa.description Indicate where to download the Jacoco agent location
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values a valid URL
 * 
 * @galasa.examples 
 * <code>java.jacoco.agent.location=http://download/jacocoagent.jar</code>
 * 
 */
public class JacocoAgentLocation extends CpsProperties {

    public static String get() throws JavaManagerException {
        
        try {
            return getStringNulled(JavaPropertiesSingleton.cps(), "jacoco.agent", "location") ;
        } catch (ConfigurationPropertyStoreException e) {
            throw new JavaManagerException("Problem retrieving the jacoco agent location", e);
        }
    }
}