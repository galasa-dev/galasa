/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.creds;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;

import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockFrameworkInitialisation;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.creds.CredentialsException;

/**
 * <p>
 * This test class checks the behaviour of registering a local Credentials
 * Store.
 * </p>
 *
 *  
 */
public class FileCredentialsRegistrationTest {

    private Properties bootstrap;

    /**
     * <p>
     * This test method checks that a local Credentials Store can be registered and
     * initialised.
     * </p>
     *
     * @throws IOException
     * @throws CredentialsStoreException
     * @throws URISyntaxException
     * @throws InvalidSyntaxException
     * @throws FrameworkException
     */
    @Test
    public void testInitialise()
            throws IOException, CredentialsException, URISyntaxException, InvalidSyntaxException, FrameworkException {
        File testCreds = File.createTempFile("galasa", ".properties");
        FileCredentialsRegistration fileCredsReg = new FileCredentialsRegistration();

        bootstrap = new Properties();
        bootstrap.setProperty("framework.config.store", "");

        MockFramework mockFramework = new MockFramework(testCreds);
        IFrameworkInitialisation fi = new MockFrameworkInitialisation(null, null, testCreds.toURI(), mockFramework);
        fileCredsReg.initialise(fi);
        assertTrue("Dummy", true);
    }
}
