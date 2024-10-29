/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.dss;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkPropertyFileException;

/**
 * Test the Framework DSS Stub. Most of the functionality will exist in the
 * service, so this is just a quick drive of the stub code.
 *
 *  
 *
 */
public class FpfDynamicStatusStoreTest {

    // private static final String NAMESPACE = "testy";
    // private static final String PREFIX = "dss." + NAMESPACE + ".";

    private Path                  tempProperties;
    private FpfDynamicStatusStore dss;

    @Before
    public void setup() throws IOException, FrameworkPropertyFileException, DynamicStatusStoreException {
        this.tempProperties = Files.createTempFile("galasa_dss_junit", ".propertes");
        // final FrameworkInitialisation frameworkInitialisation = new
        // FrameworkInitialisation(
        // this.tempProperties.toUri());

        // final FpfDynamicStatusStore dsss = new
        // FpfDynamicStatusStore(tempProperties.toUri());
        // dsss.initialise(frameworkInitialisation);
        this.dss = new FpfDynamicStatusStore(tempProperties.toUri());
    }

    @After
    public void teardown() throws IOException {
        if ((this.tempProperties != null) && Files.exists(this.tempProperties)) {
            Files.delete(this.tempProperties);
        }
    }

    @Test
    public void testSimpleMethods() throws DynamicStatusStoreException, IOException {
        final String key = "a_little_key";
        final String value = UUID.randomUUID().toString();

        this.dss.put(key, value);
        Assert.assertEquals("Key values differ", value, getKey(key));
        Assert.assertEquals("Key values differ", value, this.dss.get(key));

        this.dss.delete(key);
        Assert.assertNull("Should have gone", getKey(key));
        Assert.assertNull("Should have gone", this.dss.get(key));
    }

    @Test
    public void testMapMethods() throws DynamicStatusStoreException, IOException {
        final HashMap<String, String> map = new HashMap<>();

        final String key1 = "a_little_key";
        final String value1 = UUID.randomUUID().toString();
        final String key2 = "a_tiny_key";
        final String value2 = UUID.randomUUID().toString();

        map.put(key1, value1);
        map.put(key2, value2);

        this.dss.put(map);

        Assert.assertEquals("Key values differ", value1, getKey(key1));
        Assert.assertEquals("Key values differ", value2, getKey(key2));

        this.dss.delete(map.keySet());
        Assert.assertNull("Should have gone", getKey(key1));
        Assert.assertNull("Should have gone", getKey(key2));
    }

    @Test
    public void testPrefixMethods() throws DynamicStatusStoreException, IOException {
        final String key1 = "a_little_key";
        final String value1 = UUID.randomUUID().toString();
        final String key2 = "a_tiny_key";
        final String value2 = UUID.randomUUID().toString();

        this.dss.put(key1, value1);
        this.dss.put(key2, value2);

        final Map<String, String> values = this.dss.getPrefix("a_");

        Assert.assertEquals("Key values differ", value1, values.get(key1));
        Assert.assertEquals("Key values differ", value2, values.get(key2));
        Assert.assertEquals("Key values differ", value1, getKey(key1));
        Assert.assertEquals("Key values differ", value2, getKey(key2));

        this.dss.deletePrefix("a_");
        Assert.assertNull("Should have gone", getKey(key1));
        Assert.assertNull("Should have gone", getKey(key2));
    }

    @Test
    public void testSwapMethods() throws DynamicStatusStoreException, InterruptedException, IOException {
        final String key1 = "a_little_key";
        final String value1a = "value1a";
        final String value1b = "value1b";
        final String value1c = "value1c";
        final String value1d = "value1d";

        final String key2 = "a_tiny_key";
        final String value2a = "value2a";
        final String value2b = "value2b";
        final HashMap<String, String> map = new HashMap<>();
        map.put(key2, value2a);

        Assert.assertTrue("Initial swap should work", dss.putSwap(key1, null, value1a));
        Assert.assertEquals("Key values differ", value1a, getKey(key1));
        Assert.assertFalse("2nd swap should false", dss.putSwap(key1, null, value1a));

        {
            this.dss.put(key1, value1b);
            Assert.assertTrue("Initial swap should work", this.dss.putSwap(key1, value1b, value1a));
            Assert.assertEquals("Key values differ", value1a, getKey(key1));
            Assert.assertFalse("2nd swap should false", this.dss.putSwap(key1, value1b, value1a));
            Assert.assertEquals("Key values differ", value1a, getKey(key1));
        }

        Assert.assertTrue("3rd swap should work", this.dss.putSwap(key1, value1a, value1c));
        Assert.assertEquals("Key values differ", value1c, getKey(key1));

        Assert.assertTrue("1st Map swap should work", this.dss.putSwap(key1, value1c, value1d, map));
        Assert.assertEquals("Key values differ", value1d, getKey(key1));
        Assert.assertEquals("Key values differ", value2a, getKey(key2));

        map.put(key2, value2b);
        Assert.assertFalse("2nd Map swap should fail", this.dss.putSwap(key1, value1c, value1a, map));
        Assert.assertEquals("Key values differ", value1d, getKey(key1));
        Assert.assertEquals("Key values differ", value2a, getKey(key2));

    }

//    @Test
//    public void testFutureDynamicResource() throws DynamicStatusStoreException {
//        Assert.assertNotNull("Should get a dynamic resource", this.dss.getDynamicResource("bob"));
//    }
//
//    @Test
//    public void testFutureDynamicRun() throws DynamicStatusStoreException {
//        Assert.assertNotNull("Should get a dynamic run", this.dss.getDynamicRun());
//    }

    private String getKey(String key) throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(tempProperties));

        return properties.getProperty(key);
    }
}