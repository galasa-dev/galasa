/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Linux Key Pair
 * <p>
 * Provide the registered Key Pair that OpenStack will use when deploying the image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.linux.[imagename].keypair=galasa<br>
 * openstack.linux.default.keypair=galasa<br>
 * Where imagename is that provided in {@link LinuxImages}<br>
 * </p>
 * <p>
 * There is no default
 * </p>
 *
 */
public class LinuxKeyPair extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringNulled(OpenstackPropertiesSingleton.cps(), "linux", "keypair", image);

    }

}
