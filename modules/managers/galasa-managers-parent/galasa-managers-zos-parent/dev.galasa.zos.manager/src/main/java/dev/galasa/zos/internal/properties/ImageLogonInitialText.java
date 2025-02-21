/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * The logon initial text
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.image.[tag].logon.initial.text
 * 
 * @galasa.description 
 * A text string that is expected to be present on a 3270 that has been connected
 * to the z/OS image [tag] but before logon to any application system has been
 * attempted.
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values Any text string 
 * 
 * @galasa.examples 
 * <code>zos.image.[tag].logon.initial.text=VAMP</code><br>
 *
 */
public class ImageLogonInitialText extends CpsProperties {
    
    public static String get(@NotNull String tag) throws ZosManagerException {
        try {
            String initialText = getStringNulled(ZosPropertiesSingleton.cps(), "image", "logon.initial.text", tag);
            return (initialText == null)?null:initialText.trim();
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the logon initial text for z/OS image with tag '"  + tag + "'", e);
        }
    }

}
