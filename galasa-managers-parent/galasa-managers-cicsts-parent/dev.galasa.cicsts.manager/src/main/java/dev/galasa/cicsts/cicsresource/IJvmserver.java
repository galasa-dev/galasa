/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cicsresource;

import java.util.List;

import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosliberty.IZosLibertyServer;

/**
 * Represents a CICS JVM server resource. It provides methods to set JVM server specific attributes on the resource
 * (via CEDA) and to manage and set attributes in CEMT. Methods are provided to manage the logs associated with the JVM server.<p>
 * JVM profile options should be managed via the {@link IJvmprofile} JVM profile object
 */
public interface IJvmserver {
	
	public enum JvmserverType {
		AXIS2("Axis2", "DFHJVMAX.jvmprofile"),
		CLASSPATH("Classpath", "DFHJVMAX.jvmprofile"),
		CMCI("CMCI", "EYUCMCIJ.jvmprofile"),
		LIBERTY("Liberty", "DFHWLP.jvmprofile"),
		OSGI("OSGi", "DFHOSGI.jvmprofile"),
		STS("STS", "DFHJVMST.jvmprofile"),
		UNKNOWN("UNKNOWN", null);

		private final String type;
		private final String cicsSuppliedProfile;
		
		JvmserverType(String type, String cicsSuppliedProfile) {
			this.type = type;
			this.cicsSuppliedProfile = cicsSuppliedProfile;
		}
		
		@Override
		public String toString() {
			return this.type;
		}
		
		public String getCicsSuppliedProfile() {
			return cicsSuppliedProfile;
		}
	}
	
	public enum PurgeType {
		PHASEOUT,
		PURGE,
		FORCEPURGE,
		KILL,
		FAILED
	}
	
	/**
	 * Set the CICS JVMSERVER resource definition DESCRIPTION attribute value
	 * @param value the resource definition DESCRIPTION attribute value
	 */
	public void setDefinitionDescriptionAttribute(String value);
	
	/**
	 * Set the CICS JVMSERVER resource definition STATUS attribute value
	 * @param value the resource definition STATUS attribute value
	 */
	public void setDefinitionStatusAttribute(CicsResourceStatus value);
	
	/**
	 * Set the CICS JVMSERVER resource LERUNOPTS attribute value
	 * @param lerunopts the resource LERUNOPTS attribute value
	 */
	public void setResourceDefinitionLerunoptsAttribute(String lerunopts);

	/**
	 * Set the CICS JVMSERVER resource THREADLIMIT attribute value
	 * @param threadlimit the resource THREADLIMIT attribute value
	 */
	public void setResourceDefinitionThreadlimitAttribute(int threadlimit);

	/**
	 * Return the CICS JVMSERVER resource definition NAME attribute value
	 * @return the resource definition NAME attribute value
	 */
	public String getResourceDefinitionNameAttribute();
	
	/**
	 * Return the CICS JVMSERVER resource definition GROUP attribute value
	 * @return the resource definition GROUP attribute value
	 */
	public String getResourceDefinitionGroupAttribute();
	
	/**
	 * Return the CICS JVMSERVER resource definition DESCRIPTION attribute value
	 * @return the resource definition DESCRIPTION attribute value
	 */
	public String getResourceDefinitionDescriptionAttribute();
	
	/**
	 * Return the CICS JVMSERVER resource definition STATUS attribute value
	 * @return the resource definition STATUS attribute value
	 */
	public CicsResourceStatus getResourceDefinitionStatusAttribute();

	/**
	 * Return the CICS JVMSERVER resource JVMPROFILE attribute value
	 * @return the resource JVMPROFILE attribute value
	 */
	public String getResourceDefinitionJvmprofileAttribute();

	/**
	 * Return the CICS JVMSERVER resource LERUNOPTS attribute value
	 * @return the resource LERUNOPTS attribute value
	 */
	public String getResourceDefinitionLerunoptsAttribute();

	/**
	 * Return the CICS JVMSERVER resource THREADLIMIT attribute value
	 * @return the resource THREADLIMIT attribute value
	 */
	public int getResourceDefinitionThreadlimitAttribute();

	/**
	 * Build the CICS JVMSERVER resource definition only 
	 * @throws CicsJvmserverResourceException
	 */
	public void buildResourceDefinition() throws CicsJvmserverResourceException;	
	
	/**
	 * Build and install the CICS JVMSERVER resource definition 
	 * @throws CicsJvmserverResourceException
	 */
	public void buildInstallResourceDefinition() throws CicsJvmserverResourceException;	
	
	/**
	 * Install the CICS JVMSERVER resource definition
	 * @throws CicsJvmserverResourceException
	 */
	public void installResourceDefinition() throws CicsJvmserverResourceException;	
	
	/**
	 * Check if the CICS JVMSERVER resource definition exist via CEDA DISPLAY  
	 * @throws CicsJvmserverResourceException
	 */
	public boolean resourceDefined() throws CicsJvmserverResourceException;	
	
