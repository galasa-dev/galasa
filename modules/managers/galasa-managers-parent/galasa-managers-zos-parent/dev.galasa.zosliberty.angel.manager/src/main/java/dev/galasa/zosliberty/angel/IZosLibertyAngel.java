/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.angel;

/**
 * Represents a zOS Liberty angel process
 */
public interface IZosLibertyAngel {

	/**
	 * Start the zOS Liberty angel
	 * @throws ZosLibertyAngelException
	 */
	public void start() throws ZosLibertyAngelException;

	/**
	 * Stop the zOS Liberty angel
	 * @throws ZosLibertyAngelException
	 */
	public void stop() throws ZosLibertyAngelException;

	/**
	 * Is the zOS Liberty angel active
	 * @return true/false
	 * @throws ZosLibertyAngelException
	 */
	public boolean isActive() throws ZosLibertyAngelException;

	/**
	 * Get the zOS Liberty angel "named angel name"
	 * @return the "named angel name"
	 */
	public String getName();

}
