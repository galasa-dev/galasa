/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.properties;

import java.util.List;

import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;

/**
 * zOS Security Predefined Profiles
 */
public class PredefinedProfiles extends CpsProperties {
    
    public static List<String> get(IZosImage image) throws ZosSecurityManagerException {
        try {
        	return getStringList(ZosSecurityPropertiesSingleton.cps(), "predefined", "profile", image.getImageID());
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Predefined Profiles for zOS image "  + image.getImageID(), e);
        }
    }

}
