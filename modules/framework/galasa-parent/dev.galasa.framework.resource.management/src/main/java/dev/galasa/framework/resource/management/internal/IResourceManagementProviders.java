/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.List;

import dev.galasa.framework.spi.IResourceManagementProvider;

/**
 * Interface representing a list of resource management providers that we can get from the OSGi framework.
 * Operations against this list of providers will be passed on to each provider separately.
 */
public interface IResourceManagementProviders {
    List<IResourceManagementProvider> getLoadedResourceManagementProviders();

    void shutdown();

    void start();

    void runOnce();

    void runFinishedOrDeleted(String runName);
}
