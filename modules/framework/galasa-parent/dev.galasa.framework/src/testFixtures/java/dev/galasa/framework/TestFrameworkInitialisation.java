/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import dev.galasa.framework.mocks.MockBundleContext;
import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockConfidentialTextStore;
import dev.galasa.framework.mocks.MockCredentialsStore;
import dev.galasa.framework.mocks.MockDSSStore;
import dev.galasa.framework.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockEventsService;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockLog;
import dev.galasa.framework.mocks.MockRASStoreService;
import dev.galasa.framework.mocks.MockServiceReference;
import dev.galasa.framework.spi.*;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.osgi.framework.Bundle;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

public class TestFrameworkInitialisation extends FrameworkInitialisationTestBase {

    @Test
    public void testFrameworkCreatedDefaultPathOk() throws Exception {
        createFrameworkInit();
    }

    public FrameworkInitialisation createFrameworkInit() throws Exception{
        Properties bootstrapProperties = new Properties();
        bootstrapProperties.setProperty("framework.galasa.home","/myuser/home");
        return createFrameworkInit(bootstrapProperties);
    }



    public FrameworkInitialisation createFrameworkInit(Properties bootstrapProps) throws Exception {
        MockEnvironment env = new MockEnvironment();
        return createFrameworkInit(bootstrapProps,env);
    }

    @Test
    public void TestBootstrapConfigPropStorePathIsBasedOffUserHome() throws Exception {
        Properties bootstrapProperties = new Properties();
        bootstrapProperties.setProperty("framework.galasa.home","/myuser/home");
        MockEnvironment env = new MockEnvironment();
        FrameworkInitialisation frameworkInit = createFrameworkInit(bootstrapProperties, env);
        assertThat(frameworkInit.getBootstrapConfigurationPropertyStore().getPath()).isEqualTo("/myuser/home/cps.properties");
    }

    @Test
    public void TestDSSUriIsBasedOffUserHome() throws Exception {
        Properties bootstrapProperties = new Properties();
        bootstrapProperties.setProperty("framework.galasa.home","/myuser/home");
        MockEnvironment env = new MockEnvironment();
        FrameworkInitialisation frameworkInit = createFrameworkInit(bootstrapProperties, env);
        assertThat(frameworkInit.getDynamicStatusStoreUri().getPath()).isEqualTo("/myuser/home/dss.properties");
    }

    @Test
    public void TestRasUriIsBasedOffUserHome() throws Exception {
        Properties bootstrapProperties = new Properties();
        bootstrapProperties.setProperty("framework.galasa.home","/myuser/home");
        MockEnvironment env = new MockEnvironment();
        FrameworkInitialisation frameworkInit = createFrameworkInit(bootstrapProperties, env);
        

        List<URI> rasUriList = frameworkInit.getResultArchiveStoreUris();
        
        assertThat(rasUriList).hasSize(1);
        assertThat(rasUriList.get(0).getPath()).isEqualTo("/myuser/home/ras");
    }

    @Test
    public void TestCredsUriIsBasedOffUserHome() throws Exception {
        Properties bootstrapProperties = new Properties();
        bootstrapProperties.setProperty("framework.galasa.home","/myuser/home");
        MockEnvironment env = new MockEnvironment();
        FrameworkInitialisation frameworkInit = createFrameworkInit(bootstrapProperties, env);
        assertThat(frameworkInit.getCredentialsStoreUri().getPath()).isEqualTo("/myuser/home/credentials.properties");
    }


