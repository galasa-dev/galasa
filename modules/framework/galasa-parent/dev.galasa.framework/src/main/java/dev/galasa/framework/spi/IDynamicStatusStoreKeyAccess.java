/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * <p>
 * Used to gain access to properties in the Dynamic Status Store
 * </p>
 * 
 * <p>
 * The framework will be configured with a single Dynamic Status Store where all
 * the dynamic properties for run and resources are kept.
 * </p>
 * 
 * <p>
 * etcd3 is the preferred dynamic status store for Galasa
 * </p>
 * 
 * <p>
 * An {@link IDynamicStatusStoreKeyAccess} can be obtained from
 * {@link IFramework#getDynamicStatusStoreService(String)}.
 * </p>
 * 
 *  
 *
 */
public interface IDynamicStatusStoreKeyAccess {

    /**
     * Store a new key value pair in the server
     * 
     * @param key   - the key to use
     * @param value - the value to use
     * @throws DynamicStatusStoreException
     */
    void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException;

    /**
     * Store multiple key/value pairs in the server.
     * 
     * @param keyValues - map of key/value pairs
     * @throws DynamicStatusStoreException
     */
    void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException;

    /**
     * Put a key/value pair in the server if the key is set to the oldValue.
     * 
     * @param key      - the key to use
     * @param oldValue - the value to compare with and must be equal to before the
     *                 put is actioned. Null means does not exist
     * @param newValue - The new value to set the key to
     * @return true if the put was actioned, false if not.
     * @throws DynamicStatusStoreException
     */
    boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue) throws DynamicStatusStoreException;

    /**
     * Put a key/value pair in the server if the key is set to the old value, along
     * with a set of other key value pairs
     * 
     * @param key      - the key to use
     * @param oldValue - the value to compare with and must be equal to before the
     *                 put is actioned. Null means does not exist
     * @param newValue - The new value to set the key to
     * @param others   - other key/value pairs to put if the primary key is valid.
     * @return true if the
     * @throws DynamicStatusStoreException
     */
    boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue, @NotNull Map<String, String> others)
            throws DynamicStatusStoreException;

    /**
     * <p>
     * Retrieves a string property from the Dynamic Status Store
     * </p>
     * 
     * @param key The name of the property.
     * @return The value of the property, can be null if it does not exist
     * @throws DynamicStatusStoreException
     */
    @Null
    String get(@NotNull String key) throws DynamicStatusStoreException;

    /**
     * Retrieve all values with this key prefix
     * 
     * @param keyPrefix - the prefix of all the keys to use.
     * @return A map. The keys start with the specified prefix. The value is a string.
     * @throws DynamicStatusStoreException
     */
    @NotNull
    Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException;

    /**
     * Delete the key
     * 
     * @param key - the key to use
     * @throws DynamicStatusStoreException
     */
    void delete(@NotNull String key) throws DynamicStatusStoreException;

    /**
     * Delete a set of keys from the server
     * 
     * @param keys - all the keys that need to be deleted
     * @throws DynamicStatusStoreException
     */
    void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException;

    /**
     * Delete all keys with this prefix
     * 
     * @param keyPrefix - the prefix of all the keys to use.
     * @throws DynamicStatusStoreException
     */
    void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException;
    
    
    /**
     * Will perform multiple actions on the DSS in an atomic fashion.  If any of the actions 
     * fail, they all fail.
     * 
     * @param actions a list of actions to perform on the DSS.
     * @throws DynamicStatusStoreException
     * @throws DynamicStatusStoreMatchException - if preconditions fail
     */
    void performActions(IDssAction... actions) throws DynamicStatusStoreException, DynamicStatusStoreMatchException;   

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
     * @throws DynamicStatusStoreException
     */
    UUID watch(IDynamicStatusStoreWatcher watcher, String key) throws DynamicStatusStoreException;

    /**
     * <p>
     * This method provides a single watch service to watch multiple k-v pairs with
     * a common prefix in there key.
     * </p>
     * 
     * @param watcher   - an interface for the watchers inplementation.
     * @param keyPrefix - the string prefix to a key set to watch
     * @return - returns a UUID which is used to identify a watcher service.
     * @throws DynamicStatusStoreException
     */
    UUID watchPrefix(IDynamicStatusStoreWatcher watcher, String keyPrefix) throws DynamicStatusStoreException;

    /**
     * <p>
     * This method is used to stop any watcher service with a given UUID. It removes
     * the given watcher from the watches list. If this is the final watcher in the
     * list the method also shuts down the monitor
     * </p>
     * 
     * @param watchId - the identifying UUID
     * @throws DynamicStatusStoreException
     */
    void unwatch(UUID watchId) throws DynamicStatusStoreException;

}