	/**
	 * Check if the CICS JVMSERVER resource has been installed via CEMT INQUIRE  
	 * @throws CicsJvmserverResourceException
	 */
	public boolean resourceInstalled() throws CicsJvmserverResourceException;

	/**
	 * Enable the CICS JVMSERVER resource
	 * @throws CicsJvmserverResourceException
	 */
	public void enable() throws CicsJvmserverResourceException;

	/**
	 * Wait for the CICS JVMSERVER resource to be enabled. Does NOT issue the enable command
	 * @return true if enabled, false if not enabled
	 * @throws CicsJvmserverResourceException
	 */
	public boolean waitForEnable() throws CicsJvmserverResourceException;

	/**
	 * Wait for the CICS JVMSERVER resource to be enabled with specified timeout. Does NOT issue the enable command
	 * @param timeout timeout in seconds
	 * @return true if enabled, false if not enabled
	 * @throws CicsJvmserverResourceException
	 */
	public boolean waitForEnable(int timeout) throws CicsJvmserverResourceException;

	/**
	 * Returns whether the CICS JVMSERVER resource is currently enabled
	 * @return true if enabled, false if not enabled
	 * @throws CicsJvmserverResourceException 
	 */
	public boolean isEnabled() throws CicsJvmserverResourceException;

	/**
	 * Disable the CICS JVMSERVER resource
	 * @return true if disabled, false if not disabled
	 * @throws CicsJvmserverResourceException
	 */
	public boolean disable() throws CicsJvmserverResourceException;
	
	/**
	 * Disable the CICS JVMSERVER resource with a specific {@link PurgeType}
	 * @param purgeType
	 * @return true if the resource disables within the time, false otherwise
	 * @throws CicsJvmserverResourceException
	 */
	public boolean disable(PurgeType purgeType) throws CicsJvmserverResourceException;
	
	/**
	 * Disable the CICS JVMSERVER resource with a specific {@link PurgeType} and specified timeout
	 * @param purgeType
	 * @param timeout timeout in seconds
	 * @return true if the resource disables within the time, false otherwise
	 * @throws CicsJvmserverResourceException
	 */
	public boolean disable(PurgeType purgeType, int timeout) throws CicsJvmserverResourceException;

	/**
	 * Disable the CICS JVMSERVER resource. This method will escalate through all the {@link PurgeType} levels (PHASEOUT, PURGE, FORCEPURGE, KILL) as
	 * necessary. If the disable at any level is not successful within the default timeout, escalation will happen
	 * @return the {@link PurgeType} at which the disable was successful
	 * @throws CicsJvmserverResourceException 
	 */
	public PurgeType disableWithEscalate() throws CicsJvmserverResourceException;

	/**
	 * Disable the CICS JVMSERVER resource. This method will escalate through all the {@link PurgeType} levels (PHASEOUT, PURGE, FORCEPURGE, KILL) as
	 * necessary. If the disable at any level is not successful within the stepTimeout, escalation will happen
	 * @param stepTimeout time in seconds to allow each step to disable before escalating
	 * @return the {@link PurgeType} at which the disable was successful
	 * @throws CicsJvmserverResourceException 
	 */
	public PurgeType disableWithEscalate(int stepTimeout) throws CicsJvmserverResourceException;

	/**
	 * Wait for the CICS JVMSERVER resource to be disabled. Does NOT issue the disable command
	 * @return true if disabled, false if not disabled
	 * @throws CicsJvmserverResourceException
	 */
	public boolean waitForDisable() throws CicsJvmserverResourceException;

	/**
	 * Wait for the CICS JVMSERVER resource to be disabled with specified timeout. Does NOT issue the disable command
	 * @param timeout timeout in seconds
	 * @return true if disabled, false if not disabled
	 * @throws CicsJvmserverResourceException
	 */
	public boolean waitForDisable(int timeout) throws CicsJvmserverResourceException;

	/**
	 * Delete the CICS JVMSERVER resource including it's zOS UNIX files and directories. If the resource is installed, it will be disabled and discarded
	 * @throws CicsJvmserverResourceException
	 */
	public void delete() throws CicsJvmserverResourceException;

	/**
	 * Delete the CICS JVMSERVER resource including it's zOS UNIX files and directories. If the resource is installed, it will be disabled and discarded. 
	 * Errors during the process will cause an exception to be thrown depending on the value of ignoreErrors 
	 * @param ignoreErrors 
	 * @throws CicsJvmserverResourceException
	 */
	public void delete(boolean ignoreErrors) throws CicsJvmserverResourceException;

	/**
	 * Discard the CICS JVMSERVER resource. If the resource is enabled, it will be disabled and discarded
	 * @throws CicsJvmserverResourceException
	 */
	public void discard() throws CicsJvmserverResourceException;

	/**
	 * Disable and discard the CICS JVMSERVER resource and delete the resource definition.
	 * Errors during the process will cause an exception to be thrown depending on the value of ignoreErrors 
	 * @param ignoreErrors 
	 * @throws CicsJvmserverResourceException
	 */
	public void disableDiscardDelete(boolean ignoreErrors) throws CicsJvmserverResourceException;

