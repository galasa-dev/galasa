/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.Message;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SetContentType;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRun;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.IResource;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.properties.KubernetesStorageClass;
import dev.galasa.kubernetes.internal.resources.ConfigMapImpl;
import dev.galasa.kubernetes.internal.resources.DeploymentImpl;
import dev.galasa.kubernetes.internal.resources.PersistentVolumeClaimImpl;
import dev.galasa.kubernetes.internal.resources.SecretImpl;
import dev.galasa.kubernetes.internal.resources.ServiceImpl;
import dev.galasa.kubernetes.internal.resources.StatefulSetImpl;
import dev.galasa.kubernetes.internal.resources.Utility;
import io.kubernetes.client.ProtoClient;
import io.kubernetes.client.ProtoClient.ObjectOrStatus;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimList;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1ReplicaSetList;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetList;
import io.kubernetes.client.openapi.models.V1StatefulSetSpec;
import io.kubernetes.client.proto.V1.Namespace;
import io.kubernetes.client.util.Yaml;

/**
 * The Kubernetes Namespace implementation.
 * 
 * The kubernetes manager will allocate a namespace for tests to work on.   These namespaces
 * should be isolated just for use by the test.
 * 
 * See example scripts in examples/ to see how to create a name space with the necessary rbac.
 * 
 *  
 *
 */
public class KubernetesNamespaceImpl implements IKubernetesNamespace {

    private final static Log                 logger = LogFactory.getLog(KubernetesNamespaceImpl.class);

    private final KubernetesClusterImpl      cluster;
    private final String                     namespaceId;
    private final IFramework                 framework;
    private final IDynamicStatusStoreService dss;
    private final String                     runName;
    private final String                     tag;

    public KubernetesNamespaceImpl(KubernetesClusterImpl cluster, String namespaceId, String tag, IFramework framework, IDynamicStatusStoreService dss) {
        this.cluster     = cluster;
        this.namespaceId = namespaceId;
        this.tag         = tag;
        this.framework   = framework;
        this.dss         = dss;
        this.runName     = this.framework.getTestRunName();
    }

    public String getId() {
        return this.namespaceId;
    }

    @Override
    public String getFullId() {
        return this.cluster.getId() + "/" + this.namespaceId;
    }


    public KubernetesClusterImpl getCluster() {
        return this.cluster;
    }

    /**
     * Initialise the namespace by creating a ConfigMap with the runname.  This is to ensure 
     * that the namespace was clean and not in use.
     * 
     * @throws KubernetesManagerException If there is a problem with the cluster or the configmap already exists
     */
    public void initialiseNamespace() throws KubernetesManagerException {
        ApiClient apiClient = this.cluster.getApi();
        CoreV1Api api = new CoreV1Api(apiClient);

        //*** Create a ConfigMap in the namespace so that we can identify which Run the namespace is for
        HashMap<String, String> data = new HashMap<>();
        data.put("galasaRun", this.framework.getTestRunName());

        V1ConfigMap configMap = new V1ConfigMap();
        configMap.setApiVersion("v1");
        configMap.setKind("ConfigMap");
        configMap.setData(data);

        V1ObjectMeta metadata = new V1ObjectMeta();
        configMap.setMetadata(metadata);
        metadata.setName("galasa");
        addRunLabel(metadata);

        try {
            api.createNamespacedConfigMap(this.namespaceId, configMap).execute();
        } catch(ApiException e) {         
            if (e.getCode() == 409) {
                throw new KubernetesManagerException("The allocated namespace " + this.namespaceId + " on cluster " + this.cluster.getId() + " is dirty, the configmap galasa still exists", e);
            }
            throw new KubernetesManagerException("Unable to initialise the namespace with a configmap", e);
        }
    }



    /**
     * Discard all the resources that are in the namespace
     * 
     * @param runName The runname the namespace was allocated to so we can clean up the DSS
     * @throws KubernetesManagerException Any problem with teh cluster or DSS
     */
    public void discard(String runName) throws KubernetesManagerException {
        if (cleanNamespace()) {
            clearSlot(runName);
        }
    }

