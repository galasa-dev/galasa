/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.linux.LinuxImage;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Image Capabilities
 * <p>
 * A comma separated list of what capabilities a image has. This is installation
 * specific and freeform.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.linux.image.[imagename].capabilities=java,kubectl,git<br>
 * Where imagename is that provided in {@link LinuxImages}<br>
 * In the above example, it is indicating the image has java, kubectl and git
 * installed ready for the test. The test can request these capabilities via
 * {@link LinuxImage}
 * </p>
 * <p>
 * The default is no capabilities
 * </p>
 *
 */
public class LinuxImageCapabilities extends CpsProperties {

    public static @NotNull List<String> get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringList(OpenstackPropertiesSingleton.cps(), "linux.image." + image, "capabilities");

    }

}
