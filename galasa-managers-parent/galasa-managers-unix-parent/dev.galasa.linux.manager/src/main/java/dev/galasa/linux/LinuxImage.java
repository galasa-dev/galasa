/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Represents a Linux that has been provisioned for the test
 * 
 * <p>
 * Used to populate a {@link ILinuxImage} field
 * </p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@LinuxManagerField
@ValidAnnotatedFields({ ILinuxImage.class })
public @interface LinuxImage {

    /**
     * The tag of the Linux Image this variable is to be populated with
     */
    String imageTag() default "PRIMARY";

    /**
     * The operating system of the Linux image
     * 
     * @return
     */
    OperatingSystem operatingSystem() default OperatingSystem.any;

    /**
     * Capabilities required of this Linux Image, if any.
     */
    String[] capabilities() default {};

    /**
     * Set a variable prefix to be filled in for this Linux TODO: ****** TO BE
     * SPECED OUT FURTHER *****
     */
    String variablePrefix() default "";
}