    /**
     * Clean up the DSS and fee the slot
     * 
     * @param runName The runname the slot was allocated to
     */
    private void clearSlot(String runName) {
        try {
            String namespacePrefix = "cluster." + this.cluster.getId() + ".namespace." + this.namespaceId;
            String slotKey = "slot.run." + runName + ".cluster." + this.cluster.getId() + ".namespace." + namespaceId;

            String slotStatus = dss.get(slotKey);
            if ("active".equals(slotStatus)) {
                //*** Decrement the cluster current slot count and mark the namespace as free
                while(true) { //*** have to loop around incase another test changed the current slot count
                    int currentSlots = 0;
                    String sCurrentSlots = dss.get("cluster." + this.cluster.getId() + ".current.slots");
                    if (sCurrentSlots != null) {
                        currentSlots = Integer.parseInt(sCurrentSlots);
                    }
                    currentSlots--;

                    if (currentSlots < 0) {
                        currentSlots = 0;
                    }

                    HashMap<String, String> slotOtherValues = new HashMap<>();
                    slotOtherValues.put(namespacePrefix, "free");
                    slotOtherValues.put(slotKey, "free");
                    if (dss.putSwap("cluster." + this.cluster.getId() + ".current.slots", sCurrentSlots, Integer.toString(currentSlots), slotOtherValues)) {
                        break;
                    }
                }
            }

            //*** Slot count has been decremented, we can now delete the actual DSS properties
            dss.deletePrefix(namespacePrefix);
            dss.deletePrefix(slotKey);

            logger.debug("Kubernetes namespace " + getFullId() + " has been freed");
        } catch(Exception e) {
            logger.error("Problem discarding the namespace",e);
        }
    }

