/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        List<Repository> obrsToSearch = null;
        if (stream != null && !stream.isBlank()) {
            obrsToSearch = loadRepositoriesFromStream(stream.trim(), streamsService);
        } else {
            obrsToSearch = Arrays.asList(repositoryAdmin.listRepositories());
        }

        List<String> bundlesToLoad = getResourceMonitorBundles(obrsToSearch);

        // Load the resulting bundles that have the IResourceManagementProvider service
        for (String bundle : bundlesToLoad) {
            if (!bundleManager.isBundleActive(bundleContext, bundle)) {
                logger.info("ResourceManagement - loading bundle: " + bundle);

                bundleManager.loadBundle(repositoryAdmin, bundleContext, bundle);

                logger.info("ResourceManagement - bundle '" + bundle + "' loaded OK");
            }
        }
    }

    private List<Repository> loadRepositoriesFromStream(String streamName, IStreamsService streamsService) throws FrameworkException {
        List<Repository> streamObrs = new ArrayList<>();
        IStream stream = streamsService.getStreamByName(streamName);
        stream.validate();

        // Add the stream's maven repo to the maven repositories
        URL mavenRepo = stream.getMavenRepositoryUrl();
        logger.info("Registering test stream Maven repository: " + mavenRepo.toString());

        mavenRepository.addRemoteRepository(mavenRepo);

        logger.info("Registered test stream Maven repository OK");

        // Add the stream's OBR to the repository admin
        List<IOBR> obrs = stream.getObrs();
        for (IOBR obr : obrs) {
            try {
                String obrAsString = obr.toString();
                logger.info("Loading OBR " + obrAsString);

                Repository addedObr = repositoryAdmin.addRepository(obrAsString);
                streamObrs.add(addedObr);

                logger.info("Loaded OBR " + obrAsString + " OK");
            } catch (Exception e) {
                throw new FrameworkException("Unable to load repository " + obr, e);
            }
        }
        return streamObrs;
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

    private List<String> getResourceMonitorBundles(List<Repository> obrsToSearch) {
        List<String> bundlesToLoad = new ArrayList<>();
        for (Repository repository : obrsToSearch) {
            if (repository.getResources() != null) {
                bundlesToLoad.addAll(getResourceMonitorsFromRepository(repository));
            }
        }
        return bundlesToLoad;
    }

    private List<String> getResourceMonitorsFromRepository(Repository repository) {
        List<String> resourceMonitorBundles = new ArrayList<>();
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
