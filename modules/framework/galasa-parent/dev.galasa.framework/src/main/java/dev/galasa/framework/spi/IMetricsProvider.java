/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public interface IMetricsProvider {

    boolean initialise(IFramework framework, IMetricsServer metricsServer) throws MetricsServerException;

    void start();

    void shutdown();
}
