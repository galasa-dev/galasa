/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

/**
 * A collection of settings obtained from a config map.
 * 
 * Every so often (see the REFRESH_DELAY constants in this class), the values will be re-loaded,
 * making the settings dynamically changeable if someone changes the config map settings.
 */
public interface ISettings {

    public String getPodName();

    public String getNamespace();

    public String getEngineLabel() ;

    public int getMaxEngines() ;

    public List<String> getRequestorsByGroup() ;

    public String getNodeArch() ;

    public String getNodePreferredAffinity();    
    public String getNodeTolerations();


    public String getEngineImage() ;

    public int getEngineMemoryRequestMegabytes();
    public int getEngineCPURequestM();

    public int getEngineMemoryLimitMegabytes() ;

    public int getEngineMemoryHeapSizeMegabytes();

    public int getEngineCPULimitM();

    public long getPollSeconds() ;

    public String getEncryptionKeysSecretName();

    public long getKubeLaunchIntervalMillisecs();

    public int getMaxTestPodRetryLimit();
}

