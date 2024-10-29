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
 * Selenium Gecko Preferences CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.local.gecko.preferences
 * 
 * @galasa.description Provides extra preferences to use when using the gecko driver for extensions
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values A comma seperated list of key value pairs for the preferences
 * 
 * @galasa.examples 
 * <code>selenium.local.gecko.preferences=app.update.silent=false,dom.popup_maximum=0</code>
 * 
 */
public class SeleniumGeckoPreferences extends CpsProperties {

    public static String get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getStringNulled(SeleniumPropertiesSingleton.cps(), "local", "gecko.preferences");
    }

}