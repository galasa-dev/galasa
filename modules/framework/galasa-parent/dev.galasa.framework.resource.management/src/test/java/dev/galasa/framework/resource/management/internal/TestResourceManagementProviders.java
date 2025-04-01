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
import java.util.Map;

import org.junit.Test;
import org.osgi.framework.Bundle;

import dev.galasa.framework.mocks.MockBundleContext;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockServiceReference;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceManagement;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceManagementProvider;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IResourceManagementProvider;

public class TestResourceManagementProviders {

    private void addMockResourceMonitorToMockServiceRegistry(
        Map<String,MockServiceReference<?>> services,
        Bundle bundle,
        IResourceManagementProvider mockResourceMonitor
    ) {
        MockServiceReference<IResourceManagementProvider> mockProviderService = new MockServiceReference<IResourceManagementProvider>(mockResourceMonitor, bundle);
        services.put(IResourceManagementProvider.class.getName(), mockProviderService);
    }

    @Test
    public void testConstructorInitialisesResourceMonitors() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        Map<String, MockServiceReference<?>> mockServices = new HashMap<>();
        Bundle bundle = null;

        MockResourceManagementProvider registeredMonitor = new MockResourceManagementProvider();
        addMockResourceMonitorToMockServiceRegistry(mockServices, bundle, registeredMonitor);

        MockBundleContext mockBundleContext = new MockBundleContext(mockServices);

        MockResourceManagement mockResourceManagement = new MockResourceManagement();

        String stream = null;
        List<String> includes = List.of("*");
        List<String> excludes = new ArrayList<>();

        MonitorConfiguration monitorConfig = new MonitorConfiguration(stream, includes, excludes);

        // When...
        new ResourceManagementProviders(
            mockFramework,
            mockCps,
            mockBundleContext,
            mockResourceManagement,
            monitorConfig
        );

        // Then...
        assertThat(registeredMonitor.isInitialised()).isTrue();
        assertThat(registeredMonitor.isStarted()).isFalse();
    }

    @Test
    public void testInvalidMonitorIncludesReturnsCorrectError() throws Exception {
        // Given...
        Map<String, MockServiceReference<?>> mockServices = new HashMap<>();
        Bundle bundle = null;

        MockResourceManagementProvider registeredMonitor = new MockResourceManagementProvider();
        addMockResourceMonitorToMockServiceRegistry(mockServices, bundle, registeredMonitor);

        String stream = null;
        List<String> includes = List.of("[NOT a %^ valid pattern!");
        List<String> excludes = new ArrayList<>();
        
        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            new MonitorConfiguration(stream, includes, excludes);
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unsupported glob pattern character provided");
    }

    @Test
    public void testInvalidMonitorExcludesReturnsCorrectError() throws Exception {
        // Given...
        Map<String, MockServiceReference<?>> mockServices = new HashMap<>();
        Bundle bundle = null;

        MockResourceManagementProvider registeredMonitor = new MockResourceManagementProvider();
        addMockResourceMonitorToMockServiceRegistry(mockServices, bundle, registeredMonitor);

        String stream = null;
        List<String> includes = new ArrayList<>();
        List<String> excludes = List.of("[NOT a %^ valid pattern!");
        
        // When...
        FrameworkException thrown = catchThrowableOfType(() -> {
            new MonitorConfiguration(stream, includes, excludes);
        }, FrameworkException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unsupported glob pattern character provided");
    }

    @Test
    public void testStartProvidersStartsResourceMonitors() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        Map<String, MockServiceReference<?>> mockServices = new HashMap<>();
        Bundle bundle = null;

        MockResourceManagementProvider registeredMonitor = new MockResourceManagementProvider();
        addMockResourceMonitorToMockServiceRegistry(mockServices, bundle, registeredMonitor);

        MockBundleContext mockBundleContext = new MockBundleContext(mockServices);

        MockResourceManagement mockResourceManagement = new MockResourceManagement();

        String stream = null;
        List<String> includes = List.of(MockResourceManagementProvider.class.getCanonicalName());
        List<String> excludes = new ArrayList<>();

        MonitorConfiguration monitorConfig = new MonitorConfiguration(stream, includes, excludes);
        
        ResourceManagementProviders monitors = new ResourceManagementProviders(
            mockFramework,
            mockCps,
            mockBundleContext,
            mockResourceManagement,
            monitorConfig
        );

        // When...
        monitors.start();

        // Then...
        assertThat(registeredMonitor.isInitialised()).isTrue();
        assertThat(registeredMonitor.isStarted()).isTrue();
    }

    @Test
    public void testStartProvidersDoesNotStartExcludedResourceMonitors() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        Map<String, MockServiceReference<?>> mockServices = new HashMap<>();
        Bundle bundle = null;

        MockResourceManagementProvider registeredMonitor = new MockResourceManagementProvider();
        addMockResourceMonitorToMockServiceRegistry(mockServices, bundle, registeredMonitor);

        MockBundleContext mockBundleContext = new MockBundleContext(mockServices);

        MockResourceManagement mockResourceManagement = new MockResourceManagement();

        String stream = null;
        List<String> includes = List.of("*");
        List<String> excludes = List.of(MockResourceManagementProvider.class.getCanonicalName());

        MonitorConfiguration monitorConfig = new MonitorConfiguration(stream, includes, excludes);
        
        ResourceManagementProviders monitors = new ResourceManagementProviders(
            mockFramework,
            mockCps,
            mockBundleContext,
            mockResourceManagement,
            monitorConfig
        );

        // When...
        monitors.start();

        // Then...
        assertThat(registeredMonitor.isInitialised()).isFalse();
        assertThat(registeredMonitor.isStarted()).isFalse();
    }
}
