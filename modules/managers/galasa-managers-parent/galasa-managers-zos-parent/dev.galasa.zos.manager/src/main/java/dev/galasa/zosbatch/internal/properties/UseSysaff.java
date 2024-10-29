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
 * zOS Batch job use SYSAFF
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.batchjob.[imageid].use.sysaff
 * 
 * @galasa.description Use the run the zOS Batch job on the specified image by specifying {@code /*JOBPARM SYSAFF=[imageid]}
 * 
 * @galasa.required No
 * 
 * @galasa.default true
 * 
 * @galasa.valid_values true or false
 * 
 * @galasa.examples 
 * <code>zosbatch.batchjobe.MVSA.use.sysaff=true</code><br>
 * <code>zosbatch.batchjob.default.use.sysaff=false</code>
 *
 */
public class UseSysaff extends CpsProperties {

    public static boolean get(String imageId) throws ZosBatchManagerException {
        try {
            String sysaffString = getStringNulled(ZosBatchPropertiesSingleton.cps(), "batchjob", "use.sysaff", imageId);
            if (sysaffString == null || sysaffString.isEmpty()) {
                return true;
            }
            return Boolean.parseBoolean(sysaffString);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the batch job use SYSAFF property for zOS image "  + imageId, e);
        }
    }

}
