/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

import dev.galasa.framework.mocks.*;
import dev.galasa.framework.spi.DssPropertyKeyRunNameSuffix;
import dev.galasa.framework.spi.IDynamicStatusStoreService;

public class TestInterruptedMonitorImpl {


    @Test
    public void testCanInstantiateMonitorImpl() {
        // Given...
        IDynamicStatusStoreService dss = null;

        // When...
        new InterruptedMonitorImpl(dss, "U12345");

        // Then...
        // Getting this far meant we could create the monitor.
    }

    @Test
    public void testCanTellMeIveNotBeenInterruptedYet() throws Exception {
        // Given...
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        String testRunName = "U12345";
        // This run has not been marked as interrupted in the DSS, so the monitor should return false.
        InterruptedMonitorImpl monitor = new InterruptedMonitorImpl(dss, testRunName);

        // When...
        boolean isInterrupted = monitor.isInterrupted();

        // Then...
        assertThat(isInterrupted).isFalse();
    }

    @Test
    public void testCanDetectThatTestRunHasBeenInterruptedAlready() throws Exception {
        // Given...
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        String testRunName = "U12345";
        dss.put("run."+testRunName+"."+DssPropertyKeyRunNameSuffix.INTERRUPT_REASON, "UserCancelled");

        // This run has been marked as interrupted in the DSS, so the monitor should return true.
        InterruptedMonitorImpl monitor = new InterruptedMonitorImpl(dss, testRunName);

        // When...
        boolean isInterrupted = monitor.isInterrupted();

        // Then...
        assertThat(isInterrupted).isTrue();
    }

    @Test
    public void testCanDetectThatTestRunHasBeenInterruptedMidWayThroughDoingSomething() throws Exception {
        // Given...
        IDynamicStatusStoreService dss = new MockIDynamicStatusStoreService();
        String testRunName = "U12345";

        // This run has been marked as interrupted in the DSS, so the monitor should return true if called now.
        InterruptedMonitorImpl monitor = new InterruptedMonitorImpl(dss, testRunName);

        // Now, after everything else is initialised, lets simulate the test run being told to cancel.
        dss.put("run."+testRunName+"."+DssPropertyKeyRunNameSuffix.INTERRUPT_REASON, "UserCancelled");

        // When...
        boolean isInterrupted = monitor.isInterrupted();

        // Then...
        assertThat(isInterrupted).isTrue();
    }

}
