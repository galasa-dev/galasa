/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * RSE API Sysplex Servers 
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.sysplex.[SYSPLEXID].default.servers
 * 
 * @galasa.description The RSE API servers active on the supplied sysplex 
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values Comma separated RSE API server IDs
 * 
 * @galasa.examples 
 * <code>rseapi.sysplex.default.servers=RSASYSA,RSASYSB</code><br>
 * <code>rseapi.sysplex.PLEXA.default.servers=RSASYSA,RSASYSB</code>
 *
 */
public class SysplexServers extends CpsProperties {

    public static @NotNull List<String> get(@NotNull IZosImage zosImage) throws RseapiManagerException {
        try {
            return getStringList(RseapiPropertiesSingleton.cps(), "sysplex", "default.servers", zosImage.getSysplexID());
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Problem asking the CPS for the RSE API servers property for zOS sysplex "  + zosImage.getSysplexID(), e);
        }
    }

}
