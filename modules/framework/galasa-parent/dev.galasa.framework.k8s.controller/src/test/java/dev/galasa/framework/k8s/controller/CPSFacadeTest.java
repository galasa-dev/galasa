/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import static org.assertj.core.api.Assertions.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.junit.Test;

import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;

public class CPSFacadeTest {

    @Test 
    public void testCPSFacadeCanCreateACPSFacadeObject() {
        new CPSFacade(null);
    }
    

    @Test 
    public void testCPSFacadeCanCopeWithANullCPS() {
        CPSFacade cpsFacade = new CPSFacade(null);
        long intervalMs = cpsFacade.getKubeLaunchIntervalMilliseconds();
        assertThat(intervalMs).isEqualTo(CPSFacade.KUBE_LAUNCH_INTERVAL_MILLISECOND_DEFAULT_VALUE);
    }

    @Test 
    public void testCPSFacadeCanCopeWithNoCPSPropertyFoundForKubeLaunchIntervalMilliseconds() {
        MockIConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService();
        CPSFacade cpsFacade = new CPSFacade(mockCPS);
        long intervalMs = cpsFacade.getKubeLaunchIntervalMilliseconds();
        assertThat(intervalMs).isEqualTo(CPSFacade.KUBE_LAUNCH_INTERVAL_MILLISECOND_DEFAULT_VALUE);
    }

    @Test 
    public void testCPSFacadeCanOverrideDefaultKubeLaunchIntervalMilliseconds() {
        MockIConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService() {
            public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes) {
                assertThat(prefix).isEqualTo( CPSFacade.KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_PREFIX);
                assertThat(infixes[0]).isEqualTo( CPSFacade.KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_INFIX);
                return "2000";
            }
        };

        CPSFacade cpsFacade = new CPSFacade(mockCPS);
        long intervalMs = cpsFacade.getKubeLaunchIntervalMilliseconds();
        assertThat(intervalMs).isEqualTo(2000);
    }



    @Test 
    public void testCPSFacadeCPSPropNotANumberReturnsDefaultKubeLaunchIntervalMilliseconds() {
        MockIConfigurationPropertyStoreService mockCPS = new MockIConfigurationPropertyStoreService() {
            public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes) {
                assertThat(prefix).isEqualTo( CPSFacade.KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_PREFIX);
                assertThat(infixes[0]).isEqualTo( CPSFacade.KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_INFIX);
                return "NotANumber";
            }
        };

        CPSFacade cpsFacade = new CPSFacade(mockCPS);
        long intervalMs = cpsFacade.getKubeLaunchIntervalMilliseconds();
        assertThat(intervalMs).isEqualTo(CPSFacade.KUBE_LAUNCH_INTERVAL_MILLISECOND_DEFAULT_VALUE);
    }
}