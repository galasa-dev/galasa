/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.List;

import dev.galasa.framework.spi.IResourceManagementProvider;

public interface IResourceManagementProviders {
    List<IResourceManagementProvider> getLoadedResourceManagementProviders();

    void shutdown();

    void start();

    void runOnce();

    void runFinishedOrDeleted(String runName);
}
