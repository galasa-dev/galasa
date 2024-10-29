/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.api.run.Run;
import dev.galasa.api.runs.ScheduleStatus;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.galasaecosystem.EcosystemEndpoint;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IKubernetesEcosystem;
import dev.galasa.galasaecosystem.internal.properties.DockerRegistry;
import dev.galasa.galasaecosystem.internal.properties.DockerVersion;
import dev.galasa.galasaecosystem.internal.properties.RuntimeRepo;
import dev.galasa.galasaecosystem.internal.properties.RuntimeVersion;
import dev.galasa.galasaecosystem.internal.properties.SimplatformDockerVersion;
import dev.galasa.galasaecosystem.internal.properties.SimplatformRepo;
import dev.galasa.galasaecosystem.internal.properties.SimplatformVersion;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.kubernetes.IConfigMap;
import dev.galasa.kubernetes.IDeployment;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.IPersistentVolumeClaim;
import dev.galasa.kubernetes.IPodLog;
import dev.galasa.kubernetes.IReplicaSet;
import dev.galasa.kubernetes.IResource;
import dev.galasa.kubernetes.IService;
import dev.galasa.kubernetes.IStatefulSet;
import dev.galasa.kubernetes.KubernetesManagerException;

/**
 * Deploy the Ecsosystem into a Kubernetes Namespace
 * 
 *  
 *
 */
public class KubernetesEcosystemImpl extends AbstractEcosystemImpl implements IKubernetesEcosystem {

    private final Log                        logger = LogFactory.getLog(getClass());

    private final IKubernetesNamespace       namespace;

    private final HashMap<ResourceType, Resource> resources = new HashMap<>();

    private final Yaml                       yaml = new Yaml();

    private final GalasaGson                 gson = new GalasaGson();

    private String                           dockerVersion;
    private String                           dockerRegistry;
    private String                           simplatformDockerVersion;
    private String                           mavenVersion;
    private URL                              mavenRepository;
    private String                           simplatformVersion;
    private URL                              simplatformRepository;
    private final HashMap<String, String>    yamlReplacements = new HashMap<>();

    private IHttpClient                      etcdHttpClient;
    private IHttpClient                      apiHttpClient;

    private URL                              cpsUrl;
    private URL                              dssUrl;
    private URL                              rasUrl;
    private URL                              credsUrl;
    private URL                              apiUrl;
    private URI                              cpsUri;
    private URI                              dssUri;
    private URI                              rasUri;
    private URI                              credsUri;

    private URL                              metricsMetricsUrl;
    private URL                              metricsHealthUrl;
    private URL                              resmonMetricsUrl;
    private URL                              resmonHealthUrl;
    private URL                              engineMetricsUrl;
    private URL                              engineHealthUrl;
    private URL                              prometheusUrl;
    private URL                              grafanaUrl;
    private InetSocketAddress                simbankTelnetPort;
    private URL                              simbankWebUrl;
    private InetSocketAddress                simbankDatabasePort;
    private URL                              simbankManagementFacilityUrl;

    private RunIdPrefixImpl                  runIdPrefix;
    
    private IFramework                       framework;

    public KubernetesEcosystemImpl(GalasaEcosystemManagerImpl manager, String tag, IKubernetesNamespace namespace) {
        super(manager, tag, null, null);
        this.namespace = namespace;
        this.framework = manager.getFramework();
    }

    protected void reserveRunIdPrefix() throws InsufficientResourcesAvailableException, GalasaEcosystemManagerException {
        this.runIdPrefix = new RunIdPrefixImpl(getEcosystemManager().getFramework(), getEcosystemManager().getDss());
    }

    /**
     * Load the YAML resources from this bundle and the test bundle
     * 
     * @throws GalasaEcosystemManagerException
     */
    protected void loadYamlResources() throws GalasaEcosystemManagerException {

        this.mavenVersion = RuntimeVersion.get();
        this.mavenRepository = RuntimeRepo.get();
        this.dockerVersion = DockerVersion.get();
        this.dockerRegistry = DockerRegistry.get();
        this.simplatformDockerVersion = SimplatformDockerVersion.get();
        this.simplatformVersion = SimplatformVersion.get();
        this.simplatformRepository = SimplatformRepo.get();

        ArrayList<Map<String, Object>>   managerYaml = new ArrayList<>();
        ArrayList<Map<String, Object>>   testYaml = new ArrayList<>();

        //*** Setup the blanket replacements
        yamlReplacements.put("${dockerVersion}", dockerVersion);
        yamlReplacements.put("${dockerRegistry}", dockerRegistry);
        yamlReplacements.put("${simplatformDockerVersion}", simplatformDockerVersion);

        //*** Load all the yaml files ready for searching and processing
        try {
            IArtifactManager artifactManager = getEcosystemManager().getArtifactManager();
            IBundleResources managerBundleResources = artifactManager.getBundleResources(getClass());
            Map<String, InputStream> directoryContents = managerBundleResources.retrieveDirectoryContents("/k8s");

            for(Entry<String, InputStream> entry : directoryContents.entrySet()) {
                String filename = entry.getKey();
                InputStream is = entry.getValue();

                if (filename.endsWith(".yaml")) {
                    loadYaml(yaml, is, managerYaml);
                }

                is.close();
            }
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem loading the YAML for the Kubernetes resources from the manager bundle", e);
        }

        //*** TODO Load yaml from the test class

        //*** Search for all the resources we need to create the ecosystem
        for(ResourceType type : ResourceType.values()) {
            locateYaml(managerYaml, testYaml, type);
        }            

        logger.debug("All YAML Kubernetes resources loaded");
    }


    /**
     * Retrieve the YAML for a resource type
     * 
     * @param managerYamls the yamls from this bundle
     * @param testYamls the yamls from the test bundle
     * @param type the resource type
     * @throws GalasaEcosystemManagerException if a resource type cannot be found in either bundle
     */
    private void locateYaml(ArrayList<Map<String, Object>> managerYamls,
            ArrayList<Map<String, Object>> testYamls, 
            ResourceType type) throws GalasaEcosystemManagerException {

        Resource resource = locateYaml(testYamls, type);
        if (resource == null) {
            resource = locateYaml(managerYamls, type);
        }

        if (resource == null) {
            throw new GalasaEcosystemManagerException("Unable to locate YAML for resource " + type.toString());
        }


        this.resources.put(type, resource);
    }

