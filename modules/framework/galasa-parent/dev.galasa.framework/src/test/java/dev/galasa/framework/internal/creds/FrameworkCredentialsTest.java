/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.creds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.ICredentials;

/**
 * <p>
 * This test class checks the behaviour of retrieving credentials from the local
 * store is functional.
 * </p>
 *
 *  
 */
public class FrameworkCredentialsTest {

    private File fileCPS;
    private File fileCREDS;

    @Before
    public void setup() throws Exception {
        fileCPS = File.createTempFile("galasacps_", ".properties");
        fileCREDS = File.createTempFile("galasacreds_", ".properties");
    }

    @After
    public void teardown() throws Exception {
        if (fileCPS.exists()) {
            fileCPS.delete();
        }
        if (fileCREDS.exists()) {
            fileCREDS.delete();
        }
    }

    /**
     * This test method checks that a token stored in the local Credentials Store
     * can be retrieved.
     *
     * @throws Exception
     */
    @Test
    public void testGetCredentialsToken() throws Exception {

        Properties propsCPS = new Properties();
        saveProperties(propsCPS, fileCPS);

        Properties propsCREDS = new Properties();
        propsCREDS.setProperty("secure.credentials.testcredsid.token", "testToken");
        saveProperties(propsCREDS, fileCREDS);

        IFramework framework = new MockFramework(fileCPS);
        FileCredentialsStore fileCreds = new FileCredentialsStore(fileCREDS.toURI(), framework);
        FrameworkCredentialsService service = new FrameworkCredentialsService(framework, fileCreds);

        ICredentials creds = service.getCredentials("testcredsid");

        assertNotNull("Token credentials was not found", creds);

        CredentialsToken credsToken = (CredentialsToken) creds;

        assertEquals("Token creds incorrect value", "testToken", new String(credsToken.getToken()));
    }

    private void saveProperties(Properties properties, File file) throws Exception {
        FileOutputStream out = new FileOutputStream(file);
        properties.store(out, null);
        out.close();
    }
}
