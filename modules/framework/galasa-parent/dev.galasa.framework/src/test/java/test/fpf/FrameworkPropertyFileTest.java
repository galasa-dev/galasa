/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.fpf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.Test;

import dev.galasa.framework.spi.FrameworkPropertyFile;
import dev.galasa.framework.spi.FrameworkPropertyFileException;
import dev.galasa.framework.spi.IFrameworkPropertyFileWatcher;
import dev.galasa.framework.spi.IFrameworkPropertyFileWatcher.Event;

import org.awaitility.Duration;
import static org.awaitility.Awaitility.*;
import org.junit.After;
import org.junit.Before;

/**
 * <p>
 * Used to test the FrameworkFileProperty class functionality.
 * </p>
 * 
 *  
 *
 */
public class FrameworkPropertyFileTest {
    File testProp    = null;
    URI  testPropUri = null;

    /**
     * <p>
     * This before method sets up a properties file for the tests to use. It cleans
     * up any previous version of the file.
     * </p>
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Before
    public void createPropertiesFile() throws FileNotFoundException, IOException {
        testProp = File.createTempFile("galasafpf_", ".properties");
        testProp.deleteOnExit();
        testPropUri = testProp.toURI();

        Properties testProps = new Properties();

        testProps.setProperty("Test1", "SomeString");
        testProps.setProperty("Test2", "SomeString");
        testProps.setProperty("prefix.infix.suffix1", "SomeString1");
        testProps.setProperty("prefix.infix.suffix2", "SomeString2");
        testProps.setProperty("RootPasswordForEverySystemEver", "admin");
        testProps.setProperty("anotherString", "anotherString");

        FileOutputStream out = new FileOutputStream(testProp);
        testProps.store(out, null);
        out.close();
    }

    /**
     * <p>
     * This cleans up the properties file used during testing
     * </p>
     */
    @After
    public void deletePropertiesFile() {
        if (testProp != null && testProp.exists()) {
            testProp.delete();
        }
    }

