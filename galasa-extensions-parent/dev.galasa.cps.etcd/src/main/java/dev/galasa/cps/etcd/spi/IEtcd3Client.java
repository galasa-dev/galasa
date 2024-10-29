/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.spi;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * The etcd3 client provides limited abstracted access to the official etcd3
 * client. It is abstracted to isolate the managers from changes to the official
 * client, or to swap to a better one.
 * </p>
 * 
 * <p>
 * Leasing is not currently supported as there may not be the demand
 * </p>
 * 
 * <p>
 * An implementation of the client can be obtained via the
 * {@link IEtcd3ClientService} service.
 */
public interface IEtcd3Client {

    /**
     * Store a new key value pair in the server
     * 
     * @param key   - the key to use, may be prefixed by the client
     * @param value - the value to use
     * @throws Etcd3ClientException A failure occurred.
     */
    void put(@NotNull String key, @NotNull String value) throws Etcd3ClientException;

    /**
     * Store multiple key/value pairs in the server. The keys may be prefiex by the
     * client.
     * 
     * @param keyValues - map of key/value pairs
     * @throws Etcd3ClientException A failure occurred.
     */
    void put(@NotNull Map<String, String> keyValues) throws Etcd3ClientException;

    /**
     * Put a key/value pair in the server if the key is set to the oldValue.
     * 
     * @param key      - the key to use, may be prefixed by the client
     * @param oldValue - the value to compare with and must be equal to before the
     *                 put is actioned. Null means does not exist
     * @param newValue - The new value to set the key to
     * @return true if the put was actioned, false if not.
     * @throws Etcd3ClientException A failure occurred.
     */
    boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue) throws Etcd3ClientException;

    /**
     * Put a key/value pair in the server if the key is set to the old value, along
     * with a set of other key value pairs
     * 
     * @param key      - the key to use, may be prefixed by the client
     * @param oldValue - the value to compare with and must be equal to before the
     *                 put is actioned. Null means does not exist
     * @param newValue - The new value to set the key to
     * @param others   - other key/value pairs to put if the primary key is valid.
     * @return true if the
     * @throws Etcd3ClientException A failure occurred.
     */
    boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue, @NotNull Map<String, String> others)
            throws Etcd3ClientException;

    /**
     * Retrieve the value for a key
     * 
     * @param key - the key to use, may be prefixed by the client
     * @return the value if present, null if not
     * @throws Etcd3ClientException A failure occurred.
     */
    String get(@NotNull String key) throws Etcd3ClientException;

    /**
     * Retrieve all values with this key prefix
     * 
     * @param keyPrefix - the prefix of all the keys to use. This prefix may also be
     *                  prefixed by the client
     * @return A map of key-value pairs.
     * @throws Etcd3ClientException A failure occurred.
     */
    @NotNull
    Map<String, String> getPrefix(@NotNull String keyPrefix) throws Etcd3ClientException;

    /**
     * Delete the key from the server
     * 
     * @param key - the key to use, may be prefixed by the client
     * @throws Etcd3ClientException A failure occurred.
     */
    void delete(@NotNull String key) throws Etcd3ClientException;

    /**
     * Delete a set of keys from the server
     * 
     * @param keys - all the keys that need to be deleted, may be prefixed by the
     *             client
     * @throws Etcd3ClientException A failure occurred.
     */
    void delete(@NotNull Set<String> keys) throws Etcd3ClientException;

    /**
     * Delete all keys with this prefix
     * 
     * @param keyPrefix - the prefix of all the keys to use. This prefix may also be
     *                  prefixed by the client
     * @throws Etcd3ClientException A failure occurred.
     */
    void deletePrefix(@NotNull String keyPrefix) throws Etcd3ClientException;

    /**
     * Register a watch on a key/value pair. A UUID is returned to represent this
     * specific watch, so can be unregistered in the future.
     * 
     * @param listener  - The class to be informed on a change
     * @param key       - the key to use, may be prefixed by the client
     * @return UUID representing this watch
     * @throws Etcd3ClientException A failure occurred.
     */
    UUID registerWatch(@NotNull IEtcd3Listener listener, @NotNull String key) throws Etcd3ClientException;

    /**
     * @param listener  - The class to be informed on a change
     * @param keyPrefix - the prefix of all the keys to use. This prefix may also be
     *                  prefixed by the client
     * @return UUID representing this watch
     * @throws Etcd3ClientException A failure occurred.
     */
    UUID registerWatchPrefix(@NotNull IEtcd3Listener listener, @NotNull String keyPrefix) throws Etcd3ClientException;

    /**
     * Unregister a watch with the UUID
     * 
     * @param listenerId - UUID of the listener ID
     */
    void unregisterWatch(@NotNull UUID listenerId);

}
