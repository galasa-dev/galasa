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

/**
 * A zOS 3270 Terminal for use with a CICS TS Region that has access to the default CICS screens
 * 
 * <p>
 * Used to populate a {@link ICicsTerminal} field
 * </p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CicstsManagerField
@ValidAnnotatedFields({ ICicsTerminal.class })
public @interface CicsTerminal {

    /**
     * The tag of the CICS region terminal is to be associated with
     */
    String cicsTag() default "PRIMARY";
    
    /**
     * The CICS TS Manager will automatically connect the terminal to the CICS TS region when ever is starts 
     */
    boolean connectAtStartup() default true;
    
    /**
     * The CICS TS Manager will automatically log into the CICS TS region via CESL using the terminal with 
     * the specified secure credentials when it connects
     */
    String loginCredentialsTag() default "";
}
