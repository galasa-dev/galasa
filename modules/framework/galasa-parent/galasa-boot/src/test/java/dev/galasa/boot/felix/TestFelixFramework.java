/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.felix;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.felix.bundlerepository.Resource;
import org.junit.Test;
import org.osgi.framework.Bundle;

import dev.galasa.boot.mocks.MockRunnableService;
import dev.galasa.boot.LauncherException;
import dev.galasa.boot.mocks.MockBundle;
import dev.galasa.boot.mocks.MockBundleContext;
import dev.galasa.boot.mocks.MockFelixFramework;
import dev.galasa.boot.mocks.MockOsgiFramework;
import dev.galasa.boot.mocks.MockRepositoryAdmin;
import dev.galasa.boot.mocks.MockResolver;
import dev.galasa.boot.mocks.MockServiceReference;

public class TestFelixFramework {

    @Test
    public void testRunWebApiServerLoadsExtraApiBundles() throws Exception {
        // Given...
        String extraBundleName = "my.api.bundle";

        MockResolver mockResolver = new MockResolver();
        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(mockResolver);

        Map<String, MockServiceReference<?>> services = new HashMap<>();
        MockServiceReference<MockRunnableService> mockApiStartup = new MockServiceReference<>(new MockRunnableService(), null);
        services.put("dev.galasa.framework.api.internal.ApiStartup", mockApiStartup);

        MockBundleContext mockFrameworkBundleContext = new MockBundleContext(services);
        MockBundle mockFrameworkBundle = new MockBundle("dev.galasa.framework", mockFrameworkBundleContext);

        Bundle[] availableBundles = new Bundle[] {
            mockFrameworkBundle,
            new MockBundle("org.apache.felix.http.servlet-api"),
            new MockBundle("org.apache.felix.http.jetty"),
            new MockBundle("org.apache.felix.fileinstall"),
            new MockBundle("dev.galasa.framework.api"),
            new MockBundle(extraBundleName),
        };

        MockBundleContext mockBundleContext = new MockBundleContext(availableBundles);
        MockOsgiFramework mockOsgiFramework = new MockOsgiFramework(mockBundleContext);

        FelixFramework felixFramework = new MockFelixFramework(mockOsgiFramework, mockRepoAdmin);
        Properties bootstrapProperties = new Properties();
        Properties overridesProperties = new Properties();

        bootstrapProperties.put("api.extra.bundles", extraBundleName);

        // When...
        felixFramework.runWebApiServer(bootstrapProperties, overridesProperties, new ArrayList<>(), 0, 0);

        // Then...
        List<String> addedResourceIds = mockResolver.getAllResources()
            .stream()
            .map(Resource::getId)
            .collect(Collectors.toList());

        assertThat(addedResourceIds).contains(extraBundleName);
    }

    @Test
    public void testRunWebApiServerWithInactiveBundleThrowsException() throws Exception {
        // Given...
        String extraBundleName = "my.api.bundle";

        MockResolver mockResolver = new MockResolver();
        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(mockResolver);

        Map<String, MockServiceReference<?>> services = new HashMap<>();
        MockServiceReference<MockRunnableService> mockApiStartup = new MockServiceReference<>(new MockRunnableService(), null);
        services.put("dev.galasa.framework.api.internal.ApiStartup", mockApiStartup);

        MockBundleContext mockFrameworkBundleContext = new MockBundleContext(services);
        MockBundle mockFrameworkBundle = new MockBundle("dev.galasa.framework", mockFrameworkBundleContext);

        String frameworkApiBundleName = "dev.galasa.framework.api";
        MockBundle inactiveBundle = new MockBundle(frameworkApiBundleName);
        inactiveBundle.setState(Bundle.UNINSTALLED);

        Bundle[] availableBundles = new Bundle[] {
            mockFrameworkBundle,
            new MockBundle("org.apache.felix.http.servlet-api"),
            new MockBundle("org.apache.felix.http.jetty"),
            new MockBundle("org.apache.felix.fileinstall"),
            inactiveBundle,
            new MockBundle(extraBundleName),
        };

        MockBundleContext mockBundleContext = new MockBundleContext(availableBundles);
        MockOsgiFramework mockOsgiFramework = new MockOsgiFramework(mockBundleContext);

        FelixFramework felixFramework = new MockFelixFramework(mockOsgiFramework, mockRepoAdmin);
        Properties bootstrapProperties = new Properties();
        Properties overridesProperties = new Properties();

        bootstrapProperties.put("api.extra.bundles", extraBundleName);

        // When...
        LauncherException err = catchThrowableOfType(() -> {
            felixFramework.runWebApiServer(bootstrapProperties, overridesProperties, new ArrayList<>(), 0, 0);
        }, LauncherException.class);

        // Then...
        assertThat(err).isNotNull();
        assertThat(err.getMessage()).contains("Unable to install bundle", frameworkApiBundleName, "from OBR repository");
        assertThat(err.getCause().getMessage()).contains("Bundle '" + frameworkApiBundleName + "' failed to install and activate");
    }

