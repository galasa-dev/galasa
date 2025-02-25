/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zos.spi.ZosImageDependencyField;

/**
 * Represents an IMS TM System that has been provisioned for the test
 * 
 * <p>
 * Used to populate a {@link IImsSystem} field
 * </p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ImstmManagerField
@ZosImageDependencyField
@ValidAnnotatedFields({ IImsSystem.class })
public @interface ImsSystem {

    /**
     * The tag of the IMS system this variable is to be populated with.
     * Default value is <b>PRIMARY</b>. 
     */
    String imsTag() default "PRIMARY";
    
    /**
     * The tag of the zOS Image that this region will be provisioned on.
     * Default value is <b>PRIMARY</b>.
     */
    String imageTag() default "PRIMARY";
}