    public FrameworkInitialisation createFrameworkInit(Properties bootstrapProps, MockEnvironment mockEnv ) throws Exception {
        // Given...
        Properties bootstrapProperties = bootstrapProps;
        Properties overrideProperties = new Properties();
        boolean isTestRun = true ;
        Log logger = new MockLog();

        // A fake OSGi service registry...
        Map<String,MockServiceReference<?>> services = new HashMap<String,MockServiceReference<?>>();

        Bundle bundle = null;

        MockFramework mockFramework = addMockFrameworkToMockServiceRegistry(services,bundle);

        // We want a CPS service...
        Map<String,String> cpsProperties = new HashMap<String,String>();

        cpsProperties.put("framework.run.testbundleclass","myTestBundle/myTestClass");
        
        // framework.run.name sets the run-name explicitly.
        // cpsProperties.put("framework.run.name","myRunName");

        addMockCPSToMockServiceRegistry(services,cpsProperties, bundle);

        MockDSSStore mockDSSStore = addMockDSSToMockServiceRegistry(services, bundle);

        MockBundleContext bundleContext = new MockBundleContext(services);

        MockFileSystem mockFileSystem = new MockFileSystem();

        MockRASStoreService mockRASStoreService = addMockRASToMockServiceRegistry(services, bundle);
        MockCredentialsStore mockCredentialsStore = addMockCredentialsStoreToMockServiceRegistry(services, bundle);
        MockConfidentialTextStore mockConfidentialTextStore = addMockConfidentialTextServiceToMockServiceRegistry(services, bundle);

        MockEventsService mockEventsService = addMockEventsServiceToMockServiceRegistry(services, bundle);

        // When...
        FrameworkInitialisation frameworkInitUnderTest = new FrameworkInitialisation( 
            bootstrapProperties,  
            overrideProperties, 
            logger,
            bundleContext,
            mockFileSystem,
            mockEnv,
            isTestRun? GalasaFactory.getInstance().newTestRunInitStrategy(): GalasaFactory.getInstance().newDefaultInitStrategy()
        );

        // Then...
        assertThat(mockFramework.getConfidentialTextService()).isEqualTo(mockConfidentialTextStore);
        assertThat(mockFramework.getCredentialsStore()).isEqualTo(mockCredentialsStore);
        assertThat(mockFramework.getConfidentialTextService()).isEqualTo(mockConfidentialTextStore);
        assertThat(mockFramework.getDynamicStatusStore()).isEqualTo(mockDSSStore);
        assertThat(mockFramework.getCredentialsStore()).isEqualTo(mockCredentialsStore);
        assertThat(mockFramework.getResultArchiveStore()).isEqualTo(mockRASStoreService);
        assertThat(mockFramework.getEventsService()).isEqualTo(mockEventsService);

        //assertThat(bootstrapProperties).isEmpty();
        //assertThat(overrideProperties).isEmpty();

        return frameworkInitUnderTest;
    }


    // When no framework service has been found... should be an error.
    @Test
    public void testFrameworkCreatedNoFrameworkServiceFails() throws Exception {

        // Given...
        Properties bootstrapProperties = new Properties();
        Properties overrideProperties = new Properties();
        boolean isTestRun = true ;
        Log logger = new MockLog();
        MockEnvironment mockEnv = new MockEnvironment();

        Map<String,MockServiceReference<?>> services = new HashMap<String,MockServiceReference<?>>();
        
        // Note: The framework service isn't added as a service reference ! This should cause an error.

        MockBundleContext bundleContext = new MockBundleContext(services);
        
        MockFileSystem mockFileSystem = new MockFileSystem();

        // When...
        try {
            new FrameworkInitialisation( 
                bootstrapProperties,  
                overrideProperties, 
                logger, 
                bundleContext,
                mockFileSystem,
                mockEnv,
                isTestRun? GalasaFactory.getInstance().newTestRunInitStrategy(): GalasaFactory.getInstance().newDefaultInitStrategy()
            );
            fail("There is no CPS service configured on purpose, there should have been an error thrown!");
        } catch( Exception ex ) {
            assertThat(ex)
                .hasMessage("The framework service is missing")
                .isInstanceOf(FrameworkException.class)
                ;
        }
    }


