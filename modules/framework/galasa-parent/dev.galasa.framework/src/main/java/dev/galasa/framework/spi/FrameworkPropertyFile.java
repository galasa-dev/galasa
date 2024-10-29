/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IFrameworkPropertyFileWatcher.Event;

/**
 * <p>
 * Used by Galasa as an out of the box key value store. It is reccommended to
 * use etcd3 for a performant system.
 * </p>
 * 
 * <p>
 * When provded with a URI to a K-V properties file, the FPF class can provide
 * functionality similar to etcd3, including sets, gets, deletes, atomic sets
 * and the watchng of values inside the properties.
 * </p>
 * 
 *  
 *
 */

public class FrameworkPropertyFile implements FileAlterationListener {
    private Properties             currentProperties = new Properties();
    private HashMap<UUID, Watch>   watches           = new HashMap<>();
    private URI                    file;
    private File                   propertyFile;
    private String                 parent;
    private FileAlterationObserver observer;
    private FileAlterationMonitor  monitor;
    private static Log             fpfLog            = LogFactory.getLog(FrameworkPropertyFile.class);

    /**
     * <p>
     * This constructor loads the properties store in the file into memory. For the
     * watchers to get updates from the file in question and not other files in the
     * directory, the observer is intialised with a file filter.
     * </p>
     * 
     * @param file - URI of the java properties file
     * @throws FrameworkPropertyFileException
     */

    public FrameworkPropertyFile(URI file) throws FrameworkPropertyFileException {
        this.file = file;
        this.propertyFile = new File(file);
        this.parent = propertyFile.getParent();

        load();

        IOFileFilter filter = FileFilterUtils.nameFileFilter(propertyFile.getName());
        try {
            observer = new FileAlterationObserver(FileUtils.getFile(parent), filter);
            observer.addListener(this);
            observer.initialize();
        } catch (Exception e) {
            throw new FrameworkPropertyFileException("Problem starting observer", e);
        }
    }

    /**
     * <p>
     * This method retrieves the most up to date value from a specified key.
     * </p>
     * 
     * @param key
     * @return - corresponding value from the key, null if non-exsisting
     */
    public synchronized String get(final String key) {
        observer.checkAndNotify();
        return currentProperties.getProperty(key);

    }

    /**
     * <p>
     * This method retrieves a Map of values from the properties file that have a
     * common prefix to the key.
     * </p>
     * 
     * @param keyPrefix - a common key prefix to a number of keys in the property
     *                  store.
     * @return A map of any size or null. The key is a string which starts with the specified prefix.
     */
    public synchronized Map<String, String> getPrefix(String keyPrefix) {
        Map<String, String> values = new HashMap<>();
        observer.checkAndNotify();
        for (Object k : currentProperties.keySet()) {
            String key = (String) k;
            if (key.startsWith(keyPrefix)) {
                values.put(key, currentProperties.getProperty(key));
            }
        }
        return values;
    }

    /**
     * <p>
     * This method retrieves a List of namespaces which have properties set
     * in the properties file.
     * </p>
     * 
     * @return - List of namespaces
     */
    public synchronized List<String> getNamespaces() {
        List<String> namespaces = new ArrayList<>();
        observer.checkAndNotify();
        for (Object k : currentProperties.keySet()) {
            String name = ((String)k).substring(0,((String)k).indexOf("."));
            if(!namespaces.contains(name)) {
                namespaces.add(name);
            }
        }
        return namespaces;
    }

