/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.java.JavaManagerException;

/**
 * Use Jacoco code coverage
 * 
 * @galasa.cps.property
 * 
 * @galasa.name java.jacoco.code.coverage
 * 
 * @galasa.description Add the code coverage parameters to the java command
 * 
 * @galasa.required No
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true or false
 * 
 * @galasa.examples 
 * <code>java.jacoco.code.coverage=false</code>
 * 
 */
public class UseCodeCoverage extends CpsProperties {

    @NotNull
    public static boolean get() throws JavaManagerException {
        return Boolean.parseBoolean(getStringWithDefault(JavaPropertiesSingleton.cps(), "false", "jacoco", "code.coverage"));
    }
}