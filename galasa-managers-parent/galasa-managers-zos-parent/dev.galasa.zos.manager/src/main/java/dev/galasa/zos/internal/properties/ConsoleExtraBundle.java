/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * Extra bundle required to implement the zOS Console Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.bundle.extra.console.manager
 * 
 * @galasa.description The name of the Bundle that implements the zOS Console Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.common.zosconsole.zosmf.manager
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.bundle.extra.console.manager=dev.galasa.common.zosconsole.zosmf.manager</code><br>
 *
 */
public class ConsoleExtraBundle extends CpsProperties {
    
    private static final String DEFAULT_EXTRA_BUNDLE_CONSOLE_MANAGER = "dev.galasa.zosconsole.zosmf.manager";
    
    public static String get() throws ZosManagerException {
        try {
            String bundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "console.manager");
            if (bundleName == null)  {
                return DEFAULT_EXTRA_BUNDLE_CONSOLE_MANAGER;
            }
            return bundleName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking CPS for the zOS Console Manager extra bundle name", e); 
        }
    }
}
