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
 * The site and language specific custom zOS program compile SYSLIBs
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.[language].[imageid].compile.syslibs
 * 
 * @galasa.description The site specific and language specific (COBOL, C, PL1, ASSEMBLER) custom zOS data sets containing source
 * copybooks and macros etc used in the compile SYSLIB concatenation in the zOS program compile and link JCL
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values a comma separated list of one or more valid zOS data sets
 * 
 * @galasa.examples 
 * <code>zosprogram.cobol.MVSA.compile.syslibs=TEAM.COPYBOOK</code><br>
 * <code>zosprogram.cobol.compile.syslibs=COMPANY.COPYBOOK,TEAM.COPYBOOK</code>
 *
 */
public class ProgramLanguageCompileSyslibs extends CpsProperties {

    public static List<String> get(String imageId, Language language) throws ZosProgramManagerException {
        try {
            return getStringList(ZosProgramPropertiesSingleton.cps(), language.toString().toLowerCase(), "compile.syslibs", imageId);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program " + language + " custom compile syslibs for zOS image "  + imageId, e);
        }
    }

}
