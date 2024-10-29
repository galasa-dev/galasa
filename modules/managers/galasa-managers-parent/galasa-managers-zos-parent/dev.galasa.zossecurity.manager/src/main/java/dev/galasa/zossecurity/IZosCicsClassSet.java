/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import java.util.HashMap;
import java.util.List;

import dev.galasa.zos.IZosImage;
import dev.galasa.zossecurity.datatypes.ZosCicsClassResource;
import dev.galasa.zossecurity.datatypes.RACFAccessType;

/**
 * Represents a set of RACF Classes that is used to CICS security.  A time of writing this is 
 * 10 classes TxxxxTRN and GxxxxTRN etc.
 *  
 *  
 *
 */
public interface IZosCicsClassSet {

	/**
	 * Retrieves the name of the set.
	 * 
	 * @return Set name
	 */
	public String getName();
	
	/**
	 * Retrieves this zOS image this set belongs to
	 * 
	 * @return zOS Image
	 */
	public IZosImage getZosImage();
	
	/**
	 * Retrieve the SIT parameter value for the CICS Security class requested.
	 * <p>
	 * If the setname is JA01, this method will return JA01TRN for CICSClassResource.Transaction so 
	 * you can set the SIT to be XTRAN=JA01TRN
	 * 
	 * @param classType
	 * @return The class name for the security SIT parameter
	 */
	public String getSIT(ZosCicsClassResource classType);
	
	/**
	 * Return all SIT parameters for the security class setup.
	 * 
	 * @return A hashmap of all the class sit parameters
	 */
	public HashMap<String, String> getSIT();
	
	/**
	 * Return all SIT parameters for the security class setup appropriate for the provided CICS release.
	 * 
	 * @param cicsRelease - The CICS Release this SITs are for, internal CICS release number eg 660.
	 * @return A hashmap of all the class SIT Override parameters
	 */
	public HashMap<String, String> getSIT(int cicsRelease);
	
	/**
	 * Creates a generic '*' profile with a UACC of ALTER on all CICS security classes so 
	 * that security can be switched on and everyone will have access.
	 * It is intended that the tester create additional profiles to test the security in the 
	 * area of concern.
	 * 
	 * @return A list of profiles created
	 * @throws ZosSecurityManagerException
	 */
	public List<IZosProfile> allowAllAccess() throws ZosSecurityManagerException;
	
	/**
	 * Creates a generic '*' profile with a UACC of ALTER on all CICS security classes so 
	 * that security can be switched on and everyone will have access.
	 * It is intended that the tester create additional profiles to test the security in the 
	 * area of concern.
	 * 
	 * @param refresh - issue SETROPTS REFRESH
	 * @return A list of profiles created
	 * @throws ZosSecurityManagerException
	 */
	public List<IZosProfile> allowAllAccess(boolean refresh) throws ZosSecurityManagerException;
	
	/**
	 * Create a new Member profile within this set
	 * 
	 * @param classType - Type of security class
	 * @param profileName - The name of the profile
	 * @param uacc - The uacc to set or null
	 * @return The created profile
	 * @throws ZosSecurityManagerException
	 */
	
	public IZosCicsProfile defineMemberProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc) throws ZosSecurityManagerException;
	
	/**
	 * Create a new Member profile within this set
	 * 
	 * @param classType - Type of security class
	 * @param profileName - The name of the profile
	 * @param uacc - The uacc to set or null
	 * @param refresh - Issue SETROPTS Refresh
	 * @return The created profile
	 * @throws ZosSecurityManagerException
	 */
	
	public IZosCicsProfile defineMemberProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException;
	
	/**
	 * Create a new Grouping profile within this set
	 * 
	 * @param classType - Type of security class
	 * @param profileName - The name of the profile
	 * @param uacc - The uacc to set or null
	 * @param members - A list of members to add or null
	 * @return The created profile
	 * @throws ZosSecurityManagerException
	 */
	
	public IZosProfile defineGroupProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc, List<String> members) throws ZosSecurityManagerException;
	
	/**
	 * Create a new Grouping profile within this set
	 * 
	 * @param classType - Type of security class
	 * @param profileName - The name of the profile
	 * @param uacc - The uacc to set or null
	 * @param members - A list of members to add or null
	 * @param refresh - Issue SETROPTS Refresh
	 * @return The created profile
	 * @throws ZosSecurityManagerException
	 */
	
	public IZosProfile defineGroupProfile(ZosCicsClassResource classType, String profileName, RACFAccessType uacc, List<String> members, boolean refresh) throws ZosSecurityManagerException;
	
	/**
	 * Delete a profile.
	 * 
	 * @param profile -The profile to be deleted
	 * @throws ZosSecurityManagerException
	 */
	
	public void deleteProfile(IZosCicsProfile profile) throws ZosSecurityManagerException;
	
	/**
	 * Delete a profile.
	 * 
	 * @param profile -The profile to be deleted
	 * @param refresh - Issue SETROPTS Refresh
	 * @throws ZosSecurityManagerException
	 */
	public void deleteProfile(IZosCicsProfile profile, boolean refresh) throws ZosSecurityManagerException;

	/**
	 * For Shared CICS Classsets this will return the setting for SECPRFX= SIT parameter
	 * 
	 * @return
	 * @throws ZosSecurityManagerException
	 */
	public String getSecprfx() throws ZosSecurityManagerException;
	
	/**
	 * Free the CICS class set. This will be performed automatically at the end of the run
	 *
	 * @throws ZosSecurityManagerException
	 */
	public void free() throws ZosSecurityManagerException;
}
