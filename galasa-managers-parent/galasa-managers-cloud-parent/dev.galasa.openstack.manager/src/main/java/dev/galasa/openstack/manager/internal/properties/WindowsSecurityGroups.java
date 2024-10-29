/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack windows Security Groups
 * <p>
 * Provide the names of the Security Groups that OpenStack will apply to the image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.windows.image.[imagename].securitygroups=default,galasa<br>
 * openstack.windows.image.securitygroups=galasa<br>
 * Where securitygroup is that provided in {@link WindowsImages}<br>
 * </p>
 * <p>
 * There is no default
 * </p>
 *
 */
public class WindowsSecurityGroups extends CpsProperties {

    public static @NotNull List<String> get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringList(OpenstackPropertiesSingleton.cps(), "windows.image", "securitygroups", image);
    }

}
