/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud;

import java.net.InetSocketAddress;

/**
 * Cloud Container
 * 
 *  
 *
 */
public interface ICloudContainer {
	
	/**
	 * Retrieve the port number for an exposed port
	 * 
	 * @param portName - The name of the port
	 * @return the hostname/port
	 * @throws CloudManagerException
	 */
	public InetSocketAddress getContainerExposedPort(String portName) throws CloudManagerException;
	/**
	 * Retrieve the port number for an exposed port
	 * 
	 * @param portNumber - The number of the port
	 * @return the hostname/port
	 * @throws CloudManagerException
	 */
	public InetSocketAddress getContainerExposedPort(int portNumber) throws CloudManagerException;

	/**
	 * Retrieve the stdout/stderr log of the container
	 * 
	 * @return The log
	 * @throws CloudManagerException
	 */
	public String getLog() throws CloudManagerException;
	
	/**
	 * Retrieve the contents of a file within the container
	 * 
	 * @param path - The absolute path of the file
	 * @return The contents of the file
	 * @throws CloudManagerException
	 */
	public String retrieveFileAsString(String path) throws CloudManagerException;

	
	/**
	 * Stop the container, depending on the provider, this may result in the container being deleted.
	 * 
	 * @throws CloudManagerException
	 */
	public void stop() throws CloudManagerException;
	
	/**
	 * Start of the container, depending on the provider, this may result in the container being defined and started
	 * 
	 * @throws CloudManagerException
	 */
	public void start() throws CloudManagerException;
	
	/**
	 * @return The platform the container was provisioned on
	 */
	public String getPlatform();

}
