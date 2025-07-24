/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;


public class SettingsTest {

    @Test
    public void testCanCreateASettingsObject() throws Exception {
        K8sController controller = new K8sController() {};
        KubernetesEngineFacade kube = null ;
        new Settings( controller, kube, "myPod", "myConfigMapName");
    }

    @Test
    public void testCanReadDefaultHeapSizeIfMissingFromConfigMap() throws Exception {
        K8sController controller = new K8sController() {};
        KubernetesEngineFacade kube = null ;
        Settings settings = new Settings( controller, kube, "myPod", "myConfigMapName");
        Map<String,String> configMap = new HashMap<String,String>();
        settings.updateConfigMapProperties(configMap);

        int heapSizeGotBack = settings.getEngineMemoryHeapSizeMegabytes();

        assertThat(heapSizeGotBack).isEqualTo(150);
    }

    @Test
    public void testCanReadNonDefaultHeapSizeIfPresentInConfigMap() throws Exception {
        K8sController controller = new K8sController() {};
        KubernetesEngineFacade kube = null ;
        Settings settings = new Settings( controller, kube, "myPod", "myConfigMapName");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put("engine_memory_heap","450");
        settings.updateConfigMapProperties(configMap);

        int heapSizeGotBack = settings.getEngineMemoryHeapSizeMegabytes();

        assertThat(heapSizeGotBack).isEqualTo(450);
    }

    @Test
    public void testCanReadDefaultKubeLaunchIntervalIfMissingFromConfigMap() throws Exception {
        K8sController controller = new K8sController();
        KubernetesEngineFacade kube = null ;
        Settings settings = new Settings( controller, kube, "myPod", "myConfigMapName");
        Map<String,String> configMap = new HashMap<String,String>();
        settings.updateConfigMapProperties(configMap);

        long intervalGotBack = settings.getKubeLaunchIntervalMillisecs();

        assertThat(intervalGotBack).isEqualTo(1000);
    }

    @Test
    public void testCanReadNonDefaultKubeLaunchIntervalIfPresentInConfigMap() throws Exception {
        K8sController controller = new K8sController();
        KubernetesEngineFacade kube = null ;
        Settings settings = new Settings( controller, kube, "myPod", "myConfigMapName");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put("kube_launch_interval_milliseconds", "50");
        settings.updateConfigMapProperties(configMap);

        long intervalGotBack = settings.getKubeLaunchIntervalMillisecs();

        assertThat(intervalGotBack).isEqualTo(50);
    }

    @Test
    public void testUsesDefaultKubeLaunchIntervalIfInvalidValueGivenInConfigMap() throws Exception {
        K8sController controller = new K8sController();
        KubernetesEngineFacade kube = null ;
        Settings settings = new Settings( controller, kube, "myPod", "myConfigMapName");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put("kube_launch_interval_milliseconds", "not a number!");
        settings.updateConfigMapProperties(configMap);

        long intervalGotBack = settings.getKubeLaunchIntervalMillisecs();

        assertThat(intervalGotBack).isEqualTo(1000);
    }


    @Test
    public void testCanReadNonDefaultMaxPodRetryLimitIfPresentInConfigMap() throws Exception {
        K8sController controller = new K8sController();
        KubernetesEngineFacade kube = null ;
        Settings settings = new Settings( controller, kube, "myPod", "myConfigMapName");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put(Settings.MAX_TEST_POD_RETRY_LIMIT_CONFIG_MAP_PROPERTY_NAME, "50");
        settings.updateConfigMapProperties(configMap);

        int gotBack = settings.getMaxTestPodRetryLimit();

        assertThat(gotBack).isEqualTo(50);
    }

    @Test
    public void testCanReadDefaultMaxPodRetryLimitIfMissingFromConfigMap() throws Exception {
        K8sController controller = new K8sController();
        KubernetesEngineFacade kube = null ;
        Settings settings = new Settings( controller, kube, "myPod", "myConfigMapName");
        Map<String,String> configMap = new HashMap<String,String>();
        settings.updateConfigMapProperties(configMap);

        int gotBack = settings.getMaxTestPodRetryLimit();

        assertThat(gotBack).isEqualTo(Settings.MAX_TEST_POD_RETRY_LIMIT_DEFAULT);
    }


    @Test
    public void testCanReadNonDefaultTestPodInterruptTimeoutSecsIfPresentInConfigMap() throws Exception {
        K8sController controller = new K8sController();
        KubernetesEngineFacade kube = null;
        Settings settings = new Settings(controller, kube, "myPod", "myConfigMapName");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.put(Settings.TEST_POD_INTERRUPT_TIMEOUT_SECS_PROPERTY_NAME, "50");
        settings.updateConfigMapProperties(configMap);

        long gotBack = settings.getTestPodInterruptTimeoutSecs();

        assertThat(gotBack).isEqualTo(50);
    }

    @Test
    public void testUsesDefaultTestPodInterruptTimeoutSecsIfMissingFromConfigMap() throws Exception {
        K8sController controller = new K8sController();
        KubernetesEngineFacade kube = null;
        Settings settings = new Settings(controller, kube, "myPod", "myConfigMapName");
        Map<String,String> configMap = new HashMap<String,String>();

        settings.updateConfigMapProperties(configMap);

        long gotBack = settings.getTestPodInterruptTimeoutSecs();

        assertThat(gotBack).isEqualTo(300);
    }
}