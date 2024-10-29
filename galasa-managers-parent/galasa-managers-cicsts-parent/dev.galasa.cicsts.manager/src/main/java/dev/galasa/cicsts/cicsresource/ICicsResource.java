/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cicsresource;

import java.util.Map;

import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.IJvmserver.JvmserverType;
import dev.galasa.zosliberty.IZosLibertyServer;

/**
 * Represents a CICS Resource. Provides methods to manage the resource, e.g. set attributes, install and delete via CEDA,
 * enable, disable and discard via CEMT
 */
public interface ICicsResource {
	
	/**
	 * Create a CICS BUNDLE resource object supplying the CICS bundle content. 
	 * 
	 * The source bundle should have the same file structure as it exists
	 * on the zOS UNIX file system and will be transferred to the host in binary mode.
	 * 
	 * @param cicsTerminal a ICicsTerminal object for CEDA and CEMT transactions
	 * @param testClass a class in the same bundle containing the application archive file, use <code>this.getClass()</code>
	 * @param name the CICS BUNDLE RDO name
	 * @param group the CICS BUNDLE RDO group name
	 * @param bundlePath the path to the directory in the test class bundle containing the CICS bundle
	 * @param parameters substitution parameters to replace variables in the <code>META-INF/cics.xml</code>. Can be <code>null</code>  
	 * @return the CICS Bundle object
	 * @throws CicsBundleResourceException
	 */
	public ICicsBundle newCicsBundle(ICicsTerminal cicsTerminal, Class<?> testClass, String name, String group, String bundlePath, Map<String, String> parameters) throws CicsBundleResourceException;
	
	/**
	 * Create a CICS BUNDLE resource object without supplying the CICS bundle content, i.e. the bundle already exists on the zOS UNIX file system
	 * @param cicsTerminal a ICicsTerminal object for CEDA and CEMT transactions
	 * @param testClass a class in the same bundle containing the application archive file, use <code>this.getClass()</code>
	 * @param name the CICS BUNDLE RDO name
	 * @param group the CICS BUNDLE RDO group name
	 * @param bundleDir the CICS BUNDLE RDO BUNDLEDIR value, i.e. the location of the existing CICS bundle
	 * @return the CICS Bundle object
	 * @throws CicsBundleResourceException
	 */
	public ICicsBundle newCicsBundle(ICicsTerminal cicsTerminal, Class<?> testClass, String name, String group, String bundleDir) throws CicsBundleResourceException;

	/**
	 * Create a CICS JVMSERVER resource object using the CICS/Galasa default properties
	 * @param cicsTerminal a ICicsTerminal object for CEDA and CEMT transactions  
	 * @param name the JVM server name
	 * @param group the JVM server RDO group name
	 * @param jvmprofileName the name of the JVM profile
	 * @param jvmserverType the JVM server type
	 * @return the Galasa JVM server object
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserver newJvmserver(ICicsTerminal cicsTerminal, String name, String group, String jvmprofileName, JvmserverType jvmserverType) throws CicsJvmserverResourceException;
	
	/**
	 * Create a CICS JVMSERVER resource object using the supplied JVM profile
	 * @param cicsTerminal a ICicsTerminal object for CEDA and CEMT transactions
	 * @param name the JVM server name
	 * @param group the JVM server RDO group name
	 * @param jvmprofile the JVM server profile
	 * @return the Galasa JVM server object
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserver newJvmserver(ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile) throws CicsJvmserverResourceException;
	
	/**
	 * Create a CICS Liberty JVMSERVER resource object using the supplied JVM profile and Liberty server
	 * @param cicsTerminal a ICicsTerminal object for CEDA and CEMT transactions
	 * @param name the JVM server name
	 * @param group the JVM server RDO group name
	 * @param jvmprofile the JVM server profile
	 * @return the Galasa JVM server object
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserver newLibertyJvmserver(ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile, IZosLibertyServer libertyServer) throws CicsJvmserverResourceException;

	/**
	 * Create an empty JVM profile object for use by a {@link IJvmserver}
	 * @param jvmprofileName the name of the JVM profile
	 * @return the JVM profile
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName);

	/**
	 * Create an JVM profile object for use by a {@link IJvmserver} using the supplied {@link Map} of options.<p>
	 * See {@link IJvmprofile#setProfileValue(String, String)} for format of options
	 * @param jvmprofileName the name of the JVM profile
	 * @param content the profile content as a {@link Map}
	 * @return the JVM profile
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName, Map<String, String> content);

	/**
	 * Create an JVM profile object for use by a {@link IJvmserver} using the supplied {@link Map} of options.<p>
	 * See {@link IJvmprofile#setProfileValue(String, String)} for format of options
	 * @param jvmprofileName the name of the JVM profile
	 * @param content the profile content as a {@link String}
	 * @return the JVM profile
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName, String content);
}
