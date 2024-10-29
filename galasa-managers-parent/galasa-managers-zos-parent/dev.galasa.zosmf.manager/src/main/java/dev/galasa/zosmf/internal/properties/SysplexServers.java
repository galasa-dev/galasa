/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * zOSMF Sysplex Servers 
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.sysplex.[SYSPLEXID].default.servers
 * 
 * @galasa.description The zOSMF servers active on the supplied sysplex 
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values Comma separated zOS/MF server IDs
 * 
 * @galasa.examples 
 * <code>zosmf.sysplex.default.servers=MFSYSA,MFSYSB</code><br>
 * <code>zosmf.sysplex.PLEXA.default.servers=MFSYSA,MFSYSB</code>
 *
 */
public class SysplexServers extends CpsProperties {

    public static @NotNull List<String> get(@NotNull IZosImage zosImage) throws ZosmfManagerException {
        try {
            List<String> serverImages = getStringList(ZosmfPropertiesSingleton.cps(), "sysplex", "default.servers", zosImage.getSysplexID());

            return serverImages;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosmfManagerException("Problem asking the CPS for the zOSMF servers property for zOS sysplex "  + zosImage.getSysplexID(), e);
        }
    }

}
