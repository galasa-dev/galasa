/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.java.JavaManagerException;

/**
 * Java jacoco save location credentials
 * 
 * @galasa.cps.property
 * 
 * @galasa.name java.jacoco.save.credentials
 * 
 * @galasa.description Credentials to use when save jacoco exec files
 * 
 * @galasa.required No
 * 
 * @galasa.default none
 * 
 * @galasa.valid_values a valid credentials ID
 * 
 * @galasa.examples 
 * <code>java.jacoco.save.credentials=JACOCO</code>
 * 
 */
public class CodeCoverageSaveCredentials extends CpsProperties {
    
    public static String get() throws JavaManagerException {
        
        try {
            return getStringNulled(JavaPropertiesSingleton.cps(), "jacoco.save", "credentials") ;
        } catch (Exception e) {
            throw new JavaManagerException("Problem retrieving the java jacoco save credentials", e);
        }
    }
}