    private boolean cleanNamespace() throws KubernetesManagerException {
        CoreV1Api coreApi = new CoreV1Api(this.cluster.getApi());
        AppsV1Api appsApi = new AppsV1Api(this.cluster.getApi());
        ProtoClient pc = new ProtoClient(this.cluster.getApi());

        try {
            //*** Delete all configmaps that exist in the namespace
            V1ConfigMapList configMapList = coreApi.listNamespacedConfigMap(this.namespaceId).execute();

            for(V1ConfigMap configMap : configMapList.getItems()) {
                logger.debug("Deleting ConfigMap " + this.cluster.getId() + "/" + this.namespaceId + "/" + configMap.getMetadata().getName());
                V1DeleteOptions options = new V1DeleteOptions();
                options.setGracePeriodSeconds(0L);
                coreApi.deleteNamespacedConfigMap(configMap.getMetadata().getName(), this.namespaceId).execute();
            }

            //*** Delete all secrets that exist in the namespace
            V1SecretList secretList = coreApi.listNamespacedSecret(this.namespaceId).execute();

            for(V1Secret secret : secretList.getItems()) {
                // Check the secret is not for a service account

                V1ObjectMeta metadata = secret.getMetadata();
                if (metadata != null && metadata.getAnnotations() != null) {
                    if (metadata.getAnnotations().containsKey("kubernetes.io/service-account.name")) {
                        continue;
                    }
                }

                logger.debug("Deleting Secret " + this.cluster.getId() + "/" + this.namespaceId + "/" + secret.getMetadata().getName());
                V1DeleteOptions options = new V1DeleteOptions();
                options.setGracePeriodSeconds(0L);
                coreApi.deleteNamespacedSecret(secret.getMetadata().getName(), this.namespaceId).execute();
            }

            //*** Delete all Deployments that exist in the namespace
            V1DeploymentList deploymentList = appsApi.listNamespacedDeployment(this.namespaceId).execute();

            for(V1Deployment deployment : deploymentList.getItems()) {
                logger.debug("Deleting Deployment " + this.cluster.getId() + "/" + this.namespaceId + "/" + deployment.getMetadata().getName());
                V1DeleteOptions options = new V1DeleteOptions();
                options.setGracePeriodSeconds(0L);
                appsApi.deleteNamespacedDeployment(deployment.getMetadata().getName(), this.namespaceId).body(options).execute();
            }

            //*** Delete all StatefulSets that exist in the namespace
            V1StatefulSetList statefulsetList = appsApi.listNamespacedStatefulSet(this.namespaceId).execute();

            for(V1StatefulSet statefulset : statefulsetList.getItems()) {
                logger.debug("Deleting StatefulSet " + this.cluster.getId() + "/" + this.namespaceId + "/" + statefulset.getMetadata().getName());
                V1DeleteOptions options = new V1DeleteOptions();
                options.setGracePeriodSeconds(0L);
                appsApi.deleteNamespacedStatefulSet(statefulset.getMetadata().getName(), this.namespaceId).body(options).execute();
            }

            //*** Delete all Services that exist in the namespace
            V1ServiceList serviceList = coreApi.listNamespacedService(this.namespaceId).execute();

            for(V1Service service : serviceList.getItems()) {
                logger.debug("Deleting Service " + this.cluster.getId() + "/" + this.namespaceId + "/" + service.getMetadata().getName());
                coreApi.deleteNamespacedService(service.getMetadata().getName(), this.namespaceId).execute();
            }

            //*** Delete all PVCs that exist in the namespace
            V1PersistentVolumeClaimList pvcList = coreApi.listNamespacedPersistentVolumeClaim(this.namespaceId).execute();

            for(V1PersistentVolumeClaim pvc : pvcList.getItems()) {
                logger.debug("Deleting PVC " + this.cluster.getId() + "/" + this.namespaceId + "/" + pvc.getMetadata().getName());
                //TODO raise issue because the delete pvc api call fails

                //                V1DeleteOptions options = new V1DeleteOptions();
                //                options.setGracePeriodSeconds(0L);
                //                coreApi.deleteNamespacedPersistentVolumeClaim(pvc.getMetadata().getName(), this.namespaceId, null, null, 0, null, null, null);
                ObjectOrStatus<Message> response = pc.delete(Namespace.newBuilder(), "/api/v1/namespaces/" + this.namespaceId + "/persistentvolumeclaims/" + pvc.getMetadata().getName());
                if (response.status != null) {
                    throw new KubernetesManagerException("Failed to delete PVC:-\n" + response.status.toString());
                }
            }

            //*** Delete all remaining pods
            V1PodList pods = coreApi.listNamespacedPod(this.namespaceId).execute();
            for(V1Pod pod : pods.getItems()) {
                logger.debug("Deleting POD " + this.cluster.getId() + "/" + this.namespaceId + "/" + pod.getMetadata().getName());
                V1DeleteOptions options = new V1DeleteOptions();
                options.setGracePeriodSeconds(0L);
                try {
                    coreApi.deleteNamespacedPod(pod.getMetadata().getName(), this.namespaceId).body(options).execute();
                } catch(Exception e) {} //*** Ignore all errors as may be deleting pods from deployments or statefulsets
            }


            //*** Waiting for all pods and replicasets to be deleted

            logger.info("Waiting for all ReplicaSets, Pods and PersistentVolumeClaims to be deleted");

            long timeoutSeconds = 60;
            long checkSeconds = 10;

            if (this.framework.getTestRun() != null && this.framework.getTestRun().isLocal()) {
                timeoutSeconds = 30;
            }

            Instant timeout = Instant.now().plusSeconds(timeoutSeconds); //  Allow a maximum of 30 seconds then leave the Resource Management to clean up
            Instant check = Instant.now().plusSeconds(checkSeconds);

            while(timeout.isAfter(Instant.now())) {
                V1PodList podList = coreApi.listNamespacedPod(namespaceId).execute();
                V1ReplicaSetList replicaSetList = appsApi.listNamespacedReplicaSet(this.namespaceId).execute();
                pvcList = coreApi.listNamespacedPersistentVolumeClaim(this.namespaceId).execute();

                if (podList.getItems().isEmpty() 
                        && replicaSetList.getItems().isEmpty()
                        && pvcList.getItems().isEmpty()) {
                    logger.info("All resources discarded in namespace " + getFullId());
                    return true;
                }

                if (check.isBefore(Instant.now())) {
                    logger.debug("Still waiting");
                    check = Instant.now().plusSeconds(checkSeconds);
                }

                Thread.sleep(1000);
            }

            logger.warn("Failed to discard namespace, leaving to the next Resource Management cycle");
            return false;
        } catch(Exception e) {
            throw new KubernetesManagerException("Problem trying to delete all the resources in the namespace " + getFullId(), e);
        }
    }