    /**
     * Locate a yaml in a bundle
     * 
     * @param yamls The bundle yamls to search
     * @param type the resource type
     * @return the yaml or null if not found
     */
    private Resource locateYaml(ArrayList<Map<String, Object>> yamls, ResourceType type) {
        for(Map<String, Object> yaml : yamls) {
            String kind = getProperty(yaml, "kind");
            String name = getProperty(yaml, "metadata.name");

            if (type.getType().equals(kind) && type.getName().equals(name)) {
                return new Resource(type, yaml);
            }
        }
        return null;
    }

    /**
     * Load all YAMLs from an inputstream and perform basic string replacement
     * 
     * @param yaml
     * @param is
     * @param list
     * @throws GalasaEcosystemManagerException
     */
    @SuppressWarnings("unchecked")
    private void loadYaml(Yaml yaml, InputStream is, List<Map<String, Object>> list) throws GalasaEcosystemManagerException {
        try {
            //*** Load the yaml
            String yamlFile = IOUtils.toString(is, StandardCharsets.UTF_8);
            //*** Perform string replacements, eg docker version
            for(Entry<String, String> entry : yamlReplacements.entrySet()) {
                yamlFile = yamlFile.replace(entry.getKey(), entry.getValue());
            }

            //*** convert to YAML objects and then load the contents
            for(Object o : yaml.loadAll(yamlFile) ) {
                if (o instanceof Map) {
                    list.add((Map<String, Object>) o);
                }
            }
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem processing YAML", e);
        }
    }


