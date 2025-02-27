/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.internal;

import static com.google.common.base.Charsets.UTF_8;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DssDeletePrefix;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DssUpdate;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher.Event;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.Watch.Listener;
import io.etcd.jetcd.Watch.Watcher;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.OptionsUtil;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchEvent.EventType;
import io.etcd.jetcd.watch.WatchResponse;

/**
 * This class implements the DSS store for the use of etcd3 as the k-v store. It
 * is interacting with the Jetcd Client offered from coreOs.
 * 
 * @author James Davies
 */
public class Etcd3DynamicStatusStore implements IDynamicStatusStore {
    private final Client                            client;
    private final KV                                kvClient;
    private final Watch                             watchClient;
    private final Lease                             leaseClient;
    private Log logger = LogFactory.getLog(Etcd3DynamicStatusStore.class);

    private final HashMap<UUID, PassthroughWatcher> watchers = new HashMap<>();

    /**
     * The constructure sets up a private KVClient that can be used by this class to
     * interact with the etcd3 cluster.
     * 
     * The URI passed from the registration of the EtcdDSS should check that it is a
     * valid URI.
     * 
     * @param dssUri - http:// uri for th etcd cluster.
     */
    public Etcd3DynamicStatusStore(URI dssUri) {
        this(Client.builder().endpoints(dssUri).build());
    }

    public Etcd3DynamicStatusStore(Client client) {
        this.client = client;
        this.kvClient = client.getKVClient();
        this.watchClient = client.getWatchClient();
        this.leaseClient = client.getLeaseClient();
    }

    /**
     * A simple put class that adds a single key value in etcd key value store.
     * 
     * @param key The key to be stored.
     * @param value The value to be associated with the specified key.
     * @throws DynamicStatusStoreException A failure occurred.
     */
    @Override
    public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from(key, UTF_8);
        ByteSequence bsValue = ByteSequence.from(value, UTF_8);

