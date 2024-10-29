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
* Selenium Screenshot Failure CPS Property
* 
* @galasa.cps.property
* 
* @galasa.name selenium.screenshot.failure
* 
* @galasa.description Takes a screenshot on a test method failing
* 
* @galasa.required No
* 
* @galasa.valid_values true or false
* 
* @galasa.examples 
* <code>selenium.screenshot.failure=true</code>
* 
*/
public class SeleniumScreenshotFailure extends CpsProperties {

    public static Boolean get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return Boolean.valueOf(getStringWithDefault(SeleniumPropertiesSingleton.cps(), "false", "screenshot", "failure"));
    }

}