    // When no Cps service reference can be found
    @Test
    public void testFrameworkCreatedNoCPSServiceFails() throws Exception {

        // Given...
        Properties bootstrapProperties = new Properties();
        Properties overrideProperties = new Properties();
        boolean isTestRun = true ;
        Log logger = new MockLog();
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("user.home","/home");

        Map<String,MockServiceReference<?>> services = new HashMap<String,MockServiceReference<?>>();

        MockFramework mockFramework = new MockFramework();

        Bundle bundle = null;
        MockServiceReference<IFramework> mockFrameworkRef = new MockServiceReference<IFramework>(mockFramework, bundle );
        services.put(IFramework.class.getName(),mockFrameworkRef);

        // Note: The CPS framework service isn't added as a service reference !

        MockBundleContext bundleContext = new MockBundleContext(services);
        
        MockFileSystem mockFileSystem = new MockFileSystem();

        // When...
        FrameworkInitialisation frameworkInitUnderTest = null;
        try {
            frameworkInitUnderTest = new FrameworkInitialisation( 
                bootstrapProperties,  
                overrideProperties, 
                logger, 
                bundleContext,
                mockFileSystem,
                mockEnv,
                isTestRun? GalasaFactory.getInstance().newTestRunInitStrategy(): GalasaFactory.getInstance().newDefaultInitStrategy()
            );
            fail("There is no CPS service configured on purpose, there should have been an error thrown!");
        } catch( Exception ex ) {
            assertThat(ex)
                .hasMessage("No Configuration Property Store Services have been found")
                .isInstanceOf(FrameworkException.class)
                ;
        }
        assertThat(frameworkInitUnderTest).isNull();
    }

    @Test
    public void testLocateDynamicStatusStoreDefaultsToCPS() throws Exception {

        // Given...

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        Properties bootstrap = new Properties();
        bootstrap.setProperty("framework.galasa.home","/myuser/home");
        FrameworkInitialisation frameworkInit = createFrameworkInit(bootstrap);

        Log logger = new MockLog();

        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();

        Map<String,String> cpsProps = new HashMap<String,String>();
        cpsProps.put("dynamicstatus.store","file:///myoverriddenhome/dss.properties");
        MockCPSStore mockCPS = new MockCPSStore(cpsProps);

        // When...
        URI uri = frameworkInit.locateDynamicStatusStore(
            logger, bootstrap, mockCPS, fs);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri.getPath()).isEqualTo("/myoverriddenhome/dss.properties" );

