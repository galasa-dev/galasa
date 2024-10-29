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
 * OpenStack Linux Credentials
 * <p>
 * Provide the credentials necessary to SSH to the openstack image.  This is normally related 
 * to the openstack.linux.keypair property  
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.linux.[imagename].credentials.id=OPENSTACKSSH<br>
 * openstack.linux.credentials.id=OPENSTACKSSH<br>
 * Where imagename is that provided in {@link LinuxImages}<br>
 * </p>
 * <p>
 * The default is OPENSTACKSSH
 * </p>
 *
 */
public class LinuxCredentials extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), "OPENSTACKSSH", "linux", "credentials.id", image);

    }

}
