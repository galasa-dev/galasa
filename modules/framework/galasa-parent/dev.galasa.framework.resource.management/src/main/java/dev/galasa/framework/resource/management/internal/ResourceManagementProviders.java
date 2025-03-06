/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;

/**
 * A list of resource management providers we can get from the OSGi framework.
 * 
 * Operations against this list of providers will be passed on to each provider separately.
 */
public class ResourceManagementProviders {
    
    private Log logger = LogFactory.getLog(this.getClass());
    private List<IResourceManagementProvider> resourceManagementProviders = new ArrayList<IResourceManagementProvider>() ;

    public ResourceManagementProviders(
        IFramework framework , 
        IConfigurationPropertyStoreService cps, 
        BundleContext bundleContext,
        IResourceManagement resourceManagement
    ) throws FrameworkException {

        try {
            final ServiceReference<?>[] rmpServiceReference = bundleContext
                    .getAllServiceReferences(IResourceManagementProvider.class.getName(), null);
            if ((rmpServiceReference == null) || (rmpServiceReference.length == 0)) {
                logger.info("No additional Resource Manager providers have been found");
            } else {

                for (final ServiceReference<?> rmpReference : rmpServiceReference) {

                    final IResourceManagementProvider rmpStoreRegistration = (IResourceManagementProvider) bundleContext
                            .getService(rmpReference);
                    try {
                        if (rmpStoreRegistration.initialise(framework, resourceManagement)) {
                            logger.info(
                                    "Found Resource Management Provider " + rmpStoreRegistration.getClass().getName());
                            resourceManagementProviders.add(rmpStoreRegistration);
                        } else {
                            logger.info("Resource Management Provider " + rmpStoreRegistration.getClass().getName()
                                    + " opted out of this Resource Management run");
                        }
                    } catch (Exception e) {
                        logger.error("Failed initialisation of Resource Management Provider "
                                + rmpStoreRegistration.getClass().getName() + " ignoring", e);
                    }
                }
            }
        } catch (Exception e) {
            throw new FrameworkException("Problem during Resource Manager initialisation", e);
        }
    }

    public void shutdown() {
        for (IResourceManagementProvider provider : resourceManagementProviders) {
            logger.info("Requesting Resource Management Provider " + provider.getClass().getName() + " shutdown");
            provider.shutdown();
        }
    }

    public void start() {
        for (IResourceManagementProvider provider : resourceManagementProviders) {
            provider.start();
        }
    }

    public void runFinishedOrDeleted(String runName) {
        logger.debug("runFinishedOrDeleted() entered");
        for (IResourceManagementProvider provider : resourceManagementProviders) {
            logger.debug("About to call runFinishedOrDeleted() for provider "+provider.getClass().getCanonicalName());
            provider.runFinishedOrDeleted(runName);
            logger.debug("Returned from call runFinishedOrDeleted() for provider "+provider.getClass().getCanonicalName());
        }
        logger.debug("runFinishedOrDeleted() exiting");
    }
}
