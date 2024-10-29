/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * <p>
 * Fill this field with a unique (within the ecosystem) string of a set length.
 * The CPS property core.resource.string.[length].pattern determines the make up of the random string.
 * </p>
 * <p>
 * Will only populate public {@link dev.galasa.core.manager.IResourceString} fields.
 * </p>
 *
 *  
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
@CoreManagerField
@ValidAnnotatedFields({ dev.galasa.core.manager.IResourceString.class })
public @interface ResourceString {
    
    
    /**
     * The tag of the Resource String this variable is to be populated with.
     * 
     * The tag must be provided as there is no default for this resource.
     * 
     * The {@link dev.galasa.core.manager.IResourceString} object is keyed on the tag, so if the tag is referred to in multiple 
     * super classes,  then the lengths must be identical otherwise an exception will be thrown.
     */
    String tag();
    
    /**
     * Generate and lock a resource string
     * 
     * @return the length of the string to generate
     */
    public int length() default 8;


}
