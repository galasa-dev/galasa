/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import org.apache.commons.logging.LogConfigurationException;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Enabled
 * <p>
 * Enables this manager as a provisioner.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.server.enabled=true
 * </p>
 * <p>
 * default value is true
 * </p>
 *
 */
public class OpenStackEnabled extends CpsProperties {

    public static boolean get() throws OpenstackManagerException, LogConfigurationException {
        return Boolean.parseBoolean(getStringWithDefault(OpenstackPropertiesSingleton.cps(), "true", "server", "enabled"));
    }

}
