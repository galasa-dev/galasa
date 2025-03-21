/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resolver;
import org.junit.Test;

import dev.galasa.framework.mocks.MockBundleManager;
import dev.galasa.framework.mocks.MockCapability;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockRepository;
import dev.galasa.framework.mocks.MockRepositoryAdmin;
import dev.galasa.framework.mocks.MockResolver;
import dev.galasa.framework.mocks.MockResource;
import dev.galasa.framework.spi.IResourceManagementProvider;

public class TestResourceManagement {

    private MockResource createMockBundleWithServiceCapability(String bundleName) {
        String RESOURCE_MANAGEMENT_PROVIDER_CLASS = IResourceManagementProvider.class.getCanonicalName();
        String TEST_STREAM_OBR = "http://myhost/myObrForMyRun";

        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("objectClass", RESOURCE_MANAGEMENT_PROVIDER_CLASS);

        MockCapability mockCapability = new MockCapability("service", propertyMap);
        List<Capability> mockCapabilities = List.of(mockCapability);

        MockResource mockResource = new MockResource(TEST_STREAM_OBR);
        mockResource.setSymbolicName(bundleName);
        mockResource.setCapabilities(mockCapabilities);
        return mockResource;
    }

    @Test
    public void testLoadMonitorBundlesCanLoadBundles() throws Exception {
        // Given...
        ResourceManagement resourceManagement = new ResourceManagement();
        
        String TEST_STREAM_REPO_URL = "http://myhost/myRepositoryForMyRun";
        String BUNDLE_NAME_1 = "my.custom.bundle";
        String BUNDLE_NAME_2 = "my.other.custom.bundle";

        MockRepository mockRepo = new MockRepository(TEST_STREAM_REPO_URL);
        List<Repository> mockRepositories = List.of(mockRepo);

        MockResource mockResource1 = createMockBundleWithServiceCapability(BUNDLE_NAME_1);
        MockResource mockResource2 = createMockBundleWithServiceCapability(BUNDLE_NAME_2);
        mockRepo.addResource(mockResource1);
        mockRepo.addResource(mockResource2);

        boolean IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE = true;
        Resolver mockResolver = new MockResolver(IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE);
        MockRepositoryAdmin mockRepositoryAdmin = new MockRepositoryAdmin(mockRepositories, mockResolver);

        resourceManagement.repositoryAdmin = mockRepositoryAdmin;

        String stream = null;

        MockBundleManager mockBundleManager = new MockBundleManager();

        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        // When...
        resourceManagement.loadMonitorBundles(mockBundleManager, stream, mockCps);

        // Then...
        List<String> loadedBundles = mockBundleManager.getLoadedBundleSymbolicNames();
        assertThat(loadedBundles).hasSize(2);
        assertThat(loadedBundles).contains(BUNDLE_NAME_1, BUNDLE_NAME_2);
    }
}
