/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos3270.Zos3270ManagerException;

/**
 * zOS3270 Apply Confidential Text Filtering to screen records
 * <p>
 * This property indicates that all logs and screen recordings are to be passed
 * through the Confidential Text Filtering services, to hide text like passwords
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * zos3270.apply.ctf=true
 * </p>
 * <p>
 * default value is true
 * </p>
 * 
 *  
 *
 */
public class ApplyConfidentialTextFiltering extends CpsProperties {

    public static boolean get() throws Zos3270ManagerException {
        return Boolean.parseBoolean(getStringWithDefault(Zos3270PropertiesSingleton.cps(), "true", "apply", "ctf"));
    }

}
