/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import java.util.List;

import org.apache.commons.logging.LogConfigurationException;

import dev.galasa.Constants;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.openstack.manager.OpenstackManagerException;

/**
 * OpenStack Compute Server Name Pool
 * <p>
 * This property indicates what names will be given to the compute servers when
 * they are created.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.server.name.pool=GALASA{0-9}{0-9}
 * </p>
 * <p>
 * Can be a comma separated list of static or generated names, eg BOB1,BOB9,BOB5
 * </p>
 * <p>
 * default value is GALASA{0-9}{0-9}
 * </p>
 *
 */
public class NamePool extends CpsProperties {

    public static List<String> get() throws OpenstackManagerException, LogConfigurationException {
        return getStringListWithDefault(OpenstackPropertiesSingleton.cps(), Constants.LITERAL_NAME + "{0-9}{0-9}",
                "server", "name.pool");
    }

}