    /**
     * Called by the resource management clean up to discard the namespace
     * 
     * @param runName The runname the namespace was assigned to
     * @param clusterId  The clusterid we are working on
     * @param namespaceId the namespaceid we are discarding
     * @param dss The DSS to clean
     * @param framework A copy of the initialised framework
     * @throws KubernetesManagerException Anything could go wrong!
     */
    public static void deleteDss(String runName, String clusterId, String namespaceId, IDynamicStatusStoreService dss, IFramework framework) throws KubernetesManagerException {
        KubernetesClusterImpl cluster = new KubernetesClusterImpl(clusterId, dss, framework);

        //*** get the tag
        String tag;
        try {
            tag = AbstractManager.nulled(dss.get("slot.run." + runName + ".cluster." + clusterId + ".namespace." + namespaceId + ".tag"));
        } catch (DynamicStatusStoreException e) {
            throw new KubernetesManagerException("Problem retrieving tag from DSS", e);
        }
        if (tag == null) {
            throw new KubernetesManagerException("Missing tag for Kubernetes namespace " + clusterId + "/" + namespaceId);
        }

        KubernetesNamespaceImpl namespace = new KubernetesNamespaceImpl(cluster, namespaceId, tag, framework, dss);

        namespace.discard(runName);
    }

    @Override
    @NotNull
    public IResource createResource(@NotNull String yaml) throws KubernetesManagerException {

        if (yaml == null || yaml.trim().isEmpty()) {
            throw new KubernetesManagerException("Missing YAML");
        }

        Object oResource = null;
        try {
            oResource = Yaml.load(yaml);
        } catch (IOException e) {
            throw new KubernetesManagerException("Unable to convert resource YAML to a Kubernetes resource", e);
        }

        try {
            if (oResource instanceof V1ConfigMap) {
                return createConfigMap((V1ConfigMap) oResource);
            } else if (oResource instanceof V1PersistentVolumeClaim) {
                return createPersistentVolumeClaim((V1PersistentVolumeClaim) oResource);
            } else if (oResource instanceof V1Secret) {
                return createSecret((V1Secret) oResource);
            } else if (oResource instanceof V1Deployment) {
                return createDeployment((V1Deployment) oResource);
            } else if (oResource instanceof V1StatefulSet) {
                return createStatefulSet((V1StatefulSet) oResource);
            } else if (oResource instanceof V1Service) {
                return createService((V1Service) oResource);
            } else {
                throw new KubernetesManagerException("The Kubernetes Manager does not at present support resource type " + oResource.getClass().getSimpleName());
            }
        } catch(ApiException e) {
            throw new KubernetesManagerException("Unable to create resource:-" + e.getResponseBody(), e);
        }
    }