        CompletableFuture<PutResponse> response = kvClient.put(bsKey, bsValue);
        try {
            response.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("Could not put key-value", e);
        }
    }

    /**
     * A map put which allows a map of k-v pairs to the etcd store.
     * 
     * @param keyValues - a map of key value pairs.
     * @throws DynamicStatusStoreException A failure occurred.
     */
    @Override
    public void put(@NotNull Map<String, String> keyValues) throws DynamicStatusStoreException {
        Txn txn = kvClient.txn();
        PutOption options = PutOption.DEFAULT;

        ArrayList<Op> ops = new ArrayList<>();
        for (String key : keyValues.keySet()) {
            ByteSequence obsKey = ByteSequence.from(key, UTF_8);
            ByteSequence obsValue = ByteSequence.from(keyValues.get(key), UTF_8);
            ops.add(Op.put(obsKey, obsValue, options));
        }
        Txn request = txn.Then(ops.toArray(new Op[ops.size()]));
        CompletableFuture<TxnResponse> response = request.commit();
        try {
            response.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("", e);
        }

    }

    /**
     * Put a key-value pair into the DSS that expires after a given amount of time in seconds
     * 
     * @param key the key to be stored
     * @param value the value to be associated with the given key
     * @param timeToLiveSecs the amount of time in seconds for the key-value pair to be available for before expiring
     * @throws DynamicStatusStoreException if there was an error accessing etcd
     */
    @Override
    public void put(@NotNull String key, @NotNull String value, @NotNull long timeToLiveSecs) throws DynamicStatusStoreException {

        // Create a new lease with the given time-to-live (TTL)
        CompletableFuture<LeaseGrantResponse> leaseResponse = leaseClient.grant(timeToLiveSecs);
        try {
            LeaseGrantResponse lease = leaseResponse.get();
            PutOption putOption = PutOption.builder()
                .withLeaseId(lease.getID())
                .build();
            
            // Set the key-value pair, applying the lease to it so the key expires after the given amount of time
            ByteSequence bsKey = ByteSequence.from(key, UTF_8);
            ByteSequence bsValue = ByteSequence.from(value, UTF_8);
            kvClient.put(bsKey, bsValue, putOption).get();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("Could not set key-value pair with time-to-live", e);
        }
    }

    /**
     * A put swap method does a check before setting the key-value.
     * 
     * If the oldValue argument is not the current value of the key the newValue is
     * NOT set into the store.
     * 
     * @param key The key whose value will be swapped.
     * @param oldValue - the value the key should have for the change to succeeed
     * @param newValue - the new value to set too if the old value was correct
     * @return boolean - if the swap was successful
     * @throws DynamicStatusStoreException A failure occurred.
     */
    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue)
            throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from(key, UTF_8);
        ByteSequence bsNewValue = ByteSequence.from(newValue, UTF_8);

        Txn txn = kvClient.txn();

        Cmp cmp = null;
        if (oldValue == null) {
            cmp = new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.version(0));
        } else {
            ByteSequence bsOldValue = ByteSequence.from(oldValue, UTF_8);
            cmp = new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.value(bsOldValue));
        }

        PutOption option = PutOption.DEFAULT;

        Txn check = txn.If(cmp);
        Txn request = check.Then(Op.put(bsKey, bsNewValue, option));
        CompletableFuture<TxnResponse> response = request.commit();

        try {
            return response.get().isSucceeded();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("Put Swap failed", e);
        }
    }

    /**
     * A put swap method does a check before setting the key-value.
     * 
     * If the oldValue argument is not the current value of the key the newValue is
     * NOT set into the store. If the old value was correct then a map of other key
     * value pairs can be then set.
     * 
     * @param key The key whose value will be swapped
     * @param oldValue - the value the key should have for the change to succeeed
     * @param newValue - the new value to set too if the old value was correct
     * @param others   - a map of all subsequent values to set if the condition was
     *                 satisfied
     * @return boolean - if the swap was successful
     * @throws DynamicStatusStoreException A failure occurred.
     */
    @Override
    public boolean putSwap(@NotNull String key, String oldValue, @NotNull String newValue,
            @NotNull Map<String, String> others) throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from(key, UTF_8);
        ByteSequence bsNewValue = ByteSequence.from(newValue, UTF_8);

        Txn txn = kvClient.txn();

        Cmp cmp = null;
        if (oldValue == null) {
            cmp = new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.version(0));
        } else {
            ByteSequence bsOldValue = ByteSequence.from(oldValue, UTF_8);
            cmp = new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.value(bsOldValue));
        }

        ArrayList<Op> ops = new ArrayList<>();

        PutOption option = PutOption.DEFAULT;
        ops.add(Op.put(bsKey, bsNewValue, option));

        for (Entry<String, String> entry : others.entrySet()) {
            ByteSequence obsKey = ByteSequence.from(entry.getKey(), UTF_8);
            ByteSequence obsValue = ByteSequence.from(entry.getValue(), UTF_8);

            ops.add(Op.put(obsKey, obsValue, option));
        }

        CompletableFuture<TxnResponse> response = txn.If(cmp).Then(ops.toArray(new Op[ops.size()])).commit();
        try {
            return response.get().isSucceeded();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("Put Swap failed", e);
        }

    }

    /**
     * A simple get method that retrieves on value from one key
     * 
     * @param key The key we wish to query
     * @return The value of the key and null if not existing.
     * @throws DynamicStatusStoreException A failure occurred.
     */
    @Override
    public String get(@NotNull String key) throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from(key, UTF_8);
        CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey);

        try {
            GetResponse response = getFuture.get();
            List<KeyValue> kvs = response.getKvs();
            if (kvs.isEmpty()) {
                return null;
            }
            return kvs.get(0).getValue().toString(UTF_8);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("Could not retrieve key.", e);
        }
    }

    /**
     * A get of all keys and value that start with a specified prefix. They are
     * returned in a map of key-value pairs
     * 
     * @param keyPrefix - the prefix for any key(s)
     * @return A map of name-value pairs 
     * @throws DynamicStatusStoreException A failure occurred.
     */
    @Override
    public @NotNull Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {

        logger.debug("Etcd extension getting property with a prefix of "+keyPrefix);
        ByteSequence bsPrefix = ByteSequence.from(keyPrefix, UTF_8);

        ByteSequence prefixEnd = OptionsUtil.prefixEndOf(bsPrefix);
        GetOption options = GetOption.builder().withRange(prefixEnd).build();

        CompletableFuture<GetResponse> getFuture = kvClient.get(bsPrefix, options);
        Map<String, String> keyValues = new HashMap<>();

        try {
            GetResponse response = getFuture.get();
            List<KeyValue> kvs = response.getKvs();

            if (kvs.isEmpty()) {
                return new HashMap<>();
            }

            for (KeyValue kv : kvs) {
                keyValues.put(kv.getKey().toString(UTF_8), kv.getValue().toString(UTF_8));
            }
            return keyValues;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("Could not retrieve key.", e);
        }
    }

    /**
     * A Simple delete of a singe Key value pair.
     * 
     * @param key - the key to delete
     * @throws DynamicStatusStoreException A failure occurred.
     */
    @Override
    public void delete(@NotNull String key) throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from(key, UTF_8);
        CompletableFuture<DeleteResponse> deleteFuture = kvClient.delete(bsKey);

        try {
            deleteFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("Could not delete key.", e);
        }
    }

    /**
     * A delete of a set of provided keys and there corresponding values.
     * 
     * @param keys a set of keys to delete
     * @throws DynamicStatusStoreException A failure occurred.
     *
     */
    @Override
    public void delete(@NotNull Set<String> keys) throws DynamicStatusStoreException {
        Txn txn = kvClient.txn();
        DeleteOption options = DeleteOption.DEFAULT;

        ArrayList<Op> ops = new ArrayList<>();
        for (String key : keys) {
            ByteSequence obsKey = ByteSequence.from(key, UTF_8);
            ops.add(Op.delete(obsKey, options));
        }

        CompletableFuture<TxnResponse> response = txn.Then(ops.toArray(new Op[ops.size()])).commit();
        try {
            response.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("Could not delete key(s).", e);
        }
    }

    /**
     * A delete which removes all keys with a specified prefix and there
     * corresponding values from the store.
     * 
     * @param keyPrefix - a string prefix that all the key(s) have in common
     * @throws DynamicStatusStoreException A failure occurred.
     */
    @Override
    public void deletePrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from(keyPrefix, UTF_8);

        ByteSequence prefixEnd = OptionsUtil.prefixEndOf(bsKey);
        DeleteOption options = DeleteOption.builder().withRange(prefixEnd).build();
        CompletableFuture<DeleteResponse> deleteFuture = kvClient.delete(bsKey, options);

        try {
            deleteFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("Could not delete key(s).", e);
        }
    }

    // TODO Test and document
    @Override
    public UUID watch(IDynamicStatusStoreWatcher watcher, String key) throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from(key, UTF_8);
        PassthroughWatcher passWatcher = new PassthroughWatcher(watcher);
        passWatcher.setEtcdWatcher(watchClient.watch(bsKey, passWatcher));
        watchers.put(passWatcher.getID(), passWatcher);
        return passWatcher.getID();
    }

    // TODO Test and document
    @Override
    public UUID watchPrefix(IDynamicStatusStoreWatcher watcher, String keyPrefix) throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from(keyPrefix, UTF_8);
        PassthroughWatcher passWatcher = new PassthroughWatcher(watcher);
        ByteSequence prefixEnd = OptionsUtil.prefixEndOf(bsKey);
        WatchOption watchOption = WatchOption.builder().withRange(prefixEnd).build();
        Watcher etcdWatcher = watchClient.watch(bsKey, watchOption, passWatcher);
        passWatcher.setEtcdWatcher(etcdWatcher);
        watchers.put(passWatcher.getID(), passWatcher);
        return passWatcher.getID();
    }

    // TODO Test and document
    @Override
    public void unwatch(UUID watchId) throws DynamicStatusStoreException {
        PassthroughWatcher passWatcher = watchers.remove(watchId);
        if (passWatcher == null) {
            return;
        }

        passWatcher.getEtcdWatcher().close();
    }

    private class PassthroughWatcher implements Listener {

        private final UUID                       id = UUID.randomUUID();
        private final IDynamicStatusStoreWatcher watcher;
        private Watcher                          etcdWatcher;

        public PassthroughWatcher(IDynamicStatusStoreWatcher watcher) {
            this.watcher = watcher;
        }

        @Override
        public void onNext(WatchResponse response) {
            if (response == null) {
                return;
            }

            List<WatchEvent> events = response.getEvents();
            if (events == null) {
                return;
            }

            for (WatchEvent event : events) {
                EventType eventType = event.getEventType();
                KeyValue eventKey = event.getKeyValue();
                KeyValue eventPrev = event.getPrevKV();

                if (eventType == null || eventKey == null) {
                    continue;
                }

                switch (eventType) {
                    case DELETE:
                        watcher.propertyModified(eventKey.getKey().toString(UTF_8), Event.DELETE, null, null);
                        break;
                    case PUT:
                        if (eventPrev != null) {
                            watcher.propertyModified(eventKey.getKey().toString(UTF_8), Event.MODIFIED,
                                    eventPrev.getValue().toString(UTF_8), eventKey.getValue().toString(UTF_8));
                        } else {
                            watcher.propertyModified(eventKey.getKey().toString(UTF_8), Event.NEW, null,
                                    eventKey.getValue().toString(UTF_8));
                        }
                        break;
                    case UNRECOGNIZED:
                    default:
                        continue;
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onCompleted() {
        }

        public UUID getID() {
            return this.id;
        }

        public void setEtcdWatcher(Watcher etcdWatcher) {
            this.etcdWatcher = etcdWatcher;
        }

        public Watcher getEtcdWatcher() {
            return this.etcdWatcher;
        }
    }

    @Override
    public void shutdown() throws DynamicStatusStoreException {
        watchClient.close();
        leaseClient.close();
        kvClient.close();
        client.close();
    }

    @Override
    public void performActions(IDssAction... actions) throws DynamicStatusStoreException, DynamicStatusStoreMatchException {
        
        Txn txn = kvClient.txn();
        
        // Go through the actions and collect all the IFs
        
        for(IDssAction action : actions) {
            if (action instanceof DssAdd) {
                txn = performActionsAddIf(txn, (DssAdd) action);
            } else if (action instanceof DssDelete) {
                txn = performActionsDeleteIf(txn, (DssDelete) action);
            } else if (action instanceof DssDeletePrefix) {
                txn = performActionsDeletePrefixIf(txn, (DssDeletePrefix) action);
            } else if (action instanceof DssUpdate) {
                txn = performActionsUpdateIf(txn, (DssUpdate) action);
            } else if (action instanceof DssSwap) {
                txn = performActionsSwapIf(txn, (DssSwap) action);
            } else {
                throw new DynamicStatusStoreException("Unrecognised DSS Action - " + action.getClass().getName());
            }
        }
        
        // Now get the Thens
        for(IDssAction action : actions) {
            if (action instanceof DssAdd) {
                txn = performActionsAddThen(txn, (DssAdd) action);
            } else if (action instanceof DssDelete) {
                txn = performActionsDeleteThen(txn, (DssDelete) action);
            } else if (action instanceof DssDeletePrefix) {
                txn = performActionsDeletePrefixThen(txn, (DssDeletePrefix) action);
            } else if (action instanceof DssUpdate) {
                txn = performActionsUpdateThen(txn, (DssUpdate) action);
            } else if (action instanceof DssSwap) {
                txn = performActionsSwapThen(txn, (DssSwap) action);
            } else {
                throw new DynamicStatusStoreException("Unrecognised DSS Action - " + action.getClass().getName());
            }
        }

        
        CompletableFuture<TxnResponse> response = txn.commit();

        try {
            if (!response.get().isSucceeded()) {
                throw new DynamicStatusStoreMatchException("DSS transaction failed - matches failed");
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new DynamicStatusStoreException("DSS transaction failed", e);
        }
        
    }

    private Txn performActionsAddIf(Txn txn, DssAdd action) {
        ByteSequence bsKey = ByteSequence.from(action.getKey(), UTF_8);

        txn = txn.If(new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.version(0)));
        
        return txn;
    }

    private Txn performActionsAddThen(Txn txn, DssAdd action) {
        ByteSequence bsKey = ByteSequence.from(action.getKey(), UTF_8);
        ByteSequence bsNewValue = ByteSequence.from(action.getValue(), UTF_8);
        
        PutOption option = PutOption.DEFAULT;

        txn = txn.Then(Op.put(bsKey, bsNewValue, option));
        
        return txn;
    }

    private Txn performActionsUpdateIf(Txn txn, DssUpdate action) {
        return txn;
    }

    private Txn performActionsUpdateThen(Txn txn, DssUpdate action) {
        ByteSequence bsKey = ByteSequence.from(action.getKey(), UTF_8);
        ByteSequence bsNewValue = ByteSequence.from(action.getValue(), UTF_8);
        
        PutOption option = PutOption.DEFAULT;

        txn = txn.Then(Op.put(bsKey, bsNewValue, option));
        
        return txn;
    }

    private Txn performActionsSwapIf(Txn txn, DssSwap action) {
        ByteSequence bsKey = ByteSequence.from(action.getKey(), UTF_8);
        ByteSequence bsOldValue = ByteSequence.from(action.getOldValue(), UTF_8);
        
        txn = txn.If(new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.value(bsOldValue)));
        
        return txn;
    }
    
    private Txn performActionsSwapThen(Txn txn, DssSwap action) {
        ByteSequence bsKey = ByteSequence.from(action.getKey(), UTF_8);
        ByteSequence bsNewValue = ByteSequence.from(action.getNewValue(), UTF_8);
        
        PutOption option = PutOption.DEFAULT;
        txn = txn.Then(Op.put(bsKey, bsNewValue, option));
        
        return txn;
    }
    
    private Txn performActionsDeleteIf(Txn txn, DssDelete action) {
        ByteSequence bsKey = ByteSequence.from(action.getKey(), UTF_8);
        
        if (action.getOldValue() != null) {
            ByteSequence bsOldValue = ByteSequence.from(action.getOldValue(), UTF_8);
            txn = txn.If(new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.value(bsOldValue)));
        }
        
        return txn;
    }

    private Txn performActionsDeleteThen(Txn txn, DssDelete action) {
        ByteSequence bsKey = ByteSequence.from(action.getKey(), UTF_8);
        
        DeleteOption option = DeleteOption.DEFAULT;
        
        txn = txn.Then(Op.delete(bsKey, option));
        
        return txn;
    }

    private Txn performActionsDeletePrefixIf(Txn txn, DssDeletePrefix action) {
        return txn;
    }

    private Txn performActionsDeletePrefixThen(Txn txn, DssDeletePrefix action) {
        ByteSequence bsKey = ByteSequence.from(action.getPrefix(), UTF_8);        
        ByteSequence prefixEnd = OptionsUtil.prefixEndOf(bsKey);
        DeleteOption option = DeleteOption.builder().withRange(prefixEnd).build();
        txn = txn.Then(Op.delete(bsKey, option));
        
        return txn;
    }



}
