/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal.properties;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class DefaultLogonGmText extends CpsProperties {

    public static String get() throws CicstsManagerException {
            return getStringWithDefault(CicstsPropertiesSingleton.cps(), "DFHZC2312", "default.logon", "gm.text");
    }
}
