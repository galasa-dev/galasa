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
 * Extra bundle required to implement the zOS File Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.bundle.extra.file.manager
 * 
 * @galasa.description The name of the Bundle that implements the zOS File Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.common.zosfile.zosmf.manager
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.bundle.extra.file.manager=dev.galasa.common.zosfile.zosmf.manager</code><br>
 *
 */
public class FileExtraBundle extends CpsProperties {
    
    private static final String DEFAULT_EXTRA_BUNDLE_FILE_MANAGER = "dev.galasa.zosfile.zosmf.manager";
    
    public static String get() throws ZosManagerException {
        try {
            String fileBundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "file.manager");
            if (fileBundleName == null)  {
                return DEFAULT_EXTRA_BUNDLE_FILE_MANAGER;
            }
            return fileBundleName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking CPS for the zOS File Manager extra bundle name", e); 
        }
    }
}
