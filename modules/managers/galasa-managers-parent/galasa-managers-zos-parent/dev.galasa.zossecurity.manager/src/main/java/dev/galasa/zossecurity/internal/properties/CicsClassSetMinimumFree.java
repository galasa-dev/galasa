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
 * zOS Security CICS Classset Minimum Free
 */
public class CicsClassSetMinimumFree extends CpsProperties {
    
    public static int get(IZosImage image) throws ZosSecurityManagerException {
    	return getIntWithDefault(ZosSecurityPropertiesSingleton.cps(), 5, "cics.classset", "minimum.free", image.getImageID());
    }
}
