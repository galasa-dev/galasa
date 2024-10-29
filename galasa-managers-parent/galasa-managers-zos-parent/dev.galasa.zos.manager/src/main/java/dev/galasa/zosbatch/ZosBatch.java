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
 * z/OS Batch 
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosBatch</code> annotation requests the z/OS Manager to provide a
 * z/OS Batch instance associated with a z/OS image. 
 * The test can request multiple z/OS Batch instances, with the default being associated with the <b>primary</b> zOS image.<br>
 * At test end, the Manager stores the job output with the test results archive and removes jobs from the JES queue. 
 * 
 * @galasa.examples 
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosBatch(imageTag="A")<br>
 * public IZosBatch zosBatchA;<br></code>
 * 
 * @galasa.extra
 * The <code>IZosBatch</code> interface has a single method, {@link IZosBatch#submitJob(String, IZosBatchJobname)} to submit a JCL 
 * as a <code>String</code> and returns a <code>IZosBatchJob</code> instance.<br><br>
 * See {@link ZosBatch}, {@link IZosBatch} and {@link IZosBatchJob} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosBatchField
@ValidAnnotatedFields({ IZosBatch.class })
public @interface ZosBatch {
    
    /**
     * The <code>imageTag</code> is used to identify the z/OS image.
     */
    String imageTag() default "primary";
}
