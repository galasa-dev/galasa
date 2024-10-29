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
 * Selenium Gecko Profile CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.local.gecko.profile
 * 
 * @galasa.description Provides a profile to use when using the gecko driver for extensions
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values A valid String name of a profile
 * 
 * @galasa.examples 
 * <code>selenium.local.gecko.profile=default</code>
 * 
 */
public class SeleniumGeckoProfile extends CpsProperties {

    public static String get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringNulled(SeleniumPropertiesSingleton.cps(), "local", "gecko.profile");
    }

}