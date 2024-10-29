/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class DefaultVersion extends CpsProperties {

    private static final Log logger = LogFactory.getLog(DefaultVersion.class);

    public static ProductVersion get() {
        String version = "";
        try {
            version = getStringWithDefault(CicstsPropertiesSingleton.cps(), "5.6.0", "default", "version");
            return ProductVersion.parse(version);
        } catch (CicstsManagerException e) {
            logger.error("Problem accessing the CPS for the default CICS version, defaulting to 5.6.0");
            return ProductVersion.v(5).r(6).m(0);
        } catch (ManagerException e) {
            logger.error("Failed to parse default CICS version '" + version + "', defaulting to 5.6.0");
            return ProductVersion.v(5).r(6).m(0);
        }
    }
}
