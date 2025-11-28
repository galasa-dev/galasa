/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.spi.FrameworkException;

public interface IResourceMonitorBundleLoader {
    void loadMonitorBundles(IBundleManager bundleManager, String stream) throws FrameworkException;
}
