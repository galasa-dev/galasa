/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;

import dev.galasa.framework.spi.FrameworkException;

/**
 * Allows for some control over loading extra bundles, and seeing if bundles are already loaded.
 */
public interface IBundleManager {
    /**
     * Is the supplied active in the OSGi framework
     * @param bundleContext
     * @param bundleSymbolicName
     * @return true if it is active or false otherwise.
     */
    public boolean isBundleActive(BundleContext bundleContext, String bundleSymbolicName);

    public void loadAllGherkinManagerBundles(RepositoryAdmin repositoryAdmin, BundleContext bundleContext) throws FrameworkException;

    public void loadBundle(RepositoryAdmin repositoryAdmin, BundleContext bundleContext, String bundleSymbolicName) throws FrameworkException;

}
