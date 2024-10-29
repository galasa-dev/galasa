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
 * Extra bundle required to implement the zOS UNIX Command Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.bundle.extra.unixcomand.manager
 * 
 * @galasa.description The name of the Bundle that implements the zOS UNIX Command Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.zosunixcommand.ssh.manager
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.bundle.extra.unix.manager=dev.galasa.zosunixcommand.ssh.manager</code>
 *
 */
public class UNIXCommandExtraBundle extends CpsProperties {
    
    private static final String DEFAULT_EXTRA_BUNDLE_UNIX_COMMAND_MANAGER = "dev.galasa.zosunixcommand.ssh.manager";
    
    public static String get() throws ZosManagerException {
        try {
            String fileBundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "unixcommand.manager");
            if (fileBundleName == null)  {
                return DEFAULT_EXTRA_BUNDLE_UNIX_COMMAND_MANAGER;
            }
            return fileBundleName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking CPS for the zOS UNIX Command Manager extra bundle name", e); 
        }
    }
}
