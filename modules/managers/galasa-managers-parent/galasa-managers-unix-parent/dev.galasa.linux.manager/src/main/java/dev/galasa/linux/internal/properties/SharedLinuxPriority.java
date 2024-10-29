/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Shared Linux Provisioning priority
 * <p>
 * How important this provisioner is against the other provisioners, the larger the number the more important.
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * linux.shared.priority=100
 * </p>
 * <p>
 * default value is 1
 * </p>
 * 
 *  
 *
 */
public class SharedLinuxPriority extends CpsProperties {
    
    private final static Log logger    = LogFactory.getLog(SharedLinuxPriority.class);

    public static int get() {
        try {
            return Integer.parseInt(getStringWithDefault(LinuxPropertiesSingleton.cps(), "1", "shared", "priority"));
        } catch (Exception e) {
            logger.warn("Unable to obtain Shared Linux Priority", e);
            return 1;
        }
    }

}
