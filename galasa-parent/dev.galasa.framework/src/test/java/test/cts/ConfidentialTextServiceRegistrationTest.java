/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.cts;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import dev.galasa.framework.internal.cts.FrameworkConfidentialTextServiceRegistration;
import dev.galasa.framework.mocks.MockFrameworkInitialisation;
import dev.galasa.framework.spi.ConfidentialTextException;

/**
 * This test class ensures that confidential texts that have been registered are
 * reomved from text.
 * 
 *  
 */
public class ConfidentialTextServiceRegistrationTest {

    /**
     * This method intialises the confidentialTextService and checks no exceptions
     * are thrown.
     * 
     * @throws ConfidentialTextException - if the service cannot be registered (i.e
     *                                   more than 1 service).
     * @throws IOException
     */
    @Test
    public void testInitialise() throws ConfidentialTextException, IOException {
        FrameworkConfidentialTextServiceRegistration ctsService = new FrameworkConfidentialTextServiceRegistration();
        File testProp = File.createTempFile("galasa_", ".properties");
        ctsService.initialise(new MockFrameworkInitialisation(testProp.toURI()));
        assertTrue("dummy", true);
    }

}
