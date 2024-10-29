/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.ZosSecurityManagerException;

/**
 * zOS Security Userid Minimum Free
 */
public class UseridMinimumFree extends CpsProperties {
    
    public static int get(IZosImage image) throws ZosSecurityManagerException {
    	return getIntWithDefault(ZosSecurityPropertiesSingleton.cps(), 5, "userid", "minimum.free", image.getImageID());
    }
}
