/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;


import java.util.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicRun;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher;

public class MockIDynamicStatusStoreService implements IDynamicStatusStoreService {

    public static enum DssHistoryRecordType {
        PUT, DELETE
    }

    public static class DssHistoryRecord {
        public DssHistoryRecordType operation ;
        public String key ;
        public String value;

        public DssHistoryRecord(DssHistoryRecordType operation, String key) {
            this(operation,key,null);
        }

        public DssHistoryRecord(DssHistoryRecordType operation, String key, String value) {
            this.operation = operation;
            this.key = key; 
            this.value = value;
        }
    }

    public List<DssHistoryRecord> history = new ArrayList<>();

    public Map<String,String> data = new HashMap<>();

    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue) {
        // Don't record heartbeat events in the history. They are random how many there would be 
        // based on the speed of the heartbeat thread. So make it hard for unit tests to check
        // results.
        if (!key.endsWith(".heartbeat")) {
            history.add( new DssHistoryRecord(DssHistoryRecordType.PUT, key , newValue));
        }
        data.put(key,newValue);
        return true;
    }

    @Override
    public void delete(@NotNull String key) throws DynamicStatusStoreException {
        // Heartbeat deletion events should be recorded, as they are a sign that cleanup
        // is being done, so unit tests will want to check that.
        history.add( new DssHistoryRecord(DssHistoryRecordType.DELETE, key ));
        data.remove(key);
    }

    @Override
    public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
        return data.get(key);
    }

    // ------------------- un-implemented methods follow --------------------

    @Override
    public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'put'");
    }

    @Override
    public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'put'");
    }


    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
            @NotNull Map<String, String> others) throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'putSwap'");
    }

    @Override
    public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'getPrefix'");
    }

    @Override
    public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'deletePrefix'");
    }

    @Override
    public void performActions(IDssAction... actions)
            throws DynamicStatusStoreException, DynamicStatusStoreMatchException {
               throw new UnsupportedOperationException("Unimplemented method 'performActions'");
    }

    @Override
    public UUID watch(IDynamicStatusStoreWatcher watcher, String key) throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'watch'");
    }

    @Override
    public UUID watchPrefix(IDynamicStatusStoreWatcher watcher, String keyPrefix) throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'watchPrefix'");
    }

    @Override
    public void unwatch(UUID watchId) throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'unwatch'");
    }

    @Override
    public IDynamicResource getDynamicResource(String resourceKey) {
               throw new UnsupportedOperationException("Unimplemented method 'getDynamicResource'");
    }

    @Override
    public IDynamicRun getDynamicRun() throws DynamicStatusStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'getDynamicRun'");
    }
    
}
