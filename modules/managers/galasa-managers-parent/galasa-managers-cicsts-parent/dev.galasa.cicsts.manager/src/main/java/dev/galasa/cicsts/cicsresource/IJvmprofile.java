/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cicsresource;

import java.util.Map;

import dev.galasa.zosfile.IZosUNIXFile;

/**
 * Represents a CICS JVM server JVM profile
 */
public interface IJvmprofile {
	
	/**
	 * Set the JVM profile name in the JVM server resource definition
	 * @param name the profile name
	 * @throws CicsJvmserverResourceException 
	 */
	public void setProfileName(String name) throws CicsJvmserverResourceException;

	/**
	 * Set the location for the JVM profile on the zOS UNIX file system.<br>
	 * <b>WARNING: This should must equal to the CICS region JVMPROFILEDIR SIT parameter</b> 
	 * @param jvmProfileDir
	 * @throws CicsJvmserverResourceException 
	 */
	public void setJvmProfileDir(String jvmProfileDir) throws CicsJvmserverResourceException;

	/**
	 * Returns the JVM profile as a {@link IZosUNIXFile} object
	 * @return the JVM profile
	 */
	public IZosUNIXFile getProfile();
	
	/**
	 * Returns the JVM profile name in the JVM server resource definition
	 * @return
	 */
	public String getProfileName();

	/**
	 * Sets JVM server option or JVM system property in the JVM profile of the format <code>key=value</code>.<br>
	 * If the key exists, it will be replaced. To append a value to an existing option or property, use {@link #appendProfileValue(String, String)}.
	 * Adding the + character before key results in the value being appended to the existing option, rather than creating a new option entry<p> 
	 * Examples:<br>
	 * <table border="1">
	 *   <tr>
	 *     <th style="width:25%">Option</th>
	 *     <th style="width:25%">key</th>
	 *     <th style="width:25%">value</th>
	 *   </tr>
	 *   <tr>
	 *     <td><code>PRINT_JVM_OPTIONS=TRUE</code></td>
	 *     <td><code>PRINT_JVM_OPTIONS</code></td>
	 *     <td><code>TRUE</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>-Dcom.ibm.cics.jvmserver.trace.format=FULL</code></td>
	 *     <td><code>-Dcom.ibm.cics.jvmserver.trace.format</code></td>
	 *     <td><code>FULL</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>-Xms256M</code></td>
	 *     <td><code>-Xms</code></td>
	 *     <td><code>256M</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>-Xshareclasses:printStats</code></td>
	 *     <td><code>-Xshareclasses:</code></td>
	 *     <td><code>printStats</code></td>
	 *   </tr>
	 *   <tr>
	 *     <td><code>-Xnocompressedrefs</code></td>
	 *     <td><code>-Xnocompressedrefs</code></td>
	 *     <td><code>null</code></td>
	 *   </tr>
	 * </table>
	 * <p>
	 * @param key the JVM server option or JVM system property key
	 * @param value the JVM server option or JVM system property value
	 */
	public void setProfileValue(String key, String value);	
	
	/**
	 * Append a value to a JVM server option or JVM system property in the JVM profile of the format <code>key=value</code> in the JVM profile. 
	 * For example, given an existing JVM profile option of:<br>
	 * <code>OSGI_BUNDLES=/tmp/bundle1.jar</code><br> 
	 * the method call <code>appendProfileValue("OSGI_BUNDLES", "/tmp/bundle2.jar")</code><br> would set the option to:<br>
	 * <code>OSGI_BUNDLES=/tmp/bundle1.jar,\\\n/tmp/bundle2.jar</code>
	 * <p>
	 * If the key does not exist, then this method performs the same function as {@link #setProfileValue(String, String)}	 * 
	 * @param key the JVM server option or JVM system property key
	 * @param value the JVM server option or JVM system property value
	 */
	public void appendProfileValue(String key, String value);
	
	/**
	 * Returns the value of a JVM server option or JVM system property in the JVM profile
	 * @param key the JVM server option or JVM system property key
	 * @return the JVM server option or JVM system property value
	 */
	public String getProfileValue(String key);	
	
	/**
	 * Removes the JVM server option or JVM system property from the JVM profile
	 * @param key
	 */
	public void removeProfileValue(String key);
	
	/**
	 * Returns true it the current JVM profile object contains the given option
	 * @param option the JVM profile option
	 * @return true or false
	 */
	public boolean containsOption(String option);

	/**
	 * Returns a {@link Map} of the JVM server options and JVM system property in the JVM profile
	 * @return content of the JVM Profile
	 */
	public Map<String, String> getProfileMap();
	
	/**
	 * Returns a {@link String} of the JVM server options and JVM system property in the JVM profile
	 * @return content of the JVM Profile
	 */
	public String getProfileString();

	/**
	 * Returns the location for the JVM profile Directory
	 * @return the value of the JVM Profile Directory
	 */
	public String getJvmProfileDir();
	
	/**
	 * Print the content of the JVM profile
	 */
	public void printProfile();

	/**
	 * Build the JVM profile on the zOS UNIX file system
	 * 
	 * @throws CicsJvmserverResourceException
	 */
	public void build() throws CicsJvmserverResourceException;

	/**
	 * Delete the JVM profile on the zOS UNIX file system
	 * 
	 * @throws CicsJvmserverResourceException
	 */
	public void delete() throws CicsJvmserverResourceException;
	
