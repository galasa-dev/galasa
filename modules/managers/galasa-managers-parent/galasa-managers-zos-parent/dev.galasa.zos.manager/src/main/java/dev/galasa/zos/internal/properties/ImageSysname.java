/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * The SYSNAME for zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.image.[imageid].sysname
 * 
 * @galasa.description The SYSNAME for the zOS image
 * 
 * @galasa.required No
 * 
 * @galasa.default The image ID of the image
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.image.IMAGEA.sysname=SYSA</code><br>
 *
 */
public class ImageSysname extends CpsProperties {
    
    public static String get(@NotNull String imageId) throws ZosManagerException {
    	return getStringWithDefault(ZosPropertiesSingleton.cps(), imageId, "image", "sysname", imageId);
    }

}
