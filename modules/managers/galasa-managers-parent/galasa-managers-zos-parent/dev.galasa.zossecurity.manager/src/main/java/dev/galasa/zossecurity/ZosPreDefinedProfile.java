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

import dev.galasa.zossecurity.internal.ZosPreDefinedProfileField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Represents a Pre Defined RACF Profile where eJAT controlled Userids can be added and removed
 * 
 * This can only be used with the v2 security manager
 * 
 *  
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosPreDefinedProfileField
@ValidAnnotatedFields({ IZosPreDefinedProfile.class })
public @interface ZosPreDefinedProfile {
    
    /**
     * The <code>imageTag</code> is used to identify the z/OS image.
     */
    String imageTag() default "primary";

	/**
	 * The name of the class of the predefine profile.
	 * 
	 * @return - The class Name
	 */
	public String classname();
	
	/**
	 * The name of the pre defined profile.  Must be in the authorised list of profiles.
	 * 
	 * @return - The profile Name
	 */
	public String profile();

}
