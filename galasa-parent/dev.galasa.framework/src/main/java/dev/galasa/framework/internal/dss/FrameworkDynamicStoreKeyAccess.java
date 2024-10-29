/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.dss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDssResourceAction;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher;

/**
 * Provides the common key access to the DSS
 * 
 *  
 */

public class FrameworkDynamicStoreKeyAccess implements IDynamicStatusStoreKeyAccess {
    private final IDynamicStatusStore dssStore;
    private final String              prefix;
    private final String              namespace;

    public FrameworkDynamicStoreKeyAccess(IDynamicStatusStore dssStore, String prefix, String namespace) {
        Objects.requireNonNull(dssStore);
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(namespace);

        this.dssStore = dssStore;
        this.prefix = prefix;
        this.namespace = namespace;
    }

    protected IDynamicStatusStore getDssStore() {
        return this.dssStore;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess#put(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
        this.dssStore.put(prefixKey(key), value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess#put(java.util.Map)
     */
    @Override
    public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
        Objects.requireNonNull(keyValues);

        // *** Copy all the keys and prefix them
        final HashMap<String, String> newKeyValues = new HashMap<>();
        for (final Entry<String, String> entry : keyValues.entrySet()) {
            final String oKey = entry.getKey();
            final String oValue = entry.getValue();

            Objects.requireNonNull(oKey);
            Objects.requireNonNull(oValue);

            newKeyValues.put(prefixKey(oKey), oValue);
        }

        this.dssStore.put(newKeyValues);
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess#putSwap(java.lang.
     * String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
            throws DynamicStatusStoreException {
        Objects.requireNonNull(newValue);
        return this.dssStore.putSwap(prefixKey(key), oldValue, newValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess#putSwap(java.lang.
     * String, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
            @NotNull Map<String, String> others) throws DynamicStatusStoreException {
        Objects.requireNonNull(newValue);
        Objects.requireNonNull(others);

        // *** Copy all the other keys and prefix them
        final HashMap<String, String> newOthers = new HashMap<>();
        for (final Entry<String, String> entry : others.entrySet()) {
            final String oKey = entry.getKey();
            final String oValue = entry.getValue();

            Objects.requireNonNull(oKey);
            Objects.requireNonNull(oValue);

            newOthers.put(prefixKey(oKey), oValue);
        }

        return this.dssStore.putSwap(prefixKey(key), oldValue, newValue, newOthers);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess#get(java.lang.String)
     */
    @Override
    public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
        return this.dssStore.get(prefixKey(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess#getPrefix(java.lang.
     * String)
     */
    @Override
    public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        final Map<String, String> gotSet = this.dssStore.getPrefix(prefixKey(keyPrefix));
        final HashMap<String, String> returnSet = new HashMap<>();

        for (Entry<String, String> entry : gotSet.entrySet()) {
            String key = entry.getKey();
            final String value = entry.getValue();

            if (key.startsWith(this.prefix)) {
                key = key.substring(this.prefix.length());
                returnSet.put(key, value);
            } else {
                throw new DynamicStatusStoreException("Somehow we got keys with the wrong prefix");
            }
        }

        return returnSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess#delete(java.lang.
     * String)
     */
    @Override
    public void delete(@NotNull String key) throws DynamicStatusStoreException {
        this.dssStore.delete(prefixKey(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess#delete(java.util.Set)
     */
    @Override
    public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
        Objects.requireNonNull(keys);

        // *** Copy all the keys and prefix them
        final HashSet<String> newKeys = new HashSet<>();
        for (final String key : keys) {
            Objects.requireNonNull(key);
            newKeys.add(prefixKey(key));
        }

        this.dssStore.delete(newKeys);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess#deletePrefix(java.lang.
     * String)
     */
    @Override
    public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        this.dssStore.deletePrefix(prefixKey(keyPrefix));
    }

    /**
     * Prefix the supplied key with the namespace
     *
     * @param key
     * @return
     */
    private String prefixKey(String key) {
        Objects.requireNonNull(key);
        return this.prefix + key;
    }

    @Override
    public UUID watch(IDynamicStatusStoreWatcher watcher, String key) throws DynamicStatusStoreException {
        return this.dssStore.watch(new PassthroughWatcher(watcher, prefix), prefixKey(key));
    }

    @Override
    public UUID watchPrefix(IDynamicStatusStoreWatcher watcher, String keyPrefix) throws DynamicStatusStoreException {
        return this.dssStore.watchPrefix(new PassthroughWatcher(watcher, prefix), prefixKey(keyPrefix));
    }

    @Override
    public void unwatch(UUID watchId) throws DynamicStatusStoreException {
        this.dssStore.unwatch(watchId);
    }

    private static class PassthroughWatcher implements IDynamicStatusStoreWatcher {

        private final String                     prefix;
        private final int                        offset;
        private final IDynamicStatusStoreWatcher watcher;

        private PassthroughWatcher(IDynamicStatusStoreWatcher watcher, String prefix) {
            this.prefix = prefix;
            this.offset = this.prefix.length();
            this.watcher = watcher;
        }

        @Override
        public void propertyModified(String key, Event event, String oldValue, String newValue) {
            key = key.substring(this.offset);
            watcher.propertyModified(key, event, oldValue, newValue);
        }
    }

    @Override
    public void performActions(IDssAction... actions) throws DynamicStatusStoreException, DynamicStatusStoreMatchException {

        IDssAction[] dssActions = new IDssAction[actions.length];
        for(int i = 0; i < actions.length; i++) {
            if (actions[i] instanceof IDssResourceAction) {
                dssActions[i] = actions[i].applyPrefix("dss.framework.resource." + this.namespace + ".");
            } else {
                dssActions[i] = actions[i].applyPrefix(this.prefix);
            }
        }

        this.dssStore.performActions(dssActions);
    }

}