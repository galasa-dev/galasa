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

public class DefaultVersion extends CpsProperties {

    private static final Log logger = LogFactory.getLog(DefaultVersion.class);

    public static ProductVersion get() {
        String version = "";
        try {
            version = getStringWithDefault(ImstmPropertiesSingleton.cps(), "15.2.0", "default", "version");
            return ProductVersion.parse(version);
        } catch (ImstmManagerException e) {
            logger.error("Problem accessing the CPS for the default IMS version, defaulting to 15.2.0");
            return ProductVersion.v(15).r(2).m(0);
        } catch (ManagerException e) {
            logger.error("Failed to parse default IMS version '" + version + "', defaulting to 15.2.0");
            return ProductVersion.v(15).r(2).m(0);
        }
    }
}