    @Test
    public void testRunWebApiServerLoadsMultipleExtraApiBundles() throws Exception {
        // Given...
        String extraBundle1 = "my.api.bundle";
        String extraBundle2 = "another.extra.api.bundle";
        String extraBundle3 = "oh.look.ANOTHER.api.bundle";

        MockResolver mockResolver = new MockResolver();
        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(mockResolver);

        Map<String, MockServiceReference<?>> services = new HashMap<>();
        MockServiceReference<MockRunnableService> mockApiStartup = new MockServiceReference<>(new MockRunnableService(), null);
        services.put("dev.galasa.framework.api.internal.ApiStartup", mockApiStartup);

        MockBundleContext mockFrameworkBundleContext = new MockBundleContext(services);
        MockBundle mockFrameworkBundle = new MockBundle("dev.galasa.framework", mockFrameworkBundleContext);

        Bundle[] availableBundles = new Bundle[] {
            mockFrameworkBundle,
            new MockBundle("org.apache.felix.http.servlet-api"),
            new MockBundle("org.apache.felix.http.jetty"),
            new MockBundle("org.apache.felix.fileinstall"),
            new MockBundle("dev.galasa.framework.api"),
            new MockBundle(extraBundle3),
            new MockBundle(extraBundle2),
            new MockBundle(extraBundle1),
        };

        MockBundleContext mockBundleContext = new MockBundleContext(availableBundles);
        MockOsgiFramework mockOsgiFramework = new MockOsgiFramework(mockBundleContext);

        FelixFramework felixFramework = new MockFelixFramework(mockOsgiFramework, mockRepoAdmin);
        Properties bootstrapProperties = new Properties();
        Properties overridesProperties = new Properties();

        bootstrapProperties.put("api.extra.bundles", String.join(",", extraBundle1, extraBundle2, extraBundle3));

        // When...
        felixFramework.runWebApiServer(bootstrapProperties, overridesProperties, new ArrayList<>(), 0, 0);

        // Then...
        List<String> addedResourceIds = mockResolver.getAllResources()
            .stream()
            .map(Resource::getId)
            .collect(Collectors.toList());

        assertThat(addedResourceIds).contains(extraBundle1);
        assertThat(addedResourceIds).contains(extraBundle2);
        assertThat(addedResourceIds).contains(extraBundle3);
    }

    @Test
    public void testRunWebApiServerWithoutExtraApiBundlesDoesNotLoadBundles() throws Exception {
        // Given...
        String extraBundleName = "dont.load.this.bundle";

        MockResolver mockResolver = new MockResolver();
        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(mockResolver);

        Map<String, MockServiceReference<?>> services = new HashMap<>();
        MockServiceReference<MockRunnableService> mockApiStartup = new MockServiceReference<>(new MockRunnableService(), null);
        services.put("dev.galasa.framework.api.internal.ApiStartup", mockApiStartup);

        MockBundleContext mockFrameworkBundleContext = new MockBundleContext(services);
        MockBundle mockFrameworkBundle = new MockBundle("dev.galasa.framework", mockFrameworkBundleContext);

        Bundle[] availableBundles = new Bundle[] {
            mockFrameworkBundle,
            new MockBundle("org.apache.felix.http.servlet-api"),
            new MockBundle("org.apache.felix.http.jetty"),
            new MockBundle("org.apache.felix.fileinstall"),
            new MockBundle("dev.galasa.framework.api"),
            new MockBundle(extraBundleName),
        };

        MockBundleContext mockBundleContext = new MockBundleContext(availableBundles);
        MockOsgiFramework mockOsgiFramework = new MockOsgiFramework(mockBundleContext);

        FelixFramework felixFramework = new MockFelixFramework(mockOsgiFramework, mockRepoAdmin);
        
        // Bootstrap properties don't contain the api.extra.bundles property
        Properties bootstrapProperties = new Properties();
        Properties overridesProperties = new Properties();

        // When...
        felixFramework.runWebApiServer(bootstrapProperties, overridesProperties, new ArrayList<>(), 0, 0);

        // Then...
        List<String> addedResourceIds = mockResolver.getAllResources()
            .stream()
            .map(Resource::getId)
            .collect(Collectors.toList());

        assertThat(addedResourceIds).doesNotContain(extraBundleName);
    }
}
