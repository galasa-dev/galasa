/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zosrseapi.internal.RseapiManagerField;

/**
 * RSE API 
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}Rseapi</code> annotation requests the RSE API Manager to provide a
 * RSE API server instance associated with a z/OS image. 
 * The test can request multiple RSE API instances, with the default being associated with the <b>primary</b> zOS image.
 * 
 * @galasa.examples 
 * <code>
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}Rseapi(imageTag="A")<br>
 * public IRseapi rseapiA;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IRseapi</code> interface has a number of methods to issue requests to the RSE API REST API.
 * 
 * @see Rseapi
 * @see IRseapi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@RseapiManagerField
@ValidAnnotatedFields({ IRseapi.class })
public @interface Rseapi {
    
    /**
     * The tag of the zOS Image this variable is to be populated with
     * @return The tag name of the image
     */
    String imageTag() default "PRIMARY";

}
