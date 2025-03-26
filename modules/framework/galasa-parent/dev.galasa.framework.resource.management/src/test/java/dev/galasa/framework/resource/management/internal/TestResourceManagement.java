/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import static org.assertj.core.api.Assertions.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resolver;
import org.junit.Test;

import dev.galasa.framework.mocks.MockBundleManager;
import dev.galasa.framework.mocks.MockCapability;
import dev.galasa.framework.mocks.MockMavenRepository;
import dev.galasa.framework.mocks.MockRepository;
import dev.galasa.framework.mocks.MockRepositoryAdmin;
import dev.galasa.framework.mocks.MockResolver;
import dev.galasa.framework.mocks.MockResource;
import dev.galasa.framework.mocks.MockStream;
import dev.galasa.framework.mocks.MockStreamsService;
import dev.galasa.framework.spi.FrameworkException;
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
        
        String REPO_URL = "http://myhost/myRepositoryForMyRun";
        String BUNDLE_NAME_1 = "my.custom.bundle";
        String BUNDLE_NAME_2 = "my.other.custom.bundle";

        MockRepository mockRepo = new MockRepository(REPO_URL);
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

        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());

        // When...
        resourceManagement.loadMonitorBundles(mockBundleManager, stream, mockStreamsService);

        // Then...
        List<String> loadedBundles = mockBundleManager.getLoadedBundleSymbolicNames();
        assertThat(loadedBundles).hasSize(2);
        assertThat(loadedBundles).contains(BUNDLE_NAME_1, BUNDLE_NAME_2);
    }

    @Test
    public void testLoadMonitorBundlesAddsRepositoryFromTestStream() throws Exception {
        // Given...
        ResourceManagement resourceManagement = new ResourceManagement();
        
        String REPO_URL = "http://myhost/myRepositoryForMyRun";
        String STREAM_OBR_REPO_URL = "http://myhost/myOtherRepositoryForMyRun";
        String STREAM_MAVEN_REPO_URL = "http://myhost/myOtherMavenRepositoryForMyRun";
        String BUNDLE_NAME_1 = "my.custom.bundle";
        String BUNDLE_NAME_2 = "my.other.custom.bundle";

        MockRepository mockRepo = new MockRepository(REPO_URL);
        List<Repository> mockRepositories = List.of(mockRepo);

        MockResource mockResource1 = createMockBundleWithServiceCapability(BUNDLE_NAME_1);
        MockResource mockResource2 = createMockBundleWithServiceCapability(BUNDLE_NAME_2);
        mockRepo.addResource(mockResource1);
        mockRepo.addResource(mockResource2);

        boolean IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE = true;
        Resolver mockResolver = new MockResolver(IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE);
        MockRepositoryAdmin mockRepositoryAdmin = new MockRepositoryAdmin(mockRepositories, mockResolver);
        MockMavenRepository mockMavenRepository = new MockMavenRepository();

        resourceManagement.repositoryAdmin = mockRepositoryAdmin;
        resourceManagement.mavenRepository = mockMavenRepository;

        String stream = "myStream";

        MockBundleManager mockBundleManager = new MockBundleManager();

        MockStream mockStream = new MockStream();
        mockStream.setName(stream);
        mockStream.setObrLocation(STREAM_OBR_REPO_URL);
        mockStream.setMavenRepositoryUrl(STREAM_MAVEN_REPO_URL);

        MockStreamsService mockStreamsService = new MockStreamsService(List.of(mockStream));

        // When...
        resourceManagement.loadMonitorBundles(mockBundleManager, stream, mockStreamsService);

        // Then...
        // Check that the maven repository associated with the stream has been added
        List<URL> remoteRepos = mockMavenRepository.getRemoteRepositories();
        assertThat(remoteRepos).hasSize(1);
        assertThat(remoteRepos.get(0).toString()).isEqualTo(STREAM_MAVEN_REPO_URL);

        // Check that the OBR associated with the stream has been added
        Repository[] obrRepositories = mockRepositoryAdmin.listRepositories();
        assertThat(obrRepositories).hasSize(2);
        assertThat(obrRepositories[1].getURI()).isEqualTo(STREAM_OBR_REPO_URL);
        
        List<String> loadedBundles = mockBundleManager.getLoadedBundleSymbolicNames();
        assertThat(loadedBundles).hasSize(2);
        assertThat(loadedBundles).contains(BUNDLE_NAME_1, BUNDLE_NAME_2);
    }

    @Test
    public void testLoadMonitorBundlesWithBadStreamOBRThrowsCorrectError() throws Exception {
        // Given...
        ResourceManagement resourceManagement = new ResourceManagement();
        
        String REPO_URL = "http://myhost/myRepositoryForMyRun";
        String STREAM_MAVEN_REPO_URL = "http://myhost/myOtherMavenRepositoryForMyRun";
        String BUNDLE_NAME_1 = "my.custom.bundle";
        String BUNDLE_NAME_2 = "my.other.custom.bundle";

        MockRepository mockRepo = new MockRepository(REPO_URL);
        List<Repository> mockRepositories = List.of(mockRepo);

        MockResource mockResource1 = createMockBundleWithServiceCapability(BUNDLE_NAME_1);
        MockResource mockResource2 = createMockBundleWithServiceCapability(BUNDLE_NAME_2);
        mockRepo.addResource(mockResource1);
        mockRepo.addResource(mockResource2);

        boolean IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE = true;
        Resolver mockResolver = new MockResolver(IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE);
        MockRepositoryAdmin mockRepositoryAdmin = new MockRepositoryAdmin(mockRepositories, mockResolver);
        MockMavenRepository mockMavenRepository = new MockMavenRepository();

        resourceManagement.repositoryAdmin = mockRepositoryAdmin;
        resourceManagement.mavenRepository = mockMavenRepository;

        String stream = "myStream";

        MockBundleManager mockBundleManager = new MockBundleManager();

        MockStream mockStream = new MockStream();
        mockStream.setName(stream);
        mockStream.setMavenRepositoryUrl(STREAM_MAVEN_REPO_URL);

        MockStreamsService mockStreamsService = new MockStreamsService(List.of(mockStream));

        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            resourceManagement.loadMonitorBundles(mockBundleManager, stream, mockStreamsService);
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown).hasMessageContaining("No OBR has been configured into the provided test stream");
    }

    @Test
    public void testLoadMonitorBundlesWithBadStreamMavenRepoThrowsCorrectError() throws Exception {
        // Given...
        ResourceManagement resourceManagement = new ResourceManagement();
        
        String REPO_URL = "http://myhost/myRepositoryForMyRun";
        String STREAM_OBR_REPO_URL = "http://myhost/myOtherRepositoryForMyRun";
        String BUNDLE_NAME_1 = "my.custom.bundle";
        String BUNDLE_NAME_2 = "my.other.custom.bundle";

        MockRepository mockRepo = new MockRepository(REPO_URL);
        List<Repository> mockRepositories = List.of(mockRepo);

        MockResource mockResource1 = createMockBundleWithServiceCapability(BUNDLE_NAME_1);
        MockResource mockResource2 = createMockBundleWithServiceCapability(BUNDLE_NAME_2);
        mockRepo.addResource(mockResource1);
        mockRepo.addResource(mockResource2);

        boolean IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE = true;
        Resolver mockResolver = new MockResolver(IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE);
        MockRepositoryAdmin mockRepositoryAdmin = new MockRepositoryAdmin(mockRepositories, mockResolver);
        MockMavenRepository mockMavenRepository = new MockMavenRepository();

        resourceManagement.repositoryAdmin = mockRepositoryAdmin;
        resourceManagement.mavenRepository = mockMavenRepository;

        String stream = "myStream";

        MockBundleManager mockBundleManager = new MockBundleManager();

        MockStream mockStream = new MockStream();
        mockStream.setName(stream);
        mockStream.setObrLocation(STREAM_OBR_REPO_URL);

        MockStreamsService mockStreamsService = new MockStreamsService(List.of(mockStream));

        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            resourceManagement.loadMonitorBundles(mockBundleManager, stream, mockStreamsService);
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown).hasMessageContaining("No remote maven repository has been configured into the provided test stream");
    }
}
