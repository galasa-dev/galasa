/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * z/OS TSO Command
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosTSOCommand</code> annotation requests the z/OS Manager to provide a
 * z/OS TSO Command instance associated with a z/OS image. 
 * The test can request multiple z/OS TSO Command instances, with the default being associated with the <b>primary</b> z/OS image.
 * 
 * @galasa.examples 
 * <code>
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosTSOCommand(imageTag="A")<br>
 * public IZosTSOCpmmand zosTSOA;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IZosTSOCommand</code> interface provides the methods {@link IZosTSOCommand#issueCommand(String)} and {@link IZosTSOCommand#issueCommand(String, long)}
 * to issue a command to z/OS TSO Command and returns a <code>String</code>.
 *
 * @see IZosTSOCommand
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosTSOCommandField
@ValidAnnotatedFields({ IZosTSOCommand.class })
public @interface ZosTSOCommand {
    
    /**
     * The tag of the zOS Image this variable is to be populated with
     */
    String imageTag() default "primary";
}