    /**
     * <p>
     * This method deletes a k-v pair from the property store. As the properties
     * file is changed, the fileModified method is invoked to update any watchers.
     * </p>
     * 
     * @param key - the string key to be removoed from the loaded and file
     *            properties.
     * @throws FrameworkPropertyFileException
     */
    public synchronized void delete(String key) throws FrameworkPropertyFileException {
        // Make the current properties as close to the values in the file as 
        // we can. So when we write they are up-to-date.
        if (observer!=null) {
            observer.checkAndNotify();
        }

        // There is a small window here when another JVM may write to the file,
        // which will get lost when we over-write it.
        synchronized (FrameworkPropertyFile.class) {
            try (FileChannel fileChannel = getWriteChannel(false)) {
                Properties oldProperties = (Properties) this.currentProperties.clone();
                this.currentProperties.remove(key);
                write(fileChannel, this.currentProperties);
                fileModified(this.currentProperties, oldProperties);
            } catch (IOException e) {
                fpfLog.error("Unable to delete the key: " + key, e);
                throw new FrameworkPropertyFileException("Unable to delete key: " + key, e);
            }
        }
    }

    /**
     * This method does the same as the regular delete for a single k-v pair, but
     * for a set of key values. Again the fileModified is invoked.
     * 
     * @param keys - a set of string keys to remove from properties
     * @throws FrameworkPropertyFileException
     */
    public synchronized void delete(Set<String> keys) throws FrameworkPropertyFileException {
        // Make the current properties as close to the values in the file as 
        // we can. So when we write they are up-to-date.
        if (observer!=null) {
            observer.checkAndNotify();
        }
        // There is a small window here when another JVM may write to the file,
        // which will get lost when we over-write it.
        synchronized (FrameworkPropertyFile.class) {
            try (FileChannel fileChannel = getWriteChannel(false)) {
                Properties oldProperties = (Properties) this.currentProperties.clone();

                for (String key : keys) {
                    this.currentProperties.remove(key);
                }

                write(fileChannel, this.currentProperties);
                fileModified(this.currentProperties, oldProperties);
            } catch (IOException e) {
                fpfLog.error("Unable to delete keys", e);
                throw new FrameworkPropertyFileException("Unable to delete keys.", e);
            }
        }
    }

    /**
     * This method deletes the set of key values with a certain prefix.
     * 
     * @param prefix - a prefix of keys to remove from properties
     * @throws FrameworkPropertyFileException
     */
    public synchronized void deletePrefix(String prefix) throws FrameworkPropertyFileException {
        Set<String> deleteKeys = new HashSet<>();

        // refresh our cache of properties from the file contents.
        // Note that we can't do this while holding a write lock as the check
        // attempts to get a read lock which fails, as the write lock is held.
        if (observer!=null) {
            observer.checkAndNotify();
        }
        // There is a small timing window here where another JVM will enter and write a
        // new set of properties to the file. We have no lock protecting it, so are 
        // likely to over-write the file with our property cache values...
        // Meaning this code is not able to protect itself from other JVMs interfering, and 
        // vice-versa.

        synchronized (FrameworkPropertyFile.class) {
            try {
                // Block other JVMs from writing to the property file while we delete things.
                try (FileChannel fileChannel = getWriteChannel(false)) {

                    // Gather the list of keys to be deleted.
                    for (Object k : currentProperties.keySet()) {
                        String key = (String) k;
                        if (key.startsWith(prefix)) {
                            deleteKeys.add(key);
                        }
                    }

                    // Make a note of the current property values, so we retain a before... and after...
                    // set to be used later notifying any observers of property changes.
                    Properties oldProperties = (Properties) this.currentProperties.clone();

                    // Now delete the keys
                    for (String key : deleteKeys) {
                        this.currentProperties.remove(key);
                    }

                    write(fileChannel, this.currentProperties);
                    fileModified(this.currentProperties, oldProperties);
                }
            } catch (IOException e) {
                fpfLog.error("Failed to update file with DSS actions", e);
                throw new FrameworkPropertyFileException("Unable to delete key prefix: " + prefix, e);
            }
        }
    }

