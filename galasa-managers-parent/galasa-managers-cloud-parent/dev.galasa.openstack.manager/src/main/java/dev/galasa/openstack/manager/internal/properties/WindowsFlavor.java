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
 * OpenStack Windows Flavor
 * <p>
 * Provide the image flavor to use when building the instance
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.windows.[imagename].flavor=m1.medium<br>
 * openstack.windows.default.flavor=m1.medium<br>
 * Where imagename is that provided in {@link WindowsImages}<br>
 * </p>
 * <p>
 * The default is m1.medium
 * </p>
 *
 */
public class WindowsFlavor extends CpsProperties {

    public static @NotNull String get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), "m1.medium", "windows", "flavor", image);

    }

}
