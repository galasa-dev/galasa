/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal.properties;

import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * zOS Batch job truncate JCL
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.batchjob.[imageid].truncate.jcl.records
 * 
 * @galasa.description The z/OSMF submit job will fail if supplied with JCL records greater than 80 characters. Setting this property to true will truncate any records to 80
 * characters and issue a warning message.
 * 
 * @galasa.required No
 * 
 * @galasa.default true
 * 
 * @galasa.valid_values true or false
 * 
 * @galasa.examples 
 * <code>zosbatch.batchjobe.MVSA.truncate.jcl.records=true</code><br>
 * <code>zosbatch.batchjob.default.truncate.jcl.records=false</code>
 *
 */
public class TruncateJCLRecords extends CpsProperties {

    public static boolean get(String imageId) throws ZosBatchManagerException {
        try {
            String sysaffString = getStringNulled(ZosBatchPropertiesSingleton.cps(), "batchjob", "truncate.jcl.records", imageId);
            if (sysaffString == null || sysaffString.isEmpty()) {
                return true;
            }
            return Boolean.parseBoolean(sysaffString);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the truncate JCL records property for zOS image "  + imageId, e);
        }
    }

}
