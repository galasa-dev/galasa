/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Network name
 * 
 * The Openstack Network name that the OpenStack Manager will use
 * to create a Floating IP address within. The Network name is used
 * to get the Network ID.
 * 
 * The property is:
 * <code>openstack.network.name=my_network_name</code>
 * 
 * There is no default
 *
 */
public class OpenStackNetworkName extends CpsProperties {

    public static String get()
            throws ConfigurationPropertyStoreException, OpenstackManagerException {
        return getStringNulled(OpenstackPropertiesSingleton.cps(), "network", "name");
    }

}
