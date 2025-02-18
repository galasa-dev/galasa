/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;

import java.util.Properties;

/**
 * Controls how a Framework is initialised, based on the type of server we want to stand up.
 */
public interface IFrameworkInitialisationStrategy {

    void setTestRunName(Framework framework, IConfigurationPropertyStoreService cps) throws FrameworkException;
    
    void startLoggingCapture(Framework framework) throws FrameworkException;
        
    Properties applyOverrides(IFramework framework, IDynamicStatusStoreService dss, Properties overrideProperties) throws DynamicStatusStoreException ;

}