    public synchronized void performActions(IDssAction... actions) throws DynamicStatusStoreException, DynamicStatusStoreMatchException {
        synchronized (FrameworkPropertyFile.class) {
            try (FileChannel fileChannel = getWriteChannel(false)) {
                Properties oldProperties = (Properties) this.currentProperties.clone();

                for(IDssAction action : actions) {
                    if (action instanceof DssAdd) {
                        performActionsAdd((DssAdd) action);
                    } else if (action instanceof DssDelete) {
                        performActionsDelete((DssDelete) action);
                    } else if (action instanceof DssDeletePrefix) {
                        performActionsDeletePrefix((DssDeletePrefix) action);
                    } else if (action instanceof DssUpdate) {
                        performActionsUpdate((DssUpdate) action);
                    } else if (action instanceof DssSwap) {
                        performActionsSwap((DssSwap) action);
                    } else {
                        throw new DynamicStatusStoreException("Unrecognised DSS Action - " + action.getClass().getName());
                    }
                }

                write(fileChannel, this.currentProperties);
                fileModified(this.currentProperties, oldProperties);
            } catch (IOException e) {
                fpfLog.error("Failed to update file with DSS actions", e);
                throw new DynamicStatusStoreException("Failed to update file with DSS actions", e);
            }
        }
    }

    private void performActionsAdd(DssAdd dssAdd) throws DynamicStatusStoreMatchException {
        String key = dssAdd.getKey();
        String value = dssAdd.getValue();

        String currentValue = this.currentProperties.getProperty(key);
        if (currentValue != null) {
            throw new DynamicStatusStoreMatchException("Attempt to add new property '" + key + "' but it already exists");
        }

        this.currentProperties.put(key, value);
    }


    private void performActionsDelete(DssDelete dssDelete) throws DynamicStatusStoreMatchException {
        String key = dssDelete.getKey();
        String oldValue = dssDelete.getOldValue();

        if (oldValue != null) {
            String currentValue = this.currentProperties.getProperty(key);
            if (!oldValue.equals(currentValue)) {
                throw new DynamicStatusStoreMatchException("Attempt to delete property '" + key + "', but current value '" + currentValue + "' does not match required value '" +oldValue + "'");
            }
        }

        this.currentProperties.remove(key);
    }


    private void performActionsDeletePrefix(DssDeletePrefix dssDeletePrefix) {
        ArrayList<String> toBeDeleted = new ArrayList<>();
        for (Object k : this.currentProperties.keySet()) {
            String key = (String) k;
            if (key.startsWith(dssDeletePrefix.getPrefix())) {
                toBeDeleted.add(key);
            }
        }
        
        for(String key : toBeDeleted) {
            this.currentProperties.remove(key);
        }
        
    }


    private void performActionsUpdate(DssUpdate dssUpdate) {
        String key   = dssUpdate.getKey();
        String value = dssUpdate.getValue();

        this.currentProperties.put(key, value);
    }


    private void performActionsSwap(DssSwap dssSwap) throws DynamicStatusStoreMatchException {
        String key      = dssSwap.getKey();
        String newValue = dssSwap.getNewValue();
        String oldValue = dssSwap.getOldValue();
        
        String currentValue = this.currentProperties.getProperty(key);

        if (oldValue == null) {
            if (currentValue != null) {
                throw new DynamicStatusStoreMatchException("Attempt to swap property '" + key + "', but current value '" + currentValue + "' does not match required value '" +oldValue + "'");
            }
        } else {
            if (!oldValue.equals(currentValue)) {
                throw new DynamicStatusStoreMatchException("Attempt to swap property '" + key + "', but current value '" + currentValue + "' does not match required value '" +oldValue + "'");
            }
        }
        
        this.currentProperties.put(key, newValue);
    }


    /**
     * <p>
     * This method is used for the writing of the current properties in memory to be
     * stored in the java properties file defined by the URI.
     * </p>
     * 
     * @param fileChannel   - a write file channel that has an exclusive lock.
     * @param newProperties - the most up to date properties in memory
     * @throws IOException
     */
    public synchronized void write(FileChannel fileChannel, Properties newProperties) throws IOException {
        fileChannel.truncate(0);
        OutputStream out = Channels.newOutputStream(fileChannel);
        newProperties.store(out, null);
        out.close();
    }

