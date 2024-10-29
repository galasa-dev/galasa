/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosfile.ZosFileManagerException;

/**
 * zOS File UNIX permission bits to be used in creating the file or directory
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosfile.[imageid].unix.file.permission
 * 
 * @galasa.description The UNIX file or directory permission bits to be used in creating the file or directory
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosfile.unix.file.permission=rwxrwx---</code><br>
 * <code>zosfile.SYSA.unix.file.permission=rwxrwxrrx</code>
 *
 */
public class UnixFilePermissions extends CpsProperties {

    private static final String UNIX_FILE_PERMISSIONS = "rwxrwxr-x";

    public static String get(String imageId) throws ZosFileManagerException {
        try {
            String modeString = getStringNulled(ZosFilePropertiesSingleton.cps(), "zosfile","unix.file.permissions", imageId);

            if (modeString == null) {
                return UNIX_FILE_PERMISSIONS;
            } else {
                if (!modeString.matches("([-r][-w][-x]){3}")) {
                    throw new ZosFileManagerException("The default UNIX file permissions property must be in the range \"---------\" to \"rwxrwxrwx\" and match the regex expression \"([-r][-w][-x]){3}\"");
                }
                return modeString;
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosFileManagerException("Problem asking the CPS for the default UNIX file permissions property for zOS image "  + imageId, e);
        }
    }

}
