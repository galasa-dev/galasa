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
 * Extra bundle required to implement the zOS Batch Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.bundle.extra.batch.manager
 * 
 * @galasa.description The name of the Bundle that implements the zOS Batch Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.common.zosbatch.zosmf.manager
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.bundle.extra.batch.manager=dev.galasa.common.zosbatch.zosmf.manager</code><br>
 *
 */
public class BatchExtraBundle extends CpsProperties {
    
    private static final String DEFAULT_EXTRA_BUNDLE_BATCH_MANAGER = "dev.galasa.zosbatch.zosmf.manager";
    
    public static String get() throws ZosManagerException {
        try {
            String batchBundleName = getStringNulled(ZosPropertiesSingleton.cps(), "bundle.extra", "batch.manager");
            if (batchBundleName == null)  {
                return DEFAULT_EXTRA_BUNDLE_BATCH_MANAGER;
            }
            return batchBundleName;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking CPS for the zOS Batch Manager extra bundle name", e); 
        }
    }
}
