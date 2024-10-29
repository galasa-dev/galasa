/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cloud.ICloudContainer;
import dev.galasa.cloud.spi.ICloudContainerPort;
import dev.galasa.cloud.spi.ICloudContainerProvider;
import dev.galasa.cloud.spi.ICloudManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.SharedEnvironmentRunType;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.KubernetesNamespace;
import dev.galasa.kubernetes.internal.properties.KubernetesClusters;
import dev.galasa.kubernetes.internal.properties.KubernetesNamespaceTagSharedEnvironment;
import dev.galasa.kubernetes.internal.properties.KubernetesPropertiesSingleton;
import dev.galasa.kubernetes.spi.IKubernetesManagerSpi;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.util.Yaml;

/**
 * The Kubernetes Manager implementation
 * 
 *  
 *
 */
@Component(service = { IManager.class })
public class KubernetesManagerImpl extends AbstractManager implements IKubernetesManagerSpi, ICloudContainerProvider {

    protected final static String               NAMESPACE = "kubernetes";
    private final Log                           logger = LogFactory.getLog(KubernetesManagerImpl.class);
    private IDynamicStatusStoreService          dss;

    private HashMap<String, KubernetesNamespaceImpl> taggedNamespaces = new HashMap<>();
    private HashSet<String> sharedEnvironmentNamespacesTags = new HashSet<>();

    private HashMap<String, KubernetesClusterImpl> clusters = new HashMap<>();
    
    private boolean                             required         = false;

    /**
     * Initialise the Kubernetes Manager if the Kubernetes TPI is included in the test class
     */
    @SuppressWarnings("deprecation")
	@Override
    public void initialise(@NotNull IFramework framework, 
            @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, 
            @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
        	
        	//*** Check to see if the cloud manager is present, if it is, we may be required
        	for(IManager otherManager : allManagers) {
        		if (otherManager instanceof ICloudManagerSpi) {
        			((ICloudManagerSpi)otherManager).registerCloudContainerProvider(this);
        			required = true;
        			break;
        		}
        	}
        	
            //*** Check to see if we are needed for annotated fields
            if (!required) {
                for(Field field : galasaTest.getJavaTestClass().getFields()) {
                    if (field.getType() == IKubernetesNamespace.class) {
                        required = true;
                        break;
                    }
                }
            }

            if (!required) {
                return;
            }
        }

        youAreRequired(allManagers, activeManagers, galasaTest);

        //*** Initialise the CPS and DSS fields
        try {
            KubernetesPropertiesSingleton.setCps(getFramework().getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new KubernetesManagerException("Failed to set the CPS with the kubernetes namespace", e);
        }

        try {
            this.dss = this.getFramework().getDynamicStatusStoreService(NAMESPACE);
        } catch(DynamicStatusStoreException e) {
            throw new KubernetesManagerException("Unable to provide the DSS for the Kubernetes Manager", e);
        }

        
        //*** Load the YAML supported types so that the YAML can serialize
        Yaml.addModelMap("/v1", "ConfigMap", V1ConfigMap.class);
        Yaml.addModelMap("/v1", "PersistentVolumeClaim", V1PersistentVolumeClaim.class);
        Yaml.addModelMap("/v1", "Service", V1Service.class);
        Yaml.addModelMap("/v1", "Secret", V1Secret.class);
        Yaml.addModelMap("apps/v1", "Deployment", V1Deployment.class);
        Yaml.addModelMap("apps/v1", "StatefulSet", V1StatefulSet.class);
        
        this.logger.info("Kubernetes Manager initialised");

    }

