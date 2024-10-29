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
 * Extra bundle required to implement the zOS TSO Command Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.bundle.extra.tsocommand.manager
 * 
 * @galasa.description The name of the Bundle that implements the zOS TSO Command Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.zostsocommand.ssh.manager
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.bundle.extra.tsocommand.manager=dev.galasa.zostsocommand.ssh.manager</code>
 *
 */
public class TSOCommandExtraBundle extends CpsProperties {
    
    private static final String DEFAULT_EXTRA_BUNDLE_TSO_COMMAND_MANAGER = "dev.galasa.zostsocommand.ssh.manager";
    
    public static String get() throws ZosManagerException {
        try {
            String fileBundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "tsocommand.manager");
            if (fileBundleName == null)  {
                return DEFAULT_EXTRA_BUNDLE_TSO_COMMAND_MANAGER;
            }
            return fileBundleName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking CPS for the zOS TSO Command Manager extra bundle name", e); 
        }
    }
}
