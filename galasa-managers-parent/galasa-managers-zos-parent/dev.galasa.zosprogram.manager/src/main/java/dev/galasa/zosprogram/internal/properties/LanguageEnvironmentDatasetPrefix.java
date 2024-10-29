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

/**
 * zOS Language Environment data set prefix
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.le.[imageid].dataset.prefix
 * 
 * @galasa.description The prefix of the Language Environment zOS data sets containing load modules (SCEERUN, SCEERUN2) and source
 * copybooks, macros, link SYSIN etc (SCEESAMP) to be used in program compile and link JCL
 * 
 * @galasa.required Yes
 * 
 * @galasa.default Defaults to 'CEE'
 * 
 * @galasa.valid_values a comma separated list of one or more valid zOS data set prefixes
 * 
 * @galasa.examples 
 * <code>zosprogram.le.MVSA.dataset.prefix=CEE</code><br>
 * <code>zosprogram.le.dataset.prefix=SYS1.LE,CEE</code>
 *
 */
public class LanguageEnvironmentDatasetPrefix extends CpsProperties {

    public static List<String> get(String imageId) throws ZosProgramManagerException {
        try {
            List<String> datasetPrefixValue = getStringList(ZosProgramPropertiesSingleton.cps(), "le", "dataset.prefix", imageId);
            if (datasetPrefixValue.isEmpty()) {
                throw new ZosProgramManagerException("Required property zosprogram.le.[imageid].dataset.prefix not supplied");
            }
            return datasetPrefixValue;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS Language Environment dataset prefix for zOS image "  + imageId, e);
        }
    }

}
