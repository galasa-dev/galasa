/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * z/OS Batch Jobname
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosBatchJobname</code> annotation requests the z/OS Manager to provide a
 * {@link IZosBatchJobname} field that is guaranteed to be unique which can be used for running Jobs or STCs on a zOS Image.
 * The Test can provision many jobnames as are needed for the test
 * 
 * @galasa.examples 
 * <code>{@literal @}ZosBatchJobname(imageTag="A")<br>
 * public IZosBatchJobname jobName;<br></code>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosBatchField
@ValidAnnotatedFields({ IZosBatchJobname.class })
public @interface ZosBatchJobname {
    
    /**
     * The tag of the zOS Image this variable is to be populated with
     */
    String imageTag() default "primary";
}
