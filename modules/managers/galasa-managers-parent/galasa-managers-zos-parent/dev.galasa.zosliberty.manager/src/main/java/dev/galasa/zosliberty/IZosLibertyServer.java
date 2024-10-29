/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty;

import java.util.List;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosUNIXFile;

/**
 * Represents a zOS Liberty server
 */
public interface IZosLibertyServer {
    
    public enum ApplicationType {
        EAR("ear"),
        EBA("eba"),
        EJB("ejb"),
        WAR("war");

        private final String type;
        
        ApplicationType(String type) {
            this.type = type;
        }
        
        @Override
        public String toString() {
            return this.type;
        }
    }

    /**
     * Provides the Liberty Server Name
     * @param serverName 
     * @throws ZosLibertyServerException
     */
    public void setServerName(String serverName);
    
    /**
     * Provides the location of the Liberty Install Directory <code>${WLP_INSTALL_DIR}</code>
     * @param wlpInstallDir
     * @throws ZosLibertyServerException
     */
    public void setWlpInstallDir(String wlpInstallDir) throws ZosLibertyServerException;

    /**
     * Provides the location of the Liberty User Directory <code>${WLP_USER_DIR}</code>
     * @param wlpUserDir
     * @throws ZosLibertyServerException
     */
    public void setWlpUserDir(String wlpUserDir) throws ZosLibertyServerException;
    
    /**
     * Provides the location of the Liberty Output Directory <code>${WLP_OUTPUT_DIR}</code>
     * @param wlpOutputDir
     * @throws ZosLibertyServerException
     */
    public void setWlpOutputDir(String wlpOutputDir) throws ZosLibertyServerException;
    
    /**
     * Set the value of JAVA_HOME for this Liberty server
     * @param javaHome
     * @throws ZosLibertyServerException 
     */
    public void setJavaHome(String javaHome) throws ZosLibertyServerException;
    
    /**
     * Returns the Liberty version
     * @return Liberty version
     * @throws ZosLibertyServerException 
     */
    public String getVersion() throws ZosLibertyServerException;

    /**
     * Returns the Liberty Server Name
     * @return the Liberty Server Name
     */
    public String getServerName();
    
    /**
     * Returns the Liberty Install Directory <code>${WLP_INSTALL_DIR}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Install Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getWlpInstallDir() throws ZosLibertyServerException;
    
    /**
     * Returns the Liberty User Directory <code>${WLP_USER_DIR}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty User Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getWlpUserDir() throws ZosLibertyServerException;
    
    /**
     * Returns the Liberty Output Directory <code>${WLP_OUTPUT_DIR}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Output Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getWlpOutputDir() throws ZosLibertyServerException;
    
    /**
     * Returns the Liberty Shared Application Directory <code>${shared.app.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Shared Application Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getSharedAppDir() throws ZosLibertyServerException;

    /**
     * Returns the Liberty Server Configuration Directory <code>${server.config.app.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Server Configuration Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getServerConfigDir() throws ZosLibertyServerException;

    /**
     * Returns the Liberty Server Output Directory <code>${server.output.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Server Output Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getServerOutputDir() throws ZosLibertyServerException;

    /**
     * Returns the Liberty Shared Configuration Directory <code>${shared.config.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Shared Configuration Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getSharedConfigDir() throws ZosLibertyServerException;

    /**
     * Returns the Liberty Shared Resources Directory <code>${shared.resources.dir}</code> as a {@link IZosUNIXFile} object
     * @return the Liberty Shared Resources Directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getSharedResourcesDir() throws ZosLibertyServerException;

    /**
     * Get the Liberty server logs directory
     * @return the Liberty server logs directory
     * @throws ZosLibertyServerException 
     */
    public IZosUNIXFile getLogsDirectory() throws ZosLibertyServerException;

    /**
     * Get the Liberty server dropins directory
     * @return the dropins directory
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getDropinsDir() throws ZosLibertyServerException;

    /**
     * Get the JAVA_HOME value for this Liberty server
     * @return
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getJavaHome() throws ZosLibertyServerException;
    
    /**
     * Get the zOS Image for this Liberty server
     * @return
     */
    public IZosImage getZosImage();