        // The empty file system should now have a blank file also.
        Path expectedPath = Path.of("/myoverriddenhome","dss.properties");
        assertThat(fs.exists(expectedPath)).isTrue();
    }

    @Test
    public void testLocateDynamicStatusStoreDefaultsGALASA_HOME() throws Exception {

        // Given...

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
    //    MockEnvironment mockEnv = new MockEnvironment();
    //    // The user home... which should be ignored if GALASA_HOME is set.
    //    mockEnv.setProperty("user.home","/myuser2/home");
    //    // The value we expect to be used...
    //    mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");

        // When...
        URI uri = frameworkInit.locateDynamicStatusStore(
            logger, new Properties(), frameworkInit.getFramework().getConfigurationPropertyService("framework"), fs);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri.getPath()).isEqualTo("/myuser/home/dss.properties" );

        // The empty file system should now have a blank file also.
        Path expectedPath = Path.of("/myuser/home/dss.properties");
        assertThat(fs.exists(expectedPath)).isTrue();
    }

    @Test
    public void testLocateCredentialsStoreUsesCPS() throws Exception {

        // Given...

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
//        MockEnvironment mockEnv = new MockEnvironment();
//        // The user home... which should be ignored if GALASA_HOME is set.
//        mockEnv.setProperty("user.home","/myuser2/home");

        Map<String,String> cpsProps = new HashMap<String,String>();
        cpsProps.put("credentials.store","file:///myoverriddenhome/credentials.properties");
        MockCPSStore mockCPS = new MockCPSStore(cpsProps);

        // When...
        URI uri = frameworkInit.locateCredentialsStore(
            logger, new Properties(), mockCPS, fs);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri.getPath()).isEqualTo("/myoverriddenhome/credentials.properties" );

        // The empty file system should now have a blank file also.
        Path expectedPath = Path.of("/myoverriddenhome","credentials.properties");
        assertThat(fs.exists(expectedPath)).isTrue();
    }

    @Test
    public void testLocateCredentialsStoreNoticesGALASA_HOME() throws Exception {

        // Given...

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Log logger = new MockLog();
        
        // A fresh file system...
        MockFileSystem fs = new MockFileSystem();
//        MockEnvironment mockEnv = new MockEnvironment();
//        // The user home... which should be ignored if GALASA_HOME is set.
//        mockEnv.setProperty("user.home","/myuser2/home");
//        // The value we expect to be used...
//        mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");

        // When...
        URI uri = frameworkInit.locateCredentialsStore(
            logger, new Properties(), frameworkInit.getFramework().getConfigurationPropertyService("framework"), fs);

        // Then...
        assertThat(uri).isNotNull();
        assertThat(uri.getPath()).isEqualTo("/myuser/home/credentials.properties" );

        // The empty file system should now have a blank file also.
        Path expectedPath = Path.of("/myuser","home","credentials.properties");
        assertThat(fs.exists(expectedPath)).isTrue();
    }

    @Test
    public void testCreatingRASUriListUsesCPSNoOverrides() throws Exception {

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Map<String,String> cpsProps = new HashMap<String,String>();
        cpsProps.put("resultarchive.store","file:///myoverriddenhome/rasFromCPS");
        MockCPSStore mockCPS = new MockCPSStore(cpsProps);

        // When...
        List<URI> uriList = frameworkInit.createUriResultArchiveStores(new Properties(), mockCPS);
        assertThat(uriList).contains(URI.create("file:///myoverriddenhome/rasFromCPS"));
    }

    @Test
    public void testCreatingRASUriListUsesOverridesNotCPS() throws Exception {

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

        Map<String,String> cpsProps = new HashMap<String,String>();
        cpsProps.put("resultarchive.store","file:///myoverriddenhome/rasFromCPS");
        MockCPSStore mockCPS = new MockCPSStore(cpsProps);

        Properties overrides = new Properties();
        overrides.setProperty("framework.resultarchive.store", "file:///myoverriddenhome/rasFromOverrides");

        // When...
        List<URI> uriList = frameworkInit.createUriResultArchiveStores(overrides, mockCPS);
        assertThat(uriList).contains(URI.create("file:///myoverriddenhome/rasFromOverrides"));
        assertThat(uriList).doesNotContain(URI.create("file:///myoverriddenhome/rasFromCPS"));
    }

    @Test
    public void testCreatingRASUriListDefaultsToGalasaHome() throws Exception {

        // As all the logic is inside a constructor ! (bad)
        // we can't call any methods on the class until we have constructed it
        // using a good passing test...
        FrameworkInitialisation frameworkInit = createFrameworkInit();

//        MockEnvironment mockEnv = new MockEnvironment();
//        // The user home... which should be ignored if GALASA_HOME is set.
//        mockEnv.setProperty("user.home","/myuser2/home");
        // GALASA_HOME is not set...
        // mockEnv.setProperty("GALASA_HOME","/myoverriddenhome");

        Map<String,String> cpsProps = new HashMap<String,String>();
        MockCPSStore mockCPS = new MockCPSStore(cpsProps);

        // When...
        List<URI> uriList = frameworkInit.createUriResultArchiveStores(new Properties(),mockCPS);
        assertThat(uriList).contains(URI.create("file:///myuser/home/ras"));
    }

    @Test
    public void TestGetGalasaHomePicksUpUserProfileOnWindows() throws Exception {
        Properties bootstrapProperties = new Properties();
        MockEnvironment env = new MockEnvironment();
        env.setenv("USERPROFILE", "myhomevalue");
        FrameworkInitialisation frameworkInit = createFrameworkInit(bootstrapProperties, env);
        String home = frameworkInit.getGalasaHome(env);
        assertThat(home).isEqualTo("myhomevalue/.galasa");
    }

    @Test
    public void TestGetGalasaHomeDeaultsToTildaGalasaJustInCase() throws Exception {
        Properties bootstrapProperties = new Properties();
        MockEnvironment env = new MockEnvironment();
        FrameworkInitialisation frameworkInit = createFrameworkInit(bootstrapProperties, env);
        String home = frameworkInit.getGalasaHome(env);
        assertThat(home).isEqualTo("~/.galasa");
    }

    @Test
    public void testInitialiseEventsServiceSetsFrameworkEventsService() throws Exception {

        // Given...
        Properties bootstrap = new Properties();
        FrameworkInitialisation frameworkInit = createFrameworkInit(bootstrap);

        // When...
        IEventsService eventsService = frameworkInit.getFramework().getEventsService();

        // Then...
        assertThat(eventsService).isNotNull();
        assertThat(eventsService.getClass().getName()).isEqualTo(MockEventsService.class.getName());
    }
}