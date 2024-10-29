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
 * zOS Batch job execution wait timeout
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.batchjob.[imageid].timeout
 * 
 * @galasa.description The value in seconds to wait for the zOS Batch job execution to complete when submitted via zOSMF
 * 
 * @galasa.required No
 * 
 * @galasa.default 350
 * 
 * @galasa.valid_values 0 to {@link Integer#MAX_VALUE}
 * 
 * @galasa.examples 
 * <code>zosbatch.batchjob.MVSA.timeout=350</code><br>
 * <code>zosbatch.batchjob.default.timeout=60</code>
 *
 */
public class JobWaitTimeout extends CpsProperties {

    private static final int DEFAULT_JOB_WAIT_TIMEOUT = 300;

    public static int get(String imageId) throws ZosBatchManagerException {
        try {
            String timeoutString = getStringNulled(ZosBatchPropertiesSingleton.cps(), "batchjob", "timeout", imageId);

            if (timeoutString == null) {
                return DEFAULT_JOB_WAIT_TIMEOUT;
            } else {
                int timeout = Integer.parseInt(timeoutString);
                if (timeout < 0) {
                    throw new ZosBatchManagerException("Batch job wait timeout property must be a positive integer");
                }
                return timeout;
            }
        } catch (ConfigurationPropertyStoreException | NumberFormatException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the batch job wait timeout property for zOS image "  + imageId, e);
        }
    }

}
