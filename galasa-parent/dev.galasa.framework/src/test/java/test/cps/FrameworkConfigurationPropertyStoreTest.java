/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.cps;

import static org.assertj.core.api.Assertions.*;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.internal.cps.FpfConfigurationPropertyStore;
import dev.galasa.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

/**
 * This test class tests the behaviour of the
 * FrameworkConfigurationPropertyStore class. The purpose of the class is to
 * drive the registered CPS service.
 */
public class FrameworkConfigurationPropertyStoreTest {
    File testProp;

    @Before
    public void makeSureTempPropertiesDeleted() throws IOException {
        testProp = File.createTempFile("galasafpf_", ".properties");
    }

    @After
    public void deletePropertiesFile() throws IOException {
        if (testProp != null && testProp.exists()) {
            testProp.delete();
        }
    }

    /**
     * This test method ensures the object can be insantiated
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testFrameworkConstructor() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("Test1", "SomeString");
        testProps.setProperty("Test2", "SomeString");
        testProps.setProperty("RootPasswordForEverySystemEver", "admin");
        testProps.setProperty("anotherString", "anotherString");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new MockFramework(), fpfcps,
                overrides, record, "zos");

        assertThat(test).as("Framework CPS could not bre created").isNotNull();
    }

    /**
     * This test method does a simple get property check from a basic properties
     * file.
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testFrameworkGetProperty() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();
        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new MockFramework(), fpfcps,
                overrides, record, "zos");

        String valueGotBack = test.getProperty("image", "credentialid", "PLEXMA");
        assertThat(valueGotBack)
                .as("Unexpected Value retrieved from CPS")
                .isEqualTo("Waddup");

        assertThat(record.get("zos.image.PLEXMA.credentialid._source"))
                .as("Unexpected Value retrieved from CPS")
                .isEqualTo("cps");

    }

    /**
     * This test method ensures that the getProperty method retrieves the preferred
     * key when two viable options available.
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testFrameworkGetPropertyWithMultipleViableOptions()
            throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");
        testProps.setProperty("zos.image.PLEXMA.MVMA.credentialid", "Spoon");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new MockFramework(), fpfcps,
                overrides, record, "zos");

        String valueGotBack = test.getProperty("image", "credentialid", "PLEXMA", "MVMA");
        assertThat(valueGotBack)
                .as("Unexpected Value retrieved from CPS")
                .isEqualTo("Spoon");

        assertThat(record.get("zos.image.PLEXMA.MVMA.credentialid._source"))
                .as("Property access history indicates it came from the wrong source")
                .isEqualTo("cps");
    }

    /**
     * This test method ensures that the getProperty method checks the overrides and
     * retrieves the correct values over the CPS stored version.
     * 
     * @throws ConfigurationPropertyStoreException
     * @throws IOException
     */
    @Test
    public void testFrameworkGetPropertyFromOverrides() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");
        testProps.setProperty("zos.image.PLEXMA.MVMA.credentialid", "Spoon");

        overrides.setProperty("zos.image.PLEXMA.credentialid", "Sever2");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new MockFramework(), fpfcps,
                overrides, record, "zos");

        String valueGotFromPropStoreService = test.getProperty("image", "credentialid", "PLEXMA");
        assertThat(valueGotFromPropStoreService)
                .as("Unexpected Value retrieved from CPS")
                .isEqualTo("Sever2");

        assertThat(record.getProperty("zos.image.PLEXMA.credentialid._source"))
                .as("recorded property access history is saying the wrong source")
                .isEqualTo("overrides");
    }

    @Test
    public void testNullInfixes() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");
        testProps.setProperty("zos.image.PLEXMA.MVMA.credentialid", "Spoon");
        testProps.setProperty("zos.image.credentialid", "tab!=space");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new MockFramework(), fpfcps,
                overrides, record, "zos");

        assertThat(test.getProperty("image", "credentialid"))
                .as("Unexpected Value retrieved from CPS")
                .isEqualTo("tab!=space");
    }

    @Test
    public void testReportOfSearchKeys() throws ConfigurationPropertyStoreException, IOException {
        Properties overrides = new Properties();
        Properties record = new Properties();

        Properties testProps = new Properties();

        testProps.setProperty("zos.image.PLEXMA.credentialid", "Waddup");
        testProps.setProperty("zos.image.PLEXMA.MVMA.credentialid", "Spoon");
        testProps.setProperty("zos.image.credentialid", "tab!=space");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();

        FpfConfigurationPropertyStore fpfcps = new FpfConfigurationPropertyStore(testProp.toURI());

        FrameworkConfigurationPropertyService test = new FrameworkConfigurationPropertyService(new MockFramework(), fpfcps,
                overrides, record, "zos");

        assertThat(test.reportPropertyVariants("image", "credentialid", "PLEXMA", "MVMA")[0])
                .as("Unexpected Key retrieved from CPS")
                .isEqualTo("zos.image.PLEXMA.MVMA.credentialid");

        assertThat(test.reportPropertyVariants("image", "credentialid", "PLEXMA", "MVMA")[1])
                .as("Unexpected Key retrieved from CPS")
                .isEqualTo("zos.image.PLEXMA.credentialid");
                
        assertThat(test.reportPropertyVariants("image", "credentialid", "PLEXMA", "MVMA")[2])
                .as("Unexpected Key retrieved from CPS")
                .isEqualTo("zos.image.credentialid");

        assertThat(record).hasSize(0);
    }
}