package dev.galasa.framework.k8s.controller.mocks;

import java.util.List;

import dev.galasa.framework.k8s.controller.ISettings;

public class MockISettings implements ISettings {

    public int maxTestPodRetriesLimit = 2;

    @Override
    public String getEngineLabel() {
        return "myEngineLabel";
    }

    @Override
    public String getPodName() {
        return "myPodName";
    }

    @Override
    public String getNamespace() {
        return "myNamespace1";
    }

    @Override
    public String getNodeArch() {
        return "myNodeArch";
    }

    @Override
    public String getNodePreferredAffinity() {
        return "MyNodePreferredAffinity=MyNodePreferredAffinityValue:23";
    }

    @Override
    public String getNodeTolerations() {
        return "MyNodeTolerations=MyNodeTolerationsValue:23";
    }

    @Override
    public String getEncryptionKeysSecretName() {
        return "myFakeEncKeySecName";
    }

    @Override
    public String getEngineImage() {
        return "myEngineImage";
    }

    @Override
    public int getMaxEngines() {
        return 5;
    }

    @Override
    public int getEngineMemoryRequestMegabytes() {
        return 300;
    }

    @Override
    public int getEngineCPURequestM() {
        return 200;
    }

    @Override
    public int getEngineMemoryLimitMegabytes() {
        return 350;
    }

    @Override
    public int getEngineMemoryHeapSizeMegabytes() {
        return 400;
    }

    @Override
    public int getEngineCPULimitM() {
        return 300;
    }

    @Override
    public long getPollSeconds() {
        return 5;
    }

    @Override
    public long getKubeLaunchIntervalMillisecs() {
        return 200;
    }

    @Override
    public int getMaxTestPodRetryLimit() {
        return maxTestPodRetriesLimit;
    }

        @Override
    public List<String> getRequestorsByGroup() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRequestorsByGroup'");
    }
    
}
