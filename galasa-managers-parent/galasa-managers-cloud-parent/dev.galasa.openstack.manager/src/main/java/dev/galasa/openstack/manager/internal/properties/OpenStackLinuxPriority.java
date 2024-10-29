/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * OpenStack Linux Provisioning priority
 * <p>
 * How important this provisioner is against the other provisioners, the larger the number the more important.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.linux.priority=100
 * </p>
 * <p>
 * default value is 1
 * </p>
 *
 */
public class OpenStackLinuxPriority extends CpsProperties {
    
    private final static Log logger    = LogFactory.getLog(OpenStackLinuxPriority.class);

    public static int get() {
        try {
            return Integer.parseInt(getStringWithDefault(OpenstackPropertiesSingleton.cps(), "1", "linux", "priority"));
        } catch (Exception e) {
            logger.warn("Unable to obtain OpenStack Linux Priority", e);
            return 1;
        }
    }

}
