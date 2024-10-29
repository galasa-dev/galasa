/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosprogram.ZosProgramManagerException;
//TODO: Should we get this from the CICS Manager?
/**
 * zOS CICS data set prefix
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.cics.[imageid].dataset.prefix
 * 
 * @galasa.description  The prefix of the CICS zOS data sets containing load modules (SDFHLOAD) and source 
 * copybooks, macros, link SYSIN etc (SDFHC370, SDFHCOB, SDFHPL1, SDFHMAC, SDFHSAMP) to be used in program compile and link JCL
 *  
 * @galasa.required Yes, for CICS programs only
 * 
 * @galasa.default For CICS programs, defaults to 'CICS'. Not used in non CICS programs
 * 
 * @galasa.valid_values a comma separated list of one or more valid zOS data set prefixes
 * 
 * @galasa.examples 
 * <code>zosprogram.cics.MVSA.dataset.prefix=CICS</code><br>
 * <code>zosprogram.cics.default.dataset.prefix=SYS1,CICS</code>
 *
 */
public class CICSDatasetPrefix extends CpsProperties {

    public static List<String> get(String imageId) throws ZosProgramManagerException {
        try {
            List<String> datasetPrefixValue = getStringList(ZosProgramPropertiesSingleton.cps(), "cics", "dataset.prefix", imageId);

            if (datasetPrefixValue.isEmpty()) {
                throw new ZosProgramManagerException("Required property zosprogram.cics.[imageid].dataset.prefix not supplied");
            }
            return datasetPrefixValue;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program CICS dataset prefix for zOS image "  + imageId, e);
        }
    }

}
