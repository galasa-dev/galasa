/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.ArtifactManager;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;
import dev.galasa.kubernetes.IConfigMap;
import dev.galasa.kubernetes.IDeployment;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.IPersistentVolumeClaim;
import dev.galasa.kubernetes.ISecret;
import dev.galasa.kubernetes.IService;
import dev.galasa.kubernetes.IStatefulSet;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.KubernetesNamespace;

@Test
public class KubernetesManagerIVT {
    
    @Logger
    public Log logger;
    
    @KubernetesNamespace
    public IKubernetesNamespace namespace;
    
    @ArtifactManager
    public IArtifactManager artifactManager;
    
    @Test
    public void ensureSetupOk() {
        assertThat(this.namespace).as("Kubernetes Namespace").isNotNull();
    }
    
    @Test
    public void createConfigMap() throws TestBundleResourceException, IOException, KubernetesManagerException {
        IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
        
        String configMapYaml = bundleResources.streamAsString(bundleResources.retrieveFile("/testConfigMap.yaml"));
        
        IConfigMap configMap = (IConfigMap) namespace.createResource(configMapYaml);
        
        logger.info("YAML for the created ConfigMap:-\n" + configMap.getYaml());     
    }

    @Test
    public void createPvc() throws TestBundleResourceException, IOException, KubernetesManagerException {
        IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
        
        String pvcYaml = bundleResources.streamAsString(bundleResources.retrieveFile("/testPvc.yaml"));
        
        IPersistentVolumeClaim persistentVolumeClaim = (IPersistentVolumeClaim) namespace.createResource(pvcYaml);
        
        logger.info("YAML for the created PersistentVolumeClaim:-\n" + persistentVolumeClaim.getYaml());     
    }

    @Test
    public void createSecret() throws TestBundleResourceException, IOException, KubernetesManagerException {
        IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
        
        String secretYaml = bundleResources.streamAsString(bundleResources.retrieveFile("/testSecret.yaml"));
        
        ISecret secret = (ISecret) namespace.createResource(secretYaml);
        
        logger.info("YAML for the created Secret:-\n" + secret.getYaml());     
    }

    @Test
    public void createDeployment() throws TestBundleResourceException, IOException, KubernetesManagerException {
        IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
        
        String deploymentYaml = bundleResources.streamAsString(bundleResources.retrieveFile("/testDeployment.yaml"));
        
        IDeployment deployment = (IDeployment) namespace.createResource(deploymentYaml);
        
        logger.info("YAML for the created Deployment:-\n" + deployment.getYaml());     
    }

    @Test
    public void createStatefulSet() throws TestBundleResourceException, IOException, KubernetesManagerException {
        IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
        
        String statefulSetYaml = bundleResources.streamAsString(bundleResources.retrieveFile("/testStatefulSet.yaml"));
        
        IStatefulSet statefulSet = (IStatefulSet) namespace.createResource(statefulSetYaml);
        
        logger.info("YAML for the created StatefulSet:-\n" + statefulSet.getYaml());     
    }

    @Test
    public void createService() throws TestBundleResourceException, IOException, KubernetesManagerException {
        IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
        
        String serviceYaml = bundleResources.streamAsString(bundleResources.retrieveFile("/testService.yaml"));
        
        IService service = (IService) namespace.createResource(serviceYaml);
        
        logger.info("YAML for the created Service:-\n" + service.getYaml());     
    }

}
