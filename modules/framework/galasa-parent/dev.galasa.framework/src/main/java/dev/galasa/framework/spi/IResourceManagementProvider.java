/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public interface IResourceManagementProvider {

    /**
     * Initialises the resource management provider.
     * 
     * @param framework the framework instance that the provider is running within
     * @param resourceManagement the resource management instance that is running the provider
     * @return true if the provider has been initialised successfully, false otherwise
     * @throws ResourceManagerException if there was an issue initialising the resource management provider
     */
    boolean initialise(IFramework framework, IResourceManagement resourceManagement) throws ResourceManagerException;

    /**
     * Schedule and starts resource cleanup jobs that are intended to run indefinitely for this resource management provider
     * This is called by Galasa's resource management process when running in a Galasa service.
     */
    void start();

    /**
     * Runs resource cleanup for this resource management provider once.
     * This is called by Galasa's resource management when running locally.
     */
    default void runOnce() {}

    /**
     * Shuts down this resource management provider.
     */
    void shutdown();

    /**
     * Run resource cleanup when a run has been marked as finished or has been deleted from Galasa's
     * Dynamic Status Store (DSS).
     * 
     * @param runName the name of the run that has just finished or has been deleted from the DSS
     */
    void runFinishedOrDeleted(String runName);
}
