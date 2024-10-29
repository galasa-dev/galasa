/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Build Timeout value
 * <p>
 * In minutes, how long the OpenStack Manager should wait for Compute to build
 * and start the server.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.build.timeout=9
 * </p>
 * <p>
 * default value is 10 minutes
 * </p>
 *
 */
public class BuildTimeout extends CpsProperties {

    public static int get() throws OpenstackManagerException {
        return getIntWithDefault(OpenstackPropertiesSingleton.cps(), 10, "build", "timeout");
    }

}
