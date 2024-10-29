/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.windows.WindowsImage;
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
 * openstack.windows.image.[imagename].capabilities=java,kubectl,git<br>
 * Where imagename is that provided in {@link WindowsImages}<br>
 * In the above example, it is indicating the image has java, kubectl and git
 * installed ready for the test. The test can request these capabilities via
 * {@link WindowsImage}
 * </p>
 * <p>
 * The default is no capabilities
 * </p>
 *
 */
public class WindowsImageCapabilities extends CpsProperties {

    public static @NotNull List<String> get(@NotNull String image)
            throws ConfigurationPropertyStoreException, OpenstackManagerException {

        return getStringList(OpenstackPropertiesSingleton.cps(), "windows.image." + image, "capabilities");

    }

}