    /**
     * Store the content JVM profile on the zOS UNIX system to the Results Archive Store
     * @param rasPath path in Results Archive Store
     * @throws CicsJvmserverResourceException
     */
    public void saveToResultsArchive(String rasPath) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>WLP_INSTALL_DIR</code> environment variable in the JVM profile
	 * 
	 * Galasa sets the default value of<code>USSHOME/wlp</code>
	 * @param wlpInstallDir the value of <code>WLP_INSTALL_DIR</code>
	 * @throws CicsJvmserverResourceException
	 */
	public void setWlpInstallDir(String wlpInstallDir) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>WLP_USER_DIR</code> environment variable in the JVM profile
	 * @param wlpUserDir the value of <code>WLP_USER_DIR</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpUserDir(String wlpUserDir) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>WLP_OUTPUT_DIR</code> environment variable in the JVM profile
	 * @param wlpOutputDir the value for <code>WLP_OUTPUT_DIR</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpOutputDir(String wlpOutputDir) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>ZCEE_INSTALL_DIR</code> environment variable in the JVM profile using the value supplied
	 * in the Galasa Configuration Property Service
	 * @throws CicsJvmserverResourceException
	 */
	void setZosConnectInstallDir() throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>ZCEE_INSTALL_DIR</code> environment variable in the JVM profile
	 * @param zOSConnectInstallDir the value of <code>ZCEE_INSTALL_DIR</code>
	 * @throws CicsJvmserverResourceException
	 */
	public void setZosConnectInstallDir(String zOSConnectInstallDir) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.server.name</code> JVM system property in the JVM profile.
	 * Liberty defaults this to defaultServer. 
	 * @param serverName the value for <code>-Dcom.ibm.cics.jvmserver.wlp.server.name</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerName(String serverName) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.autoconfigure</code> JVM system property in the JVM profile
	 * @param autoconfigure the value for <code>-Dcom.ibm.cics.jvmserver.wlp.autoconfigure</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpAutoconfigure(boolean autoconfigure) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.server.host</code> JVM system property in the JVM profile
	 * @param hostname the value of <code>-Dcom.ibm.cics.jvmserver.wlp.server.host</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerHost(String hostname) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.server.http.port</code> JVM system property in the JVM profile
	 * @param httpPort the value of <code>-Dcom.ibm.cics.jvmserver.wlp.server.http.port</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerHttpPort(int httpPort) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.server.https.port</code> JVM system property in the JVM profile
	 * @param httpsPort the value of <code>-Dcom.ibm.cics.jvmserver.wlp.server.https.port</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerHttpsPort(int httpsPort) throws CicsJvmserverResourceException;

	/**
	 * Set the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.wab</code> JVM system property in the JVM profile
	 * @param wabEnabled the value of <code>-Dcom.ibm.cics.jvmserver.wlp.wab</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public void setWlpServerWabEnabled(boolean wabEnabled) throws CicsJvmserverResourceException;

	/**
	 * Returns the value of the <code>WLP_INSTALL_DIR</code> environment variable in the JVM profile 
	 * @return the value of <code>WLP_INSTALL_DIR</code>
	 * @throws CicsJvmserverResourceException 
	 */
	public String getWlpInstallDir() throws CicsJvmserverResourceException;

	/**
	 * Returns the value of the <code>WLP_USER_DIR</code> environment variable in the JVM profile
	 * @return the value of <code>WLP_USER_DIR</code>
	 */
	public String getWlpUserDir();
	
	/**
	 * Returns the value of the <code>WLP_OUTPUT_DIR</code> environment variable in the JVM profile
	 * @return the value of <code>WLP_OUTPUT_DIR</code>
	 */
	public String getWlpOutputDir();
	
	/**
	 * Returns the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.server.name</code> JVM system property in the JVM profile
	 * @return the value of <code>-Dcom.ibm.cics.jvmserver.wlp.server.name</code>
	 */
	public String getWlpServerName();	
	
	/**
	 * Returns the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.autoconfigure</code> JVM system property in the JVM profile
	 * @return the value of <code>-Dcom.ibm.cics.jvmserver.wlp.autoconfigure</code>
	 */
	public String getWlpAutoconfigure();

	/** 
	 * Returns the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.server.host</code> JVM system property in the JVM profile
	 * @return the value of <code>-Dcom.ibm.cics.jvmserver.wlp.server.host</code>
	 */	
	public String getWlpServerHost();
	
	/**
	 * Returns the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.server.http.port</code> JVM system property in the JVM profile
	 * @return the value of <code>-Dcom.ibm.cics.jvmserver.wlp.server.http.port</code>
	 */
	public String getWlpServerHttpPort();

	/**
	 * Returns the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.server.https.port</code> JVM system property in the JVM profile
	 * @return the value of <code>-Dcom.ibm.cics.jvmserver.wlp.server.https.port</code>
	 */
	public String getWlpServerHttpsPort();	

	/**
	 * Returns the value of the <code>-Dcom.ibm.cics.jvmserver.wlp.wab</code> JVM system property in the JVM profile
	 * @return the value of <code>-Dcom.ibm.cics.jvmserver.wlp.wab</code>
	 */
	public boolean getWlpServerWabEnabled();
}