    public void build() throws GalasaEcosystemManagerException {
        logger.info("Starting the build of Galasa Ecosystem " + getTag() + " Kubernetes namespace " + this.namespace.getFullId());

        Instant buildStart = Instant.now();
        try {
            //*** Build all the external services, need these for some configmaps
            logger.info("Building external services Kubernetes resources");
            build(ResourceType.CPS_EXTERNAL_SERVICE);
            build(ResourceType.RAS_EXTERNAL_SERVICE);
            build(ResourceType.API_EXTERNAL_SERVICE);
            build(ResourceType.METRICS_EXTERNAL_SERVICE);
            build(ResourceType.METRICS_HEALTH_SERVICE);
            build(ResourceType.RESMON_EXTERNAL_SERVICE);
            build(ResourceType.ENGINE_EXTERNAL_SERVICE);
            build(ResourceType.PROMETHEUS_EXTERNAL_SERVICE);
            build(ResourceType.GRAFANA_EXTERNAL_SERVICE);
            build(ResourceType.SIMBANK_TELNET_SERVICE);
            build(ResourceType.SIMBANK_WEBSERVICE_SERVICE);
            build(ResourceType.SIMBANK_DATABASE_SERVICE);
            build(ResourceType.SIMBANK_MANAGEMENT_FACILITY_SERVICE);

            //*** Generate the known URLs
            generateKnownUrls();
            modifyBootstrapConfigMap();

            //*** Build all the internal services
            logger.info("Building external services Kubernetes resources");
            build(ResourceType.CPS_INTERNAL_SERVICE);
            build(ResourceType.RAS_INTERNAL_SERVICE);
            build(ResourceType.API_INTERNAL_SERVICE);
            build(ResourceType.METRICS_INTERNAL_SERVICE);
            build(ResourceType.RESMON_INTERNAL_SERVICE);
            build(ResourceType.ENGINE_INTERNAL_SERVICE);
            build(ResourceType.PROMETHEUS_INTERNAL_SERVICE);
            build(ResourceType.GRAFANA_INTERNAL_SERVICE);

            //*** Create all the Persistent Volume Claims
            logger.info("Building Persistent Volume Claim Kubernetes resources");
            build(ResourceType.API_PVC); //*** TODO not needed until we reinstate the test catalog
            build(ResourceType.PROMETHEUS_PVC);
            build(ResourceType.GRAFANA_PVC);

            //*** Build the config maps.   
            logger.info("Building Config Maps Kubernetes resources");
            build(ResourceType.CONFIG_CONFIGMAP);     
            build(ResourceType.BOOTSTRAP_CONFIGMAP);     
            build(ResourceType.TESTCATALOG_CONFIGMAP);     
            build(ResourceType.PROMETHEUS_CONFIGMAP);     
            build(ResourceType.GRAFANA_CONFIGMAP);     
            build(ResourceType.GRAFANA_DASHBOARD_CONFIGMAP);     
            build(ResourceType.GRAFANA_AUTODASHBOARD_CONFIGMAP);     
            build(ResourceType.GRAFANA_PROVISIONING_CONFIGMAP);    

            //*** Starting Prometheus and Grafana early as they take a while and doesn't need the CPS
            logger.info("Building Prometheus and Grafana early, as doesn't need the CPS");
            build(ResourceType.PROMETHEUS_DEPLOYMENT);
            build(ResourceType.GRAFANA_DEPLOYMENT);
            build(ResourceType.SIMBANK_DEPLOYMENT);

            //*** Create all the StatefulSets
            logger.info("Building Stateful Set Kubernetes resources");
            build(ResourceType.CPS_STATEFULSET);
            build(ResourceType.RAS_STATEFULSET);

            //*** Wait for the CPS to have completed startup
            logger.info("Waiting for the CPS and RAS to have completed startup");
            waitForMessageInAllPodLogs(ResourceType.CPS_STATEFULSET, "etcd", "serving insecure client requests on [::]:2379", 180);
            waitForMessageInAllPodLogs(ResourceType.RAS_STATEFULSET, "couchdb", "Apache CouchDB has started on http://any:5986/", 180);

            //*** Set the RAS, CREDS properties in the CPS
            logger.info("Setting up initial properties in the CPS");
            storeCpsProperty("framework.dynamicstatus.store", this.dssUri.toString());
            storeCpsProperty("framework.resultarchive.store", this.rasUri.toString());
            storeCpsProperty("framework.credentials.store", this.credsUri.toString());

            //*** Create the API and bootstrap servers
            logger.info("Starting the API server for the bootstrap service");
            build(ResourceType.API_DEPLOYMENT);
            waitForBootstrapPort();

            //*** Start the remaining Deployments
            logger.info("Starting the remaining Deployments");
            build(ResourceType.METRICS_DEPLOYMENT);
            build(ResourceType.RESMON_DEPLOYMENT);
            build(ResourceType.ENGINE_DEPLOYMENT);

            logger.info("Waiting for all the remaining services to start");
            waitForMessageInAllPodLogs(ResourceType.RESMON_DEPLOYMENT, "resource-monitor", "ResourceManagement.run - Resource Manager has started", 180);
            waitForMessageInAllPodLogs(ResourceType.ENGINE_DEPLOYMENT, "engine-controller", "K8sController.run - Kubernetes controller has started", 180);
            waitForMessageInAllPodLogs(ResourceType.METRICS_DEPLOYMENT, "metrics", "MetricsServer.run - Metrics Server has started", 180);
            waitForMessageInAllPodLogs(ResourceType.PROMETHEUS_DEPLOYMENT, "prometheus", "Server is ready to receive web requests", 180);
            waitForMessageInAllPodLogs(ResourceType.GRAFANA_DEPLOYMENT, "grafana", "msg=\"HTTP Server Listen\" logger=http.server address=[::]:3000", 180);
            waitForMessageInAllPodLogs(ResourceType.SIMBANK_DEPLOYMENT, "simbank", "Simplatform main ... Simplatform started", 180);

            Instant buildEnd = Instant.now();
            long seconds = buildEnd.getEpochSecond() - buildStart.getEpochSecond();

            saveEcosystemInDss();

            //*** Set up fast cleanup
            storeCpsProperty("framework.resource.management.dead.heartbeat.timeout", "30");
            storeCpsProperty("framework.resource.management.finished.timeout", "40");

            //*** Set up streams
            storeCpsProperty("framework.stream.simbank.obr", "mvn:dev.galasa/dev.galasa.simbank.obr/" + this.simplatformVersion + "/obr");
            storeCpsProperty("framework.stream.simbank.repo", this.simplatformRepository.toString());  

            //*** Set up SimBank
            storeCpsProperty("secure.credentials.SIMBANK.username", "IBMUSER");
            storeCpsProperty("secure.credentials.SIMBANK.password", "SYS1");

            storeCpsProperty("zos.dse.tag.SIMBANK.imageid", "SIMBANK");
            storeCpsProperty("zos.dse.tag.SIMBANK.clusterid", "SIMBANK");
            storeCpsProperty("zos.image.SIMBANK.ipv4.hostname", this.simbankTelnetPort.getHostString());
            storeCpsProperty("zos.image.SIMBANK.telnet.port", Integer.toString(this.simbankTelnetPort.getPort()));
            storeCpsProperty("zos.image.SIMBANK.telnet.tls", "false");
            storeCpsProperty("zos.image.SIMBANK.credentials", "SIMBANK");

            storeCpsProperty("zosmf.image.SIMBANK.servers", "MFSIMBANK");
            storeCpsProperty("zosmf.server.MFSIMBANK.port", Integer.toString(this.simbankManagementFacilityUrl.getPort()));
            storeCpsProperty("zosmf.server.MFSIMBANK.https", "false");
            storeCpsProperty("zosmf.server.MFSIMBANK.image", "SIMBANK");

            storeCpsProperty("simbank.dse.instance.name","SIMBANK");
            storeCpsProperty("simbank.instance.SIMBANK.zos.image","SIMBANK");
            storeCpsProperty("simbank.instance.SIMBANK.database.port", Integer.toString(this.simbankDatabasePort.getPort()));
            storeCpsProperty("simbank.instance.SIMBANK.webnet.port", Integer.toString(this.simbankWebUrl.getPort()));
            insertLastRunIDIntoDSS();

            logger.info("Kubernetes Ecosystem successfully built on " + this.namespace.getFullId() + " in " + seconds + " seconds");

            logger.info("--------------------------------------------------------------------------------------------");
            logger.info("Bootstrap URL = " + this.apiUrl.toString() + "/bootstrap");
            logger.info("CPS URI       = " + this.cpsUri.toString());
            logger.info("DSS URI       = " + this.dssUri.toString());
            logger.info("RAS URI       = " + this.rasUri.toString());
            logger.info("CREDS URI     = " + this.credsUri.toString());
            logger.info("API URL       = " + this.apiUrl.toString());
            logger.info("");
            logger.info("Resource Monitor Metrics URL    = " + this.resmonMetricsUrl);
            logger.info("Resource Monitor Health URL     = " + this.resmonHealthUrl);
            logger.info("Metrics Metrics URL             = " + this.metricsMetricsUrl);
            logger.info("Metrics Health Health URL       = " + this.metricsHealthUrl);
            logger.info("Engine Controller Metrics URL   = " + this.engineMetricsUrl);
            logger.info("Engine Controller Health URL    = " + this.engineHealthUrl);
            logger.info("Prometheus URL                  = " + this.prometheusUrl);
            logger.info("Grafana URL                     = " + this.grafanaUrl);
            logger.info("Simbank Telnet Port             = " + this.simbankTelnetPort);
            logger.info("Simbank Webservice URL          = " + this.simbankWebUrl);
            logger.info("Simbank Database Port           = " + this.simbankDatabasePort);
            logger.info("Simbank Management Facility URL = " + this.simbankManagementFacilityUrl);
            logger.info("--------------------------------------------------------------------------------------------");
        } catch(GalasaEcosystemManagerException e) {
            try {
                this.namespace.saveNamespaceConfiguration();
                throw e;
            } catch (KubernetesManagerException e1) {
                logger.error("Failed to save the Kubernetes namespace configuration for Galasa Ecosystem " + getTag());
                throw e;
            }
        }
    }

