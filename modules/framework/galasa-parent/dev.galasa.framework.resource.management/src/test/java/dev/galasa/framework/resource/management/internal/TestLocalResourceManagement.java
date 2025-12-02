/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockDSSStore;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockStreamsService;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceManagementFactory;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceManagementProvider;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceManagementProviders;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceMonitorBundleLoader;
import dev.galasa.framework.resource.management.internal.mocks.MockScheduledExecutorService;
import dev.galasa.framework.spi.FrameworkException;

public class TestLocalResourceManagement {

    @Test
    public void testCanRunLocalResourceManagementWithNoProviders() throws Exception {
        // Given...
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());

        MockFramework mockFramework = new MockFramework(mockCps, mockDss);
        mockFramework.setStreamsService(mockStreamsService);

        MockResourceManagementProviders providers = new MockResourceManagementProviders();
        MockResourceMonitorBundleLoader monitorBundleLoader = new MockResourceMonitorBundleLoader();
        MockScheduledExecutorService scheduledExecutorService = new MockScheduledExecutorService();
        MockResourceManagementFactory factory = new MockResourceManagementFactory(mockFramework, providers, scheduledExecutorService, monitorBundleLoader);
        LocalResourceManagement resourceManagement = new LocalResourceManagement(factory);

        Properties bootstrapProps = new Properties();
        Properties overrideProps = new Properties();
        String stream = null;
        List<String> bundleIncludes = new ArrayList<>();
        List<String> bundleExcludes = new ArrayList<>();

        // When...
        resourceManagement.run(bootstrapProps, overrideProps, stream, bundleIncludes, bundleExcludes);

        // Then...
        assertThat(providers.getLoadedResourceManagementProviders()).isEmpty();
        assertThat(providers.isRunOnceCalled()).isFalse();
        assertThat(providers.isShutdown()).isTrue();
        assertThat(scheduledExecutorService.isShutdown()).isTrue();
    }

    @Test
    public void testCanRunLocalResourceManagementWithProviders() throws Exception {
        // Given...
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());

        MockFramework mockFramework = new MockFramework(mockCps, mockDss);
        mockFramework.setStreamsService(mockStreamsService);

        MockResourceManagementProviders providers = new MockResourceManagementProviders();
        MockResourceManagementProvider provider = new MockResourceManagementProvider();
        providers.addResourceManagementProvider(provider);

        MockResourceMonitorBundleLoader monitorBundleLoader = new MockResourceMonitorBundleLoader();
        MockScheduledExecutorService scheduledExecutorService = new MockScheduledExecutorService();
        MockResourceManagementFactory factory = new MockResourceManagementFactory(mockFramework, providers, scheduledExecutorService, monitorBundleLoader);
        LocalResourceManagement resourceManagement = new LocalResourceManagement(factory);

        Properties bootstrapProps = new Properties();
        Properties overrideProps = new Properties();
        String stream = null;
        List<String> bundleIncludes = new ArrayList<>();
        List<String> bundleExcludes = new ArrayList<>();

        // When...
        resourceManagement.run(bootstrapProps, overrideProps, stream, bundleIncludes, bundleExcludes);

        // Then...
        assertThat(providers.getLoadedResourceManagementProviders()).hasSize(1);
        assertThat(providers.isRunOnceCalled()).isTrue();
        assertThat(providers.isShutdown()).isTrue();
        assertThat(provider.isRunCalled()).isTrue();
        assertThat(scheduledExecutorService.isShutdown()).isTrue();
    }

    @Test
    public void testLocalResourceManagementShutsDownOnError() throws Exception {
        // Given...
        MockCPSStore mockCps = new MockCPSStore(new HashMap<>());
        MockDSSStore mockDss = new MockDSSStore(new HashMap<>());
        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());

        MockFramework mockFramework = new MockFramework(mockCps, mockDss);
        mockFramework.setStreamsService(mockStreamsService);

        MockResourceManagementProviders providers = new MockResourceManagementProviders();
        MockResourceManagementProvider provider = new MockResourceManagementProvider();
        providers.addResourceManagementProvider(provider);

        // This part simulates a framework error
        MockResourceMonitorBundleLoader monitorBundleLoader = new MockResourceMonitorBundleLoader();
        monitorBundleLoader.setSimulatedErrorRequested(true);

        MockScheduledExecutorService scheduledExecutorService = new MockScheduledExecutorService();
        MockResourceManagementFactory factory = new MockResourceManagementFactory(mockFramework, providers, scheduledExecutorService, monitorBundleLoader);
        LocalResourceManagement resourceManagement = new LocalResourceManagement(factory);

        Properties bootstrapProps = new Properties();
        Properties overrideProps = new Properties();
        String stream = null;
        List<String> bundleIncludes = new ArrayList<>();
        List<String> bundleExcludes = new ArrayList<>();

        // When...
        catchThrowableOfType(() -> {
            resourceManagement.run(bootstrapProps, overrideProps, stream, bundleIncludes, bundleExcludes);
        }, FrameworkException.class);

        // Then...
        assertThat(scheduledExecutorService.isShutdown()).isTrue();
    }
}
