/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.zossecurity.internal.ZosSecurityField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Annotation to provide a zOS Security Manager instance.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosSecurityField
@ValidAnnotatedFields({ IZosSecurity.class })
public @interface ZosSecurity {
    
    /**
     * The <code>imageTag</code> is used to identify the z/OS image.
     */
    String imageTag() default "primary";	
}
