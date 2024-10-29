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
 * Java jacoco code coverage save locations
 * 
 * @galasa.cps.property
 * 
 * @galasa.name java.jacoco.save.location
 * 
 * @galasa.description Indicate where to save the exec files that were generated in testing
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values a valid URL
 * 
 * @galasa.examples 
 * <code>java.jacoco.agent.location=http://upload/codecoverage</code>
 * 
 */
public class CodeCoverageSaveLocation extends CpsProperties {

    public static String get() throws JavaManagerException {
        
        try {
            return getStringNulled(JavaPropertiesSingleton.cps(), "jacoco.save", "location") ;
        } catch (ConfigurationPropertyStoreException e) {
            throw new JavaManagerException("Problem retrieving the jacoco save location", e);
        }
    }
}