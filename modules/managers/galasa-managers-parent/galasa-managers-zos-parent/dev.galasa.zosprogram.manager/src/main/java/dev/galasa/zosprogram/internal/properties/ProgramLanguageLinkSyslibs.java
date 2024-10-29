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
 * The site and language specific custom zOS program link SYSLIBs
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.[language].[imageid].link.syslibs
 * 
 * @galasa.description The site specific and language specific (COBOL, C, PL1, ASSEMBLER) custom zOS data sets containing load
 * modules used in the link SYSLIB concatenation in the zOS program compile and link JCL
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values a comma separated list of zOS data sets
 * 
 * @galasa.examples 
 * <code>zosprogram.cobol.MVSA.link.syslibs=TEAM.LOADLIB</code><br>
 * <code>zosprogram.cobol.link.syslibs=COMPANY.LOADLIB,TEAM.LOADLIB</code>
 *
 */
public class ProgramLanguageLinkSyslibs extends CpsProperties {

    public static List<String> get(String imageId, Language language) throws ZosProgramManagerException {
        try {
            return getStringList(ZosProgramPropertiesSingleton.cps(), language.toString().toLowerCase(), "link.syslibs", imageId);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program " + language + " custom link syslibs for zOS image "  + imageId, e);
        }
    }

}
