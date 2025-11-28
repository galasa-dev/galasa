/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.BundleContext;

import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;

/**
 * This class is responsible for loading OSGi bundles used in resource management processes.
 */
public class ResourceMonitorBundleLoader implements IResourceMonitorBundleLoader {

    private final Log logger = LogFactory.getLog(this.getClass());

    private IStreamsService streamsService;
    private BundleContext bundleContext;

    private RepositoryAdmin repositoryAdmin;
    private IMavenRepository mavenRepository;

    public ResourceMonitorBundleLoader(
        BundleContext bundleContext,
        IStreamsService streamsService,
        RepositoryAdmin repositoryAdmin,
        IMavenRepository mavenRepository
    ) {
        this.bundleContext = bundleContext;
        this.streamsService = streamsService;
        this.repositoryAdmin = repositoryAdmin;
        this.mavenRepository = mavenRepository;
    }

    public void loadMonitorBundles(IBundleManager bundleManager, String stream) throws FrameworkException {
        if (stream != null && !stream.isBlank()) {
            loadRepositoriesFromStream(stream.trim(), streamsService);
        }

        Set<String> bundlesToLoad = getResourceMonitorBundles();

        // Load the resulting bundles that have the IResourceManagementProvider service
        for (String bundle : bundlesToLoad) {
            if (!bundleManager.isBundleActive(bundleContext, bundle)) {
                logger.info("ResourceManagement - loading bundle: " + bundle);

                bundleManager.loadBundle(repositoryAdmin, bundleContext, bundle);

                logger.info("ResourceManagement - bundle '" + bundle + "' loaded OK");
            }
        }
    }

    private void loadRepositoriesFromStream(String streamName, IStreamsService streamsService) throws FrameworkException {
        IStream stream = streamsService.getStreamByName(streamName);
        stream.validate();

        // Add the stream's maven repo to the maven repositories
        URL mavenRepo = stream.getMavenRepositoryUrl();
        mavenRepository.addRemoteRepository(mavenRepo);

        // Add the stream's OBR to the repository admin
        List<IOBR> obrs = stream.getObrs();
        for (IOBR obr : obrs) {
            try {
                repositoryAdmin.addRepository(obr.toString());
            } catch (Exception e) {
                throw new FrameworkException("Unable to load repository " + obr, e);
            }
        }
    }

    private boolean isResourceMonitorCapability(Capability capability) {
        boolean isResourceMonitor = false;
        if ("service".equals(capability.getName())) {
            Map<String, Object> properties = capability.getPropertiesAsMap();
            String services = (String) properties.get("objectClass");
            if (services == null) {
                services = (String) properties.get("objectClass:List<String>");
            }

            if (services != null) {
                for (String service : services.split(",")) {
                    if ("dev.galasa.framework.spi.IResourceManagementProvider".equals(service)) {
                        isResourceMonitor = true;
                        break;
                    }
                }
            }
        }
        return isResourceMonitor;
    }

    private Set<String> getResourceMonitorBundles() {
        Set<String> bundlesToLoad = new HashSet<>();
        for (Repository repository : repositoryAdmin.listRepositories()) {
            if (repository.getResources() != null) {
                bundlesToLoad.addAll(getResourceMonitorsFromRepository(repository));
            }
        }
        return bundlesToLoad;
    }

    private Set<String> getResourceMonitorsFromRepository(Repository repository) {
        Set<String> resourceMonitorBundles = new HashSet<>();
        for (Resource resource : repository.getResources()) {
            if (isResourceContainingAResourceMonitor(resource)) {
                resourceMonitorBundles.add(resource.getSymbolicName());
            }
        }
        return resourceMonitorBundles;
    }

    private boolean isResourceContainingAResourceMonitor(Resource resource) {
        boolean isResourceContainsResourceMonitor = false;
        if (resource.getCapabilities() != null) {
            for (Capability capability : resource.getCapabilities()) {
                if (isResourceMonitorCapability(capability)) {
                    isResourceContainsResourceMonitor = true;
                    break;
                }
            }
        }
        return isResourceContainsResourceMonitor;
    }
}
