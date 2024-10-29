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
 * Selenium Available Drivers CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.driver.type
 * 
 * @galasa.description Describes the selenium driver types that can be selected.
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values A valid String the describes any of the supported drivers: FIREFOX,CHROME,OPERA,EDGE
 * 
 * @galasa.examples 
 * <code>selenium.available.drivers=CHROME,FIREFOX,OPERA,EDGE</code>
 * 
 */
public class SeleniumAvailableDrivers extends CpsProperties {
    
    public static String[] get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        String drivers = getStringNulled(SeleniumPropertiesSingleton.cps(), "available", "drivers");
        if (drivers != null) {
            return drivers.split(",");
        }
        throw new SeleniumManagerException("No avilable drivers, set selenium.available.drivers.");
    }

}