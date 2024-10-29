/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zossecurity.ZosSecurityManagerException;

/**
 * zOS Security Resource Reporting
 */
public class ResourceReporting extends CpsProperties {
    
    public static boolean get(String sysplexId) throws ZosSecurityManagerException {
    	return Boolean.parseBoolean(getStringWithDefault(ZosSecurityPropertiesSingleton.cps(), "true", "resource.reporting", sysplexId));
    }

}
