/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.dss;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import dev.galasa.framework.mocks.MockFrameworkInitialisation;
import dev.galasa.framework.spi.DynamicStatusStoreException;

public class FpfDynamicStatusStoreRegistrationTest {

    @Test
    public void testInitialse() throws IOException, DynamicStatusStoreException {
        File testProp = File.createTempFile("galasa", ".properties");
        FpfDynamicStatusStoreRegistration fpfDssReg = new FpfDynamicStatusStoreRegistration();
        fpfDssReg.initialise(new MockFrameworkInitialisation(null, testProp.toURI(), null));
        assertTrue("Dummy", true);
    }
}
