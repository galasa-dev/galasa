/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.internal;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resource;

import dev.galasa.framework.BundleManagement;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkInitialisation;

@Component(service = { ApiStartup.class })
public class ApiStartup {

    private final Log logger = LogFactory.getLog(getClass());

    private BundleContext bundleContext;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private RepositoryAdmin repositoryAdmin;

    private boolean shutdown = false;
    private boolean shutdownComplete = false;

    public void run(Properties bootstrapProperties, Properties overrideProperties, List<String> extraBundles)
            throws FrameworkException {
        logger.info("API server is initialising");

        // *** Add shutdown hook to allow for orderly shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        try {
            // *** setup the bootstrap configuration

            ServiceReference<?> configurationAdminReference = bundleContext
                    .getServiceReference(ConfigurationAdmin.class.getName());
            if (configurationAdminReference == null) {
                throw new FrameworkException("Unable to initialise, can't find configuration admin reference");
            }

            if (bootstrapProperties != null) {
                ConfigurationAdmin confAdmin = (ConfigurationAdmin) bundleContext
                        .getService(configurationAdminReference);

                Configuration configuration = confAdmin.createFactoryConfiguration("dev.galasa.bootstrap", null);
                Hashtable<String, Object> dictionary = new Hashtable<>();
                for (Entry<Object, Object> entry : bootstrapProperties.entrySet()) {
                    dictionary.put((String) entry.getKey(), entry.getValue());
                }
                configuration.update(dictionary);
            }
        } catch (Throwable t) {
            throw new FrameworkException("Unable to initialise the API server", t);
        }

        // *** Initialise the framework
        IFrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new ApiServerInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Services", e);
        }
        frameworkInitialisation.getFramework();

        // *** Load all the requested api bundles
        if (extraBundles != null && !extraBundles.isEmpty()) {
            for (String bundle : extraBundles) {
                BundleManagement.loadBundle(repositoryAdmin, bundleContext, bundle);
            }
        } else {
            try {
                // *** Load all bundles that have IResourceManagementProvider service
                HashSet<String> bundlesToLoad = new HashSet<>();

                for (Repository repository : repositoryAdmin.listRepositories()) {
                    if (repository.getResources() != null) {
                        resourceSearch: for (Resource resource : repository.getResources()) {
                            if (resource.getCapabilities() != null) {
                                for (Capability capability : resource.getCapabilities()) {
                                    if ("service".equals(capability.getName())) {
                                        Map<String, Object> properties = capability.getPropertiesAsMap();
                                        String services = (String) properties.get("objectClass");
                                        if (services == null) {
                                            services = (String) properties.get("objectClass:List<String>");
                                        }
                                        if (services != null) {
                                            String[] split = services.split(",");

                                            for (String service : split) {
                                                if ("javax.servlet.Servlet".equals(service)) {
                                                    bundlesToLoad.add(resource.getSymbolicName());
                                                    continue resourceSearch;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (String bundle : bundlesToLoad) {
                    if (!BundleManagement.isBundleActive(bundleContext, bundle)) {
                        BundleManagement.loadBundle(repositoryAdmin, bundleContext, bundle);
                    }
                }
            } catch (Throwable t) {
                throw new FrameworkException("Problem loading API bundles", t);
            }
        }

        logger.info("API server has started");

        // *** Loop until we are asked to shutdown
        long heartbeatExpire = 0;
        while (!shutdown) {
            if (System.currentTimeMillis() >= heartbeatExpire) {
                // updateHeartbeat(dss);
                heartbeatExpire = System.currentTimeMillis() + 20000;
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                throw new FrameworkException("Interrupted sleep", e);
            }
        }

        logger.info("API server shutdown is complete");

    }

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            ApiStartup.this.logger.info("Shutdown request received");
            ApiStartup.this.shutdown = true;

            while (!shutdownComplete) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    ApiStartup.this.logger.info("Shutdown wait was interrupted", e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

}