    /**
     * Set the server.xml object for this Liberty server 
     * @param serverXml the {@link IZosLibertyServerXml} server.xml object
     * @throws ZosLibertyServerException
     */
    public void setServerXml(IZosLibertyServerXml serverXml) throws ZosLibertyServerException;
    
    /**
     * Get the server.xml object for this Liberty server
     * @return the server.xml
     * @throws ZosLibertyServerException
     */
    public IZosLibertyServerXml getServerXml() throws ZosLibertyServerException;
    
    /**
     * Load the content of the Liberty server.xml from the file system into the internal {@link IZosLibertyServerXml} object 
     * @return the server.xml
     * @throws ZosLibertyServerException
     */
    public IZosLibertyServerXml loadServerXmlFromFileSystem() throws ZosLibertyServerException;
    
    /**
     * Get the contents of the Liberty servers logs directory
     * @return the Liberty server logs
     * @throws ZosLibertyServerException
     */
    public IZosLibertyServerLogs getLogs() throws ZosLibertyServerException ;
    
    /**
     * Clear the contents of the Liberty servers logs directory
     * @throws ZosLibertyServerException
     */
    public void clearLogs() throws ZosLibertyServerException ;

    /**
     * Convenience method to checkpoint the known Liberty server logs, i.e. <code>messages.log</code> and <code>trace.log</code>
     * @throws ZosLibertyServerException
     */
    public void checkpointLogs() throws ZosLibertyServerException;

    /**
     * Get the current contents of the Liberty server <code>messages.log</code> as a {@link String}
     * @return the content of the <code>messages.log</code> or <code>null</code> if the file does not exist
     * @throws ZosLibertyServerException
     */
    public String retrieveMessagesLog() throws ZosLibertyServerException;    

    /**
     * Get the current contents of the Liberty server <code>trace.log</code> as a {@link String}
     * @return the content of the <code>messages.log</code> or <code>null</code> if the file does not exist
     * @throws ZosLibertyServerException
     */
    public String retrieveTraceLog() throws ZosLibertyServerException;    
    
    /**
     * Get the current contents of the Liberty server <code>messages.log</code> as a {@link String} since the last checkpoint
     * @return the content of the <code>messages.log</code>
     * @throws ZosLibertyServerException
     */
    public String retrieveMessagesLogSinceCheckpoint() throws ZosLibertyServerException;    
    
    /**
     * Get the current contents of the Liberty server <code>trace.log</code> as a {@link String} since the last checkpoint
     * @return the content of the <code>messages.log</code>
     * @throws ZosLibertyServerException
     */
    public String retrieveTraceLogSinceCheckpoint() throws ZosLibertyServerException;

    /**
     * Builds the the Liberty server on the zOS UNIX file system from any objects that have been created such as the server.xml
     * @throws ZosLibertyServerException
     */
    public void build() throws ZosLibertyServerException;    

    /**
     * Create a Liberty server using the <code>server create</code> command 
     * @return <code>server</code> command return code
     * @throws ZosLibertyServerException
     */
    public int create() throws ZosLibertyServerException;

    /**
     * Start the Liberty server using the <code>server start</code> command
     * @return <code>server</code> command return code
     * @throws ZosLibertyServerException
     */
    public int start() throws ZosLibertyServerException;

    /**
     * Run the Liberty server as a zOS batch job
     * @return <code>server</code> command return code
     * @throws ZosLibertyServerException
     */
    public int run() throws ZosLibertyServerException;
    
    /**
     * Wait for the Liberty server to start using the default timeout
     * @return the return code from the <code>server status</code> command
     * @throws ZosLibertyServerException
     */
    public int waitForStart() throws ZosLibertyServerException;

    /**
     * Wait for the Liberty server to start using the supplied timeout
     * @param timeout the timeout in seconds
     * @return the return code from the <code>server status</code> command
     * @throws ZosLibertyServerException
     */
    public int waitForStart(int timeout) throws ZosLibertyServerException;

    /**
     * Wait for the Liberty server to issue the <code>CWWKF0011I</code> message to the <code>messages.log</code> using the default timeout
     * @return true if message was found in log
     * @throws ZosLibertyServerException
     */
    public boolean waitForStartMessage() throws ZosLibertyServerException;

