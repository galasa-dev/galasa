/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import java.nio.charset.Charset;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * The code page for a zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.image.[tag].codepage
 * 
 * @galasa.description The code page set for a zOS Image with the specified tag
 * 
 * @galasa.required No
 * 
 * @galasa.valid_values A valid java.nio.charset EBCDIC character encoding (e.g. 037, 1047)
 * 
 * @galasa.default 037
 * 
 * @galasa.examples 
 * <code>zos.image.[tag].codepage=1047</code><br>
 *
 */
public class ImageCodePage extends CpsProperties {
    
    public static Charset get(String imageId) throws ZosManagerException {
        String codePage = getStringWithDefault(ZosPropertiesSingleton.cps(), "037", "image", "codepage", imageId);
        return Charset.forName(codePage);
    }
}
