/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.init;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
    
import dev.galasa.framework.GalasaFactory;
import dev.galasa.framework.IFrameworkInitialisationStrategy;
import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockDSSStore;
import dev.galasa.framework.mocks.MockFramework;


public class TestTestRunInitStrategy {

    @Test
    public void testCanCreateStrategy() {
        GalasaFactory.getInstance().newTestRunInitStrategy();
    }

    @Test
    public void testCanSetTestRunAsresourceManagementRunNameGotFromCPS() throws Exception {
        // Given...
        IFrameworkInitialisationStrategy strategy = GalasaFactory.getInstance().newTestRunInitStrategy();
        MockFramework framework = new MockFramework();

        Map<String,String> properties = new HashMap<String,String>();
        properties.put("run.name","myRunName");
        MockCPSStore mockCPS = new MockCPSStore(properties);

        // When...
        strategy.setTestRunName(framework, mockCPS);

        // Then...
        String nameGotBack = framework.getTestRunName();
        assertThat(nameGotBack).isEqualTo("myRunName");
    }
    
    @Test
    public void testCanStartLoggingCaptureWithNulls() throws Exception {
        MockFramework framework = new MockFramework();

        IFrameworkInitialisationStrategy strategy = GalasaFactory.getInstance().newTestRunInitStrategy();
        strategy.startLoggingCapture(framework);
        assertThat(framework.isLogCaptured).isTrue();
    }
    @Test
    public void testCanApplyOverrides() throws Exception {
        IFrameworkInitialisationStrategy strategy = GalasaFactory.getInstance().newTestRunInitStrategy();


        MockFramework framework = new MockFramework();

        // Set up the test run name.
        Map<String,String> cpsProperties = new HashMap<String,String>();
        cpsProperties.put("run.name","myRunName");
        MockCPSStore mockCPS = new MockCPSStore(cpsProperties);
        framework.setMockCps(mockCPS);
        framework.setTestRunName("myRunName");

        // Set up the dss with a few override values.
        Map<String,String> dssProperties = new HashMap<String,String>();
        String overridesJson = "[ { \"key\" :\"prop1\" , \"value\" : \"overrideProp1Value\" } , { \"key\" : \"prop2\", \"value\": \"overrideProp2Value\" }]";
        dssProperties.put("run.myRunName.overrides",overridesJson);
        MockDSSStore dss = new MockDSSStore(dssProperties);
        
        // Set up some properties which can be over-ridden... or not.
        Properties propsIn = new Properties();
        propsIn.setProperty("prop1", "originalProp1Value");
        propsIn.setProperty("prop3", "originalProp3Value");

        // When...
        Properties propsOut = strategy.applyOverrides(framework, dss, propsIn);

        // Then...
        assertThat(propsOut.getProperty("prop1")).as("Expected the original property to be overridden").isEqualTo("overrideProp1Value");
        assertThat(propsOut.getProperty("prop2")).as("Expected the override to be visible as there was no original value").isEqualTo("overrideProp2Value");
        assertThat(propsOut.getProperty("prop3")).as("Expected the original property to show through as it wasn't overridden").isEqualTo("originalProp3Value");
    }
}
