/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.util.List;

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

    public long getInterruptedTestRunCleanupGracePeriodSeconds();

    public long getAllocatedTestRunTimeoutMinutes();
}

