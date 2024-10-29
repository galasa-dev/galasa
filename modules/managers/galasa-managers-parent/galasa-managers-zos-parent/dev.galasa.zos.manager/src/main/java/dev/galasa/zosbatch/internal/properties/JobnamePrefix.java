/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * zOS Batch jobname prefix
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.jobname.[imageid].prefix
 * 
 * @galasa.description The zOS Batch jobname prefix when submitted via zOSMF
 * 
 * @galasa.required No
 * 
 * @galasa.default GAL
 * 
 * @galasa.valid_values 1-7 characters
 * 
 * @galasa.examples 
 * <code>zosbatch.jobname.MVSA.prefix=JOB</code><br>
 * <code>zosbatch.jobname.default.prefix=XXX</code>
 *
 */
public class JobnamePrefix extends CpsProperties {
    
    private static final Log logger = LogFactory.getLog(JobnamePrefix.class);

    private static final String DEFAULT_JOBNAME_PREFIX = "GAL";

    public static String get(String imageId) throws ZosBatchManagerException {
        try {
            String jobNamePrefixValue = getStringNulled(ZosBatchPropertiesSingleton.cps(), "jobname", "prefix", imageId);

            if (jobNamePrefixValue == null) {
                return DEFAULT_JOBNAME_PREFIX;
            } else {
                String jobNamePrefix = jobNamePrefixValue.toUpperCase();
                if (jobNamePrefix.length() > 7 || !jobNamePrefix.matches("^[A-Z$#@][A-Z0-9$#@]*$")) {
                    logger.warn("Invalid Batch Job prefix \"" + jobNamePrefixValue + "\". Using default value of \"" + DEFAULT_JOBNAME_PREFIX + "\"");
                    return DEFAULT_JOBNAME_PREFIX;
                }
                return jobNamePrefix;
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the zOSMF jobname prefix for zOS image "  + imageId, e);
        }
    }

}
