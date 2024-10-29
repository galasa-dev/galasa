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
 * The role associated with a role tag.
 *
 * @galasa.cps.property
 *
 * @galasa.name sdv.roleTag.[roleTag].role
 *
 * @galasa.description The role associated with a role tag.
 *
 * @galasa.required Yes
 *
 * @galasa.default None
 *
 * @galasa.valid_values String
 *
 * @galasa.examples <code>sdv.roleTag.A.role=TELLER</code><br>
 *
 */
public class SdvRole extends CpsProperties {

    /**
     * Returns the Role name associated with a provided RoleTag.
     */
    public static String get(@NotNull String roleTag) throws SdvManagerException {
        try {
            return getStringNulled(SdvPropertiesSingleton.cps(), "roleTag", "role", roleTag);
        } catch (ConfigurationPropertyStoreException e) {
            throw new SdvManagerException(
                    "Problem asking CPS for the Role for Role tag: " + roleTag, e);
        }
    }
}
