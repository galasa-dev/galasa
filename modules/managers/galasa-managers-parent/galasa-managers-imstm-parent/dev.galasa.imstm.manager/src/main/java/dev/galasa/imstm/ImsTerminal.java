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

/**
 * A zOS 3270 Terminal for use with an IMS TM System that has access to the default IMS screens
 * 
 * <p>
 * Used to populate a {@link IImsTerminal} field
 * </p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ImstmManagerField
@ValidAnnotatedFields({ IImsTerminal.class })
public @interface ImsTerminal {

    /**
     * The tag of the IMS system terminal is to be associated with
     */
    String imsTag() default "PRIMARY";
    
    /**
     * The IMS TM Manager will automatically connect the terminal to the IMS TM System when ever it starts 
     */
    boolean connectAtStartup() default true;
    
    /**
     * The IMS TM Manager will automatically log into the IMS TM region using the terminal with 
     * the specified secure credentials when it connects
     */
    String loginCredentialsTag() default "";
}
