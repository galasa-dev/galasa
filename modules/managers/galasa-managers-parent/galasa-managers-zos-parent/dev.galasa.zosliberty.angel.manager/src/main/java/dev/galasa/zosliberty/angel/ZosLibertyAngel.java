/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.angel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zosliberty.angel.internal.ZosLibertyAngelField;

/**
 * z/OS Liberty Angel 
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosLibertyAngel</code> annotation requests the zOS Liberty Angel Manager to provide a
 * zOS Liberty Angel instance.
 * 
 * @galasa.examples 
 * <code>
 * {@literal @}ZosImage(imageTag="A")<br> 
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosLibertyAngel(imageTag="A", name="ANGELA")<br>
 * public IZosLibertyAngel zosLibertyAngel;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IZosLibertyAngel</code> interface has a number of methods to manage a zOS Liberty Angel
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ZosLibertyAngelField
@ValidAnnotatedFields({ IZosLibertyAngel.class })
public @interface ZosLibertyAngel {
	
    /**
     * @return The <code>imageTag</code> is used to identify the zOS image
     */
    String imageTag() default "primary";
    
    /**
     * @return The Liberty Named Angel name. If null, a name will be generated
     */
    String name() default "";
}
