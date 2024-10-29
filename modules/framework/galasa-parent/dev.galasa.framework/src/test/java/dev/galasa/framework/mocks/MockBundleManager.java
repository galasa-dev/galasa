/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;

import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.spi.FrameworkException;

public class MockBundleManager implements IBundleManager {

    List<String> loadedSymbolicNames = new ArrayList<String>();

    @Override
    public void loadBundle(RepositoryAdmin repositoryAdmin, BundleContext bundleContext, String bundleSymbolicName)
            throws FrameworkException {
        this.loadedSymbolicNames.add(bundleSymbolicName);
    }

    public List<String> getLoadedBundleSymbolicNames() {
        return this.loadedSymbolicNames;
    }

    @Override
    public boolean isBundleActive(BundleContext bundleContext, String bundleSymbolicName) {
        return loadedSymbolicNames.contains(bundleSymbolicName);
    }

    @Override
    public void loadAllGherkinManagerBundles(RepositoryAdmin repositoryAdmin, BundleContext bundleContext)
            throws FrameworkException {
    }

}
