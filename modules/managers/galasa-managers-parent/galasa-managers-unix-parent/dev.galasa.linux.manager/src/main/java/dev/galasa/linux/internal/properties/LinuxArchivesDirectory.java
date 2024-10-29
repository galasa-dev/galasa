/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.linux.LinuxManagerException;

/**
 * Linux Archives Directory
 * <p>
 * Where are archives stored on this image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * linux.image.GALLNX.archives.directory=/opt/archives
 * linux.image.archives.directory=/opt/archives
 * </p>
 * <p>
 * Default is /opt/archives
 * </p>
 * 
 *  
 *
 */
public class LinuxArchivesDirectory extends CpsProperties {

    public static String get(String imageName) throws ConfigurationPropertyStoreException, LinuxManagerException {
        return getStringWithDefault(LinuxPropertiesSingleton.cps(), "/opt/archives", "image", "archives.directory", imageName);
    }

}
