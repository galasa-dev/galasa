/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.mocks;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;

public class MockResourceManagementProvider implements IResourceManagementProvider {

    private boolean isInitialised = false;
    private boolean isStarted = false;

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {
        isInitialised = true;
        return true;
    }

    @Override
    public void start() {
        isStarted = true;
    }

    public boolean isInitialised() {
        return isInitialised;
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
        throw new UnsupportedOperationException("Unimplemented method 'runFinishedOrDeleted'");
    }

}