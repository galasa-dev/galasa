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
     * Used to schedule and start the resource management provider's cleanup jobs that are intended to run forever
     */
    void start();

    /**
     * Used to runs the resource management provider's cleanup jobs only once.
     * This is called by Galasa's resource management when running locally.
     */
    default void run() {}

    /**
     * Shuts down this resource management provider
     */
    void shutdown();

    /**
     * Used to run specific resource cleanup when a run has been marked as finished or has been deleted from Galasa's
     * Dynamic Status Store (DSS).
     * 
     * @param runName the name of the run that has just finished or has been deleted from the DSS
     */
    void runFinishedOrDeleted(String runName);
}