    /**
     * This or another manager has indicated that this manager is required
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        super.youAreRequired(allManagers, activeManagers, galasaTest);

        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);

        this.required = true;
    }
    
    @Override
    public boolean doYouSupportSharedEnvironments() {
        return true;
    }

    /**
     * Generate all the annotated fields, uses the standard generate by method mechanism
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        if (getFramework().getTestRun().isSharedEnvironment()) {
            logger.info("Manager running in Shared Environment setup");
        }
        
        if (this.clusters.isEmpty()) {
            buildClusterMap();
        }

        //*** Shared Environment Discard processing
        try {
            if (getFramework().getSharedEnvironmentRunType() == SharedEnvironmentRunType.DISCARD) {
                KubernetesNamespaceImpl.loadNamespacesFromRun(getFramework(), dss, clusters, taggedNamespaces, getFramework().getTestRun());
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new KubernetesManagerException("Unable to determine Shared Environment phase", e);
        }

        generateAnnotatedFields(KubernetesManagerField.class);
    }


    /**
     * Generate a Kubernetes Namespace
     * 
     * @param field The test field
     * @param annotations any annotations with the namespace
     * @return a {@link IKubernetesNamespace} namespace
     * @throws KubernetesManagerException if there is a problem generating a namespace
     */
    @GenerateAnnotatedField(annotation = KubernetesNamespace.class)
    public IKubernetesNamespace generateKubernetesNamespace(Field field, List<Annotation> annotations) throws ResourceUnavailableException, KubernetesManagerException {
        KubernetesNamespace annotation = field.getAnnotation(KubernetesNamespace.class);

        String tag = annotation.kubernetesNamespaceTag().trim().toUpperCase();
        if (tag.isEmpty()) {
            tag = "PRIMARY";
        }

        //*** First check if we already have the tag
        KubernetesNamespaceImpl namespace = taggedNamespaces.get(tag);
        if (namespace != null) {
            return namespace;
        }
        
        try {
            if (getFramework().getSharedEnvironmentRunType() == SharedEnvironmentRunType.DISCARD) {
                throw new KubernetesManagerException("Attempt to generate a new Namespace during Shared Environment discard");
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new KubernetesManagerException("Unable to determine Shared Environment phase", e);
        }
        
        //*** check to see if the tag is a shared environment
        String sharedEnvironmentRunName = KubernetesNamespaceTagSharedEnvironment.get(tag);
        if (sharedEnvironmentRunName != null) {
            try {
                IRun sharedEnvironmentRun = getFramework().getFrameworkRuns().getRun(sharedEnvironmentRunName);
                
                if (sharedEnvironmentRun == null || !sharedEnvironmentRun.isSharedEnvironment()) {
                    throw new KubernetesManagerException("Unable to locate Shared Environment " + sharedEnvironmentRunName + " for Namespace Tag " + tag);
                }
                
                HashMap<String, KubernetesNamespaceImpl> tempSharedEnvironmentNamespaces = new HashMap<>();
                KubernetesNamespaceImpl.loadNamespacesFromRun(getFramework(), dss, clusters, tempSharedEnvironmentNamespaces, sharedEnvironmentRun);
                
                namespace = tempSharedEnvironmentNamespaces.get(tag);
                if (namespace == null) {
                    throw new KubernetesManagerException("Unable to locate Shared Environment " + sharedEnvironmentRunName + " for Namespace Tag " + tag);
                }
                
                this.taggedNamespaces.put(tag, namespace);
                this.sharedEnvironmentNamespacesTags.add(tag);
                
                logger.info("Namespace tag " + tag + " is using Shared Environment " + sharedEnvironmentRunName);
                
                return namespace;
            } catch(FrameworkException e) {
                throw new KubernetesManagerException("Problem loading Shared Environment " + sharedEnvironmentRunName + " for Namespace Tag " + tag, e);
            }
        } 
        
        //*** Allocate a namespace,  may take a few passes through as other tests may be trying to get namespaces at the same time

        ArrayList<KubernetesClusterImpl> availableClusters = new ArrayList<>(this.clusters.values());
        KubernetesNamespaceImpl allocatedNamespace = null;
        while(allocatedNamespace == null) {

            //*** Workout which cluster has the most capability
            float percentageAvailable = -1.0f;
            KubernetesClusterImpl selectedCluster = null;
            for(KubernetesClusterImpl cluster : availableClusters) {
                Float availability = cluster.getAvailability();
                if (availability != null && availability > percentageAvailable) {
                    selectedCluster = cluster;
                    percentageAvailable = availability;
                }
            }

            //*** Did we find a cluster with availability?
            if (selectedCluster == null) {
                throw new ResourceUnavailableException("Unable to allocate a slot on any Kubernetes Cluster");
            }

            //*** ask cluster to allocate a namespace,  if it returns null, means we have run out of available namespaces
            //*** so try a different cluster
            allocatedNamespace = selectedCluster.allocateNamespace(tag);
            if (allocatedNamespace == null) { // Failed to allocate namespace, remove from available clusters
                availableClusters.remove(selectedCluster);
            }
        }

        logger.info("Allocated Kubernetes Namespace " + allocatedNamespace.getId() + " on Cluster " + allocatedNamespace.getCluster().getId() + " for tag " + tag);

        taggedNamespaces.put(tag, allocatedNamespace);

        return allocatedNamespace;
    }



    /**
     * Delete all the supported resources in the namespaces
     */
    @Override
    public void provisionDiscard() {

        for(KubernetesNamespaceImpl namespace : taggedNamespaces.values()) {
            if (this.sharedEnvironmentNamespacesTags.contains(namespace.getTag())) {
                logger.debug("Not discarding Shared Environment namespace tag " + namespace.getTag());
                continue;  //*** Do not discard shared environment namespaces (during test runs)
            }
            
            try {
                namespace.discard(this.getFramework().getTestRunName());
            } catch(KubernetesManagerException e) {
                logger.error("Problem discarding namespace " + namespace.getId() + " on cluster " + namespace.getCluster().getId(), e);
            }
        }
        
        super.provisionDiscard();
    }

    /**
     * Build a map of all the defined clusters
     * 
     * @throws KubernetesManagerException
     */
    private void buildClusterMap() throws KubernetesManagerException {
        List<String> clusterIds = KubernetesClusters.get();

        for(String clusterId : clusterIds) {
            this.clusters.put(clusterId, new KubernetesClusterImpl(clusterId, dss, getFramework()));
        }
        return;
    }

    @Override
    public IKubernetesNamespace getNamespaceByTag(@NotNull String namespaceTag) {
        String tag = namespaceTag.trim().toUpperCase();

        return taggedNamespaces.get(tag);
    }

	@Override
	public ICloudContainer generateCloudContainer(String tag, 
			String platform, 
			String image, 
			ICloudContainerPort[] ports,
			Properties environmentProperties, 
			String[] runArguments) throws ManagerException, InsufficientResourcesAvailableException {
		return null;
	}

	@Override
	public @NotNull String getName() {
		return "Kubernetes";
	}
}
