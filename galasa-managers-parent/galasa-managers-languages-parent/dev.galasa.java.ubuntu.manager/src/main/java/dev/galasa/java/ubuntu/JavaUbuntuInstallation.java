/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.ubuntu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.java.JavaType;
import dev.galasa.java.JavaVersion;

/**
 * Represents a Java installation on a Ubuntu image
 * 
 * <p>
 * Used to populate a {@link IJavaUbuntuInstallation} field
 * </p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@JavaUbuntuManagerField
@ValidAnnotatedFields({ IJavaUbuntuInstallation.class })
public @interface JavaUbuntuInstallation {
    
    JavaType javaType() default JavaType.jdk;
    
    JavaVersion javaVersion() default JavaVersion.v11;
    
    String javaJvm() default "hotspot";

    /**
     * The tag to be assigned to this Java installation
     */
    String javaTag() default "PRIMARY";

    /**
     * The tag of the Linux Image this installation is to be associated with
     */
    String imageTag() default "PRIMARY";

}
