/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import org.apache.commons.logging.LogConfigurationException;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack project name
 * <p>
 * The Openstack Project name that the manager will authenticate against and
 * create compute resources under. This property is required as no default is
 * available.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.server.project.name=xxxxx
 * </p>
 * <p>
 * There is no default
 * </p>
 *
 */
public class OpenStackProjectName extends CpsProperties {

    public static String get()
            throws ConfigurationPropertyStoreException, OpenstackManagerException, LogConfigurationException {
        return getStringNulled(OpenstackPropertiesSingleton.cps(), "server", "project.name");
    }

}
