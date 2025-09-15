/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.mocks;

import java.util.concurrent.ScheduledExecutorService;

import dev.galasa.framework.spi.IResourceManagement;


public class MockResourceManagement implements IResourceManagement {

    public boolean isSuccessful;

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        throw new UnsupportedOperationException("Unimplemented method 'getScheduledExecutorService'");
    }

    @Override
    public void resourceManagementRunSuccessful() {
        this.isSuccessful = true;
    }
    
}