	/**
	 * Set the JVMSERVER Threadlimit value in CEMT
	 * @param threadlimit max number of threads used by the JVM server
	 * @throws CicsJvmserverResourceException 
	 */
	public void setThreadLimit(int threadlimit) throws CicsJvmserverResourceException;
	
	/**
	 * Get the JVMSERVER Threadlimit value from CEMT
	 * @return the Threadlimit value
	 * @throws CicsJvmserverResourceException 
	 */
	public int getThreadLimit() throws CicsJvmserverResourceException;	
	
	/**
	 * Get the JVMSERVER Threadcount value form CEMT, i.e the number of threads in use
	 * @return the Threadcount value
	 * @throws CicsJvmserverResourceException 
	 */
	public int getThreadCount() throws CicsJvmserverResourceException;

	/**
	 * Build the complete JVM server including the profile zOS UNIX file and the CICS resource definition. This method will install the CICS 
	 * resource and wait for it to become enabled
	 * @throws CicsJvmserverResourceException
	 */
	public void build() throws CicsJvmserverResourceException;
	
	/**
	 * Return the JVM profile for this JVM server
	 * @return the JVM profile object
	 */
	public IJvmprofile getJvmprofile();
	
	/**
	 * Build the JVM server profile zOS UNIX file only
	 * @throws CicsJvmserverResourceException
	 */
	public void buildProfile() throws CicsJvmserverResourceException;	
	
	/**
	 * Checks if the JVM profile has been built
	 * @return Boolean
	 */
	public boolean isProfileBuilt();	
	
	/**
	 * Sets the zOS Liberty server object associated with this JVM server
	 * @param zosLibertyServer the {@link IZosLibertyServer} object to associate with this JVM server
	 */
	public void setLibertyServer(IZosLibertyServer zosLibertyServer);

	/**
	 * Returns the zOS Liberty server object associated with this JVM server
	 * @return the {@link IZosLibertyServer} associated with this JVM server
	 */
	public IZosLibertyServer getLibertyServer();
	
	/**
	 * Returns the JVM server name as defined in the CICS Resource Definition
	 * @return the JVM server name
	 */
	public String getName();

	/**
	 * Convenience method that returns the JAVA_HOME as defined in the JVM Profile
	 * @return the JAVA_HOME value
	 * @throws CicsJvmserverResourceException
	 */
	public IZosUNIXFile getJavaHome() throws CicsJvmserverResourceException;
	
	/**
	 * Convenience method that returns the WORK_DIR as defined in the JVM Profile
	 * @return the WORK_DIR value
	 * @throws CicsJvmserverResourceException
	 */
	public IZosUNIXFile getWorkingDirectory() throws CicsJvmserverResourceException;
	
	/**
	 * Returns the JVM server JVMLOG
	 * @return JVMLOG {@link IJvmserverLog}
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserverLog getJvmLog() throws CicsJvmserverResourceException;

	/**
	 * Returns the JVM server STDOUT
	 * @return STDOUT {@link IJvmserverLog}
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserverLog getStdOut() throws CicsJvmserverResourceException;
	
	/**
	 * Returns the JVM server STDERR
	 * @return STDERR {@link IJvmserverLog}
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserverLog getStdErr() throws CicsJvmserverResourceException;
	
	/**
	 * Returns the JVM server JVMTRACE
	 * @return JVMTRACE {@link IJvmserverLog}
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserverLog getJvmTrace() throws CicsJvmserverResourceException;
	
	/**
	 * Save a checkpoint of the current state of the JVM server logs
	 * @throws CicsJvmserverResourceException 
	 */
	public void checkpointLogs() throws CicsJvmserverResourceException;
	
	/**
	 * Get a {@link List} of Java log files, i.e Snap.*.trc, javacore.*.txt etc.
	 * @return
	 * @throws CicsJvmserverResourceException
	 */
	public List<IZosUNIXFile> getJavaLogs() throws CicsJvmserverResourceException;

	/**
     * Store the content of the JVM server logs to the default location in the Results Archive Store
     * @throws CicsJvmserverResourceException
     */
    public void saveToResultsArchive() throws CicsJvmserverResourceException;
	
	/**
	 * Store the content of the JVM server logs to the Results Archive Store
	 * @param rasPath path in Results Archive Store
	 * @throws CicsJvmserverResourceException
	 */
	public void saveToResultsArchive(String rasPath) throws CicsJvmserverResourceException;

	/**
	 * Delete the JVM server logs
     * @throws CicsJvmserverResourceException
	 */
	public void clearJvmLogs() throws CicsJvmserverResourceException;

    /**
     * Set flag to control if the JVM server should be automatically stored to the test output. Defaults to true
     */    
    public void setShouldArchive(boolean shouldArchive);

    /**
     * Return flag that controls if the JVM server should be automatically stored to the test output
     */    
    public boolean shouldArchive();

    /**
     * Set flag to control if the JVM server should be automatically purged from zOS. Defaults to true
     */    
    public void setShouldCleanup(boolean shouldCleanup);

    /**
     * Return flag that controls if the JVM server should be automatically purged from zOS
     */    
    public boolean shouldCleanup();
}
