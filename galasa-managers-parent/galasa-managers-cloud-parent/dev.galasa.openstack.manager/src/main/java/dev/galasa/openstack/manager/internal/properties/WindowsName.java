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
 * OpenStack Windows image Name
 * <p>
 * Provide the image name to use when building the instance
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.windows.image.[imagename].name=aname<br>
 * openstack.windows.image.name=bname<br>
 * Where imagename is that provided in {@link WindowsImages}<br>
 * </p>
 * <p>
 * The default is the same as the imagename
 * </p>
 *
 */
public class WindowsName extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), image, "windows.image", "name", image);

    }

}
