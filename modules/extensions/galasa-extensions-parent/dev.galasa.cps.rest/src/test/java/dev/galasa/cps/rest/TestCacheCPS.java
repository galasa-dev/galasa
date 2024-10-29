/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import dev.galasa.cps.rest.mocks.MockCPS;
import dev.galasa.extensions.mocks.MockLogFactory;
import dev.galasa.extensions.common.api.LogFactory;

import java.util.*;

public class TestCacheCPS {

    LogFactory logFactory = new MockLogFactory();

    @Test
    public void testCanCreateNewCache() throws Exception {
        // Given...
        MockCPS mockCPS = new MockCPS(Map.of(
            CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"true",
            "framework.prop1","23",
            "mynamespace.prop1", "24"
        ));
        new CacheCPS(mockCPS, logFactory);
    }

    @Test
    public void testCanGetNamespacesOk() throws Exception {
        // Given...
        MockCPS mockCPS = new MockCPS(Map.of(
            CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"true",
            "framework.prop1","23", 
            "mynamespace.prop1", "24"
        ));
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        List<String> namespaces = cache.getNamespaces();
        cache.getNamespaces();
        cache.getNamespaces();

        // Then...
        assertThat(namespaces).contains("framework","mynamespace");
        // We expect the underlying child CPS to have it's namespaces queried once only, when the cache is primed.
        assertThat(mockCPS.callCounterForGetNamespaces).isEqualTo(1);
    }        

    @Test
    public void testCanGetNamespacesOkWithCacheDisabled() throws Exception {
        // Given...
        MockCPS mockCPS = new MockCPS(Map.of(
            // Disabled cache... CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"true",
            "framework.prop1","23", 
            "mynamespace.prop1", "24"
        ));
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        List<String> namespaces = cache.getNamespaces();
        cache.getNamespaces();
        cache.getNamespaces();

        // Then...
        assertThat(namespaces).contains("framework","mynamespace");
        // We expect the underlying child CPS to have it's namespaces queried once per request above.
        assertThat(mockCPS.callCounterForGetNamespaces).isEqualTo(3);
    }      

    @Test
    public void testNamespacesAreCached() throws Exception {
        MockCPS mockCPS = new MockCPS(Map.of(
            CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"true",
            "framework.prop1","23",
            "mynamespace.prop1", "24"
        ));
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        cache.getNamespaces();
        List<String> namespaces2 = cache.getNamespaces();

        // Then...
        assertThat(namespaces2).contains("framework","mynamespace");
        assertThat(mockCPS.callCounterForGetNamespaces).isEqualTo(1);
    }


    @Test
    public void testGettingTwoPropertiesWithCacheEnabledOnlyGetsOnePropertyIndividuallyFromTheChildCPS() throws Exception {
        // Given...
        MockCPS mockCPS = new MockCPS(Map.of(
            CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"true",
            "framework.frodo","24",
            "mynamespace.bilbo", "36"
        ));
        
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        String prop1Value = cache.getProperty("framework.frodo");
        String prop2Value = cache.getProperty("mynamespace.bilbo");
        
        // Then...
        assertThat(prop1Value).isEqualTo("24");
        assertThat(prop2Value).isEqualTo("36");

        // The mock should have been called once for the is-cache-enabled property
        // Other properties were recovered in bulk.
        // The two above getProperty calls should have been dealt with by the cache.
        assertThat(mockCPS.callCounterForGetProperty).isEqualTo(1);
    }  


    @Test
    public void testSecureNamespacePropsAreNotCached() throws Exception {
        // Given...
        // Even when the underlying CPS has secure properties inside,
        MockCPS mockCPS = new MockCPS(Map.of(
            CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"true",
            "secure.prop1.value","24"
        ));
        
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        String prop1Value = cache.getProperty("secure.prop1.value");
        
        // Then...
        // We can't get those values out though the cache.
        assertThat(prop1Value).isNull();
    }  


    @Test
    public void testGettingTwoPropertiesWithCacheDisabledGetsThreePropertiesIndividuallyFromTheChildCPS() throws Exception {
        // Given...
        MockCPS mockCPS = new MockCPS(Map.of(
            // CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"true", Caching is disabled.
            "framework.frodo","24",
            "mynamespace.bilbo", "36"
        ));
        
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        String prop1Value = cache.getProperty("framework.frodo");
        String prop2Value = cache.getProperty("mynamespace.bilbo");
        
        // Then...
        assertThat(prop1Value).isEqualTo("24");
        assertThat(prop2Value).isEqualTo("36");

        // The mock should have been called once for the is-cache-enabled property
        // and the 2 above calls to getProperty should have been delegated to the child CPS.
        assertThat(mockCPS.callCounterForGetProperty).isEqualTo(3);
    }  
    
