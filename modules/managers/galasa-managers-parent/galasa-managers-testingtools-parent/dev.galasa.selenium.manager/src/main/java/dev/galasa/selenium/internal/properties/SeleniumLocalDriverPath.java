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
 * Selenium Driver Path CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.local.driver.BROWSER.path
 * 
 * @galasa.description Provides a path to a local webdriver on the system being tested
 * 
 * @galasa.required Yes
 * 
 * @galasa.valid_values A valid String representation of a path
 * 
 * @galasa.examples 
 * <code>selenium.local.driver.CHROME.path=/usr/bin/chromedriver</code>
 * 
 */
public class SeleniumLocalDriverPath extends CpsProperties {

    public static String get(String browser) throws ConfigurationPropertyStoreException, SeleniumManagerException {
        String path = getStringNulled(SeleniumPropertiesSingleton.cps(), "local",  "path", "driver", browser);
        if (path == null) {
            throw new SeleniumManagerException("No path provided for driver selected. Please set selenium.local.driver."+browser+".path");
        }
        return path;
    }

}