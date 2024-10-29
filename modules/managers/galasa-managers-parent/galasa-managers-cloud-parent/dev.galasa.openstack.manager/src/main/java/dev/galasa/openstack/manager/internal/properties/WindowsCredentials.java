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
 * OpenStack Windows Credentials
 * <p>
 * Provide the credentials necessary to SSH to the openstack image.  This is normally related 
 * to the openstack.windows.keypair property  
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.windows.[imagename].credentials.id=OPENSTACKSSH<br>
 * openstack.windows.credentials.id=OPENSTACKSSH<br>
 * Where imagename is that provided in {@link WindowsImages}<br>
 * </p>
 * <p>
 * The default is OPENSTACKSSH
 * </p>
 *
 */
public class WindowsCredentials extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), "OPENSTACKSSH", "windows", "credentials.id", image);

    }

}
