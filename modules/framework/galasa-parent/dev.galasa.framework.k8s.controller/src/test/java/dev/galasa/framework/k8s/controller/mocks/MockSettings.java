/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.mocks;

import dev.galasa.framework.k8s.controller.K8sController;
import dev.galasa.framework.k8s.controller.K8sControllerException;
import dev.galasa.framework.k8s.controller.Settings;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;

public class MockSettings extends Settings {

    public MockSettings(K8sController controller, KubernetesEngineFacade kube, String podName , String engineName) throws K8sControllerException {
        super(controller, kube, podName , engineName);
    }

}