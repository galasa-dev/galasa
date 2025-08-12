package dev.galasa.framework.k8s.controller.interruptedruns;

import dev.galasa.framework.k8s.controller.K8sControllerException;

import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import io.kubernetes.client.openapi.models.V1Pod;


public class PodDeleter {
    private final KubernetesEngineFacade kubeApi;
    
    public PodDeleter(KubernetesEngineFacade kubeApi) {
        this.kubeApi = kubeApi;
    }

    public void deletePod(String runName) throws K8sControllerException {
        V1Pod pod = kubeApi.getTestPod(runName);
        if ( pod != null ) {
            kubeApi.deletePod(pod);
        }
    }
    
}
