/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.JavaVersion;

/**
 * Java default version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name java.default.version
 * 
 * @galasa.description Indicate what the default Java version is
 * 
 * @galasa.required No
 * 
 * @galasa.default v11
 * 
 * @galasa.valid_values string value of the JavaVersion enum
 * 
 * @galasa.examples 
 * <code>java.default.version=v11</code>
 * 
 */
public class DefaultVersion extends CpsProperties {
    
    public final static JavaVersion DEFAULT_VERSION = JavaVersion.v11;

    @NotNull
    public static JavaVersion get() throws JavaManagerException {
        
        try {
            String version = getStringWithDefault(JavaPropertiesSingleton.cps(), DEFAULT_VERSION.toString(), "default", "version") ;
            
            return JavaVersion.valueOf(version);
        } catch (Exception e) {
            throw new JavaManagerException("Problem retrieving the java default version", e);
        }
    }
}