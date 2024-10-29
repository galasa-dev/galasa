/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.zossecurity.internal.ZosUseridField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Requests a zOS Userid. Will not have any groups attached, a default password
 * set, no passphrase and no access to any resources.
 * 

 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ZosUseridField
@ValidAnnotatedFields({ IZosUserid.class })
public @interface ZosUserid {

	/**
	 * The <code>imageTag</code> is used to identify the z/OS image.
	 */
	public String imageTag() default "primary";

	/**
	 * Set a symbolic for this user which can be used in environment models
	 * 
	 * @return - the set symbolic
	 */
	public String setSymbolic() default "";

	/**
	 * If true this user will be used as the run user for this test class
	 * 
	 * @return - true if run user
	 */
	public boolean runUser() default false;

	/**

	 * @see #ensZosClient()
	 * @return - the set symbolic
	 */
	public String ensZosClient() default "";
}
