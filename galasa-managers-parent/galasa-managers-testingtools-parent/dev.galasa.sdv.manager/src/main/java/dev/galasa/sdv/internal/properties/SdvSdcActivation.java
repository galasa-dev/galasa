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
 * Should the SDV manager configure and make SDC active on the CICS region?.
 *
 * @galasa.cps.property
 *
 * @galasa.name sdv.cicsTag.[cicsTag].SdcActivation
 *
 * @galasa.description Should the SDV manager configure all pre-reqs and make SDC active on the CICS
 *                     region?
 *
 *                     Typically should be enabled for CICS regions that will be created as part of
 *                     test, and then discarded.
 *
 * @galasa.required No
 *
 * @galasa.default false
 *
 * @galasa.valid_values true, false
 *
 * @galasa.examples <code>sdv.cicsTag.A.SdcActivation=true</code><br>
 *
 */
public class SdvSdcActivation extends CpsProperties {

    /**
     * Returns a boolean indicating if the manager should activate SDC as part of its set up.
     */
    public static boolean get(@NotNull String cicsTag) throws SdvManagerException {
        try {
            String sdvSdcActivation = getStringNulled(SdvPropertiesSingleton.cps(), "cicsTag",
                    "SdcActivation", cicsTag);

            if (sdvSdcActivation == null) {
                return false;
            }
            return Boolean.parseBoolean(sdvSdcActivation);
        } catch (ConfigurationPropertyStoreException e) {
            throw new SdvManagerException(
                    "Problem asking CPS for SDC activation config for CICS tag: " + cicsTag, e);
        }
    }
}
