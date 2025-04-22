/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicRun;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher;

public class MockDSSStore implements IDynamicStatusStore, IDynamicStatusStoreService {

    private Map<String,String> valueMap ;
    private Log logger = LogFactory.getLog(MockDSSStore.class.getName());
    private boolean isSwapSetToFail = false;

    public MockDSSStore(Map<String,String> valueMap) {
        this.valueMap = valueMap;
    }

    @Override
    public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
        valueMap.put(key, value);
    }

    @Override
    public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
        valueMap.putAll(keyValues);
    }

    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
            throws DynamicStatusStoreException {
        logger.debug("DSS putswap of property "+key+" oldValue:"+oldValue+" newValue:"+newValue);
        valueMap.put(key,newValue);
        return true;
    }

    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
            @NotNull Map<String, String> others) throws DynamicStatusStoreException {
        logger.debug("DSS putswap of property "+key+" oldValue:"+oldValue+" newValue:"+newValue);
        boolean isSuccessful = !isSwapSetToFail;
        if (isSuccessful) {
            valueMap.put(key,newValue);
            for (Entry<String, String> entry : others.entrySet()) {
                valueMap.put(entry.getKey(), entry.getValue());
            }
        }
        return isSuccessful;
    }

    @Override
    public @Null String get(@NotNull String key) throws DynamicStatusStoreException {
        String value = valueMap.get(key);
        logger.debug("DSS get of property "+key+" returning "+value);
        return value;
    }

    @Override
    public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        Map<String, String> results = new HashMap<String,String>();
        for (String key : valueMap.keySet()){
            if (key.startsWith(keyPrefix)) {
                results.put(key,valueMap.get(key));
            }
        }
        logger.debug("DSS getPrefix of property "+keyPrefix+" returning "+results.toString());
        return results;
    }

    public void setSwapSetToFail(boolean isSwapSetToFail) {
        this.isSwapSetToFail = isSwapSetToFail;
    }

    @Override
    public void delete(@NotNull String key) throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
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
    public void shutdown() throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

    @Override
    public IDynamicResource getDynamicResource(String resourceKey) {
        throw new UnsupportedOperationException("Unimplemented method 'getDynamicResource'");
    }

    @Override
    public IDynamicRun getDynamicRun() throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getDynamicRun'");
    }

    @Override
    public void put(@NotNull String key, @NotNull String value, @NotNull long timeToLiveSecs)
            throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'put'");
    }
    
}
