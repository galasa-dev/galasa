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
 * Selenium Driver Type CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.driver.type
 * 
 * @galasa.description Describes the selenium runtime that will be used.
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values A valid String representation of a type. Available choices: local, docker, kubernetes, grid
 * 
 * @galasa.examples 
 * <code>selenium.driver.type=docker</code>
 * 
 */
public class SeleniumWebDriverType extends CpsProperties {

    public static String get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringWithDefault(SeleniumPropertiesSingleton.cps(), "local", "driver", "type");
    }

}