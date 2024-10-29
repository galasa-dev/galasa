/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public interface IResourceManagementProvider {

    boolean initialise(IFramework framework, IResourceManagement resourceManagement) throws ResourceManagerException;

    void start();

    void shutdown();

    void runFinishedOrDeleted(String runName);
}