    private @NotNull IResource createPersistentVolumeClaim(@NotNull V1PersistentVolumeClaim persistentVolumeClaim) throws KubernetesManagerException, ApiException {
        if (persistentVolumeClaim.getMetadata() == null) {
            persistentVolumeClaim.setMetadata(new V1ObjectMeta());
        }
        addRunLabel(persistentVolumeClaim.getMetadata());
        CoreV1Api api = new CoreV1Api(cluster.getApi());

        String storageClass = KubernetesStorageClass.get(this.cluster);
        if (storageClass != null) {
            V1PersistentVolumeClaimSpec spec = persistentVolumeClaim.getSpec();
            if (spec == null) {
                spec = new V1PersistentVolumeClaimSpec();
                persistentVolumeClaim.setSpec(spec);
            }

            spec.setStorageClassName(storageClass);
        }

        V1PersistentVolumeClaim actualPvc = api.createNamespacedPersistentVolumeClaim(this.namespaceId, persistentVolumeClaim).execute();

        logger.debug("PersistentVolumeClaim " + actualPvc.getMetadata().getName() + " created in namespace " + this.namespaceId + " on cluster " + this.cluster.getId());

        return new PersistentVolumeClaimImpl(this, actualPvc);
    }

    private void addRunLabel(V1ObjectMeta metadata) {
        if (metadata.getLabels() == null) {
            metadata.setLabels(new HashMap<>());
        }
        metadata.getLabels().put("galasa-run", runName);

    }
    private @NotNull IResource createConfigMap(@NotNull V1ConfigMap configMap) throws KubernetesManagerException, ApiException {
        if (configMap.getMetadata() == null) {
            configMap.setMetadata(new V1ObjectMeta());
        }
        addRunLabel(configMap.getMetadata());

        CoreV1Api api = new CoreV1Api(cluster.getApi());
        V1ConfigMap actualConfig = api.createNamespacedConfigMap(namespaceId, configMap).execute();

        logger.debug("ConfigMap " + actualConfig.getMetadata().getName() + " created in namespace " + this.namespaceId + " on cluster " + this.cluster.getId());

        return new ConfigMapImpl(this, actualConfig);
    }


    private @NotNull IResource createSecret(@NotNull V1Secret secret) throws KubernetesManagerException, ApiException {
        if (secret.getMetadata() == null) {
            secret.setMetadata(new V1ObjectMeta());
        }
        addRunLabel(secret.getMetadata());

        CoreV1Api api = new CoreV1Api(cluster.getApi());
        V1Secret actualSecret = api.createNamespacedSecret(namespaceId, secret).execute();

        logger.debug("Secret " + actualSecret.getMetadata().getName() + " created in namespace " + this.namespaceId + " on cluster " + this.cluster.getId());

        return new SecretImpl(this, actualSecret);
    }

    private @NotNull IResource createService(@NotNull V1Service service) throws KubernetesManagerException, ApiException {
        if (service.getMetadata() == null) {
            service.setMetadata(new V1ObjectMeta());
        }
        addRunLabel(service.getMetadata());

        CoreV1Api api = new CoreV1Api(cluster.getApi());
        V1Service actualService = api.createNamespacedService(namespaceId, service).execute();

        logger.debug("Service " + actualService.getMetadata().getName() + " created in namespace " + this.namespaceId + " on cluster " + this.cluster.getId());

        return new ServiceImpl(this, actualService);
    }

    private @NotNull IResource createDeployment(@NotNull V1Deployment deployment) throws KubernetesManagerException, ApiException {
        if (deployment.getMetadata() == null) {
            deployment.setMetadata(new V1ObjectMeta());
        }
        addRunLabel(deployment.getMetadata());

        if (deployment.getSpec() != null 
                && deployment.getSpec().getTemplate() != null) {
            if (deployment.getSpec().getTemplate().getMetadata() == null) {
                deployment.getSpec().getTemplate().setMetadata(new V1ObjectMeta());
            }
            addRunLabel(deployment.getSpec().getTemplate().getMetadata());
        }


        AppsV1Api api = new AppsV1Api(cluster.getApi());
        V1Deployment actualDeployment = api.createNamespacedDeployment(namespaceId, deployment).execute();

        logger.debug("Deployment " + actualDeployment.getMetadata().getName() + " created in namespace " + this.namespaceId + " on cluster " + this.cluster.getId());

        return new DeploymentImpl(this, actualDeployment);
    }