    @Test
    public void testTryToGetAMissingPropertyToGetNullValueBack() throws Exception {
        // Given...
        MockCPS mockCPS = new MockCPS(Map.of(
            CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"true",
            "framework.prop1","23",
            "mynamespace.prop1", "24"
        ));
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        String propValue = cache.getProperty("framework.doesntexist");

        // Then...
        assertThat(propValue).isNull();
    }  

    @Test
    public void testTryToGetAMissingPropertyToGetNullValueBackWithCacheDisabled() throws Exception {
        // Given...
        MockCPS mockCPS = new MockCPS(Map.of(
            CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"false", // disabled.
            "framework.prop1","23",
            "mynamespace.prop1", "24"
        ));
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        String propValue = cache.getProperty("framework.doesntexist");

        // Then...
        assertThat(propValue).isNull();
    }  

    @Test
    public void testSetPropertyDelegatesToClientCPS() throws Exception {
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(true);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        String frodoAge = cache.getProperty("framework.frodo.age");
        assertThat(frodoAge).isEqualTo("24");

        // When...
        cache.setProperty("framework.frodo.age","25");

        String frodoAgeAfter = cache.getProperty("framework.frodo.age");
        assertThat(frodoAgeAfter).isEqualTo("25");
    }  

    @Test
    public void testSetPropertyDelegatesToClientCPSWithCacheDisbaled() throws Exception {
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(false);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        String frodoAge = cache.getProperty("framework.frodo.age");
        assertThat(frodoAge).isEqualTo("24");

        // When...
        cache.setProperty("framework.frodo.age","25");

        String frodoAgeAfter = cache.getProperty("framework.frodo.age");
        assertThat(frodoAgeAfter).isEqualTo("25");
    }  

    private MockCPS createFrodoAndBilboMockCPS(boolean isCacheEnabled) {
        MockCPS mockCPS = new MockCPS(Map.of(
            CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,Boolean.toString(isCacheEnabled),
            "framework.frodo.age","24",
            "framework.frodo.height","95cm",
            "mynamespace.bilbo.age", "116",
            "mynamespace.bilbo.height", "86cm"
        ));
        return mockCPS;
    }
    
    @Test
    public void testCanGetAPropertyByPrefix() throws Exception {
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(true);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        Map<String, String> frodo = cache.getPrefixedProperties("framework.frodo");
        Map<String, String> bilbo = cache.getPrefixedProperties("mynamespace.bilbo");

        // Then...
        assertThat(frodo.keySet().size()).isEqualTo(2);
        assertThat(frodo.keySet()).contains("framework.frodo.age");
        assertThat(frodo.keySet()).contains("framework.frodo.height");
        assertThat(frodo.get("framework.frodo.height")).isEqualTo("95cm");
        assertThat(frodo.get("framework.frodo.age")).isEqualTo("24");

        assertThat(bilbo.keySet().size()).isEqualTo(2);
        assertThat(bilbo.keySet()).contains("mynamespace.bilbo.age");
        assertThat(bilbo.keySet()).contains("mynamespace.bilbo.height");
        assertThat(bilbo.get("mynamespace.bilbo.height")).isEqualTo("86cm");
        assertThat(bilbo.get("mynamespace.bilbo.age")).isEqualTo("116");
    }  

    @Test
    public void testCanGetAPropertyByPrefixCacheDisabled() throws Exception {
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(false);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        Map<String, String> frodo = cache.getPrefixedProperties("framework.frodo");
        Map<String, String> bilbo = cache.getPrefixedProperties("mynamespace.bilbo");

        // Then...
        assertThat(frodo.keySet().size()).isEqualTo(2);
        assertThat(frodo.keySet()).contains("framework.frodo.age");
        assertThat(frodo.keySet()).contains("framework.frodo.height");
        assertThat(frodo.get("framework.frodo.height")).isEqualTo("95cm");
        assertThat(frodo.get("framework.frodo.age")).isEqualTo("24");

        assertThat(bilbo.keySet().size()).isEqualTo(2);
        assertThat(bilbo.keySet()).contains("mynamespace.bilbo.age");
        assertThat(bilbo.keySet()).contains("mynamespace.bilbo.height");
        assertThat(bilbo.get("mynamespace.bilbo.height")).isEqualTo("86cm");
        assertThat(bilbo.get("mynamespace.bilbo.age")).isEqualTo("116");
    }  

