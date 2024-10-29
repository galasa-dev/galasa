/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos3270.Zos3270ManagerException;

/**
 * Are the terminal images logged to the console/runlog
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * zos3270.console.terminal.images=xxxxxxxx
 * </p>
 * <p>
 * default is TRUE
 * </p>
 * 
 *  
 *
 */
public class LogConsoleTerminals extends CpsProperties {

    public static boolean get() throws Zos3270ManagerException {
        return Boolean.parseBoolean(
                getStringWithDefault(Zos3270PropertiesSingleton.cps(), "true", "console.terminal", "images"));
    }

}