    private @NotNull IResource createStatefulSet(@NotNull V1StatefulSet statefulSet) throws KubernetesManagerException, ApiException {
        if (statefulSet.getMetadata() == null) {
            statefulSet.setMetadata(new V1ObjectMeta());
        }
        addRunLabel(statefulSet.getMetadata());

        if (statefulSet.getSpec() != null 
                && statefulSet.getSpec().getTemplate() != null) {
            if (statefulSet.getSpec().getTemplate().getMetadata() == null) {
                statefulSet.getSpec().getTemplate().setMetadata(new V1ObjectMeta());
            }
            addRunLabel(statefulSet.getSpec().getTemplate().getMetadata());
        }
        if (statefulSet.getSpec() != null 
                && statefulSet.getSpec().getVolumeClaimTemplates() != null) {
            for(V1PersistentVolumeClaim claim : statefulSet.getSpec().getVolumeClaimTemplates()) {
                if (claim.getMetadata() == null) {
                    claim.setMetadata(new V1ObjectMeta());
                }
                addRunLabel(claim.getMetadata());
            }
        }

        AppsV1Api api = new AppsV1Api(cluster.getApi());

        //*** Add the storage class to any persistent volume claim templates
        String storageClass = KubernetesStorageClass.get(this.cluster);
        if (storageClass != null) {
            V1StatefulSetSpec spec = statefulSet.getSpec();
            if (spec != null) {
                List<V1PersistentVolumeClaim> vcTemplates = spec.getVolumeClaimTemplates();
                if (vcTemplates != null) {
                    for(V1PersistentVolumeClaim claim : vcTemplates) {
                        V1PersistentVolumeClaimSpec vcspec = claim.getSpec();
                        if (vcspec == null) {
                            vcspec = new V1PersistentVolumeClaimSpec();
                            claim.setSpec(vcspec);
                        }

                        vcspec.setStorageClassName(storageClass);
                    }
                }
            }
        }




        V1StatefulSet actualStatefulSet = api.createNamespacedStatefulSet(namespaceId, statefulSet).execute();

        logger.debug("StatefulSet " + actualStatefulSet.getMetadata().getName() + " created in namespace " + this.namespaceId + " on cluster " + this.cluster.getId());

        return new StatefulSetImpl(this, actualStatefulSet);
    }

    @Override
    public void saveNamespaceConfiguration() throws KubernetesManagerException {
        saveNamespaceConfiguration(null);

    }

    @Override
    public void saveNamespaceConfiguration(String storedArtifactPath) throws KubernetesManagerException {
        logger.info("Saving Kubernetes Namespace" + getFullId() + " configuration");
        if (storedArtifactPath == null || storedArtifactPath.trim().isEmpty()) {
            storedArtifactPath = "/kubernetes/" + getFullId();
        }
        storedArtifactPath = storedArtifactPath.trim();

        Path root = this.framework.getResultArchiveStore().getStoredArtifactsRoot();
        Path directory = root.resolve(storedArtifactPath);
        try {
            Files.createDirectories(directory);
        } catch(IOException e) {
            throw new KubernetesManagerException("Unable to create the save namespace configuration directory " + directory, e);
        }

        CoreV1Api coreApi = new CoreV1Api(this.cluster.getApi());
        AppsV1Api appsApi = new AppsV1Api(this.cluster.getApi());

        saveNamespaceConfigMap(coreApi, directory);
        saveNamespacePersistentVolumeClaim(coreApi, directory);
        saveNamespaceSecret(coreApi, directory);
        saveNamespaceService(coreApi, directory);
        saveNamespaceDeployment(appsApi, coreApi, directory);
        saveNamespaceStatefulSet(appsApi, coreApi, directory);

        logger.info("Saved Kubernetes Namespace" + getFullId() + " configuration to " + directory);
    }

