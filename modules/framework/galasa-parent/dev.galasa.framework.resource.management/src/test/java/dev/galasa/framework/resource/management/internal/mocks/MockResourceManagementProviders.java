/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.resource.management.internal.IResourceManagementProviders;
import dev.galasa.framework.spi.IResourceManagementProvider;

public class MockResourceManagementProviders implements IResourceManagementProviders {

    private List<IResourceManagementProvider> resourceManagementProviders = new ArrayList<>();
    private boolean isShutdown = false;
    private boolean isStarted = false;
    private boolean isRunOnceCalled = false;

    @Override
    public List<IResourceManagementProvider> getLoadedResourceManagementProviders() {
        return this.resourceManagementProviders;
    }

    @Override
    public void shutdown() {
        this.isShutdown = true;
    }

    @Override
    public void start() {
        this.isStarted = true;
    }

    @Override
    public void runOnce() {
        this.isRunOnceCalled = true;
        for (IResourceManagementProvider provider : resourceManagementProviders) {
            provider.runOnce();
        }
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
        // Do nothing...
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isRunOnceCalled() {
        return isRunOnceCalled;
    }

    public void addResourceManagementProvider(IResourceManagementProvider providerToAdd) {
        this.resourceManagementProviders.add(providerToAdd);
    }
}
