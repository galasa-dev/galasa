/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.init;

import org.junit.Test;

import dev.galasa.framework.GalasaFactory;
import dev.galasa.framework.IFrameworkInitialisationStrategy;


public class TestDefaultInitStrategy {

    @Test
    public void testCanCreateStrategy() {
        GalasaFactory.getInstance().newDefaultInitStrategy();
    }

    @Test
    public void testCanSetTestRunNameWithNulls() throws Exception {
        IFrameworkInitialisationStrategy strategy = GalasaFactory.getInstance().newDefaultInitStrategy();
        strategy.setTestRunName(null, null);
    }
    
    @Test
    public void testCanStartLoggingCaptureWithNulls() throws Exception {
        IFrameworkInitialisationStrategy strategy = GalasaFactory.getInstance().newDefaultInitStrategy();
        strategy.startLoggingCapture(null);
    }
    @Test
    public void testCanApplyOverridesWithNulls() throws Exception {
        IFrameworkInitialisationStrategy strategy = GalasaFactory.getInstance().newDefaultInitStrategy();
        strategy.applyOverrides(null, null, null);
    }
}
