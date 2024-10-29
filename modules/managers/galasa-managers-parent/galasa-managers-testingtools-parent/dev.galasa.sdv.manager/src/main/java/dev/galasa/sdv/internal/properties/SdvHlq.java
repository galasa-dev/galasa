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
 * The HLQ of the CICS region install.
 *
 * @galasa.cps.property
 *
 * @galasa.name sdv.cicsTag.[cicsTag].hlq
 *
 * @galasa.description The HLQ of the CICS region install
 *
 * @galasa.required Yes
 *
 * @galasa.default None
 *
 * @galasa.valid_values String
 *
 * @galasa.examples <code>sdv.cicsTag.A.hlq=CICS.INSTALL</code><br>
 *
 */
public class SdvHlq extends CpsProperties {

    /**
     * Returns the HLQ for a given cicsTag.
     */
    public static String get(@NotNull String cicsTag) throws SdvManagerException {
        try {
            return getStringNulled(SdvPropertiesSingleton.cps(), "cicsTag", "hlq", cicsTag);
        } catch (ConfigurationPropertyStoreException e) {
            throw new SdvManagerException("Problem asking CPS for the HLQ for CICS tag: " + cicsTag,
                    e);
        }
    }
}
