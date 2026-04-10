/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.mocks;

import dev.galasa.framework.spi.IResourceManagement;


public class MockResourceManagement implements IResourceManagement {

    public boolean isSuccessful;
    private MockScheduledExecutorService scheduledExecutorService = new MockScheduledExecutorService();

    @Override
    public MockScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    @Override
    public void resourceManagementRunSuccessful() {
        this.isSuccessful = true;
    }
    
}
