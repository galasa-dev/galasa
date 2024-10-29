/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.internal.json.DockerContainerJSON;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.StandAloneHttpClient;

/**
 * Resource monitor for cleaning up orphaned containers.
 * 
 *   
 */
public class DockerContainerResourceMonitor implements Runnable {
    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IConfigurationPropertyStoreService cps;
    private final IDynamicStatusStoreService dss;

    private final Log logger = LogFactory.getLog(DockerContainerResourceMonitor.class);

    private GalasaGson gson = new GalasaGson();

    private Map<String,IHttpClient> dockerEngines = new HashMap<>();

    public DockerContainerResourceMonitor(IFramework framework, IResourceManagement resourceManagement, IConfigurationPropertyStoreService cps, IDynamicStatusStoreService dss) {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.cps = cps;
        this.dss = dss;

        logger.info("Docker container resource monitor intialised");
    }

    @Override
    public void run() {
        logger.info("Docker container resouce check started");

        updateDockerEngines();

        for (String engine : this.dockerEngines.keySet()) {
            List<String> containers = getOrphanedContainers(engine, this.dockerEngines.get(engine));
            logger.info("Engine " + engine + " has " + containers.size() + " orphaned containers found");
            if (containers.size() > 0) {
                killContainers(containers, this.dockerEngines.get(engine));
            }
        }
        logger.info("Docker container resource check finished");
    }

    /**
     * Stops and removed containers from an engines
     * @param containers
     * @param client
     */
    private void killContainers(List<String> containers, IHttpClient client) {
        logger.info("Shutting down orphaned containers");
        try {
            for (String id : containers) {
                HttpClientResponse<String> resp = client.deleteText("/containers/"+id+"?force=true");
                if (resp.getStatusCode() != 204) {
                    logger.error("Something went wrong when removing container: " + resp.getStatusLine());
                    return;
                }
            }
        } catch (HttpClientException e) {
            logger.error("Failed to kill containers.", e);
        }        
    }

    /**
     * Looks at all containers on a Engine, locates Galasa specifics and ensures they have a decicated slot
     * @param engine
     * @param client
     * @return
     */
    private List<String> getOrphanedContainers(String engine, IHttpClient client) {
        List<String> orphanedContainers = new ArrayList<>();
        try {
            HttpClientResponse<String> resp = client.getText("/containers/json?all=true");
            if (resp.getStatusCode() != 200) {
                logger.error("Something went wrong when retrieving containers: " + resp.getStatusLine());
                return orphanedContainers;
            }

            DockerContainerJSON[] activeContainers = gson.fromJson(resp.getContent(), DockerContainerJSON[].class);
            for (DockerContainerJSON container : activeContainers) {
                String runName = container.getLabels().getRunId();
                String slotId = container.getLabels().getSlotId();
                // Other non Galasa pod
                if (runName == null) {
                    continue;
                }

                // Check Slot name against runID. If Null or another run then container orphaned
                if (!runName.equals(dss.get("engine."+engine+".slot."+slotId))) {
                    orphanedContainers.add(container.getId());
                }
            }
        } catch (HttpClientException | FrameworkException e) {
            logger.error("Failed to get containers.", e);
        }
        return orphanedContainers;
    }

    /**
     * Looks at CPS for the docker Engines available
     * @return
     */
    private void updateDockerEngines() {
        try { 
            // This will have to be changed if we support engine clusters
            String[] enginesTags = cps.getProperty("default", "engines").split(",");
            for (String engine : enginesTags) {
                if (this.dockerEngines.get(engine) == null) {
                    String hostname = cps.getProperty("engine", "hostname", engine);
                    String port = cps.getProperty("engine", "port", engine);
    
                    IHttpClient client = StandAloneHttpClient.getHttpClient(3600, logger);
                    client.setURI(new URI(hostname+":"+port));
                    this.dockerEngines.put(engine, client);
                }
            }
        } catch (ConfigurationPropertyStoreException | URISyntaxException e) {
            logger.error("Failed to get Docker engines.", e);
        }
    }
    
}