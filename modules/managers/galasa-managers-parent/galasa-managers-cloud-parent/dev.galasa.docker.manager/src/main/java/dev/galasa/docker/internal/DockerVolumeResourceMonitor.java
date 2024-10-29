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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssDeletePrefix;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.StandAloneHttpClient;

public class DockerVolumeResourceMonitor implements Runnable {
    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final IConfigurationPropertyStoreService cps;
    private final Log logger = LogFactory.getLog(DockerVolumeResourceMonitor.class);

    private Map<String, IHttpClient> dockerEngines = new HashMap<>();

    private final Pattern slotRunPattern = Pattern.compile("^volume\\.(\\w+)\\.engine");

    public DockerVolumeResourceMonitor(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, DockerResourceManagement dockerResourceManagement,
            IConfigurationPropertyStoreService cps) {
        this.framework = framework;
        this.dss = dss;
        this.cps = cps;
        this.resourceManagement = resourceManagement;

        this.logger.info("Docker volume resource monitor intialised");
    }

    @Override
    public void run() {
        logger.info("Starting search for orphaned volumes.");
        try {
            updateDockerEngines();
        
            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            removeStaleProperties(activeRunNames);
            checkForStaleVolumes(activeRunNames);
        } catch (Exception e)  {
            logger.error("Volume monitor failed: ", e);
        }
        
        logger.info("Finished search for orphaned volumes.");
    }

    public void checkForStaleVolumes(Set<String> activeRunNames) {
        for (String engine : this.dockerEngines.keySet()) {
            List<String> volumes = getOrphanedVolumes(engine, this.dockerEngines.get(engine), activeRunNames);
            logger.info("Engine " + engine + " has " + volumes.size() + " orphaned volumes found");
            if (volumes.size() > 0) {
                killVolumes(volumes, this.dockerEngines.get(engine));
            }
        }
    }

    public List<String> getOrphanedVolumes(String engineName, IHttpClient client, Set<String> activeRunNames) {
        List<String> orphanedVolumes = new ArrayList<>();
        try {
            HttpClientResponse<JsonObject> resp = client.getJson("/volumes");
            JsonObject json = resp.getContent();
            if (resp.getStatusCode() != 200) {
                logger.error("Something went wrong when retrieving volumes: " + resp.getStatusLine());
                return orphanedVolumes;
            }
            JsonArray volumes = json.getAsJsonArray("Volumes");

            for (int i=0; i<=volumes.size(); i++) {
                JsonObject volJson = volumes.get(i).getAsJsonObject();
                if (volJson.get("Labels").isJsonNull()) {
                    continue;
                }
                JsonObject labels = volJson.get("Labels").getAsJsonObject();
                if (!(labels.get("GALASA") == null)) {
                    if (!activeRunNames.contains(labels.get("RUN_ID").getAsString())){
                        logger.info("Found orphaned volume: " + volJson.get("Name").getAsString());
                        orphanedVolumes.add(volJson.get("Name").getAsString());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to locate orphaned volumes");
        }
        return orphanedVolumes;
    }

    public void killVolumes(List<String> volumes, IHttpClient client) {
        try {
            for (String volume : volumes) {
                HttpClientResponse<String> resp = client.deleteText("/volumes/"+volume+"?force=true");
                if (resp.getStatusCode() != 204) {
                    logger.error("Something went wrong when removing volume (Likely the a container still mounted): " + resp.getStatusLine());
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete volumes");
        }
    }

    public void removeStaleProperties (Set<String> activeRunNames) {

        try{
            Map<String, String> volumeProps = dss.getPrefix("slot");
            

            for(String key : volumeProps.keySet()) {
                Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
                    String volumeName = matcher.group(0);
                    String runID = volumeProps.get("dss.docker.volume." + volumeName + "run");
                    if (activeRunNames.contains(runID)) {
                        break;
                    } else {
                        logger.info("Stale properties found, cleaning up propeties for volume: " + volumeName);
                        dss.performActions(new DssDeletePrefix("volume."+volumeName));
                    }
                } 
            }
            
        } catch (Exception e) {
            logger.error("Failed to clean DSS volume properties");
        }
    }

    /**
     * Looks at CPS for the docker Engines available
     * @return
     */
    private void updateDockerEngines() {
        try { 
            // This will have to be changed if we support engine clusters
        	String[] tags;
            String enginesTags = cps.getProperty("default", "engines");
            if (enginesTags == null) {
            	logger.info("No default Docker engines defined, moving on");
            	return;
            }
            tags = enginesTags.split(",");
            for (String engine : tags) {
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