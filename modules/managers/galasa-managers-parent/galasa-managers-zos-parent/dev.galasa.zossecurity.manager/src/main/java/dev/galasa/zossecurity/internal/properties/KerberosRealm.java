/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.properties;

import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;

/**
 * zOS Security Kerberos Realm
 */
public class KerberosRealm extends CpsProperties {
    
    public static String get(IZosImage image) throws ZosSecurityManagerException {
        try {
            String value = getStringNulled(ZosSecurityPropertiesSingleton.cps(), "kerberos", "realm", image.getImageID());
            if (value == null) {
                throw new ZosSecurityManagerException("Missing property for the zOS Security Kerberos Realm for zOS image "  + image.getImageID());
            }
            return value;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Kerberos Realm for zOS image "  + image.getImageID(), e);
        }
    }
}
