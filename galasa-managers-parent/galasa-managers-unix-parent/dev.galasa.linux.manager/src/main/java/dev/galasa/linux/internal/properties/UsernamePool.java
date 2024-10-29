/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.properties;

import java.util.List;

import org.apache.commons.logging.LogConfigurationException;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.linux.LinuxManagerException;

/**
 * Linux User name pool
 * <p>
 * This property indicates what username patterns can be used on the linux image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * linux.image.GALLNX01.username.pool=galasa{0-9}{0-9}
 * linux.image.username.pool=galasa{0-9}{0-9}
 * </p>
 * <p>
 * Can be a comma separated list of static or generated names, eg BOB1,BOB9,BOB5
 * </p>
 * <p>
 * default value is galasa{0-9}{0-9}
 * </p>
 * 
 *  
 *
 */
public class UsernamePool extends CpsProperties {

    public static List<String> get(String hostId) throws LinuxManagerException, LogConfigurationException {
        return getStringListWithDefault(LinuxPropertiesSingleton.cps(), "galasa" + "{0-9}{0-9}",
                "image", "username.pool", hostId);
    }

}
