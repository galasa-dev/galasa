/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.mocks;

import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.resource.management.internal.IResourceMonitorBundleLoader;
import dev.galasa.framework.spi.FrameworkException;

public class MockResourceMonitorBundleLoader implements IResourceMonitorBundleLoader {

    private boolean isSimulatedErrorRequested = false;

    @Override
    public void loadMonitorBundles(IBundleManager bundleManager, String stream) throws FrameworkException {
        if (isSimulatedErrorRequested) {
            throw new FrameworkException("simulating a framework exception");
        }
    }

    public void setSimulatedErrorRequested(boolean isSimulatedErrorRequested) {
        this.isSimulatedErrorRequested = isSimulatedErrorRequested;
    }
}