    private void saveNamespaceConfigMap(CoreV1Api coreApi, Path directory) {
        try {
            V1ConfigMapList configMapList = coreApi.listNamespacedConfigMap(this.namespaceId).execute();

            for(V1ConfigMap configMap : configMapList.getItems()) {
                saveNamespaceFile(directory, configMap, "configmap_", configMap.getMetadata());
            }
        } catch(ApiException | IOException e) {
            logger.error("Failed to save the ConfigMap configuration",e);
        }
    }

    private void saveNamespacePersistentVolumeClaim(CoreV1Api coreApi, Path directory) {
        try {
            V1PersistentVolumeClaimList pvcList = coreApi.listNamespacedPersistentVolumeClaim(this.namespaceId).execute();

            for(V1PersistentVolumeClaim pvc : pvcList.getItems()) {
                saveNamespaceFile(directory, pvc, "pvc_", pvc.getMetadata());
            }
        } catch(ApiException | IOException e) {
            logger.error("Failed to save the PVC configuration",e);
        }
    }

    private void saveNamespaceSecret(CoreV1Api coreApi, Path directory) {
        try {
            V1SecretList secretList = coreApi.listNamespacedSecret(this.namespaceId).execute();

            for(V1Secret secret : secretList.getItems()) {
                // Check the secret is not for a service account

                V1ObjectMeta metadata = secret.getMetadata();
                if (metadata != null && metadata.getAnnotations() != null) {
                    if (metadata.getAnnotations().containsKey("kubernetes.io/service-account.name")) {
                        continue;
                    }
                }

                saveNamespaceFile(directory, secret, "secret_", secret.getMetadata());
            }
        } catch(ApiException | IOException e) {
            logger.error("Failed to save the Secret configuration",e);
        }
    }

    private void saveNamespaceService(CoreV1Api coreApi, Path directory) {
        try {
            V1ServiceList serviceList = coreApi.listNamespacedService(this.namespaceId).execute();

            for(V1Service service : serviceList.getItems()) {
                saveNamespaceFile(directory, service, "service_", service.getMetadata());
            }
        } catch(ApiException | IOException e) {
            logger.error("Failed to save the Service configuration",e);
        }
    }

    private void saveNamespaceDeployment(AppsV1Api appsApi, CoreV1Api coreApi, Path directory) {
        try {
            V1DeploymentList deploymentList = appsApi.listNamespacedDeployment(this.namespaceId).execute();

            for(V1Deployment deployment : deploymentList.getItems()) {
                saveNamespaceFile(directory, deployment, "deployment_", deployment.getMetadata());


                saveNamespacePods(coreApi, directory, deployment.getSpec().getSelector(), "deployment_" + deployment.getMetadata().getName() + "_pod_");
            }
        } catch(ApiException | IOException | KubernetesManagerException e) {
            logger.error("Failed to save the Deployment configuration",e);
        }
    }

    private void saveNamespacePods(CoreV1Api coreApi, Path directory, V1LabelSelector labelSelector, String prefix) throws KubernetesManagerException, ApiException, IOException {
        String convertedLabelSelector = Utility.convertLabelSelector(labelSelector);

        V1PodList pods = coreApi.listNamespacedPod(this.namespaceId)
            .labelSelector(convertedLabelSelector)
            .execute();

        for(V1Pod pod : pods.getItems()) {
            String name = pod.getMetadata().getName();

            saveNamespaceFile(directory, pod, prefix, pod.getMetadata());

            if (pod.getSpec() != null && pod.getSpec().getContainers() != null) {
                if (pod.getSpec().getContainers().size() == 1) {
                    if (pod.getSpec().getContainers().get(0).getName() != null) {
                        saveNamespaceContainer(coreApi, directory, name, pod.getSpec().getContainers().get(0).getName(), prefix + name);
                    }
                } else {
                    for(V1Container container : pod.getSpec().getContainers()) {
                        if (container.getName() != null) {
                            saveNamespaceContainer(coreApi, directory, name, container.getName(), prefix + name + "_container_" + container.getName());
                        }      
                    }
                }
            }
        }
    }

