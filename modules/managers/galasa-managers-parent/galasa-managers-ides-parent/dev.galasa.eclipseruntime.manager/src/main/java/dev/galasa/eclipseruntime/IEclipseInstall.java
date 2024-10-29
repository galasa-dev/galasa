/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.eclipseruntime;

import dev.galasa.java.IJavaInstallation;

/**
 * 
 *  
 *
 */
public interface IEclipseInstall {
	
	/**
	 * Returns the version of eclipse being used
	 * @return EclipseVersion
	 */
	EclipseVersion getEclipseVersion();
	
	/**
	 * Returns the types of eclipse being used
	 * @return EclipseType
	 */
	EclipseType getEclipseType();
	
	/**
	 * Returns the Instance of java being used
	 * @return String
	 */
	IJavaInstallation getJavaInstallation();
}

