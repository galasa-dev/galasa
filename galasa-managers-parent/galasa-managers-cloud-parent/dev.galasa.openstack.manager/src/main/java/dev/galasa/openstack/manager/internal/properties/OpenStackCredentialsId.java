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
 * OpenStack Credentials ID
 * <p>
 * The Credentials ID to be used to authenticate with the OpenStack Server.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.server.credentials.id=openstack
 * </p>
 * <p>
 * default value is openstack
 * </p>
 *
 */
public class OpenStackCredentialsId extends CpsProperties {

    public static String get() throws OpenstackManagerException, LogConfigurationException {
        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), "OPENSTACK", "server", "credentials.id");
    }

}
