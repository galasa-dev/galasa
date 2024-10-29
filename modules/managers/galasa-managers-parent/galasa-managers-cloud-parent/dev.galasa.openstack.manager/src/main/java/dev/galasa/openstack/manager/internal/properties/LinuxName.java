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
 * OpenStack Linux Name
 * <p>
 * Provide the image name to use when building the instance
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.linux.image.[imagename].name=aname<br>
 * openstack.linux.image.name=bname<br>
 * Where imagename is that provided in {@link LinuxImages}<br>
 * </p>
 * <p>
 * The default is the same as the imagename
 * </p>
 *
 */
public class LinuxName extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), image, "linux.image", "name", image);

    }

}
