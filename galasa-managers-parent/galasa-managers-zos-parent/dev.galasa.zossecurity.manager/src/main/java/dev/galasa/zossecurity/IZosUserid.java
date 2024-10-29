/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import dev.galasa.zos.IZosImage;

/**
 * Represents a pooled userid 
 * 
 *  
 *
 */
public interface IZosUserid {

	/**
	 * Retreive the userid
	 * 
	 * @return The userid
	 */
	public String getUserid();
	
	/**
	 * Retrieve the password
	 * 
	 * @return The password, will never be null
	 */
	public String getPassword();
	
	/**
	 * Retrieve the passphrase
	 * 
	 * @return The passphrase, may be null if one hasn't been set
	 */
	public String getPassphrase();
	
	/**
	 * Retrieve the zOS image this userid belongs to 
	 * 
	 * @return zOS image
	 */
	public IZosImage getZosImage();
	
	/**
	 * Set the password and/or the passphrase.
	 * <p>
	 * A password must always be set, the passphrase is optional
	 * 
	 * @param password The password to be set, mandatory
	 * @param passphrase The passphrase to be set, optional
	 * @throws ZosSecurityManagerException
	 */
	public void setPassword(String password, String passphrase) throws ZosSecurityManagerException;
	
	/**
	 * Set the password and/or the passphrase, with or without expiry
	 * <p>
	 * A password must always be set, the passphrase is optional
	 * 
	 * @param password The password to be set, mandatory
	 * @param passphrase The passphrase to be set, optional
	 * @param expire true = expired to be set with the password
	 * @throws ZosSecurityManagerException
	 */
	public void setPassword(String password, String passphrase, boolean expire) throws ZosSecurityManagerException;
	
	/**
	 * Revoke the userid
	 * 
	 * @throws ZosSecurityManagerException
	 */
	public void revoke() throws ZosSecurityManagerException;
	
	/**
	 * Resume the userid
	 * 
	 * @throws ZosSecurityManagerException
	 */
	public void resume() throws ZosSecurityManagerException;
	
	/**
	 * Connect this userid to a group
	 * 
	 * @param group
	 * @throws ZosSecurityManagerException
	 */
	public void connectToGroup(String group) throws ZosSecurityManagerException;
	
	/**
	 * Remove this userid from a group
	 * 
	 * @param group
	 * @throws ZosSecurityManagerException
	 */
	public void removeFromGroup(String group) throws ZosSecurityManagerException;
	
	/**
	 * Returns a list of connected groups for this userid
	 * 
	 * @return list of groups
	 */
	public IZosGroup[] getConnectedGroups();
	
	/**
	 * Sets the WHEN option for a RACF userid.<br>
	 * Days can be<br>
	 * <b>ANYDAY</b> Specifies that the user can access the system on any day.<br>
     * <b>WEEKDAYS</b> Specifies that the user can access the system only on weekdays (Monday through Friday).<br>
	 * <b>day</b> ... Specifies that the user can access the system only on the days specified, where day can be<br>
	 * <b>MONDAY</b>, <b>TUESDAY</b>, <b>WEDNESDAY</b>, <b>THURSDAY</b>, <b>FRIDAY</b>, <b>SATURDAY</b>, or <b>SUNDAY</b>, and you can specify the days in any order.<br>
	 * space separated.
	 * 
	 * Time can be<br>
	 * <b>ANYTIME</b>  Specifies that the user can access the system at any time.<br>
	 * <b>start-time:end-time</b> Specifies that the user can access the system only during the specified time period. The format of both start-time and
     * end-time is hhmm, where hh is the hour in 24-hour notation (00 - 23) and mm is the minutes (00 - 59). Note that 0000
	 * is not a valid time value.<br>
	 * If start-time is greater than end-time, the interval spans
	 * midnight and extends into the following day. 
	 * 
	 * @param days - eg "MONDAY" or "MONDAY TUESDAY" or "ANYDAY"
	 * @param time - eg "0400:1900" or "ANYTIME"
	 * @throws ZosSecurityManagerException
	 */
	public void setWhen(String days, String time) throws ZosSecurityManagerException;
	
	/**
	 * Free the userid. This will be performed automatically at the end of the run
	 *
	 * @throws ZosSecurityManagerException
	 */
	public void free() throws ZosSecurityManagerException;

	/**
	 * Delete the userid.
	 * @throws ZosSecurityManagerException
	 */
	public void delete() throws ZosSecurityManagerException;
}
