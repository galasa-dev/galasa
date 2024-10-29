/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.sdv.SdvManagerException;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * The list of credentials that belong to a given role on a zOS image.
 *
 * @galasa.cps.property
 *
 * @galasa.name sdv.zosImage.[imageID].role.[Role].credTags
 *
 * @galasa.description The list of credentials that belong to a given role on a zOS image
 *
 * @galasa.required Yes
 *
 * @galasa.default []
 *
 * @galasa.valid_values String[]
 *
 * @galasa.examples <code>sdv.zosImage.ABC.role.TELLER.credTags=USER1,USER2</code><br>
 *
 */
public class SdvPoolUsers extends CpsProperties {

    /**
     * Returns a List of users from the pool that belong to the provided role.
     */
    public static @NotNull @NotNull List<String> get(String image, String role)
            throws SdvManagerException {
        try {
            return getStringList(SdvPropertiesSingleton.cps(), "zosImage", "credTags", image,
                    "role", role);
        } catch (ConfigurationPropertyStoreException e) {
            throw new SdvManagerException("Problem asking CPS for the User pool for zOS image: "
                    + image + ", and role: " + role, e);
        }
    }
}
