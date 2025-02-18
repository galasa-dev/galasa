/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.init;

import java.util.Properties;

import dev.galasa.framework.Framework;
import dev.galasa.framework.IFrameworkInitialisationStrategy;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;

public class DefaultInitStrategy implements IFrameworkInitialisationStrategy {

    @Override
    public void setTestRunName(Framework framework, IConfigurationPropertyStoreService cps) throws FrameworkException {
        // Do nothing by default.
    }

    @Override
    public void startLoggingCapture(Framework framework) throws FrameworkException {
        // Do nothing by default.

    }

    @Override
    public Properties applyOverrides(IFramework framework, IDynamicStatusStoreService dss,
            Properties overrideProperties) throws DynamicStatusStoreException {
        // Do nothing by default.
        return overrideProperties;
    }

}
