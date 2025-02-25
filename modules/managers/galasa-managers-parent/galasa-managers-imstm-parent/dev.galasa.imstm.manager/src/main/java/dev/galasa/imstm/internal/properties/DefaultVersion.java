/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * IMS TM Systems - Default Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name imstm.default.version
 * 
 * @galasa.description Provides the default version of IMS TM systems to a custom provisioner.  
 * 
 * @galasa.required Not required for DSE, otherwise requirement is defined by custom provisioner
 * 
 * @galasa.default 15.5.0
 * 
 * @galasa.valid_values A valid V.R.M version format, eg 15.5.0
 * 
 * @galasa.examples 
 * <code>imstm.default.version=15.5.0</code><br>
 *
 */
public class DefaultVersion extends CpsProperties {

    private static final Log logger = LogFactory.getLog(DefaultVersion.class);
    private static final ProductVersion DEFAULT_VERSION = ProductVersion.v(15).r(5).m(0);

    public static ProductVersion get() {
        String version = "";
        try {
            version = getStringWithDefault(ImstmPropertiesSingleton.getCps(), DEFAULT_VERSION.toString(), "default", "version");
            return ProductVersion.parse(version);
        } catch (ImstmManagerException e) {
            logger.error("Problem accessing the CPS for the default IMS version, defaulting to " + DEFAULT_VERSION.toString());
            return DEFAULT_VERSION;
        } catch (ManagerException e) {
            logger.error("Failed to parse default IMS version '" + version + "', defaulting to " + DEFAULT_VERSION.toString());
            return DEFAULT_VERSION;
        }
    }
}
