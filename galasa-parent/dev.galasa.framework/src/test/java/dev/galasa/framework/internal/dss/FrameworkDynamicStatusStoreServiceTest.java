/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.dss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.spi.DynamicStatusStoreException;

/**
 * <p>
 * This test class checks the behaviour of registering a local DSS using the FPF
 * class in functional
 * </p>
 * 
 *  
 */

public class FrameworkDynamicStatusStoreServiceTest {

    private File                               testFile;
    private FpfDynamicStatusStore              fpfDss;
    private FrameworkDynamicStatusStoreService fDss;

    @Before
    public void setup() throws IOException, DynamicStatusStoreException {
        this.testFile = File.createTempFile("galasafpf_", ".properties");
        fpfDss = new FpfDynamicStatusStore(testFile.toURI());
        fDss = new FrameworkDynamicStatusStoreService(new MockFramework(), fpfDss, "temp");
    }

    @After
    public void teardown() {
        if (testFile != null && testFile.exists()) {
            testFile.delete();
        }
    }

    /**
     * <p>
     * This test checks for no exceptions when putting a key/value pair into the DSS
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testPutSingle() throws DynamicStatusStoreException, IOException {
        fDss.put("testKey", "testValue");
        assertTrue("Exception during put of single key/value pair", true);
    }

    /**
     * <p>
     * This test checks for no exceptions when putting a map of multiple key/value
     * pairs into the DSS
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testPutMultiple() throws DynamicStatusStoreException, IOException {
        HashMap<String, String> keyValuePairs = new HashMap<>();
        keyValuePairs.put("key1", "value1");
        keyValuePairs.put("key2", "value2");
        fDss.put(keyValuePairs);
        assertTrue("Exception during put of multiple key/value pairs", true);
    }

    /**
     * <p>
     * This test checks the returned boolean from attempting a swap which has
     * already occured. Expected False.
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testPutSwap1() throws DynamicStatusStoreException, IOException {
        fDss.put("testKey", "testValue2");
        assertFalse("Swap occured when not required", fDss.putSwap("testKey", "testValue1", "testValue2"));
    }

    /**
     * <p>
     * This test checks the returned boolean from attempting a swap. Expected True.
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testPutSwap2() throws DynamicStatusStoreException, IOException {
        fDss.put("testKey", "testValue1");
        assertTrue("Swap did not successfully occur", fDss.putSwap("testKey", "testValue1", "testValue2"));
    }

    /**
     * <p>
     * This test checks the returned boolean from attempting a swap (and putting
     * multiple key/value pairs from a Map) which has already occured. Expected
     * False.
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testPutSwapMultiple1() throws DynamicStatusStoreException, IOException {
        fDss.put("testKey", "testValue2");
        HashMap<String, String> keyValuePairs = new HashMap<>();
        keyValuePairs.put("key1", "value1");
        keyValuePairs.put("key2", "value2");
        assertFalse("Swap occured when not required",
                fDss.putSwap("testKey", "testValue1", "testValue2", keyValuePairs));
    }

    /**
     * <p>
     * This test checks the returned boolean from attempting a swap and put of a Map
     * of key/value pairs. Expected True.
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testPutSwapMultiple2() throws DynamicStatusStoreException, IOException {
        fDss.put("testKey", "testValue1");
        HashMap<String, String> keyValuePairs = new HashMap<>();
        keyValuePairs.put("key1", "value1");
        keyValuePairs.put("key2", "value2");
        assertTrue("Swap did not successfully occur",
                fDss.putSwap("testKey", "testValue1", "testValue2", keyValuePairs));
    }

    /**
     * <p>
     * This test checks that a value can be retrieved.
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testGet() throws DynamicStatusStoreException, IOException {
        fDss.put("testKey", "testValue");
        assertEquals("Incorrect value retrieved", "testValue", fDss.get("testKey"));
    }

    /**
     * <p>
     * This test checks if all key/value pairs with a common prefix can be
     * retrieved.
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testGetPrefix() throws DynamicStatusStoreException, IOException {
        fDss.put("prefix.infix1.suffix", "testValue1");
        fDss.put("prefix.infix2.suffix", "testValue2");
        HashMap<String, String> keyValuePairs = new HashMap<>();
        keyValuePairs.put("prefix.infix1.suffix", "testValue1");
        keyValuePairs.put("prefix.infix2.suffix", "testValue2");
        assertEquals("Incorrect values retrieved", keyValuePairs, fDss.getPrefix("prefix"));
    }

    /**
     * <p>
     * This test checks if a key/value pair can be deleted.
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testDelete() throws DynamicStatusStoreException, IOException {
        fDss.put("testKey", "testValue");
        fDss.delete("testKey");
        assertNull("Key/value pair not deleted successfully", fDss.get("testKey"));
    }

    /**
     * <p>
     * This test checks if all key/value pairs with a common prefix can be deleted.
     * </p>
     * 
     * @throws DynamicStatusStoreException
     * @throws IOException
     */
    @Test
    public void testDeletePrefix() throws DynamicStatusStoreException, IOException {
        fDss.put("prefix.infix1.suffix", "testValue1");
        fDss.put("prefix.infix2.suffix", "testValue2");
        fDss.deletePrefix("prefix");
        assertEquals("Key/value pairs not deleted successfully", new HashMap<String, String>(),
                fDss.getPrefix("prefix"));
    }
}
