/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Represents a Windows that has been provisioned for the test
 * 
 * <p>
 * Used to populate a {@link IWindowsImage} field
 * </p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@WindowsManagerField
@ValidAnnotatedFields({ IWindowsImage.class })
public @interface WindowsImage {

    /**
     * The tag of the Windows Image this variable is to be populated with
     */
    String imageTag() default "PRIMARY";

    /**
     * Capabilities required of this Windows Image, if any.
     */
    String[] capabilities() default {};

    /**
     * Set a variable prefix to be filled in for this Windows TODO: ****** TO BE
     * SPECED OUT FURTHER *****
     */
    String variablePrefix() default "";
}