    /**
     * <p>
     * This method is used to set a single k-v pair into the properties file.
     * fileModified is invoked as to update watchers to any changed values that are
     * being watched.
     * </p>
     * 
     * @param key   - String key
     * @param value - String value
     * @throws FrameworkPropertyFileException
     */
    public synchronized void set(String key, String value) throws FrameworkPropertyFileException {

        if (observer!=null) {
            observer.checkAndNotify();
        }

        synchronized (FrameworkPropertyFile.class) {
            try (FileChannel fileChannel = getWriteChannel(false)) {
                Properties oldProperties = (Properties) this.currentProperties.clone();

                this.currentProperties.put(key, value);

                write(fileChannel, this.currentProperties);
                fileModified(this.currentProperties, oldProperties);
            } catch (IOException e) {
                fpfLog.error("Unable to set key value pair: " + key + ":" + value, e);
                throw new FrameworkPropertyFileException("Failed Setting value: " + key + "=" + value, e);
            }
        }
    }

    /**
     * <p>
     * This method is used for setting multiple k-v pairs into the file. This method
     * is sycnronized on the class to ensure all values are set before any other
     * work is completed
     * </p>
     * 
     * @param values - a String String map of k-v pairs.
     * @throws FrameworkPropertyFileException
     * @throws IOException
     */
    public synchronized void set(Map<String, String> values) throws FrameworkPropertyFileException, IOException {
        if (observer!=null) {
            observer.checkAndNotify();
        }
        synchronized (FrameworkPropertyFile.class) {
            try (FileChannel fileChannel = getWriteChannel(false)) {
                Properties oldProperties = (Properties) this.currentProperties.clone();

                this.currentProperties.putAll(values);

                write(fileChannel, this.currentProperties);
                fileModified(this.currentProperties, oldProperties);
            } catch (IOException e) {
                fpfLog.error("Unable to set values", e);
                throw new FrameworkPropertyFileException("Unable to set values", e);
            }
        }
    }

    /**
     * <p>
     * This method provides a watching service for a key value pair inside
     * properties. The value does not need to exsists to create a watcher. The
     * watcher records the activity and event type on detection of chnageds
     * (Modified, Deleted, Created).
     * </p>
     * 
     * <p>
     * The watcher service uses two methods of detecting changes to the file. A
     * polling service which montiors the file every 50ms for any changes. It also
     * uses the checkAndNotify() methods provided from the observer set up on the
     * class intialiastion, which is a manual check for file changes which notifies
     * any watches.
     * </p>
     * 
     * @param watcher - an interface for the watchers inplementation.
     * @param key     - the string key to watch
     * @return - returns a UUID which is used to identify a watcher service.
     * @throws FrameworkPropertyFileException
     */
    public synchronized UUID watch(IFrameworkPropertyFileWatcher watcher, String key)
            throws FrameworkPropertyFileException {
        if (monitor == null) {
            monitor = new FileAlterationMonitor(50, observer);
            try {
                monitor.start();
            } catch (Exception e) {
                throw new FrameworkPropertyFileException("Unable to start file monitor", e);
            }
        }

        UUID watchID = UUID.randomUUID();
        this.watches.put(watchID, new Watch(watcher, key, false));
        return watchID;
    }

    /**
     * <p>
     * This method is used to stop any watcher service with a given UUID. It removes
     * the given watcher from the watches list. If this is the final watcher in the
     * list the method also shuts down the monitor
     * </p>
     * 
     * @param watchId - the identifying UUID
     * @throws FrameworkPropertyFileException
     */
    public synchronized void unwatch(UUID watchId) throws FrameworkPropertyFileException {
        this.watches.remove(watchId);

        if (this.watches.isEmpty() && this.monitor != null) {
            this.monitor.removeObserver(observer);
            try {
                this.monitor.stop();
            } catch (Exception e) {
                throw new FrameworkPropertyFileException("Problems encountered during the stop of the monitor", e);
            }
            this.monitor = null;
        }
    }

