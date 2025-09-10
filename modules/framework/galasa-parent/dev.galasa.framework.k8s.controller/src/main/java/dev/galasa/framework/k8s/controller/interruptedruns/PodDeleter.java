package dev.galasa.framework.k8s.controller.interruptedruns;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.k8s.controller.K8sControllerException;

import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import io.kubernetes.client.openapi.models.V1Pod;


public class PodDeleter {
    private Log logger = LogFactory.getLog(getClass());

    private final KubernetesEngineFacade kubeApi;
    
    public PodDeleter(KubernetesEngineFacade kubeApi) {
        this.kubeApi = kubeApi;
    }

    public void deletePod(String runName) throws K8sControllerException {
        V1Pod pod = kubeApi.getTestPod(runName);
        if ( pod != null ) {
            kubeApi.deletePod(pod);
            logger.info("Deleted pod for run " + runName);
        } else {
            logger.info("No pod to delete was found for run " + runName);
        }
    }
    
}
