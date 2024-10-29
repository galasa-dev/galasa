/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zos.spi.ZosImageDependencyField;

/**
 * Represents a CICS TS region that has been provisioned for the test
 * 
 * <p>
 * Used to populate a {@link ICicsRegion} field
 * </p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CicstsManagerField
@ZosImageDependencyField
@ValidAnnotatedFields({ ICicsRegion.class })
public @interface CicsRegion {

    /**
     * The tag of the CICS region this variable is to be populated with
     */
    String cicsTag() default "PRIMARY";
    
    /**
     * The tag of the zOS Image that this region will be provisioned on 
     */
    String imageTag() default "PRIMARY";
}
