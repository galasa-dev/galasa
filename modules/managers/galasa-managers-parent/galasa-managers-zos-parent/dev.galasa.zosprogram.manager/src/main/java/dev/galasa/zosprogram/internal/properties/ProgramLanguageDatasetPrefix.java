/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramManagerException;

/**
 * zOS Program Language data set prefix
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.[language].[imageid].dataset.prefix
 * 
 * @galasa.description The prefix of the language specific zOS data sets containing STEPLIB load modules used in program compile
 * and link JCL, e.g.<br>
 * COBOL - SIGYCOMP<br>
 * C - SCCNCMP<br>
 * PL1 - SIBMZCMP<br>
 * 
 * @galasa.required An entry required for each language used e,g, COBOL, C, PL1, ASSEMBLER
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values a comma separated list of one or more valid zOS data set prefixes
 * 
 * @galasa.examples 
 * <code>zosprogram.cobol.MVSA.dataset.prefix=IGY.V6R3M0</code><br>
 * <code>zosprogram.cobol.dataset.prefix=SYS1.COBIL,IGY.V6R3M0</code>
 *
 */
public class ProgramLanguageDatasetPrefix extends CpsProperties {

    public static List<String> get(String imageId, Language language) throws ZosProgramManagerException {
        try {
            List<String> datasetPrefixValue = getStringList(ZosProgramPropertiesSingleton.cps(), language.toString().toLowerCase(), "dataset.prefix", imageId);

            if (datasetPrefixValue.isEmpty()) {
                throw new ZosProgramManagerException("Required property zosprogram." + language.toString().toLowerCase() + ".[imageid].dataset.prefix not supplied");
            }
            return datasetPrefixValue;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program " + language + " dataset prefix for zOS image "  + imageId, e);
        }
    }

}
