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
 * OpenStack Linux Archives Directory
 * <p>
 * Where are archives stored on this image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * openstack.image.GALLNX.archives.directory=/opt/archives
 * openstack.image.archives.directory=/opt/archives
 * </p>
 * <p>
 * Default is /opt/archives
 * </p>
 *
 */
public class LinuxArchivesDirectory extends CpsProperties {

    public static String get(String image) throws ConfigurationPropertyStoreException, OpenstackManagerException {
        return getStringWithDefault(OpenstackPropertiesSingleton.cps(), "/opt/archives", "image", "archives.directory", image);
    }

}