    /**
     * <p>
     * This method provides a single watch service to watch multiple k-v pairs with
     * a common prefix in there key.
     * </p>
     * 
     * @param watcher   an interface for the watchers inplementation.
     * @param keyPrefix the string prefix to a key set to watch
     * @return returns a UUID which is used to identify a watcher service.
     * @throws FrameworkPropertyFileException
     */
    public synchronized UUID watchPrefix(IFrameworkPropertyFileWatcher watcher, String keyPrefix)
            throws FrameworkPropertyFileException {
        if (monitor == null) {
            monitor = new FileAlterationMonitor(50, observer);
            try {
                monitor.start();
            } catch (Exception e) {
                throw new FrameworkPropertyFileException("Unable to start file monitor for prefixs", e);
            }
        }

        UUID watchID = UUID.randomUUID();
        this.watches.put(watchID, new Watch(watcher, keyPrefix, true));
        return watchID;
    }

    /**
     * <p>
     * This memthod can perform a atomic set. This provides a set functionality in
     * the case where a key is currrenly set to a specific value. If the value is
     * not as expected, then no set is performed. fileModified is invoked to update
     * any possible watches.
     * </p>
     * 
     * <p>
     * This method is sycnronized on the class to ensure all values are set before
     * any other work is completed
     * </p>
     * 
     * <p>
     * The method will also fail if the oldvalue is null, which indicates that there
     * is no key of that string.
     * </p>
     * 
     * @param key      the key that is to be changed.
     * @param oldValue the expected current value.
     * @param newValue the value to change to if the expected value is true.
     * @return returns a boolean which informs if the set took place.
     * @throws FrameworkPropertyFileException
     */
    public synchronized boolean setAtomic(String key, String oldValue, String newValue)
            throws FrameworkPropertyFileException {

        if (observer!=null) {
            observer.checkAndNotify();
        }

        synchronized (FrameworkPropertyFile.class) {
            try (FileChannel fileChannel = getWriteChannel(false)) {
                Properties oldProperties = (Properties) this.currentProperties.clone();
                if (oldValue == null && oldProperties.get(key) == null) {
                    this.currentProperties.put(key, newValue);
                } else {
                    if (oldValue == null) {
                        return false;
                    }
                    if (!this.currentProperties.replace(key, oldValue, newValue)) {
                        return false;
                    }
                }

                write(fileChannel, this.currentProperties);
                fileModified(this.currentProperties, oldProperties);
                return true;
            } catch (IOException e) {
                fpfLog.error("Failed to set Atomically", e);
                throw new FrameworkPropertyFileException("Failed to set atomically", e);
            }
        }
    }

    /**
     * <p>
     * This methods also performs and atomic set, but with the additional feature of
     * setting a map of other k-v pairs if the old value is found to be the current
     * value
     * </p>
     * 
     * <p>
     * This method is sycnronized on the class to ensure all values are set before
     * any other work is completed
     * </p>
     * 
     * <p>
     * The method will also fail if the oldvalue is null, which indicates that there
     * is no key of that string.
     * </p>
     * 
     * @param key         - String key
     * @param oldValue    - String expected value
     * @param newValue    - String value to change to if key has oldvalue
     * @param otherValues - Map of k-v pairs to set if key has oldvalue
     * @return - boolean for if the atomic set was done
     * @throws FrameworkPropertyFileException
     */
    public synchronized boolean setAtomic(String key, String oldValue, String newValue, Map<String, String> otherValues)
            throws FrameworkPropertyFileException {

        if (observer!=null) {
            observer.checkAndNotify();
        }

        synchronized (FrameworkPropertyFile.class) {
            try (FileChannel fileChannel = getWriteChannel(false)) {
                Properties oldProperties = (Properties) this.currentProperties.clone();

                if (oldValue == null && oldProperties.get(key) == null) {
                    this.currentProperties.put(key, newValue);
                } else {
                    if (oldValue == null) {
                        return false;
                    }
                    if (!this.currentProperties.replace(key, oldValue, newValue)) {
                        return false;
                    }
                }

                this.currentProperties.putAll(otherValues);

                write(fileChannel, this.currentProperties);
                fileModified(this.currentProperties, oldProperties);
                return true;
            } catch (IOException e) {
                fpfLog.error("Failed to set Atomically", e);
                throw new FrameworkPropertyFileException("Failed to set atomically", e);
            }
        }
    }