    /**
     * Wait for the Liberty server to issue the <code>CWWKF0011I</code> message to the <code>messages.log</code> using the supplied timeout
     * @param timeout the timeout in seconds
     * @return true if message was found in log
     * @throws ZosLibertyServerException
     */
    public boolean waitForStartMessage(int timeout) throws ZosLibertyServerException;

    /**
     * Stop the Liberty server
     * @return the return code from the <code>server status</code> command
     * @throws ZosLibertyServerException
     */
    public int stop() throws ZosLibertyServerException;
    
    /**
     * Wait for the Liberty server to stop using the default timeout
     * @return the return code from the <code>server status</code> command
     * @throws ZosLibertyServerException
     */
    public int waitForStop() throws ZosLibertyServerException;

    /**
     * Wait for the Liberty server to stop using the supplied timeout
     * @param timeout the timeout in seconds
     * @return the return code from the <code>server status</code> command
     * @throws ZosLibertyServerException
     */
    public int waitForStop(int timeout) throws ZosLibertyServerException;

    /**
     * Wait for the Liberty server to issue the <code>CWWKE0036I</code> message to the <code>messages.log</code> using the default timeout
     * @return true if message was found in log
     * @throws ZosLibertyServerException
     */
    public boolean waitForStopMessage() throws ZosLibertyServerException;

    /**
     * Wait for the Liberty server to issue the <code>CWWKE0036I</code> message to the <code>messages.log</code> using the supplied timeout
     * @param timeout the timeout in seconds
     * @return true if message was found in log
     * @throws ZosLibertyServerException
     */
    public boolean waitForStopMessage(int timeout) throws ZosLibertyServerException;

    /**
     * Get the Liberty sever status using the <code>server status</code> command
     * @return the return code from the <code>server status</code> command
     * @throws ZosLibertyServerException
     */
    public int status() throws ZosLibertyServerException;
    
    /**
     * Delete the Liberty server files and directories
     * @throws ZosLibertyServerException
     */
    public void delete() throws ZosLibertyServerException;
    
    /**
     * Convenience method to create or replace the <code>defaultHttpEndpoint</code> element in the Liberty server <code>server.xml</code><p>
     * <b>NOTE:</b> This method loads the <code>server.xml</code> from the file system, updates the XML and stores it back on the file system
     * @param host the value for the <code>host</code> property. The property will not be set when the value is null
     * @param httpPort the value of the <code>httpPort</code> property. The property will not be set when the value less than 0 
     * @param httpsPort the value of the <code>httpPorts</code> property. The property will not be set when the value less than 0
     * @throws ZosLibertyServerException
     */
    public void setDefaultHttpEndpoint(String host, int httpPort, int httpsPort) throws ZosLibertyServerException;
    
    /**
     * Convenience method to list the current features listed in the <code>featureManager</code> element in the Liberty server <code>server.xml</code><p>
     * <b>NOTE:</b> This method loads the <code>server.xml</code> from the file system
     * @return a list of features
     * @throws ZosLibertyServerException
     */
    public List<String> listFeatures() throws ZosLibertyServerException;
    
    /**
     * Convenience method to add a feature to the Liberty server <code>server.xml</code><p>
     * <b>NOTE:</b> This method loads the <code>server.xml</code> from the file system, updates the XML and stores it back on the file system
     * @param feature the feature to add
     * @throws ZosLibertyServerException
     */
    public void addFeature(String feature) throws ZosLibertyServerException;
    
    /**
     * Convenience method remove a feature from the Liberty server <code>server.xml</code><p>
     * <b>NOTE:</b> This method loads the <code>server.xml</code> from the file system, updates the XML and stores it back on the file system
     * @param feature the feature to remove
     * @throws ZosLibertyServerException
     */
    public void removeFeature(String feature) throws ZosLibertyServerException;
    
    /**
     * Deploy an application to the Liberty server. This method copies the application archive file to the zOS UNIX file system and
     * creates an <code>application</code> element in the server.xml.
     * @param testClass a class in the same bundle containing the application archive file, use <code>this.getClass()</code>
     * @param path the path in the bundle to the application archive file
     * @param targetLocation the location on the zOS UNIX file system to store the application archive file. If the value is null, 
     * <code>${shared.app.dir}/fileName</code> will be used
     * @param type the application type {@link dev.galasa.zosliberty.IZosLibertyServer.ApplicationType}
     * @param name the name of the application
     * @param contextRoot the application context-root. Can be null
     * @throws ZosLibertyServerException
     */
    public void deployApplication(Class<?> testClass, String path, String targetLocation, ApplicationType type, String name, String contextRoot) throws ZosLibertyServerException;
    
