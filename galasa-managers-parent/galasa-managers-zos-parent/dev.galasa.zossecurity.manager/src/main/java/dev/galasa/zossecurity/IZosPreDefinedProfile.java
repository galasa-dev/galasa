/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import dev.galasa.zossecurity.datatypes.RACFAccessType;

/**
 * Represents a predefined profile, which userids can be permitted to.
 * Only "new" eJAT controlled userids will be allowed to be added/deleted.  Existing
 * userids, members and uacc can not be changed 
 * 
 * This can only be used with the v2 security manager
 * 
 *  
 *
 */
public interface IZosPreDefinedProfile {
	
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
	 * Grant access to this profile to a userid
	 * 
	 * @param userid - The userid to receive the access
	 * @param access - The access level
	 * @throws ZosSecurityManagerException
	 */
	public void setAccess(IZosUserid userid, RACFAccessType access) throws ZosSecurityManagerException;
	
	/**
	 * Grant access to this profile to a userid or group
	 * 
	 * @param userid - The userid to receive the access
	 * @param access - The access level
	 * @param refresh - issue SETROPTS REFRESH
	 * @throws ZosSecurityManagerException
	 */
	public void setAccess(IZosUserid userid, RACFAccessType access, boolean refresh) throws ZosSecurityManagerException;	
}
