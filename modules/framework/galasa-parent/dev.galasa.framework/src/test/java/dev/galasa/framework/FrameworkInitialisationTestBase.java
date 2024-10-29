/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;

import dev.galasa.ICredentials;
import dev.galasa.framework.mocks.MockCPSRegistration;
import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockConfidentialTextStore;
import dev.galasa.framework.mocks.MockConfidentialTextStoreRegistration;
import dev.galasa.framework.mocks.MockCredentialsStore;
import dev.galasa.framework.mocks.MockCredentialsStoreRegistration;
import dev.galasa.framework.mocks.MockDSSRegistration;
import dev.galasa.framework.mocks.MockDSSStore;
import dev.galasa.framework.mocks.MockEventsService;
import dev.galasa.framework.mocks.MockEventsServiceRegistration;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockRASRegistration;
import dev.galasa.framework.mocks.MockRASStoreService;
import dev.galasa.framework.mocks.MockServiceReference;
import dev.galasa.framework.spi.IConfidentialTextServiceRegistration;
import dev.galasa.framework.spi.IConfigurationPropertyStoreRegistration;
import dev.galasa.framework.spi.IDynamicStatusStoreRegistration;
import dev.galasa.framework.spi.IEventsServiceRegistration;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreRegistration;
import dev.galasa.framework.spi.creds.ICredentialsStoreRegistration;

public class FrameworkInitialisationTestBase {

    protected MockFramework addMockFrameworkToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        MockFramework mockFramework = new MockFramework();
        MockServiceReference<IFramework> mockFrameworkRef = new MockServiceReference<>(mockFramework, bundle );
        services.put(IFramework.class.getName(),mockFrameworkRef);
        return mockFramework;
    }

    protected void addMockCPSToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Map<String,String> cpsProperties, Bundle bundle) {
        MockCPSStore mockCPSStore = new MockCPSStore(cpsProperties);
        MockCPSRegistration mockCPSRegistration = new MockCPSRegistration(mockCPSStore);
        MockServiceReference<IConfigurationPropertyStoreRegistration> mockCPSRef = new MockServiceReference<>(mockCPSRegistration, bundle );
        services.put(IConfigurationPropertyStoreRegistration.class.getName(),mockCPSRef);
    }

    protected MockDSSStore addMockDSSToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        Map<String,String> dssProps = new HashMap<String,String>();
        MockDSSStore mockDSSStore = new MockDSSStore(dssProps);
        MockDSSRegistration mockDSSRegistration = new MockDSSRegistration(mockDSSStore);
        MockServiceReference<IDynamicStatusStoreRegistration> mockDSSRef = 
            new MockServiceReference<>(mockDSSRegistration, bundle );
        services.put(IDynamicStatusStoreRegistration.class.getName(),mockDSSRef);
        return mockDSSStore;
    }

    protected MockRASStoreService addMockRASToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        Map<String,String> rasProps = new HashMap<String,String>();
        MockRASStoreService mockRASStoreService = new MockRASStoreService(rasProps);
        MockRASRegistration mockRASRegistration = new MockRASRegistration(mockRASStoreService);
        MockServiceReference<IResultArchiveStoreRegistration> mockRASRef = 
            new MockServiceReference<>(mockRASRegistration, bundle );
        services.put(IResultArchiveStoreRegistration.class.getName(),mockRASRef);
        return mockRASStoreService;
    }

    protected MockCredentialsStore addMockCredentialsStoreToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        Map<String,ICredentials> credsProps = new HashMap<String,ICredentials>();
        MockCredentialsStore mockCredentialsStore = new MockCredentialsStore(credsProps);
        MockCredentialsStoreRegistration mockCredentialsStoreRegistration = new MockCredentialsStoreRegistration(mockCredentialsStore);
        MockServiceReference<ICredentialsStoreRegistration> mockCredsRegRef = 
            new MockServiceReference<>(mockCredentialsStoreRegistration, bundle );
        services.put(ICredentialsStoreRegistration.class.getName(),mockCredsRegRef);
        return mockCredentialsStore;
    }

    protected MockConfidentialTextStore addMockConfidentialTextServiceToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        Map<String,String> confidentialTextProps = new HashMap<String,String>();
        MockConfidentialTextStore mockConfidentialTextStore = new MockConfidentialTextStore(confidentialTextProps);
        MockConfidentialTextStoreRegistration mockConfidentialTextStoreRegistration = new MockConfidentialTextStoreRegistration(mockConfidentialTextStore);
        MockServiceReference<IConfidentialTextServiceRegistration> mockConfidentialTextServiceRegRef = 
            new MockServiceReference<>(mockConfidentialTextStoreRegistration, bundle );
        services.put(IConfidentialTextServiceRegistration.class.getName(),mockConfidentialTextServiceRegRef);
        return mockConfidentialTextStore;
    }

    protected MockEventsService addMockEventsServiceToMockServiceRegistry(Map<String,MockServiceReference<?>> services, Bundle bundle) {
        MockEventsService mockEventsService = new MockEventsService();
        MockEventsServiceRegistration mockEventsServiceRegistration = new MockEventsServiceRegistration(mockEventsService);
        MockServiceReference<IEventsServiceRegistration> mockEventsServiceRegRef =
            new MockServiceReference<>(mockEventsServiceRegistration, bundle);
        services.put(IEventsServiceRegistration.class.getName(), mockEventsServiceRegRef);
        return mockEventsService;
    }
}
