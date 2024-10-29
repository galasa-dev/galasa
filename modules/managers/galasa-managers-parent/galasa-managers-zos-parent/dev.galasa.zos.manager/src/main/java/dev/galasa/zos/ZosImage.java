/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Represents a zOS Image that has been provisioned for the test
 * 
 * <p>Used to populate a {@link IZosImage} field</p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosManagerField
@ValidAnnotatedFields({ IZosImage.class })
public @interface ZosImage {
    
    /**
     * The tag of the zOS Image this variable is to be populated with
     */
    String imageTag() default "PRIMARY";
    
    /**
     * Capabilities required of this zOS Image, if any.
     */
    String[] capabilities() default {};
    
    /**
     * Set a variable prefix to be filled in for this zOS Image
     * TODO: ****** TO BE SPECED OUT FURTHER  *****
     */
    String variablePrefix() default "";
}
