/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

/**
 * Representation of a Kerberos Principal
 * 
 *  
 *
 */
public interface IZosKerberosPrincipal {

	/**
	 * 
	 * @return - the principal name
	 */
	public String getPrincipalName();
	
	/**
	 * 
	 * @return - the realm in which the principal exists
	 */
	public String getRealm();
	
	/**
	 * 
	 * @return - the zOS userid for which this principal was created
	 */
	public IZosUserid getUserid();

	/**
	 * 
	 * @return - password for the underlying userid
	 */
	public String getPassword();
	
	/**
	 * Free the principal. This will be performed automatically at the end of the run
	 *
	 * @throws ZosSecurityManagerException
	 */
	public void free() throws ZosSecurityManagerException;
}
