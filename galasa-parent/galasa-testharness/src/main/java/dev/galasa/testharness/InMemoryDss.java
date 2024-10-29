/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.testharness;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher.Event;

public class InMemoryDss implements IDynamicStatusStore {

    public final Properties properties = new Properties();
    
    public final HashMap<UUID, Watcher> watchers = new HashMap<>();

    @Override
    public synchronized void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
        
        Event event = Event.MODIFIED;
        
        String oldValue = this.properties.getProperty(key);
        if (oldValue == null) {
            event = Event.NEW;
        }
        
        this.properties.put(key, value);
        
        informWatchers(key, event, oldValue, value);
    }

    @Override
    public synchronized void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
        for(Entry<Object, Object> entry : this.properties.entrySet()) {
            put((String)entry.getKey(), (String)entry.getValue());
        }
    }

    @Override
    public synchronized boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
            throws DynamicStatusStoreException {
        
        String value = this.properties.getProperty(key);
        if (oldValue == null && value != null) {
            return false;
        }
        if (!oldValue.equals(value)) {
            return false;
        }
        
        put(key, newValue);
        
        return true;
    }

    @Override
    public synchronized boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
            @NotNull Map<String, String> others) throws DynamicStatusStoreException {

        String value = this.properties.getProperty(key);
        if (oldValue == null && value != null) {
            return false;
        }
        if (!oldValue.equals(value)) {
            return false;
        }
        
        put(key, newValue);
        put(others);
        
        return true;
    }

    @Override
    public synchronized @Null String get(@NotNull String key) throws DynamicStatusStoreException {
        return this.properties.getProperty(key);
    }

    @Override
    public synchronized @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        HashMap<String, String> props = new HashMap<>();

        for(Entry<Object, Object> entry : this.properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(keyPrefix)) {
                props.put(key, (String) entry.getValue());
            }
        }

        return props;
    }

    @Override
    public synchronized void delete(@NotNull String key) throws DynamicStatusStoreException {
        String oldValue = this.properties.getProperty(key);
        
        if (oldValue != null) {
            this.properties.remove(key);
            informWatchers(key, Event.DELETE, oldValue, null);
        }
    }

    @Override
    public synchronized void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
        for(String key : keys) {
            delete(key);
        }
    }

    @Override
    public synchronized void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        HashSet<String> deleteKeys = new HashSet<>();
        
        for(Object o : this.properties.keySet()) {
            String key = (String)o;
            if (key.startsWith(keyPrefix)) {
                deleteKeys.add(key);
            }
        }
        
        delete(deleteKeys);
    }

    public void informWatchers(@NotNull String key, Event event, String oldValue, @NotNull String newValue) {
        for(Watcher watcher : this.watchers.values()) {
            watcher.updatedKey(key, event, oldValue, newValue);
        }
    }

    @Override
    public synchronized UUID watch(IDynamicStatusStoreWatcher watcher, String key) throws DynamicStatusStoreException {
        UUID uuid = UUID.randomUUID();
        
        this.watchers.put(uuid, new Watcher(watcher, key, null));
        
        return uuid;
    }

    @Override
    public synchronized UUID watchPrefix(IDynamicStatusStoreWatcher watcher, String keyPrefix) throws DynamicStatusStoreException {
        UUID uuid = UUID.randomUUID();
        
        this.watchers.put(uuid, new Watcher(watcher, null, keyPrefix));
        
        return uuid;
    }

    @Override
    public synchronized void unwatch(UUID watchId) throws DynamicStatusStoreException {
        this.watchers.remove(watchId);
        
    }

    @Override
    public synchronized void shutdown() throws DynamicStatusStoreException {
    }
    
    
    public static class Watcher {
        public final IDynamicStatusStoreWatcher watcher;
        public final String key;
        public final String keyPrefix;
        
        public Watcher(IDynamicStatusStoreWatcher watcher, String key, String keyPrefix) {
            this.watcher = watcher;
            this.key = key;
            this.keyPrefix = keyPrefix;
        }
        
        public void updatedKey(String changedKey, Event event, String oldValue, String newValue) {
            if (key != null) {
                if (!changedKey.equals(this.key)) {
                    return;
                }
            } else if (keyPrefix != null) {
                if (!changedKey.startsWith(this.keyPrefix)) {
                    return;
                }
            } else {
                return;
            }
            
            this.watcher.propertyModified(changedKey, event, oldValue, newValue);
        }
    }


    @Override
    public void performActions(IDssAction... actions) throws DynamicStatusStoreException {
        throw new DynamicStatusStoreException("Need to add support for actions");
        
    }


}
