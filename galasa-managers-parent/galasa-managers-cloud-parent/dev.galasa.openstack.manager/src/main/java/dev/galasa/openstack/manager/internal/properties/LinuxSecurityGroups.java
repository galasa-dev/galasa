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

import java.util.List;
import java.util.Arrays;

/**
 * OpenStack Linux Security Groups
 * <p>
 * Provide the names of the Security Groups that OpenStack will apply to the image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.linux.image.[imagename].securitygroups=default,galasa<br>
 * openstack.linux.image.securitygroups=default<br>
 * Where securitygroup is that provided in {@link LinuxImages}<br>
 * </p>
 * <p>
 * There is no default
 * </p>
 *
 */
public class LinuxSecurityGroups extends CpsProperties {

    public static @NotNull List<String> get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {
         
        return getStringList(OpenstackPropertiesSingleton.cps(), "linux.image", "securitygroups", image);
    }

}
