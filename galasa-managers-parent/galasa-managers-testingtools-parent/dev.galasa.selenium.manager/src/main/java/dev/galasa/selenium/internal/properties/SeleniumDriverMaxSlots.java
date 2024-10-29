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
 * Selenium Driver Max Slots CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.driver.max.slots
 * 
 * @galasa.description Allows number of concurrent drivers to be limited. If docker selected, the docker slot limit will also be enforced
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values Int value for number of congruent drivers
 * 
 * @galasa.examples 
 * <code>selenium.driver.max.slots=3</code>
 * 
 */
public class SeleniumDriverMaxSlots extends CpsProperties {
    
    public static int get() throws ConfigurationPropertyStoreException, SeleniumManagerException {
        return getIntWithDefault(SeleniumPropertiesSingleton.cps(), 3, "driver", "max.slots");
    }

}