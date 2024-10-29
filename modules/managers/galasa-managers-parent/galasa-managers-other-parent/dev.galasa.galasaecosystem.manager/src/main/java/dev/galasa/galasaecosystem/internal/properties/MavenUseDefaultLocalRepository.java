/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;


/**
 * Maven use default local repository
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.maven.use.default.local.repository
 * 
 * @galasa.description The Local ecosystems will use a dedicated local repository, however, this
 * slows the installation, so setting this property to true will use the normal ~/.m2/repository
 * so downloads happen only once per day, useful for rapid development and testings
 * 
 * @galasa.required No
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true or false
 * 
 * @galasa.examples 
 * <code>galasaecosystem.maven.use.default.local.repository=true</code>
 * 
 */
public class MavenUseDefaultLocalRepository extends CpsProperties {

    public static boolean get() throws GalasaEcosystemManagerException {
            return Boolean.parseBoolean(getStringWithDefault(GalasaEcosystemPropertiesSingleton.cps(), "false", "maven", "use.default.local.repository")) ;
    }
}