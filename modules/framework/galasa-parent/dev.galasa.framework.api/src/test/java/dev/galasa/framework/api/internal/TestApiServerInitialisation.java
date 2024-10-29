/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.internal;

import dev.galasa.framework.FrameworkInitialisationTestBase;
import dev.galasa.framework.api.mocks.MockAuthStore;
import dev.galasa.framework.api.mocks.MockAuthStoreRegistration;
import dev.galasa.framework.mocks.MockBundleContext;
import dev.galasa.framework.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockLog;
import dev.galasa.framework.mocks.MockServiceReference;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreRegistration;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.osgi.framework.Bundle;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.*;

public class TestApiServerInitialisation extends FrameworkInitialisationTestBase {

    private ApiServerInitialisation createApiServerInit(Properties bootstrapProps) throws Exception {
        return createApiServerInit(bootstrapProps, new MockEnvironment());
    }

    private ApiServerInitialisation createApiServerInit(Properties bootstrapProps, MockEnvironment mockEnv)
            throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<String, String>();
        Properties overrideProperties = new Properties();
        Log logger = new MockLog();
        Bundle bundle = null;

        // A fake OSGi service registry...
        Map<String, MockServiceReference<?>> services = new HashMap<String, MockServiceReference<?>>();

        MockFramework mockFramework = addMockFrameworkToMockServiceRegistry(services, bundle);

        addMockCPSToMockServiceRegistry(services, cpsProperties, bundle);
        addMockConfidentialTextServiceToMockServiceRegistry(services, bundle);
        addMockDSSToMockServiceRegistry(services, bundle);
        addMockRASToMockServiceRegistry(services, bundle);
        addMockCredentialsStoreToMockServiceRegistry(services, bundle);
        addMockEventsServiceToMockServiceRegistry(services, bundle);

        MockAuthStore mockAuthStore = addMockAuthStoreToMockServiceRegistry(services, bundle);

        MockBundleContext bundleContext = new MockBundleContext(services);
        MockFileSystem mockFileSystem = new MockFileSystem();

        // When...
        ApiServerInitialisation frameworkInitUnderTest = new ApiServerInitialisation(bootstrapProps,
                overrideProperties, logger, bundleContext, mockFileSystem, mockEnv);

        // Then...
        assertThat(mockFramework.getAuthStore()).isEqualTo(mockAuthStore);

        return frameworkInitUnderTest;
    }

    private MockAuthStore addMockAuthStoreToMockServiceRegistry(Map<String, MockServiceReference<?>> services, Bundle bundle) {
        MockAuthStore mockAuthStore = new MockAuthStore();
        MockAuthStoreRegistration mockAuthStoreRegistration = new MockAuthStoreRegistration(mockAuthStore);
        MockServiceReference<IAuthStoreRegistration> mockAuthStoreRef = new MockServiceReference<>(mockAuthStoreRegistration, bundle);
        services.put(IAuthStoreRegistration.class.getName(), mockAuthStoreRef);
        return mockAuthStore;
    }

    @Test
    public void testLocateAuthStoreDefaultsToNull() throws Exception {

        // Given...
        Properties bootstrap = new Properties();

        // The framework.auth.store property hasn't been set, so there is no auth store to use.
        ApiServerInitialisation frameworkInit = createApiServerInit(bootstrap);

        Log logger = new MockLog();

        // When...
        URI uri = frameworkInit.locateAuthStore(logger, bootstrap);

        // Then...
        assertThat(uri).isNull();
    }

    @Test
    public void testLocateAuthStoreGetsFrameworkAuthStoreUri() throws Exception {

        // Given...
        Properties bootstrap = new Properties();

        URI authStoreUri = URI.create("couchdb:http://my-user-store");

        bootstrap.setProperty("framework.auth.store", authStoreUri.toString());
        ApiServerInitialisation frameworkInit = createApiServerInit(bootstrap);

        Log logger = new MockLog();

        // When...
        URI uri = frameworkInit.locateAuthStore(logger, bootstrap);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri).isEqualTo(authStoreUri);
    }

    @Test
    public void testInitialiseAuthStoreSetsFrameworkAuthStore() throws Exception {

        // Given...
        Properties bootstrap = new Properties();

        URI authStoreUri = URI.create("couchdb:http://my-user-store");

        bootstrap.setProperty("framework.auth.store", authStoreUri.toString());
        ApiServerInitialisation frameworkInit = createApiServerInit(bootstrap);

        // When...
        IAuthStore authStore = frameworkInit.getFramework().getAuthStore();

        // Then...
        assertThat(authStore).isNotNull();
        assertThat(authStore.getClass().getName()).isEqualTo(MockAuthStore.class.getName());
    }
}