/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.eclipseruntime.ubuntu;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import dev.galasa.eclipseruntime.EclipseType;
import dev.galasa.eclipseruntime.EclipseVersion;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * <p>
 * Represents an Eclipse Installation on an Ubuntu Image
 * </p>
 * 
 * <p>
 * Used to populate a {@link IEclipseInstallUbuntu} field
 * </p>
 * 
 *  
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@EclipseUbuntuManagerField
@ValidAnnotatedFields({ IEclipseInstallUbuntu.class })
public @interface EclipseInstallUbuntu {
	/**
	 * The version of Eclipse that should be installed format - %%%
	 */
	EclipseVersion eclipseVersion() default EclipseVersion.v4_16;
	
	/**
	 * The type of eclipse to install.
	 */
	EclipseType eclipseType() default EclipseType.JavaDev;
	
	/**
	 * The version of java to be used with the eclipse installation.
	 */
	String javaInstallationTag() default "PRIMARY";
	
	/**
	 * The version of java to be used with the eclipse installation.
	 */
	String linuxImageTag() default "PRIMARY";
}

