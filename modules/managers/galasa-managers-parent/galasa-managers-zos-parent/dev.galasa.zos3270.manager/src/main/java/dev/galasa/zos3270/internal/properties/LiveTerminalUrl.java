/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import java.net.URL;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos3270.Zos3270ManagerException;

/**
 * Which URL to send live terminal updates for displaying in Eclipse
 * <p>
 * Eclipse will set this property in the overrides to indicate the zOS3270 is to
 * place the terminal images ready for live viewing in the Eclipse UI
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * zos3270.live.terminal.images=xxxxxxxx
 * </p>
 * <p>
 * there is no default, lack of presence or empty value means no live recording
 * to be done
 * </p>
 * 
 *  
 *
 */
public class LiveTerminalUrl extends CpsProperties {

    public static URL get() throws Zos3270ManagerException {
        try {
            String url = getStringNulled(Zos3270PropertiesSingleton.cps(), "live.terminal", "images");
            if (url == null) {
                return null;
            }

            return new URL(url);
        } catch (Exception e) {
            throw new Zos3270ManagerException("Unable to retrieve the live.terminal.images property", e);
        }
    }

}
