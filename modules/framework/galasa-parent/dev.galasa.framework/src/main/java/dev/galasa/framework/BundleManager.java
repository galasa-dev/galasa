/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;

import dev.galasa.framework.spi.FrameworkException;

public class BundleManager implements IBundleManager {

    @Override
    public boolean isBundleActive(BundleContext bundleContext, String bundleSymbolicName) {
        return BundleManagement.isBundleActive(bundleContext, bundleSymbolicName);
    }

    @Override
    public void loadAllGherkinManagerBundles(RepositoryAdmin repositoryAdmin, BundleContext bundleContext)
            throws FrameworkException {
        BundleManagement.loadAllGherkinManagerBundles(repositoryAdmin, bundleContext);
    }

    @Override
    public void loadBundle(RepositoryAdmin repositoryAdmin, BundleContext bundleContext, String bundleSymbolicName)
            throws FrameworkException {
        BundleManagement.loadBundle(repositoryAdmin, bundleContext, bundleSymbolicName);
    }
    
}
