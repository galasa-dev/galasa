/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsKeyStore;
import dev.galasa.docker.internal.json.DockerContainerJSON;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.creds.CredentialsException;
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
    private final IConfigurationPropertyStoreService cps;
    private final IDynamicStatusStoreService dss;

    private final Log logger = LogFactory.getLog(DockerContainerResourceMonitor.class);

    private GalasaGson gson = new GalasaGson();

    private Map<String,IHttpClient> dockerEngines = new HashMap<>();

    public DockerContainerResourceMonitor(IFramework framework, IResourceManagement resourceManagement, IConfigurationPropertyStoreService cps, IDynamicStatusStoreService dss) {
        this.cps = cps;
        this.dss = dss;
        this.framework = framework;

        logger.info("Docker container resource monitor intialised");
    }

    @Override
    public void run() {
        logger.info("Docker container resource check started");

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
                    String credentialsId = null;

                    // Try to get credentials ID
                    try {
                        credentialsId = cps.getProperty("engine", "credentials.id", engine);
                    } catch (ConfigurationPropertyStoreException e) {
                        logger.debug("No credentials configured for engine " + engine);
                    }

                    // Check if hostname already has a scheme
                    if (!hostname.startsWith("http://") && !hostname.startsWith("https://")) {
                        // If no scheme, determine based on credentials configuration
                        String scheme = credentialsId != null ? "https://" : "http://";
                        hostname = scheme + hostname;
                    }

                    IHttpClient client = StandAloneHttpClient.getHttpClient(3600, logger);

                    // Configure SSL if credentials are provided
                    if (credentialsId != null) {
                        configureClientSsl(client, credentialsId);
                    }

                    client.setURI(new URI(hostname + ":" + port));
                    this.dockerEngines.put(engine, client);
                }
            }
        } catch (ConfigurationPropertyStoreException | URISyntaxException e) {
            logger.error("Failed to get Docker engines.", e);
        }
    }

    /**
     * Configure the HTTP client with SSL context using the provided KeyStore credentials.
     *
     * @param client the HTTP client to configure
     * @param credentialsId the ID of the credentials containing the KeyStore
     */
    private void configureClientSsl(IHttpClient client, String credentialsId) {
        try {
            ICredentials credentials = framework.getCredentialsService().getCredentials(credentialsId);

            if (credentials == null) {
                logger.error("Credentials '" + credentialsId + "' not found in Credentials Store for Docker engine");
                return;
            }

            if (!(credentials instanceof ICredentialsKeyStore)) {
                logger.error("Credentials '" + credentialsId + "' must be of type KeyStore for Docker HTTPS. " +
                    "Found type: " + credentials.getClass().getSimpleName());
                return;
            }

            ICredentialsKeyStore keyStoreCreds = (ICredentialsKeyStore) credentials;
            KeyStore keyStore = keyStoreCreds.getKeyStore();
            String password = keyStoreCreds.getKeyStorePassword();

            // Create KeyManagerFactory for client authentication
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password.toCharArray());

            // Create TrustManagerFactory for server certificate validation
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            // Configure HTTP client with SSL context
            client.setupClientAuth(keyStore, password);

            logger.debug("SSL context configured for Docker engine with credentials: " + credentialsId);

        } catch (CredentialsException | HttpClientException e) {
            logger.error("Failed to configure SSL context for Docker engine", e);
        } catch (Exception e) {
            logger.error("Failed to setup client SSL authentication for Docker engine", e);
        }
    }

}