    /**
     * <p>
     * This tests that the FPF object can be intialised and the properties file read
     * </p>
     */
    @Test
    public void testReadFile() throws FrameworkPropertyFileException, URISyntaxException, IOException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testProp.toURI());
        assertNotNull("FPF did not initialise", fpf);
    }

    /**
     * <p>
     * This tests that a k-v pair can be queried from the properties file.
     * </p>
     */
    @Test
    public void testGetValueFromKey() throws FrameworkPropertyFileException, URISyntaxException, IOException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);

        assertEquals("Value from key was wrong", "admin", fpf.get("RootPasswordForEverySystemEver"));
        fpf.destroy();
    }

    /**
     * <p>
     * This test that a map of values can be returned from the properties file given
     * a common prefix for the keys
     * </p>
     */
    @Test
    public void testGetValuesWithKeysWithTest() throws FrameworkPropertyFileException, URISyntaxException, IOException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);
        Map<String, String> values = fpf.getPrefix("Test");

        for (String key : values.keySet()) {
            assertEquals("On of the values in the map was inccorect", "SomeString", values.get(key));
        }
        fpf.destroy();
    }

    /**
     * <p>
     * This tests the setting of a k-v pair into the properties file.
     * </p>
     */
    @Test
    public void testSetValueIntoProperties() throws FrameworkPropertyFileException, URISyntaxException, IOException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);

        fpf.set("ThisIsAHobbit", "ThisIsTheRingOfPower");
        String value = fpf.get("RootPasswordForEverySystemEver");

        assertTrue("Value not set correctly", value.equals("admin"));

        value = fpf.get("ThisIsAHobbit");

        assertTrue("Value not changed", value.equals("ThisIsTheRingOfPower"));
        fpf.destroy();
    }

    /**
     * <p>
     * This tests tthat a value that already exsists in the properties file is
     * properly updated
     * </p>
     */
    @Test
    public void testUpdateValueThatAlreadyExists()
            throws FrameworkPropertyFileException, URISyntaxException, IOException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);

        fpf.set("ThisIsAKey", "ThisIsValuable");
        String value = fpf.get("RootPasswordForEverySystemEver");

        assertTrue("Value not set correctly", value.equals("admin"));

        fpf.set("RootPasswordForEverySystemEver", "password");
        value = fpf.get("RootPasswordForEverySystemEver");

        assertEquals("Updated value was not as expected", "password", value);
        fpf.destroy();
    }

    /**
     * <p>
     * This tests the setting of a map of k-v pairs into the properties file.
     * </p>
     */
    @Test
    public void testSetMapValuesToPropertiesFile()
            throws FrameworkPropertyFileException, URISyntaxException, IOException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);
        Map<String, String> map = new HashMap<String, String>();

        map.put("key", "value");
        map.put("key2", "value");
        fpf.set(map);
        String value = fpf.get("key");

        assertTrue("Setting a map of values was not successful", value.equals("value"));
        fpf.destroy();
    }

    /**
     * <p>
     * This tests that a file is correctly locked for writing to a file.
     * </p>
     */
    @Test
    public void testTryAndAccessLockedFile() throws FrameworkPropertyFileException, FileNotFoundException, IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get(testPropUri), StandardOpenOption.WRITE);
        fileChannel.lock();
        boolean failed = false;

        try {
            FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);
            fpf.set("thisShouldBeAdded", "It wasnt");
            fpf.destroy();
        } catch (Exception e) {
            failed = true;
        }
        fileChannel.close();
        assertTrue("Failed to lock file", failed);

    }

    /**
     * <p>
     * This tests that a k--v pair can be deleted from the properties file
     * </p>
     */
    @Test
    public void testDeleteKey() throws FrameworkPropertyFileException, URISyntaxException, IOException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);

        fpf.delete("RootPasswordForEverySystemEver");

        assertNull("Delete failed, value non null", fpf.get("RootPasswordForEverySystemEver"));
        fpf.destroy();
    }

    /**
     * <p>
     * This tests that a set of keys can be removed from the properties file and
     * their corresponding value.
     * </p>
     */
    @Test
    public void testDeleteSetOfKeys()
            throws InterruptedException, FrameworkPropertyFileException, URISyntaxException, IOException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);
        Set<String> keys = new HashSet<String>();

        keys.add("Test1");
        keys.add("Test2");
        keys.add("RootPasswordForEverySystemEver");

        fpf.delete(keys);

        assertNull("Value recieved was not null", fpf.get("Test1"));
        assertNull("Second value was not null", fpf.get("Test2"));
        assertNull("Third value from get was not null", fpf.get("RootPasswordForEverySystemEver"));

        fpf.destroy();
    }

    /**
     * <p>
     * This tests that keys with a certain prefix can be removed from the properties
     * file and their corresponding values.
     * </p>
     */
    @Test
    public void testDeletePrefix() throws FrameworkPropertyFileException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);

        fpf.deletePrefix("prefix");

        assertNull("Value was not null", fpf.get("prefix"));

        fpf.destroy();
    }

    /**
     * <p>
     * This tests to see if a watch can be created and that it detects changes to
     * the watched value.
     * </p>
     */
    @Test
    public void testAddNewWatch()
            throws FrameworkPropertyFileException, URISyntaxException, IOException, InterruptedException {
        String key = "watchTest";
        String newValue = "AreYouWatching";
        String oldValue = "no";

        URI file = testProp.toURI();
        FrameworkPropertyFile fpf1 = new FrameworkPropertyFile(file);
        FrameworkPropertyFile fpf2 = new FrameworkPropertyFile(file);

        fpf1.set(key, oldValue);

        Watcher watcher = new Watcher();
        UUID watchId = fpf1.watch(watcher, key);

        String check = fpf1.get(key);

        assertEquals("The old value was not correctly set", oldValue, check);

        fpf2.set(key, newValue);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key, newValue));

        fpf1.unwatch(watchId);
        assertEquals("Wacther not using the correct key", key, watcher.key);
        assertEquals("Event recorded by the watcher is not as expected", Event.MODIFIED, watcher.event);
        assertEquals("The old value was not correctly set in the watcher", oldValue, watcher.oldValue);
        assertEquals("The new value was not set in the wactcher", newValue, watcher.newValue);

        testProp.deleteOnExit();
        fpf1.destroy();
        fpf2.destroy();
    }

    /**
     * <p>
     * This tests a watch can be created, detect a change, the watch be removed and
     * no futher changes are noftied.
     * </p>
     */
    @Test
    public void testAddWatchThenUnwatch()
            throws FrameworkPropertyFileException, URISyntaxException, IOException, InterruptedException {
        String key = "Mug";
        String value1 = "ofCoffee";
        String value2 = "ofTea";
        String value3 = "BEES!!";

        FrameworkPropertyFile fpf1 = new FrameworkPropertyFile(testPropUri);
        FrameworkPropertyFile fpf2 = new FrameworkPropertyFile(testPropUri);

        fpf1.set(key, value1);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key, value1));

        Watcher watcher = new Watcher();
        UUID watchId = fpf1.watch(watcher, key);

        fpf2.set(key, value2);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key, value2));

        fpf1.unwatch(watchId);
        fpf2.set(key, value3);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key, value3));

        assertEquals("Key stored in the watcher is incorrect", key, watcher.key);
        assertEquals("Watcher recorded the inccorected event", Event.MODIFIED, watcher.event);
        assertEquals("Old value in the wacher is incorect", value1, watcher.oldValue);
        assertEquals("New value in the watcher is incorrect", value2, watcher.newValue);

        testProp.deleteOnExit();
        fpf1.destroy();
        fpf2.destroy();
    }

    /**
     * <p>
     * This tests a prefix watch can be created and detect changes to any of the
     * values.
     * </p>
     */
    @Test
    public void testAddPrefixWatch()
            throws FrameworkPropertyFileException, URISyntaxException, IOException, InterruptedException {
        String prefix = "Test";
        String key1 = "Test1";
        String value1 = "ThisIsNotADrill";
        String key2 = "Test2";
        String value2 = "nextValuePlease";

        FrameworkPropertyFile fpf1 = new FrameworkPropertyFile(testPropUri);
        FrameworkPropertyFile fpf2 = new FrameworkPropertyFile(testPropUri);

        Watcher watcher = new Watcher();
        UUID watchId = fpf1.watchPrefix(watcher, prefix);

        fpf2.set(key1, value1);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key1, value1));

        assertEquals("Wrong key in the watcher was found", "Test1", watcher.key);
        assertEquals("Wrong event from the watcher was recorded", Event.MODIFIED, watcher.event);
        assertEquals("Wrong new value for the watcher was found", value1, watcher.newValue);

        fpf2.set(key2, value2);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key2, value2));

        assertEquals("Wrong key found", "Test2", watcher.key);
        assertEquals("Watcher recorded the wrog event", Event.MODIFIED, watcher.event);
        assertEquals("Wrong newValue in watcher found", "nextValuePlease", watcher.newValue);

        fpf1.unwatch(watchId);
        fpf1.destroy();
        fpf2.destroy();
    }

    /**
     * <p>
     * This tests a prefix watch can be created and detect changes to any of the
     * values, and then be unwatched and report no further changes to any value.
     * </p>
     */
    @Test
    public void testAddPrefixWatchThenUnwatch()
            throws FrameworkPropertyFileException, URISyntaxException, IOException, InterruptedException {
        String prefix = "Test";
        String key1 = "Test1";
        String value1 = "ComputerSays";
        String key2 = "Test2";
        String value2 = "No";

        FrameworkPropertyFile fpf1 = new FrameworkPropertyFile(testPropUri);
        FrameworkPropertyFile fpf2 = new FrameworkPropertyFile(testPropUri);

        Watcher watcher = new Watcher();
        UUID watchId = fpf1.watchPrefix(watcher, prefix);

        fpf2.set(key1, value1);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key1, value1));

        assertEquals("Wrong key found", "Test1", watcher.key);
        assertEquals("Watcher event was incorrect", Event.MODIFIED, watcher.event);
        assertEquals("Watcher value wrong", "ComputerSays", watcher.newValue);

        fpf1.unwatch(watchId);

        fpf2.set(key2, value2);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key2, value2));

        assertEquals("Wrong value found, unwatch failed", key1, watcher.key);
        assertEquals("Watcher evernt was not as exoected, unwatch failed", Event.MODIFIED, watcher.event);
        assertEquals("Watcher new value was incorrect, unwatch failed", value1, watcher.newValue);
        fpf1.destroy();
        fpf2.destroy();
    }

    /**
     * <p>
     * This tests that an atomic set of a single value can be peformed
     * </p>
     */
    @Test
    public void testAtomicSet()
            throws InterruptedException, URISyntaxException, IOException, FrameworkPropertyFileException {
        String newValue = "AtomicallySet";
        String nonSetString = "ShouldntBeThis";

        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);

        assertTrue("Setting atomically failed", fpf.setAtomic("Test1", "SomeString", newValue));
        assertEquals("Value not set", newValue, fpf.get("Test1"));
        assertFalse("Atomic was incorrect", fpf.setAtomic("Test1", "SomeString", nonSetString));
        assertNotEquals("The wrong value set", nonSetString, fpf.get("Test1"));
        fpf.destroy();
    }

    /**
     * 
     * <p>
     * This tests that an atomic set of a single value can be peformed and the
     * epxcted behaviour with null is working
     * </p>
     */
    @Test
    public void testAtomicSetWithNullOldvalue()
            throws InterruptedException, URISyntaxException, IOException, FrameworkPropertyFileException {
        String newValue = "AtomicallySet";
        String nonSetString = "ShouldntBeThis";

        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);

        assertTrue("Setting atomically failed", fpf.setAtomic("RandomTestKey", null, newValue));
        assertEquals("The value set", newValue, fpf.get("RandomTestKey"));
        assertFalse("Key doesnt exists", fpf.setAtomic("RandomTestKey", null, nonSetString));
        assertNotEquals("The value set", nonSetString, fpf.get("RandomTestKey"));
        fpf.destroy();
    }

    /**
     * <p>
     * This tests that a atomic set to a value and map can be completed
     * </p>
     */
    @Test
    public void testAtomicSetAMap() throws URISyntaxException, IOException, FrameworkPropertyFileException {
        String newValue = "AtomicallySet";
        String nonSetString = "ShouldntBeThis";

        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        map.put("key2", "value");

        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("key", nonSetString);
        map2.put("key2", nonSetString);

        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);

        assertTrue("Atomic set failed", fpf.setAtomic("Test1", "SomeString", newValue, map));
        assertEquals("Wrong value set", "value", fpf.get("key"));
        assertFalse("Atomic set failed", fpf.setAtomic("Test1", "SomeString", nonSetString, map2));
        assertEquals("Wrong Value set", "value", fpf.get("key"));
        assertNotEquals("Atmoic set was applied incorrectly", nonSetString, fpf.get("Test1"));
        fpf.destroy();
    }

    /**
     * 
     * <p>
     * This tests that an atomic set of a single value can be peformed and the
     * epxcted behaviour with null is working
     * </p>
     */
    @Test
    public void testAtomicSetMapWithNullOldvalue()
            throws InterruptedException, URISyntaxException, IOException, FrameworkPropertyFileException {
        String newValue = "AtomicallySet";
        String nonSetString = "ShouldntBeThis";

        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        map.put("key2", "value");

        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("key", nonSetString);
        map2.put("key2", nonSetString);

        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testPropUri);

        assertTrue("Setting atomically failed", fpf.setAtomic("RandomTestKey", null, newValue, map));
        assertEquals("The value set", newValue, fpf.get("RandomTestKey"));
        assertFalse("Key doesnt exists", fpf.setAtomic("RandomTestKey", null, nonSetString, map2));
        assertNotEquals("The value set", nonSetString, fpf.get("RandomTestKey"));
        fpf.destroy();
    }

    /**
     * <p>
     * This tests that the watcher can detect a delete event
     * </p>
     */
    @Test
    public void testAddNewWatchAndLookForDelete()
            throws FrameworkPropertyFileException, URISyntaxException, IOException, InterruptedException {
        String key = "watchTest";
        String oldValue = "no";

        URI file = testProp.toURI();
        FrameworkPropertyFile fpf1 = new FrameworkPropertyFile(file);
        FrameworkPropertyFile fpf2 = new FrameworkPropertyFile(file);

        fpf1.set(key, oldValue);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key, oldValue));

        Watcher watcher = new Watcher();
        UUID watchId = fpf1.watch(watcher, key);

        String check = fpf1.get(key);

        assertEquals("Value not set correctly", oldValue, check);

        fpf2.delete(key);
        await().atMost(Duration.ONE_SECOND).until(checkDeleteValue(key));

        fpf1.unwatch(watchId);
        assertEquals("Watcher key was not as expected.", key, watcher.key);
        assertEquals("Watcher event was not as expected.", Event.DELETE, watcher.event);

        testProp.deleteOnExit();
        fpf1.destroy();
        fpf2.destroy();
    }

    /**
     * <p>
     * This tests that a watcher can detect a NEW event
     * </p>
     */
    @Test
    public void testAddNewWatchAndLookForNew()
            throws FrameworkPropertyFileException, URISyntaxException, IOException, InterruptedException {
        String key = "watchTest";
        String value = "AreYouWatching";

        URI file = testProp.toURI();
        FrameworkPropertyFile fpf1 = new FrameworkPropertyFile(file);
        FrameworkPropertyFile fpf2 = new FrameworkPropertyFile(file);

        Watcher watcher = new Watcher();
        UUID watchId = fpf1.watch(watcher, key);

        fpf2.set(key, value);
        await().atMost(Duration.ONE_SECOND).until(checkSetValue(key, value));

        fpf1.unwatch(watchId);
        assertEquals("Watcher key was not as expected.", key, watcher.key);
        assertEquals("Watcher event was not as expected.", Event.NEW, watcher.event);
        assertEquals("Watcher value was not as expected.", value, watcher.newValue);
        assertEquals("Watcher old value was not as expected.", null, watcher.oldValue);

        testProp.deleteOnExit();
        fpf1.destroy();
        fpf2.destroy();
    }

    /**
     * <p>
     * This callable mehtod is used to detect changes to the file so the await()
     * function can be used of thread.sleep()
     * </p>
     */
    private Callable<Boolean> checkSetValue(String key, String value) throws FrameworkPropertyFileException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testProp.toURI());

        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return fpf.get(key).equals(value);
            }
        };
    }

    /**
     * <p>
     * This callable mehtod is used to detect deletes to the file so the await()
     * function can be used of thread.sleep()
     * </p>
     */
    private Callable<Boolean> checkDeleteValue(String key) throws FrameworkPropertyFileException {
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(testProp.toURI());

        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return fpf.get(key) == null;
            }
        };
    }

    /**
     * <p>
     * This class implements the watcher interface for testing purposes.
     * </p>
     */
    private static class Watcher implements IFrameworkPropertyFileWatcher {

        private Event  event;
        private String key;
        private String newValue;
        private String oldValue;

        /**
         * <p>
         * This method overides the interface to test the watcher methods.
         * </p>
         */
        @Override
        public void propertyModified(String key, Event event, String oldValue, String newValue) {
            this.event = event;
            this.key = key;
            this.newValue = newValue;
            this.oldValue = oldValue;
        }
    }
}
