/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.sdv.SdvManagerException;
import javax.validation.constraints.NotNull;

/**
 * The port the SDC TCPIPSERVICE should serve from on a CICS region.
 *
 * @galasa.cps.property
 *
 * @galasa.name sdv.cicsTag.[cicsTag].port
 *
 * @galasa.description The port the SDC TCPIPSERVICE should serve from on a CICS region
 *
 * @galasa.required Yes
 *
 * @galasa.default None
 *
 * @galasa.valid_values String
 *
 * @galasa.examples <code>sdv.cicsTag.A.port=32000</code><br>
 *
 */
public class SdvPort extends CpsProperties {

    /**
     * Returns the port number for a given cicsTag a TCPIPSERVICE should serve from.
     */
    public static String get(@NotNull String cicsTag) throws SdvManagerException {
        try {
            return getStringNulled(SdvPropertiesSingleton.cps(), "cicsTag", "port", cicsTag);
        } catch (ConfigurationPropertyStoreException e) {
            throw new SdvManagerException(
                    "Problem asking CPS for the SDV port for CICS tag: " + cicsTag, e);
        }
    }
}