    /**
     * Deploy an application to the Liberty server dropins directory. This method copies the application archive file to the Liberty server dropins directory
     * @param testClass a class in the same bundle containing the application archive file, use <code>this.getClass()</code>
     * @param path the path in the bundle to the application archive file
     * @throws ZosLibertyServerException
     */
    public void deployApplicationToDropins(Class<?> testClass, String path) throws ZosLibertyServerException;
    
    /**
     * Remove an application from the Liberty server. This method removes the <code>application</code> from the server.xml and
     * deletes the application archive file from the zOS UNIX file system. Set name to <code>null</code> to remove all applications from dropins
     * @param name the application name
     * @throws ZosLibertyServerException
     */
    public void removeApplication(String name) throws ZosLibertyServerException;
    
    /**
     * Remove an application from the Liberty server dropins directory. Set fileName to <code>null</code> to remove all applications from dropins
     * @param fileName the name of the file in the dropins directory
     * @throws ZosLibertyServerException
     */
    public void removeApplicationFromDropins(String fileName) throws ZosLibertyServerException;
    
    /**
     * Wait for an application to start using the default timeout by looking for <code>CWWKZ0001I</code> message in messages.log.
     * If the <code>messages.log</code> has not been check-pointed, the whole file will be searched.
     * @param name the application name
     * @return true if application has started within the timeout
     * @throws ZosLibertyServerException
     */
    public boolean waitForApplicationStart(String name) throws ZosLibertyServerException;
    
    /**
     * Wait for an application to start using the supplied timeout by looking for <code>CWWKZ0001I</code> message in messages.log.
     * If the <code>messages.log</code> has not been check-pointed, the whole file will be searched.
     * @param name the application name
     * @param timeout the timeout in seconds
     * @return true if application has stopped within the timeout
     * @throws ZosLibertyServerException
     */
    public boolean waitForApplicationStart(String name, int timeout) throws ZosLibertyServerException;
    
    /**
     * Wait for an application to stop using the default timeout by looking for <code>CWWKZ0009I</code> message in messages.log.
     * If the <code>messages.log</code> has not been check-pointed, the whole file will be searched.
     * @param name the application name
     * @return true if application has started within the timeout
     * @throws ZosLibertyServerException
     */
    public boolean waitForApplicationStop(String name) throws ZosLibertyServerException;
    
    /**
     * If the <code>messages.log</code> has not been check-pointed, the whole file will be searched.
     * @param name the application name
     * @param timeout the timeout in seconds
     * @return true if application has stopped within the timeout
     * @throws ZosLibertyServerException
     */
    public boolean waitForApplicationStop(String name, int timeout) throws ZosLibertyServerException;
    
    /**
     * Returns the keystore file for this Liberty server
     * @return the Liberty <code>key.p12</code> or <code>key.jks</code> keystore file. Will return <code>null</code> if the file does not exist.
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getKeystoreFile() throws ZosLibertyServerException;
    
    /**
     * Use the Liberty securityUtility command to encode a password
     * @param password the password to encode
     * @return the encoded password
     * @throws ZosLibertyServerException
     */
    public String securityUtilityEncode(String password) throws ZosLibertyServerException;
        
    /**
     * Use the Liberty securityUtility to create a new keyStore
     * @param password the password used to create the certificate/keystore
     * @return the new keystore file
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile securityUtilityGenerateKeystore(String password) throws ZosLibertyServerException;

    /**
     * Store the content of the Liberty server logs and configuration to the default location in the Results Archive Store
     * @throws ZosLibertyServerException
     */
    public void saveToResultsArchive() throws ZosLibertyServerException;
    
    /**
     * Store the content of the Liberty server logs and configuration to the Results Archive Store
     * @param rasPath path in Results Archive Store
     * @throws ZosLibertyServerException
     */
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException;
}
