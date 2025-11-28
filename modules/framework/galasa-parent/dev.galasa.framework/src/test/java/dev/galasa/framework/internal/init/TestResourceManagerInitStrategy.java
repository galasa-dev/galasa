/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.init;

import org.junit.Test;
    
import dev.galasa.framework.GalasaFactory;
import dev.galasa.framework.IFrameworkInitialisationStrategy;

public class TestResourceManagerInitStrategy {

    @Test
    public void testCanCreateStrategy() {
        GalasaFactory.getInstance().newResourceManagerInitStrategy();
    }
    
    @Test
    public void testCanStartLoggingCaptureWithNulls() throws Exception {
        IFrameworkInitialisationStrategy strategy = GalasaFactory.getInstance().newResourceManagerInitStrategy();
        strategy.startLoggingCapture(null);
    }
    @Test
    public void testCanApplyOverridesWithNulls() throws Exception {
        IFrameworkInitialisationStrategy strategy = GalasaFactory.getInstance().newResourceManagerInitStrategy();
        strategy.applyOverrides(null, null, null);
    }
}
