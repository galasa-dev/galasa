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
 * Selenium Grid Endpoint CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.grid.endpoint
 * 
 * @galasa.description States the grid endpoint
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values ip's and hostnames for a selenium grid
 * 
 * @galasa.examples 
 * <code>selenium.grid.endpoint=127.0.0.1:4444</code>
 * 
 */
public class SeleniumGridEndpoint extends CpsProperties {
    
    public static String get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        String drivers = getStringNulled(SeleniumPropertiesSingleton.cps(), "grid", "endpoint");
        if (drivers != null) {
            return drivers;
        }
        throw new SeleniumManagerException("No grid enpoint given, set 'selenium.grid.endpoint'.");
    }

}