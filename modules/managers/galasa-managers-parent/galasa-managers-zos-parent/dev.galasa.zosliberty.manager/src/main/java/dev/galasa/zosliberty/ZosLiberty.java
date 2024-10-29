/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zosliberty.internal.ZosLibertyField;

/**
 * z/OS Liberty 
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosLiberty</code> annotation requests the zOS Liberty Manager to provide a
 * zOS Liberty instance.
 * 
 * @galasa.examples 
 * {@literal @}ZosLiberty<br>
 * public IZosLiberty zosLiberty;<br></code>
 * 
 * @galasa.extra
 * The <code>IZosLiberty</code> interface has a number of methods to manage a zOS Liberty Server
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ZosLibertyField
@ValidAnnotatedFields({ IZosLiberty.class })
public @interface ZosLiberty {

}