    private void waitForBootstrapPort() throws GalasaEcosystemManagerException {
        logger.debug("Waiting for the bootstrap server to start at " + this.apiUrl.toString() + "/bootstrap");
        try {
            Instant timeout = Instant.now().plusSeconds(180);
            Instant checkMessage = Instant.now().plusSeconds(30);

            IHttpClient client = getApiHttpClient();

            while(timeout.isAfter(Instant.now())) {

                try {
                    HttpClientResponse<String> response = client.getText("/bootstrap");

                    String text = response.getContent();

                    if (text != null && text.contains("framework.config.store=")) {
                        logger.debug("Bootstrap server has started");
                        return;
                    }
                } catch(HttpClientException e) { } //   ignore http errors

                if (checkMessage.isBefore(Instant.now())) {
                    logger.debug("Still waiting for bootstrap to start");
                    checkMessage = Instant.now().plusSeconds(30);
                }

                Thread.sleep(2000);
            }

            throw new GalasaEcosystemManagerException("The bootstrap server did not start in time");

        } catch(InterruptedException | KubernetesManagerException e) {
            throw new GalasaEcosystemManagerException("Problem waiting for the bootstrap to become availabe", e);
        }

    }

    private void modifyBootstrapConfigMap() {
        Resource bootstrapResource = this.resources.get(ResourceType.BOOTSTRAP_CONFIGMAP);
        Map<String, Object> yaml = bootstrapResource.getYaml();
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) yaml.get("data");
        String bootstrap = (String) data.get("bootstrap.properties");
        bootstrap = bootstrap.replace("${cpsURI}", this.cpsUri.toString());
        data.put("bootstrap.properties", bootstrap);  
    }

    private void generateKnownUrls() throws GalasaEcosystemManagerException {
        try {
            this.cpsUrl = getHttpUrl(ResourceType.CPS_EXTERNAL_SERVICE, 2379);
            this.credsUrl = this.cpsUrl;
            this.dssUrl = this.cpsUrl;
            this.cpsUri = new URI("etcd:" + this.cpsUrl.toString());
            this.dssUri = new URI("etcd:" + this.dssUrl.toString());
            this.credsUri = new URI("etcd:" + this.credsUrl.toString());

            this.rasUrl = getHttpUrl(ResourceType.RAS_EXTERNAL_SERVICE, 5984);
            this.rasUri = new URI("couchdb:" + this.rasUrl.toString());

            this.apiUrl = getHttpUrl(ResourceType.API_EXTERNAL_SERVICE, 8080);

            this.metricsMetricsUrl = getHttpUrl(ResourceType.METRICS_EXTERNAL_SERVICE, 9010);
            this.metricsHealthUrl = getHttpUrl(ResourceType.METRICS_HEALTH_SERVICE, 9011);

            this.resmonMetricsUrl = getHttpUrl(ResourceType.RESMON_EXTERNAL_SERVICE, 9010);
            this.resmonHealthUrl = getHttpUrl(ResourceType.RESMON_EXTERNAL_SERVICE, 9011);

            this.engineMetricsUrl = getHttpUrl(ResourceType.ENGINE_EXTERNAL_SERVICE, 9010);
            this.engineHealthUrl = getHttpUrl(ResourceType.ENGINE_EXTERNAL_SERVICE, 9011);

            this.prometheusUrl = getHttpUrl(ResourceType.PROMETHEUS_EXTERNAL_SERVICE, 9090);
            this.grafanaUrl = getHttpUrl(ResourceType.GRAFANA_EXTERNAL_SERVICE, 3000);

            this.simbankTelnetPort = getPort(ResourceType.SIMBANK_TELNET_SERVICE, 2023);
            this.simbankWebUrl = getHttpUrl(ResourceType.SIMBANK_WEBSERVICE_SERVICE, 2080);
            this.simbankDatabasePort = getPort(ResourceType.SIMBANK_DATABASE_SERVICE, 2027);
            this.simbankManagementFacilityUrl = getHttpUrl(ResourceType.SIMBANK_MANAGEMENT_FACILITY_SERVICE, 2040);
        } catch (KubernetesManagerException | MalformedURLException | URISyntaxException e) {
            throw new GalasaEcosystemManagerException("Problem generating the default URLs", e);
        }
    }

    private URL getHttpUrl(ResourceType type, int port) throws KubernetesManagerException, MalformedURLException {
        IService service = (IService) this.resources.get(type).getK8sResource();
        InetSocketAddress socketAddress = service.getSocketAddressForPort(port);
        return new URL("http://" + socketAddress.getHostString() + ":" + Integer.toString(socketAddress.getPort()));
    }

    private InetSocketAddress getPort(ResourceType type, int port) throws KubernetesManagerException, MalformedURLException {
        IService service = (IService) this.resources.get(type).getK8sResource();
        return service.getSocketAddressForPort(port);
    }

    private void storeCpsProperty(@NotNull String key, @NotNull String value) throws GalasaEcosystemManagerException {
        try {

            Encoder encoder = Base64.getEncoder();
            IHttpClient httpClient = getEtcdHttpClient();

            Instant timeout = Instant.now().plusSeconds(180);
            Instant waitMessage = Instant.now().plusSeconds(30);
            Exception lastException = null;
            while(timeout.isAfter(Instant.now())) {

                JsonObject dssJson = new JsonObject();
                dssJson.addProperty("key", new String(encoder.encode(key.getBytes())));
                dssJson.addProperty("value", new String(encoder.encode(value.getBytes())));

                try {
                    httpClient.postJson("/v3/kv/put", dssJson);
                    logger.info("Set CPS key '" + key + "' to '" + value + "'");
                    return;
                } catch(HttpClientException e) {
                    lastException = e;
                }
                
                if (waitMessage.isBefore(Instant.now())) {
                    logger.debug("Waiting for detcd port to open");
                    waitMessage = Instant.now().plusSeconds(30);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new GalasaEcosystemManagerException("wait for etcd port interrupted",e);
                }
            }
            
            throw new GalasaEcosystemManagerException("The etcd3 http port did not open in time, last exception was:-",lastException);

        } catch (KubernetesManagerException e) {
            throw new GalasaEcosystemManagerException("Problem setting CPS property " + key + "=" + value, e);
        }
    }

    private String retrieveCpsProperty(@NotNull String key) throws GalasaEcosystemManagerException {
        try {
            Encoder encoder = Base64.getEncoder();
            Decoder decoder = Base64.getDecoder();
            IHttpClient httpClient = getEtcdHttpClient();

            JsonObject dssJson = new JsonObject();
            dssJson.addProperty("key", new String(encoder.encode(key.getBytes())));

            HttpClientResponse<JsonObject> response = httpClient.postJson("/v3/kv/range", dssJson);
            JsonArray kvs = response.getContent().getAsJsonArray("kvs");
            if (kvs != null && kvs.size() == 1) {
                String encoded = kvs.get(0).getAsJsonObject().get("value").getAsString();
                return new String(decoder.decode(encoded));
            }

            return null;
        } catch (KubernetesManagerException | HttpClientException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving CPS property " + key, e);
        }
    }

    private void deleteCpsProperty(@NotNull String key) throws GalasaEcosystemManagerException {
        try {
            Encoder encoder = Base64.getEncoder();
            IHttpClient httpClient = getEtcdHttpClient();

            JsonObject dssJson = new JsonObject();
            dssJson.addProperty("key", new String(encoder.encode(key.getBytes())));

            httpClient.postJson("/v3/kv/deleterange", dssJson);
        } catch (KubernetesManagerException | HttpClientException e) {
            throw new GalasaEcosystemManagerException("Problem deleting CPS property " + key, e);
        }
    }

    private synchronized IHttpClient getEtcdHttpClient() throws KubernetesManagerException, GalasaEcosystemManagerException {
        if (this.etcdHttpClient != null) {
            return this.etcdHttpClient;
        }

        try {
            this.etcdHttpClient = getEcosystemManager().getHttpManager().newHttpClient(30000);
            this.etcdHttpClient.setURI(this.cpsUrl.toURI());
        } catch (URISyntaxException e) {
            throw new KubernetesManagerException("Problem creating the HTTP Client", e);
        }

        return this.etcdHttpClient;
    }

    private synchronized IHttpClient getApiHttpClient() throws KubernetesManagerException, GalasaEcosystemManagerException {
        if (this.apiHttpClient != null) {
            return this.apiHttpClient;
        }

        try {
            this.apiHttpClient = getEcosystemManager().getHttpManager().newHttpClient(30000);
            this.apiHttpClient.setURI(this.apiUrl.toURI());
        } catch (URISyntaxException e) {
            throw new KubernetesManagerException("Problem creating the HTTP Client", e);
        }

        return this.apiHttpClient;
    }

    private void waitForMessageInAllPodLogs(ResourceType resourceType, String container, String message, long timeoutInSeconds) throws GalasaEcosystemManagerException {

        try {
            Resource resource = this.resources.get(resourceType);
            logger.debug("Looking for message '" + message + "' in all pods of " + resource.toString() + " in container " + container);

            IResource k8sResource = resource.getK8sResource();
            if (!(k8sResource instanceof IReplicaSet)) {
                throw new GalasaEcosystemManagerException("Tried to access logs on a none ReplicaSet resource - " + resource.toString());
            }

            IReplicaSet podHolder = (IReplicaSet)k8sResource;

            Instant timeout = Instant.now().plusSeconds(timeoutInSeconds);
            Instant checkMessage = Instant.now().plusSeconds(30);

            while(timeout.isAfter(Instant.now())) {
                List<IPodLog> podLogs = podHolder.getPodLogs(container);

                boolean found = true;                
                for(IPodLog podLog : podLogs) {
                    String log = podLog.getLog();

                    if (log == null || !log.contains(message)) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    logger.debug("Found message in all the pods");
                    return;
                }

                if (checkMessage.isBefore(Instant.now())) {
                    logger.debug("Still waiting for message '" + message + "'");
                    checkMessage = Instant.now().plusSeconds(30);
                }

                Thread.sleep(2000);
            }

            throw new GalasaEcosystemManagerException("Did not find message in log within timeout");
        } catch(InterruptedException e) {
            throw new GalasaEcosystemManagerException("Wait for log message interrupted", e);
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem waiting for log message", e);
        }

    }

    private void build(ResourceType resourceType) throws GalasaEcosystemManagerException {
        Resource resource = this.resources.get(resourceType);

        String yamlText = yaml.dump(resource.getYaml());

        try {
            IResource k8sResource = this.namespace.createResource(yamlText);

            resource.setK8sresource(k8sResource);
        } catch (KubernetesManagerException e) {
            throw new GalasaEcosystemManagerException("Unable to create resource " + resourceType.toString() + " in Kubernetes", e);
        }        
    }

    public void stop() {
        try {
            this.namespace.saveNamespaceConfiguration();
        } catch (KubernetesManagerException e1) {
            logger.error("Failed to save the Kubernetes namespace configuration for Galasa Ecosystem " + getTag());
        }
    }

    public void discard() {
        // Do not discard, we will leave this for the Kubernetes Manager to clean up
        logger.debug("Not discarding Galasa Ecosystem " + getTag() + ", leaving for the Kubernetes Manager to do it");

        //*** if run id prefix is present,  remove it
        if (this.runIdPrefix != null) {
            try {
                this.runIdPrefix.discard();
            } catch (GalasaEcosystemManagerException e) {
                logger.error("Unable to discard the run id prefix",e);
            }
        }

        //*** But clean up the DSS
        String runName = getEcosystemManager().getFramework().getTestRunName();
        String prefix = "run." + runName + ".kubernetes.ecosystem." + getTag() + ".";

        try {
            getEcosystemManager().getDss().deletePrefix(prefix);
        } catch(Exception e) {
            logger.error("Unable to discard the ecosystem",e);
        }       



    }

    private static String getProperty(Map<String, Object> yaml, String name) {
        String[] nameParts = name.split("\\.");

        Object currentObject = yaml;
        for(String namePart : nameParts) {
            if (currentObject instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> currentMap = (Map<String, Object>) currentObject;

                currentObject = currentMap.get(namePart);

                if (currentObject == null) {
                    return null;
                }
            } else {
                return null;
            }
        }

        if (currentObject == null) {
            return null;
        }

        if (currentObject instanceof String) {
            return (String)currentObject;
        }

        return null;
    }


    private enum ResourceType {

        CONFIG_CONFIGMAP("ConfigMap", "config", IConfigMap.class),

        CPS_EXTERNAL_SERVICE("Service", "cps-external", IService.class),
        CPS_STATEFULSET("StatefulSet", "cps", IStatefulSet.class),
        CPS_INTERNAL_SERVICE("Service", "cps", IService.class),

        RAS_EXTERNAL_SERVICE("Service", "ras-external", IService.class),
        RAS_INTERNAL_SERVICE("Service", "ras", IService.class),
        RAS_STATEFULSET("StatefulSet", "ras", IStatefulSet.class),

        BOOTSTRAP_CONFIGMAP("ConfigMap", "bootstrap-file", IConfigMap.class),
        TESTCATALOG_CONFIGMAP("ConfigMap", "testcatalog-file", IConfigMap.class),
        API_PVC("PersistentVolumeClaim", "pvc-api", IPersistentVolumeClaim.class),
        API_EXTERNAL_SERVICE("Service", "api-external", IService.class),
        API_INTERNAL_SERVICE("Service", "api", IService.class),
        API_DEPLOYMENT("Deployment","api", IDeployment.class),

        PROMETHEUS_EXTERNAL_SERVICE("Service", "prometheus-external", IService.class),
        PROMETHEUS_INTERNAL_SERVICE("Service", "prometheus", IService.class),
        PROMETHEUS_CONFIGMAP("ConfigMap", "prometheus-config", IConfigMap.class),
        PROMETHEUS_PVC("PersistentVolumeClaim", "pvc-prometheus", IPersistentVolumeClaim.class),
        PROMETHEUS_DEPLOYMENT("Deployment","prometheus", IDeployment.class),

        GRAFANA_EXTERNAL_SERVICE("Service", "grafana-external", IService.class),
        GRAFANA_INTERNAL_SERVICE("Service", "grafana", IService.class),
        GRAFANA_CONFIGMAP("ConfigMap", "grafana-config", IConfigMap.class),
        GRAFANA_PROVISIONING_CONFIGMAP("ConfigMap", "grafana-provisioning", IConfigMap.class),
        GRAFANA_DASHBOARD_CONFIGMAP("ConfigMap", "grafana-dashboard", IConfigMap.class),
        GRAFANA_AUTODASHBOARD_CONFIGMAP("ConfigMap", "grafana-auto-dashboard", IConfigMap.class),
        GRAFANA_PVC("PersistentVolumeClaim", "pvc-grafana", IPersistentVolumeClaim.class),
        GRAFANA_DEPLOYMENT("Deployment","grafana", IDeployment.class),

        METRICS_EXTERNAL_SERVICE("Service", "metrics-external", IService.class),
        METRICS_HEALTH_SERVICE("Service", "metrics-health-external", IService.class),
        METRICS_INTERNAL_SERVICE("Service", "metrics", IService.class),
        METRICS_DEPLOYMENT("Deployment", "metrics", IDeployment.class),

        RESMON_EXTERNAL_SERVICE("Service", "resource-monitor-external", IService.class),
        RESMON_INTERNAL_SERVICE("Service", "resource-monitor", IService.class),
        RESMON_DEPLOYMENT("Deployment", "resource-monitor", IDeployment.class),

        ENGINE_EXTERNAL_SERVICE("Service", "engine-controller-external", IService.class),
        ENGINE_INTERNAL_SERVICE("Service", "engine-controller", IService.class),
        ENGINE_DEPLOYMENT("Deployment", "engine-controller", IDeployment.class),

        SIMBANK_TELNET_SERVICE("Service", "simbank-telnet-external", IService.class),
        SIMBANK_WEBSERVICE_SERVICE("Service", "simbank-webservice-external", IService.class),
        SIMBANK_DATABASE_SERVICE("Service", "simbank-database-external", IService.class),
        SIMBANK_MANAGEMENT_FACILITY_SERVICE("Service", "simbank-mf-external", IService.class),
        SIMBANK_DEPLOYMENT("Deployment", "simbank", IDeployment.class);

        private final String type;
        private final String name;

        private ResourceType(String type, String name, Class<? extends IResource> k8sResourceType) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return this.type;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return type + "/" + name;
        }
    }


    private static class Resource {
        private final ResourceType        type;
        private final Map<String, Object> yaml;
        private IResource                 k8sResource;

        private Resource(@NotNull ResourceType type, @NotNull Map<String, Object> yaml) {
            this.type = type;
            this.yaml = yaml;
        }

        public IResource getK8sResource() {
            return this.k8sResource;
        }

        public void setK8sresource(IResource k8sResource) {
            this.k8sResource = k8sResource;
        }

        public @NotNull Map<String, Object> getYaml() {
            return this.yaml;
        }

        @Override
        public String toString() {
            return type.toString();
        }

    }


    @Override
    public @NotNull Object getEndpoint(@NotNull EcosystemEndpoint endpoint) throws GalasaEcosystemManagerException {
        switch(endpoint) {
            case API:
                return this.apiUrl;
            case CPS:
                return this.cpsUri;
            case CREDS:
                return this.credsUri;
            case DSS:
                return this.dssUri;
            case ENGINE_CONTROLLER_HEALTH:
                return this.engineHealthUrl;
            case ENGINE_CONTROLLER_METRICS:
                return this.engineMetricsUrl;
            case GRAFANA:
                return this.grafanaUrl;
            case METRICS_HEALTH:
                return this.metricsHealthUrl;
            case METRICS_METRICS:
                return this.metricsMetricsUrl;
            case PROMETHEUS:
                return this.prometheusUrl;
            case RAS:
                return this.rasUri;
            case RESOURCE_MANAGEMENT_HEALTH:
                return this.resmonHealthUrl;
            case RESOURCE_MANAGEMENT_METRICS:
                return this.resmonMetricsUrl;
            case SIMBANK_WEBSERVICE:
                return this.simbankWebUrl;
            case SIMBANK_TELNET:
                return this.simbankTelnetPort;
            case SIMBANK_DATABASE:
                return this.simbankDatabasePort;
            case SIMBANK_MANAGEMENT_FACILITY:
                return this.simbankManagementFacilityUrl;
            default:
                throw new GalasaEcosystemManagerException("Unknown Galasa endpoint " + endpoint.toString());
        }
    }

    private void setEndpoint(@NotNull EcosystemEndpoint endpoint, URI uri) throws GalasaEcosystemManagerException {
        if (uri == null) {
            throw new GalasaEcosystemManagerException("Endpoint URI missing for " + endpoint.toString());
        }

        try {
            switch(endpoint) {
                case API:
                    this.apiUrl = uri.toURL();
                    break;
                case CPS:
                    this.cpsUri = uri;
                    this.cpsUrl = new URL(this.cpsUri.getSchemeSpecificPart());
                    break;
                case CREDS:
                    this.credsUri = uri;
                    this.credsUrl = new URL(this.credsUri.getSchemeSpecificPart());
                    break;
                case DSS:
                    this.dssUri = uri;
                    this.dssUrl = new URL(this.dssUri.getSchemeSpecificPart());
                    break;
                case ENGINE_CONTROLLER_HEALTH:
                    this.engineHealthUrl = uri.toURL();
                    break;
                case ENGINE_CONTROLLER_METRICS:
                    this.engineMetricsUrl = uri.toURL();
                    break;
                case GRAFANA:
                    this.grafanaUrl = uri.toURL();
                    break;
                case METRICS_HEALTH:
                    this.metricsHealthUrl = uri.toURL();
                    break;
                case METRICS_METRICS:
                    this.metricsMetricsUrl = uri.toURL();
                    break;
                case PROMETHEUS:
                    this.prometheusUrl = uri.toURL();
                    break;
                case RAS:
                    this.rasUri = uri;
                    this.rasUrl = new URL(this.rasUri.getSchemeSpecificPart());
                    break;
                case RESOURCE_MANAGEMENT_HEALTH:
                    this.resmonHealthUrl = uri.toURL();
                    break;
                case RESOURCE_MANAGEMENT_METRICS:
                    this.resmonMetricsUrl = uri.toURL();
                    break;
                case SIMBANK_WEBSERVICE:
                    this.simbankWebUrl = uri.toURL();
                    break;
                case SIMBANK_MANAGEMENT_FACILITY:
                    this.simbankManagementFacilityUrl = uri.toURL();
                    break;
                default:
                    throw new GalasaEcosystemManagerException("Unknown Galasa endpoint " + endpoint.toString());
            }
        } catch (MalformedURLException e) {
            throw new GalasaEcosystemManagerException("Problem with endpoint URI", e);
        }
    }

    private void setEndpointPort(@NotNull EcosystemEndpoint endpoint, InetSocketAddress port) throws GalasaEcosystemManagerException {
        if (port == null) {
            throw new GalasaEcosystemManagerException("Endpoint port missing for " + endpoint.toString());
        }

        switch(endpoint) {
            case SIMBANK_TELNET:
                this.simbankTelnetPort = port;
                break;
            case SIMBANK_DATABASE:
                this.simbankDatabasePort = port;
                break;
            default:
                throw new GalasaEcosystemManagerException("Unknown Galasa endpoint " + endpoint.toString());
        }
    }
    
    @Override
    public String getHostCpsProperty(@NotNull String namespace, @NotNull String prefix, @NotNull String suffix, String... infixes) throws GalasaEcosystemManagerException {
    	try {
			return this.framework.getConfigurationPropertyService(namespace).getProperty(prefix, suffix, infixes);
		} catch (ConfigurationPropertyStoreException e) {
			throw new GalasaEcosystemManagerException("Problem inspecting the CPS of the parent ecosystem", e);
		}
    }

    @Override
    public String getCpsProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        if (property == null || property.trim().isEmpty()) {
            throw new GalasaEcosystemManagerException("Property name is missing");
        }

        return retrieveCpsProperty(property);
    }

    @Override
    public void setCpsProperty(@NotNull String property, String value) throws GalasaEcosystemManagerException {
        if (property == null || property.trim().isEmpty()) {
            throw new GalasaEcosystemManagerException("Property name is missing");
        }

        if (value == null || value.trim().isEmpty()) {
            deleteCpsProperty(property);
        } else {
            storeCpsProperty(property, value.trim());
        }
    }

    @Override
    public String getDssProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        return getCpsProperty(property);
    }

    @Override
    public void setDssProperty(@NotNull String property, @NotNull String value) throws GalasaEcosystemManagerException {
        setCpsProperty(property, value);
    }

    @Override
    public String getCredsProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        return getCpsProperty(property);
    }

    @Override
    public void setCredsProperty(@NotNull String property, @NotNull String value) throws GalasaEcosystemManagerException {
        setCpsProperty(property, value);
    }


    private void saveEcosystemInDss() throws GalasaEcosystemManagerException {

        String runName = getEcosystemManager().getFramework().getTestRunName();
        String prefix = "run." + runName + ".kubernetes.ecosystem." + getTag();

        HashMap<String, String> ecosystemProperties = new HashMap<String, String>();
        ecosystemProperties.put(prefix + ".namespace.tag", this.namespace.getTag());

        for(EcosystemEndpoint endpoint : EcosystemEndpoint.values()) {
            String key = prefix + "." + endpoint.toString();
            if (endpoint.getEndpointType() == URL.class) {
                ecosystemProperties.put(key, getEndpoint(endpoint).toString());
            } else if (endpoint.getEndpointType() == InetSocketAddress.class) {
                InetSocketAddress socketAddress = (InetSocketAddress) getEndpoint(endpoint);
                ecosystemProperties.put(key, socketAddress.getHostString() + ":" + socketAddress.getPort());
            } else {
                throw new GalasaEcosystemManagerException("Unknown endpoint type for " + endpoint.toString());
            }
        }

        try {
            getEcosystemManager().getDss().put(ecosystemProperties);
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Unable to save the ecosystem in the DSS",e);
        }

    }






    public static void loadEcosystemsFromRun(GalasaEcosystemManagerImpl manager,
            IDynamicStatusStoreService dss,     
            HashMap<String, IInternalEcosystem> taggedEcosystems, 
            IRun testRun) throws GalasaEcosystemManagerException {

        String tagPrefix = "run." + testRun.getName() + ".kubernetes.ecosystem.";
        Pattern dssTagPattern = Pattern.compile("^" + tagPrefix + "(\\w+).namespace.tag$");

        try {
            Map<String, String> dssTags = dss.getPrefix(tagPrefix);
            for(Entry<String, String> entry : dssTags.entrySet()) {
                Matcher matcher = dssTagPattern.matcher(entry.getKey());
                if (!matcher.find()) {
                    continue;
                }

                String tag = matcher.group(1);
                String kubernetesNamespaceTag = entry.getValue();


                IKubernetesNamespace namespace = manager.getKubernetesManager().getNamespaceByTag(kubernetesNamespaceTag);

                KubernetesEcosystemImpl ecosystem = new KubernetesEcosystemImpl(manager, tag, namespace);

                for(EcosystemEndpoint endpoint : EcosystemEndpoint.values()) {
                    String key = tagPrefix + tag + "." + endpoint.toString();
                    String value = AbstractManager.nulled(dss.get(key));
                    if (value == null) {
                        throw new GalasaEcosystemManagerException("Missing URI for tag " + tag + " endpoint " + endpoint.toString());
                    }
                    if (endpoint.getEndpointType() == URL.class) {
                        ecosystem.setEndpoint(endpoint, new URI(value));
                    } else if (endpoint.getEndpointType() == InetSocketAddress.class) {
                        int pos = value.indexOf(':');
                        String host = value.substring(0, pos);
                        String port = value.substring(pos + 1);
                        ecosystem.setEndpointPort(endpoint, new InetSocketAddress(host, Integer.parseInt(port)));
                    } else {
                        throw new GalasaEcosystemManagerException("Unknown endpoint type for " + endpoint.toString());
                    }
                }

                taggedEcosystems.put(tag, ecosystem);            
            }

        } catch(GalasaEcosystemManagerException e) {
            throw e;
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Unable to load Ecosystem from the run", e);
        }

    }

    @Override
    public String submitRun(String runType, 
            String requestor, 
            String groupName, 
            @NotNull String bundleName,
            @NotNull String testName, 
            String mavenRepository, 
            String obr, 
            String stream, 
            Properties overrides)
                    throws GalasaEcosystemManagerException {

        JsonObject request = new JsonObject();
        request.addProperty("testStream", stream);
        request.addProperty("obr", obr);
        request.addProperty("mavenRepository", mavenRepository);
        request.addProperty("testStream", stream);
        request.addProperty("trace", true);

        JsonArray tests = new JsonArray(1);
        tests.add(bundleName + "/" + testName);
        request.add("classNames", tests);

        if (overrides != null) {
            JsonArray runProperties = new JsonArray();
            request.add("runProperties", runProperties);
            for(Entry<Object, Object> entry : overrides.entrySet()) {
                JsonObject property = new JsonObject();
                property.addProperty("key", entry.getKey().toString());
                property.addProperty("value", entry.getValue().toString());
                runProperties.add(property);
            }
        }

        //*** submit the test
        try {
            IHttpClient apiClient = getApiHttpClient();

            String gn = URLEncoder.encode(groupName, "utf-8");

            HttpClientResponse<JsonObject> response = apiClient.postJson("runs/" + gn, request);

            if (response.getStatusCode() != 200) {
                throw new GalasaEcosystemManagerException("Submit failed, status code " + response.getStatusCode());
            }
        } catch(KubernetesManagerException | UnsupportedEncodingException | HttpClientException e) {
            throw new GalasaEcosystemManagerException("Failed to submit test", e);
        }

        return null;
    }

    @Override
    public JsonObject getSubmittedRuns(String groupName) throws GalasaEcosystemManagerException {

        try {
            IHttpClient apiClient = getApiHttpClient();

            String gn = URLEncoder.encode(groupName, "utf-8");

            HttpClientResponse<JsonObject> response = apiClient.getJson("runs/" + gn);

            if (response.getStatusCode() != 200) {
                throw new GalasaEcosystemManagerException("get submitted runs failed, status code " + response.getStatusCode());
            }

            return response.getContent();
        } catch(KubernetesManagerException | UnsupportedEncodingException | HttpClientException e) {
            throw new GalasaEcosystemManagerException("Failed to get submitted runs", e);
        }
    }

    @Override
    public JsonObject waitForGroupNames(String groupName, long timeout) throws GalasaEcosystemManagerException {
        logger.debug("Waiting for Run Group " + groupName + " to finish");

        HashMap<String, String> previousStatus = new HashMap<>();

        JsonObject response = null;
        Instant expire = Instant.now().plus(timeout, ChronoUnit.SECONDS);
        while(Instant.now().isBefore(expire)) {
            response = getSubmittedRuns(groupName);

            ScheduleStatus schedule = this.gson.fromJson(response, ScheduleStatus.class);

            if (schedule.getRuns() != null) {
                for(Run run : schedule.getRuns()) {
                    String status = run.getStatus();
                    if (status != null) {
                        String oldStatus = previousStatus.get(run.getName());
                        if (!status.equals(oldStatus)) {
                            logger.info("Run " + run.getName() + " is " + run.getStatus());
                            previousStatus.put(run.getName(), status);
                        }
                    }
                }
            }

            if (schedule.isComplete()) {
                return response;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new GalasaEcosystemManagerException("Interrupted");
            }
        }

        throw new GalasaEcosystemManagerException("Run Group " + groupName + " did not finish in time:-\n" + response);
    }

    @Override
    public JsonObject waitForRun(String run) throws GalasaEcosystemManagerException {
        throw new GalasaEcosystemManagerException("This method needs to be written");
    }

    @Override
    public JsonObject waitForRun(String run, int minutes) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ICommandShell getCommandShell() throws GalasaEcosystemManagerException {
        throw new GalasaEcosystemManagerException("Command shell is not relevant in the Kubernetes Ecosystem"); 
    }


    @Override
    protected void insertLastRunIDIntoDSS() throws GalasaEcosystemManagerException {
        setCpsProperty("framework.request.type.UNKNOWN.prefix", this.runIdPrefix.getRunIdPrefix());
    }

}
