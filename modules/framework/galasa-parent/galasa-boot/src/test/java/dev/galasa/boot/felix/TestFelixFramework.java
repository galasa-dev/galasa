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

import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resource;
import org.junit.Test;
import org.osgi.framework.Bundle;

import dev.galasa.boot.mocks.MockRunnableService;
import dev.galasa.boot.LauncherException;
import dev.galasa.boot.ResourceManagementConfiguration;
import dev.galasa.boot.mocks.MockBundle;
import dev.galasa.boot.mocks.MockBundleContext;
import dev.galasa.boot.mocks.MockEnvironment;
import dev.galasa.boot.mocks.MockFelixFramework;
import dev.galasa.boot.mocks.MockOsgiFramework;
import dev.galasa.boot.mocks.MockRepositoryAdmin;
import dev.galasa.boot.mocks.MockRepository;
import dev.galasa.boot.mocks.MockResolver;
import dev.galasa.boot.mocks.MockResource;
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
        LauncherException err = catchThrowableOfType(LauncherException.class, () -> {
            felixFramework.runWebApiServer(bootstrapProperties, overridesProperties, new ArrayList<>(), 0, 0);
        });

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

    @Test
    public void testRunLocalResourceManagementLoadsCorrectBundles() throws Exception {
        // Given...
        String extraBundleName = "my.extra.bundle";

        MockResolver mockResolver = new MockResolver();
        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(mockResolver);

        Map<String, MockServiceReference<?>> services = new HashMap<>();
        MockServiceReference<MockRunnableService> mockService = new MockServiceReference<>(new MockRunnableService(), null);
        services.put("dev.galasa.framework.resource.management.internal.LocalResourceManagement", mockService);

        MockBundleContext mockFrameworkBundleContext = new MockBundleContext(services);
        MockBundle mockFrameworkBundle = new MockBundle("dev.galasa.framework", mockFrameworkBundleContext);

        Bundle[] availableBundles = new Bundle[] {
            mockFrameworkBundle,
            new MockBundle("dev.galasa.framework.resource.management"),
            new MockBundle(extraBundleName)
        };

        MockBundleContext mockBundleContext = new MockBundleContext(availableBundles);
        MockOsgiFramework mockOsgiFramework = new MockOsgiFramework(mockBundleContext);

        FelixFramework felixFramework = new MockFelixFramework(mockOsgiFramework, mockRepoAdmin);
        Properties bootstrapProperties = new Properties();
        Properties overridesProperties = new Properties();
        List<String> extraBundles = List.of(extraBundleName);

        MockEnvironment mockEnv = new MockEnvironment();
        List<String> includes = List.of("*");
        List<String> excludes = new ArrayList<>();
        ResourceManagementConfiguration config = new ResourceManagementConfiguration(includes, excludes, mockEnv);

        // When...
        felixFramework.runLocalResourceManagement(bootstrapProperties, overridesProperties, extraBundles, config);

        // Then...
        List<String> addedResourceIds = mockResolver.getAllResources()
            .stream()
            .map(Resource::getId)
            .collect(Collectors.toList());

        assertThat(addedResourceIds).contains("dev.galasa.framework.resource.management");
        assertThat(addedResourceIds).contains(extraBundleName);
    }

    @Test
    public void testRunPrepareInstallsTestBundleButNotUberObrBundles() throws Exception {
        // Given - a test OBR and the Galasa uber OBR (identified by its artifact ID in the URI).
        // Only the test OBR's resources are added as resolver roots; the uber OBR is registered
        // so the resolver can find dependencies but its bundles are not installed wholesale.
        String testBundleUri = "mvn:my.test/bundle1/1.0.0/jar";
        String testBundle = "my.test.bundle";

        // Test OBR - plain URI, not the uber OBR
        Repository testRepo = new MockRepository(new Resource[]{
            new MockResource(testBundle, testBundle, testBundleUri),
        });
        // Uber OBR - URI contains "dev.galasa.uber.obr", so it is skipped as a resolver root
        String uberBundleUri = "mvn:dev.galasa/uber.bundle/1.0.0/jar";
        String uberRepoUri = "mvn:dev.galasa/dev.galasa.uber.obr/1.0.0/obr";
        Repository uberRepo = new MockRepository(new Resource[]{
            new MockResource("uber.bundle", "uber.bundle", uberBundleUri),
        }, uberRepoUri);

        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(new MockResolver(), new Repository[]{ testRepo, uberRepo });

        MockBundleContext mockBundleContext = new MockBundleContext(new Bundle[0]);
        MockOsgiFramework mockOsgiFramework = new MockOsgiFramework(mockBundleContext);

        FelixFramework felixFramework = new MockFelixFramework(mockOsgiFramework, mockRepoAdmin);

        // When...
        felixFramework.runPrepare();

        // Then - the test bundle is installed; the uber OBR bundle is not
        List<String> installed = mockBundleContext.getInstalledBundleLocations();
        assertThat(installed).contains(testBundleUri);
        assertThat(installed).doesNotContain(uberBundleUri);
    }

    @Test
    public void testRunPrepareInstallsBundlesAcrossMultipleTestRepositories() throws Exception {
        // Given - two test OBRs and one uber OBR (identified by its URI)
        String uri1 = "mvn:repo.one/bundle/1.0.0/jar";
        String uri2 = "mvn:repo.two/bundle/1.0.0/jar";
        String uberBundleUri = "mvn:dev.galasa/uber/1.0.0/jar";
        String uberRepoUri = "mvn:dev.galasa/dev.galasa.uber.obr/1.0.0/obr";

        Repository testRepo1 = new MockRepository(new Resource[]{ new MockResource("b1", "b1", uri1) });
        Repository testRepo2 = new MockRepository(new Resource[]{ new MockResource("b2", "b2", uri2) });
        Repository uberRepo  = new MockRepository(new Resource[]{ new MockResource("uber", "uber", uberBundleUri) }, uberRepoUri);

        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(new MockResolver(),
            new Repository[]{ testRepo1, testRepo2, uberRepo });

        MockBundleContext mockBundleContext = new MockBundleContext(new Bundle[0]);
        MockOsgiFramework mockOsgiFramework = new MockOsgiFramework(mockBundleContext);

        FelixFramework felixFramework = new MockFelixFramework(mockOsgiFramework, mockRepoAdmin);

        // When...
        felixFramework.runPrepare();

        // Then - both test OBR bundles are installed but the uber OBR bundle is not
        List<String> installed = mockBundleContext.getInstalledBundleLocations();
        assertThat(installed).contains(uri1);
        assertThat(installed).contains(uri2);
        assertThat(installed).doesNotContain(uberBundleUri);
    }

    @Test
    public void testRunPrepareSkipsReferenceUris() throws Exception {
        // Given - one reference: bundle (already local, skip) and one mvn: bundle (download)
        String refUri = "reference:file:/some/local/bundle.jar";
        String mvnUri = "mvn:my.test/bundle/1.0.0/jar";

        Repository testRepo = new MockRepository(new Resource[]{
            new MockResource("local.bundle", "local.bundle", refUri),
            new MockResource("remote.bundle", "remote.bundle", mvnUri),
        });
        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(new MockResolver(), new Repository[]{ testRepo });

        MockBundleContext mockBundleContext = new MockBundleContext(new Bundle[0]);
        MockOsgiFramework mockOsgiFramework = new MockOsgiFramework(mockBundleContext);

        FelixFramework felixFramework = new MockFelixFramework(mockOsgiFramework, mockRepoAdmin);

        // When...
        felixFramework.runPrepare();

        // Then - reference: URI is skipped, only mvn: URI is installed
        List<String> installed = mockBundleContext.getInstalledBundleLocations();
        assertThat(installed).doesNotContain(refUri);
        assertThat(installed).contains(mvnUri);
    }

    @Test
    public void testRunPrepareWithNoRepositoriesDoesNothing() throws Exception {
        // Given...
        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(new MockResolver(), new Repository[0]);

        MockBundleContext mockBundleContext = new MockBundleContext(new Bundle[0]);
        MockOsgiFramework mockOsgiFramework = new MockOsgiFramework(mockBundleContext);

        FelixFramework felixFramework = new MockFelixFramework(mockOsgiFramework, mockRepoAdmin);

        // When...
        felixFramework.runPrepare();

        // Then - nothing should have been installed
        assertThat(mockBundleContext.getInstalledBundleLocations()).isEmpty();
    }
}
