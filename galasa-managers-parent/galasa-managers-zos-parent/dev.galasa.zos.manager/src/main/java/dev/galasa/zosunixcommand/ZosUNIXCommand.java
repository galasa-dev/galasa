/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * z/OS UNIX Command 
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosUNIXCommand</code> annotation requests the z/OS Manager to provide a
 * z/OS UNIX instance associated with a z/OS image. 
 * The test can request multiple z/OS UNIX Command instances, with the default being associated with the <b>primary</b> z/OS image.<br>
 * 
 * @galasa.examples 
 * <code>
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosUNIXCommand(imageTag="A")<br>
 * public IZosUNIXCommand zosUNIXCommandA;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IZosUNIXCommand</code> interface provides the methods {@link IZosUNIXCommand#issueCommand(String)} and {@link IZosUNIXCommand#issueCommand(String, long)}
 * to issue a command to z/OS UNIX and returns a {@link String} response.<br><br>
 * See {@link IZosUNIXCommand} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosUNIXCommandField
@ValidAnnotatedFields({ IZosUNIXCommand.class })
public @interface ZosUNIXCommand {
    
    /**
     * The tag of the zOS Image this variable is to be populated with
     */
    String imageTag() default "primary";
}
