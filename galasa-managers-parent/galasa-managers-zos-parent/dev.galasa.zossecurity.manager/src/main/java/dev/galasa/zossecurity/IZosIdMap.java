/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

/**
 * Represents a Id Map that has been created.
 * 
 *  
 *
 */
public interface IZosIdMap {

	/**
	 * Retrieve the userid this Id Map has been attached to
	 * 
	 * @return The userid
	 */
	public String getUserid();
	/**
	 * Retieve the label of this Id Map
	 * 
	 * @return The Label
	 */
	public String getLabel();
	/**
	 * Retieve the Distributed ID of this Id Map
	 * 
	 * @return The Label
	 */
	public String getDistributedID();
	/**
	 * Retieve the Registry of this Id Map
	 * 
	 * @return The Label
	 */
	public String getRegistry();
		
	/**
	 * Delete this Id Map
	 * 
	 * @throws ZosSecurityManagerException
	 */
	public void delete() throws ZosSecurityManagerException;
	
	/**
	 * Free the Id Map. This will be performed automatically at the end of the run
	 *
	 * @throws ZosSecurityManagerException
	 */
	public void free() throws ZosSecurityManagerException;
}
