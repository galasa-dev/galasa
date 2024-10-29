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
 * Should the SDV manager remove the SRR Journal and logstream on the CICS region?.
 *
 * @galasa.cps.property
 *
 * @galasa.name sdv.cicsTag.[cicsTag].SrrLogstreamRemoval
 *
 * @galasa.description Should the SDV manager remove the SRR Journal and logstream on the CICS
 *                     region?
 *
 *                     Typically should be enabled for CICS regions that will be shutdown as part of
 *                     test, and then discarded.
 *
 * @galasa.required No
 *
 * @galasa.default false
 *
 * @galasa.valid_values true, false
 *
 * @galasa.examples <code>sdv.cicsTag.A.SrrLogstreamRemoval=true</code><br>
 *
 */
public class SdvSrrLogstreamRemoval extends CpsProperties {

    /**
     * Returns a boolean indicating if the manager should delete the SRR logstream as part
     * of its cleanup.
     */
    public static boolean get(@NotNull String cicsTag) throws SdvManagerException {
        try {
            String sdvSrrLogstreamRemoval = getStringNulled(SdvPropertiesSingleton.cps(), "cicsTag",
                    "SrrLogstreamRemoval", cicsTag);

            if (sdvSrrLogstreamRemoval == null) {
                return false;
            }
            return Boolean.parseBoolean(sdvSrrLogstreamRemoval);
        } catch (ConfigurationPropertyStoreException e) {
            throw new SdvManagerException(
                    "Problem asking CPS for SRR Logstream removal config for CICS tag: " + cicsTag,
                    e);
        }
    }
}
