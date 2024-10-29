/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * zOSMF Server Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.server.SERVERID.image
 * 
 * @galasa.description The z/OS image ID this zOS/MF server lives on 
 * 
 * @galasa.required No
 * 
 * @galasa.default The SERVERID value is used as the z/OS image ID
 * 
 * @galasa.valid_values  z/OS image IDs
 * 
 * @galasa.examples 
 * <code>zosmf.server.MFSYSA.image=SYSA</code><br>
 *
 */
public class ServerImage extends CpsProperties {

    public static @NotNull String get(@NotNull String serverId) throws ZosmfManagerException {
        return getStringWithDefault(ZosmfPropertiesSingleton.cps(), serverId, "server." + serverId, "image");
    }

}
