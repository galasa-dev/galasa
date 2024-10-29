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

import dev.galasa.zossecurity.internal.ZosCicsClassSetField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Requests a CICS Security Class set to be allocated.  Use allowAllAccess to have a default profile created in each of all the classes. 
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosCicsClassSetField
@ValidAnnotatedFields({ IZosCicsClassSet.class })
public @interface ZosCicsClassSet {
    
    /**
     * The <code>imageTag</code> is used to identify the z/OS image.
     */
    public String imageTag() default "primary";
	
	/**
	 *  Create a generic '*' profile with uacc of ALTER in all the member classes 
	 */
	public boolean allowAllAccess() default false;
	
	/**
	 * Use a shared class set
	 */
	public boolean shared() default false;
}