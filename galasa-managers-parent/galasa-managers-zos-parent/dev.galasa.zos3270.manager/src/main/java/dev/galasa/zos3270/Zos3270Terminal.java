/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zos3270.internal.Zos3270ManagerField;

/**
 * Represents a zOS 3270 terminal that has been provisioned for the test
 * 
 * <p>
 * Used to populate a {@link ITerminal} field
 * </p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Zos3270ManagerField
@ValidAnnotatedFields({ ITerminal.class })
public @interface Zos3270Terminal {

    /**
     * The tag of the zOS Image for terminal this variable is to be populated with
     */
    String imageTag() default "primary";
    /**
     * Allow user to choose if the terminal autoconnects in provision start stage
     */
    boolean autoConnect() default true;
    
    int primaryColumns() default 80;
    int primaryRows()    default 24;
    
    int alternateColumns() default 0;
    int alternateRows()    default 0;

}
