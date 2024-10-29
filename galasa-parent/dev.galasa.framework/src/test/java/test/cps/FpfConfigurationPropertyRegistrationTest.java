/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.cps;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import dev.galasa.framework.internal.cps.FpfConfigurationPropertyRegistration;
import dev.galasa.framework.mocks.MockFrameworkInitialisation;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

public class FpfConfigurationPropertyRegistrationTest {

    @Test
    public void testIntialise() throws IOException, ConfigurationPropertyStoreException {
        File testProp = File.createTempFile("galasa", ".properties");
        FpfConfigurationPropertyRegistration fpfCpsReg = new FpfConfigurationPropertyRegistration();
        fpfCpsReg.initialise(new MockFrameworkInitialisation(testProp.toURI()));
        assertTrue("Dummy", true);
    }
}
