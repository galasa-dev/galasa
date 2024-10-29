/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import dev.galasa.zossecurity.datatypes.RACFAccessType;

/**
 * Represents a profile created by this zossec security manager
 * 
 *  
 *
 */
public interface IZosProfile {
	
	/**
	 * Retrieve the class name this profile is defined in
	 * 
	 * @return The class name
	 */
	public String getClassName();
	
	/**
	 * Retrieve the profile name 
	 * @return The profile name
	 */
	public String getName();
	
	/**
	 * Amend the UACC of this profile
	 * 
	 * @param newUacc - The new uacc
	 * @throws ZosSecurityManagerException
	 */
	public void alterUacc(RACFAccessType newUacc) throws ZosSecurityManagerException;
	
	/**
	 * Amend the UACC of this profile
	 * 
	 * @param newUacc - The new uacc
	 * @param refresh - issue SETROPTS REFRESH
	 * @throws ZosSecurityManagerException
	 */
	public void alterUacc(RACFAccessType newUacc, boolean refresh) throws ZosSecurityManagerException;
	
	/**
	 * Grant access to this profile to a userid
	 * 
	 * @param userid - The userid to receive the access
	 * @param access - The access level
	 * @throws ZosSecurityManagerException
	 */
	public void setAccess(IZosUserid userid, RACFAccessType access) throws ZosSecurityManagerException;
	
	/**
	 * Grant access to this profile to a userid
	 * 
	 * @param userid - The userid to receive the access
	 * @param access - The access level
	 * @param refresh - issue SETROPTS REFRESH
	 * @throws ZosSecurityManagerException
	 */
	public void setAccess(IZosUserid userid, RACFAccessType access, boolean refresh) throws ZosSecurityManagerException;
	
	/**
	 * Grant access to this profile to a userid or group
	 * 
	 * @param userid - The userid to receive the access
	 * @param access - The access level
	 * @throws ZosSecurityManagerException
	 */
	public void setAccess(String userid, RACFAccessType access) throws ZosSecurityManagerException;
	
	/**
	 * Grant access to this profile to a userid or group
	 * 
	 * @param userid - The userid to receive the access
	 * @param access - The access @Override
	level
	 * @param refresh - issue SETROPTS REFRESH
	 * @throws ZosSecurityManagerException
	 */
	public void setAccess(String userid, RACFAccessType access, boolean refresh) throws ZosSecurityManagerException;
	
	/**
	 * Add a new member to this grouping profile
	 * 
	 * @param member - The member to add
	 * @throws ZosSecurityManagerException
	 */
	public void addMember(String member) throws ZosSecurityManagerException;
	
	/**
	 * Add a new member to this grouping profile
	 * 
	 * @param member - The member to add
	 * @param refresh - issue SETROPTS REFRESH
	 * @throws ZosSecurityManagerException
	 */
	public void addMember(String member, boolean refresh) throws ZosSecurityManagerException;
	
	/**
	 * Delete this profile
	 * 
	 * @throws ZosSecurityManagerException
	 */
	public void delete() throws ZosSecurityManagerException;
	
	/**
	 * Delete this profile
	 * 
	 * @param refresh - issue SETROPTS REFRESH
	 * @throws ZosSecurityManagerException
	 */
	public void delete(boolean refresh) throws ZosSecurityManagerException;
	
	/**
	 * Free the profile. This will be performed automatically at the end of the run
	 *
	 * @throws ZosSecurityManagerException
	 */
	public void free() throws ZosSecurityManagerException;	
}