    @Test
    public void testDeletePropertyDelegatesToChildCPS() throws Exception {
        
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(true);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        String bilboAgeBefore = cache.getProperty("mynamespace.bilbo.age");
        assertThat(bilboAgeBefore).isNotNull();
        assertThat(mockCPS.callCounterForDeleteProperty).isEqualTo(0);

        // When...
        cache.deleteProperty("mynamespace.bilbo.age");

        // Check that the child CPS was told to delete the property
        assertThat(mockCPS.callCounterForDeleteProperty).isEqualTo(1);

        // Check that the cache has been updated also.
        String bilboAgeAfter= cache.getProperty("mynamespace.bilbo.age");
        assertThat(bilboAgeAfter).isNull();
    }  

    @Test
    public void testDeletePropertyDelegatesToChildCPSCacheDisabled() throws Exception {
        
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(false);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        String bilboAgeBefore = cache.getProperty("mynamespace.bilbo.age");
        assertThat(bilboAgeBefore).isNotNull();
        assertThat(mockCPS.callCounterForDeleteProperty).isEqualTo(0);

        // When...
        cache.deleteProperty("mynamespace.bilbo.age");

        // Check that the child CPS was told to delete the property
        assertThat(mockCPS.callCounterForDeleteProperty).isEqualTo(1);

        // Check that the cache has been updated also.
        String bilboAgeAfter= cache.getProperty("mynamespace.bilbo.age");
        assertThat(bilboAgeAfter).isNull();
    }  

    @Test
    public void testCanGetAllPropertiesFromANamespaceOk() throws Exception {
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(true);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        Map<String, String> frodo = cache.getPropertiesFromNamespace("framework");
        cache.getPropertiesFromNamespace("framework");
        cache.getPropertiesFromNamespace("framework");

        // Then...
        // We expect an extra property which controls cache enable/disable.
        assertThat(frodo.keySet().size()).isEqualTo(2+1);
        assertThat(frodo.keySet()).contains("framework.frodo.age");
        assertThat(frodo.keySet()).contains("framework.frodo.height");
        assertThat(frodo.get("framework.frodo.height")).isEqualTo("95cm");
        assertThat(frodo.get("framework.frodo.age")).isEqualTo("24");

        // The child call should have been called twice as we prime the cache.
        assertThat(mockCPS.callCounterForGetPropertiesFromNamespace).isEqualTo(2);
    } 

    @Test
    public void testCanGetAllPropertiesFromANamespaceOkCacheDisabled() throws Exception {
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(false);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        Map<String, String> frodo = cache.getPropertiesFromNamespace("framework");
        cache.getPropertiesFromNamespace("framework");
        cache.getPropertiesFromNamespace("framework");

        // Then...
         // We expect an extra property which controls cache enable/disable.
        assertThat(frodo.keySet().size()).isEqualTo(3);
        assertThat(frodo.keySet()).contains("framework.frodo.age");
        assertThat(frodo.keySet()).contains("framework.frodo.height");
        assertThat(frodo.get("framework.frodo.height")).isEqualTo("95cm");
        assertThat(frodo.get("framework.frodo.age")).isEqualTo("24");

        // The child call should have been called three times as above, 
        // priming the cache will have been skipped with the cache disabled.
        assertThat(mockCPS.callCounterForGetPropertiesFromNamespace).isEqualTo(3);
    } 

    @Test
    public void testShutdownShutsDownTheChildCPS() throws Exception {
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(true);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        assertThat(mockCPS.callCounterForShutdown).isEqualTo(0);

        cache.shutdown();

        assertThat(mockCPS.callCounterForShutdown).isEqualTo(1);
    }

    @Test
    public void testShutdownShutsDownTheChildCPSCacheDisabled() throws Exception {
        // Given...
        MockCPS mockCPS = createFrodoAndBilboMockCPS(false);
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        assertThat(mockCPS.callCounterForShutdown).isEqualTo(0);

        cache.shutdown();

        assertThat(mockCPS.callCounterForShutdown).isEqualTo(1);
    }

    @Test
    public void testSettingAPropertyCanCreateANewNamespaceInCache() throws Exception {
        // Given...
        MockCPS mockCPS = new MockCPS(Map.of(
            CacheCPS.FEATURE_FLAG_CPS_PROP_CACHED_CPS_ENABLED,"true"
        ));
        
        CacheCPS cache = new CacheCPS(mockCPS, logFactory);

        // When...
        cache.setProperty("mynamespace.frodo.age","23");
        List<String> namespaces = cache.getNamespaces();
        
        // Then...
        assertThat(namespaces).hasSize(2).contains("framework","mynamespace");
    }  
}
