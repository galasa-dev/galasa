/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cicsresource;

/**
 * Represents a CICS Bundle resource. It provides methods to set CICS Bundle specific attributes on the resource
 * (via CEDA) and to manage and set attributes in CEMT
 */
public interface ICicsBundle {
	
	/**
	 * Set the CICS BUNDLE resource definition DESCRIPTION attribute value
	 * @param value the resource definition DESCRIPTION attribute value
	 */
	public void setDefinitionDescriptionAttribute(String value);
	
	/**
	 * Set the CICS BUNDLE resource definition STATUS attribute value
	 * @param value the resource definition STATUS attribute value
	 */
	public void setDefinitionStatusAttribute(CicsResourceStatus value);

	/**
	 * Return the CICS BUNDLE resource definition NAME attribute value
	 * @return the resource definition NAME attribute value
	 */
	public String getResourceDefinitionNameAttribute();
	
	/**
	 * Return the CICS BUNDLE resource definition GROUP attribute value
	 * @return the resource definition GROUP attribute value
	 */
	public String getResourceDefinitionGroupAttribute();
	
	/**
	 * Return the CICS BUNDLE resource definition DESCRIPTION attribute value
	 * @return the resource definition DESCRIPTION attribute value
	 */
	public String getResourceDefinitionDescriptionAttribute();

	/**
	 * Return the CICS BUNDLE resource STATUS attribute value
	 * @return the resource STATUS attribute value
	 */
	public CicsResourceStatus getResourceDefinitionStatusAttribute();

	/**
	 * Return the CICS BUNDLE resource BUNDLEDIR attribute value
	 * @return the resource JVMPROFILE attribute value
	 */	
	public String getResourceDefinitionBundledirAttribute();

	/**
	 * Build the complete JVM server including the profile zOS UNIX file and the CICS resource definition. This method will install the CICS 
	 * resource and wait for it to become enabled
	 * @throws CicsBundleResourceException
	 */
	public void build() throws CicsBundleResourceException;

	/**
	 * Build the CICS BUNDLE resource definition only 
	 * @throws CicsBundleResourceException
	 */
	public void buildResourceDefinition() throws CicsBundleResourceException;

	/**
	 * Deploy the CICS BUNDLE to the zOS UNIX file system 
	 * @throws CicsBundleResourceException
	 */
	public void deploy() throws CicsBundleResourceException;	
	
	/**
	 * Build and install the CICS BUNDLE resource definition 
	 * @throws CicsBundleResourceException
	 */
	public void buildInstallResourceDefinition() throws CicsBundleResourceException;	
	
	/**
	 * Install the CICS BUNDLE resource definition
	 * @throws CicsBundleResourceException
	 */
	public void installResourceDefinition() throws CicsBundleResourceException;	
	
	/**
	 * Check if the CICS BUNDLE resource definition exist via CEDA DISPLAY  
	 * @return true if the resource definition exists, false otherwise
	 * @throws CicsBundleResourceException
	 */
	public boolean resourceDefined() throws CicsBundleResourceException;	
	
	/**
	 * Check if the CICS BUNDLE resource has been installed via CEMT INQUIRE  
	 * @return true if it has been installed, false otherwise.
	 * @throws CicsBundleResourceException
	 */
	public boolean resourceInstalled() throws CicsBundleResourceException;

	/**
	 * Enable the CICS BUNDLE resource
	 * @throws CicsBundleResourceException
	 */
	public void enable() throws CicsBundleResourceException;

	/**
	 * Wait for the CICS BUNDLE resource to be enabled. Does NOT issue the enable command
	 * @return true if enabled, false if not enabled
	 * @throws CicsBundleResourceException
	 */
	public boolean waitForEnable() throws CicsBundleResourceException;

	/**
	 * Wait for the CICS BUNDLE resource to be enabled with specified timeout. Does NOT issue the enable command
	 * @param timeout timeout in seconds
	 * @return true if enabled, false if not enabled
	 * @throws CicsBundleResourceException
	 */
	public boolean waitForEnable(int timeout) throws CicsBundleResourceException;

	/**
	 * Returns whether the CICS BUNDLE resource is currently enabled
	 * @return true if enabled, false if not enabled
	 * @throws CicsBundleResourceException 
	 */
	public boolean isEnabled() throws CicsBundleResourceException;

	/**
	 * Disable the CICS BUNDLE resource
	 * @return true if disabled, false if not disabled
	 * @throws CicsBundleResourceException
	 */
	public boolean disable() throws CicsBundleResourceException;

	/**
	 * Wait for the CICS BUNDLE resource to be disabled. Does NOT issue the disable command
	 * @return true if disabled, false if not disabled
	 * @throws CicsBundleResourceException
	 */
	public boolean waitForDisable() throws CicsBundleResourceException;

	/**
	 * Wait for the CICS BUNDLE resource to be disabled with specified timeout. Does NOT issue the disable command
	 * @param timeout timeout in seconds
	 * @return true if disabled, false if not disabled
	 * @throws CicsBundleResourceException
	 */
	public boolean waitForDisable(int timeout) throws CicsBundleResourceException;

	/**
	 * Disable and discard the CICS BUNDLE resource and re-install. Waits for disable and enable
	 * Errors during the process will cause an exception to be thrown
	 * @return true if disabled, false if not disabled
	 * @throws CicsBundleResourceException
	 */
	public boolean disableDiscardInstall() throws CicsBundleResourceException;

	/**
	 * Disable and discard the CICS BUNDLE resource and re-install. Waits for disable and enable
	 * Errors during the process will cause an exception to be thrown
	 * @param timeout timeout in seconds
	 * @return true if disabled, false if not disabled
	 * @throws CicsBundleResourceException
	 */
	public boolean disableDiscardInstall(int timeout) throws CicsBundleResourceException;
	
	/**
	 * Delete the CICS BUNDLE resource including it's zOS UNIX files and directories. If the resource is installed, it will be disabled and discarded
	 * @throws CicsBundleResourceException
	 */
	public void delete() throws CicsBundleResourceException;

	/**
	 * Discard the CICS BUNDLE resource. If the resource is enabled, it will be disabled and discarded
	 * @throws CicsBundleResourceException
	 */
	public void discard() throws CicsBundleResourceException;

	/**
	 * Disable and discard the CICS BUNDLE resource and delete the resource definition.
	 * Errors during the process will cause an exception to be thrown
	 * @throws CicsBundleResourceException
	 */
	public void disableDiscardDelete() throws CicsBundleResourceException;

	/**
	 * Returns the CICS BUNDLE name as defined in the CICS Resource Definition
	 * @return the CICS BUNDLE name
	 */
	public String getName();
}