    /**
     * <p>
     * This method cleans up the properties in memory, the observers and monitors.
     * </p>
     * 
     * @throws FrameworkPropertyFileException
     */
    public synchronized void destroy() throws FrameworkPropertyFileException {
        currentProperties = null;
        observer = null;
        try {
            if (monitor != null) {
                monitor.stop();
            }
        } catch (Exception e) {
            throw new FrameworkPropertyFileException("Unable to stop the monitor.", e);
        }
    }

    /**
     * <p>
     * This method is for updating any watchers. All running watches are stored in a
     * watches Map. Comparing what is loaded in memory to the file version alerts
     * any watches that a value has changed. This alert is interfaced through the
     * property modified method
     * </p>
     * 
     * @param newProperties - loaded from file
     * @param oldProperties - in memory currently
     */
    private synchronized void fileModified(Properties newProperties, Properties oldProperties) {
        // Checks a list of Strings that are keys to watch. Compares file to loaded.
        for (Watch watch : this.watches.values()) {
            for (Object oNewKey : newProperties.keySet()) {
                String newKey = (String) oNewKey;
                String newValue = newProperties.getProperty(newKey);
                if (watch.matchKey(newKey)) {
                    String oldValue = oldProperties.getProperty(newKey);
                    if (oldValue == null) {
                        watch.watcher.propertyModified(newKey, Event.NEW, oldValue, newValue);
                    } else if (!oldValue.equals(newValue)) {
                        watch.watcher.propertyModified(newKey, Event.MODIFIED, oldValue, newValue);
                    }
                }
            }

            // Check for deleted properties
            for (Object oOldKey : oldProperties.keySet()) {
                String oldKey = (String) oOldKey;
                String oldValue = oldProperties.getProperty(oldKey);

                if (watch.matchKey(oldKey)) {
                    String newValue = newProperties.getProperty(oldKey);
                    if (newValue == null) {
                        watch.watcher.propertyModified(oldKey, Event.DELETE, oldValue, newValue);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * This method is used for returning a file channel that can be used for reading
     * the property file with a shared lock
     * </p>
     * 
     * @param shared - true for a shared lock (expected)
     * @return - a read file channel
     * @throws IOException
     */
    private synchronized FileChannel getReadChannel(boolean shared) throws IOException {
        Path path = Paths.get(file);
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        try {
            fileChannel.lock(0, Long.MAX_VALUE, shared);
            return fileChannel;
        } catch (IOException e) {
            fileChannel.close();
            throw e;
        }
    }

    /**
     * <p>
     * This method is used to return a file channel that can be used for writing to
     * the properties file. It is expected to be an exlusive lock
     * </p>
     * 
     * @param shared - expected to be false for a write to the file
     * @return - a write file channel
     * @throws IOException
     */
    private synchronized FileChannel getWriteChannel(boolean shared) throws IOException {
        Path path = Paths.get(file);
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE);
        try {
            fileChannel.lock(0, Long.MAX_VALUE, shared);
            return fileChannel;
        } catch (IOException e) {
            fileChannel.close();
            throw e;
        }
    }

    /**
     * <p>
     * This method is used for loading the properties from the java properties file
     * in a memory loaded properties
     * </p>
     * 
     * *
     * <p>
     * This method is sycnronized on the class to ensure all values are set before
     * any other work is completed
     * </p>
     * 
     * @throws FrameworkPropertyFileException
     */
    public synchronized void load() throws FrameworkPropertyFileException {
        Properties newProperties = new Properties();
        synchronized (FrameworkPropertyFile.class) {
            try (FileChannel fileChannel = getReadChannel(true)) {
                InputStream in = Channels.newInputStream(fileChannel);
                newProperties.load(in);
                in.close();
            } catch (IOException e) {
                fpfLog.error("Unable to Load Property from file", e);
                throw new FrameworkPropertyFileException(
                        "Unable to Load Property from file: " + propertyFile.toString(), e);
            }
        }
        this.currentProperties = newProperties;
    }

    /**
     * <p>
     * This method is not used
     * </p>
     */
    public void onStart(FileAlterationObserver observer) {
        // Method not used for fpf
    }

    /**
     * <p>
     * This method is not used
     * </p>
     */
    public void onStop(FileAlterationObserver observer) {
        // Method not used for fpf
    }

    /**
     * <p>
     * This method invoked when the montior thread detects a change to the file
     * being observed
     * </p>
     * 
     * <p>
     * As this is an implemented method, it cannot throw a exception so the
     * exception is logged
     * </p>
     */
    public synchronized void onFileChange(File file) {
        synchronized (FrameworkPropertyFile.class) {
            try {
                Properties oldProperties = (Properties) this.currentProperties.clone();
                load();
                fileModified(this.currentProperties, oldProperties);
            } catch (FrameworkPropertyFileException e) {
                fpfLog.error("Error encounted loading file changes", e);
            }
        }
    }

    /**
     * <p>
     * This method is not used
     * </p>
     */
    public void onFileCreate(File file) {
        // Method not used for fpf
    }

    /**
     * <p>
     * This method is not used
     * </p>
     */
    public void onFileDelete(File file) {
        // Method not used for fpf
    }

    /**
     * <p>
     * This method is not used
     * </p>
     */
    public void onDirectoryCreate(File file) {
        // Method not used for fpf
    }

    /**
     * <p>
     * This method is not used
     * </p>
     */
    public void onDirectoryChange(File file) {
        // Method not used for fpf
    }

    /**
     * <p>
     * This method is not used
     * </p>
     */
    public void onDirectoryDelete(File file) {
        // Method not used for fpf
    }

    /**
     * <p>
     * This class defines a watch, and the variables required to detect changes to
     * the correct k-v pair.
     * </p>
     */
    private class Watch {

        private final IFrameworkPropertyFileWatcher watcher;
        private final String                        key;
        private final boolean                       prefix;

        /**
         * This constructor sets te key for the watcher and a boolean to define whether
         * the key string is a prefix or the full key. It also holds the implemented
         * watcher class
         * </p>
         * 
         * @param watcher - implemented watcher
         * @param key     - string key or prefix
         * @param prefix  - boolean
         */
        private Watch(IFrameworkPropertyFileWatcher watcher, String key, boolean prefix) {
            this.watcher = watcher;
            this.key = key;
            this.prefix = prefix;
        }

        /**
         * <p>
         * This method return a boolean whether a passed string matches the key for this
         * watch.
         * </p>
         * 
         * <p>
         * If the watch is a prefix watch it checks to see if the passed string is the
         * start of the key.
         * 
         * @param newKey - the string to compare to the key in the watch
         * @return - boolean for if a match
         */
        public boolean matchKey(String newKey) {
            if (prefix) {
                return newKey.startsWith(key);
            }
            return newKey.equals(key);
        }
    }

    public synchronized void shutdown() throws FrameworkPropertyFileException {
        if (this.monitor != null) {
            try {
                this.monitor.removeObserver(this.observer);
                this.monitor.stop();
                this.monitor = null;
            } catch (Throwable t) {
                throw new FrameworkPropertyFileException("Problem stopping the file monitor", t);
            }
        }

        this.watches.clear();
    }
}