    private void saveNamespaceContainer(CoreV1Api coreApi, Path directory, String pod, String container, String filename) {
        Path path = directory.resolve(filename + ".log");

        try {
            String log = coreApi.readNamespacedPodLog(pod, this.namespaceId)
                .container(container)
                .execute();

            if (log != null) {
                Files.write(path, log.getBytes(), StandardOpenOption.CREATE, new SetContentType(ResultArchiveStoreContentType.TEXT));
            }
            //*** Removed the previous container log as it was far too slow.  will have to add code to do it specifically if requested
        } catch(ApiException e) {
        } catch (IOException e) {
            logger.error("Problem saving container log " + filename, e);
        }
    }

    private void saveNamespaceStatefulSet(AppsV1Api appsApi, CoreV1Api coreApi, Path directory) {
        try {
            V1StatefulSetList statefulsetList = appsApi.listNamespacedStatefulSet(this.namespaceId).execute();

            for(V1StatefulSet statefulset : statefulsetList.getItems()) {
                saveNamespaceFile(directory, statefulset, "statefulset_", statefulset.getMetadata());


                saveNamespacePods(coreApi, directory, statefulset.getSpec().getSelector(), "statefulset_" + statefulset.getMetadata().getName() + "_pod_");
            }
        } catch(ApiException | IOException | KubernetesManagerException e) {
            logger.error("Failed to save the Deployment configuration",e);
        }
    }

    private void saveNamespaceFile(Path directory, Object resource, String prefix, V1ObjectMeta metadata) throws IOException {
        String name = prefix + metadata.getName();
        Path path = directory.resolve(name);
        String yaml = Yaml.dump(resource);
        Files.write(path, yaml.getBytes(), StandardOpenOption.CREATE, new SetContentType(ResultArchiveStoreContentType.TEXT));
    }

    /**
     * Load all the allocated namespaces from the shared environment
     * 
     * @param testRun The test run to load the tags from
     * @throws KubernetesManagerException 
     */
    protected static void loadNamespacesFromRun(IFramework framework, IDynamicStatusStoreService dss, Map<String, KubernetesClusterImpl> clusters, Map<String, KubernetesNamespaceImpl> taggedNamespaces, IRun testRun) throws KubernetesManagerException {

        try {
            String tagPrefix = "slot.run." + testRun.getName() + ".cluster.";
            Pattern dssTagPattern = Pattern.compile("^" + tagPrefix + "(\\w+)\\.namespace\\.(\\w+).tag$");

            Map<String, String> dssTags = dss.getPrefix(tagPrefix);
            for(Entry<String, String> entry : dssTags.entrySet()) {
                Matcher matcher = dssTagPattern.matcher(entry.getKey());
                if (!matcher.find()) {
                    continue;
                }

                String clusterId   = matcher.group(1);             
                String namespaceId = matcher.group(2);             
                String seTag = entry.getValue();            

                // Check to see if we have already processed it
                if (taggedNamespaces.containsKey(seTag)) {
                    continue;
                }

                if (clusterId == null) {
                    throw new KubernetesManagerException("Missing cluster id for tag " + seTag);
                }

                KubernetesClusterImpl cluster = clusters.get(clusterId);
                if (cluster == null) {
                    throw new KubernetesManagerException("Missing configuration for cluster ID " + clusterId);
                }

                KubernetesNamespaceImpl namespace = new KubernetesNamespaceImpl(cluster, namespaceId, seTag, framework, dss);
                taggedNamespaces.put(seTag, namespace);
            }


        } catch(Exception e) {
            throw new KubernetesManagerException("Problem loading Shared Environment", e);
        }
    }

    @Override
    public @NotNull String getTag() {
        return this.tag;
    }


}