/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.internal;

import static com.google.common.base.Charsets.UTF_8;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;

/**
 * This class impletements the CPS for etcd using the JETCD client.
 * 
 * @author James Davies
 * @author Matthew Chivers
 */
public class Etcd3ConfigurationPropertyStore implements IConfigurationPropertyStore {
    private final Client client;
    private final KV kvClient;

    /**
     * This constructor create a priate KVClient from JETCD for store interactions.
     * 
     * @param cpsUri - location of the etcd
     */
    public Etcd3ConfigurationPropertyStore(URI cpsUri) {
        client = Client.builder().endpoints(cpsUri).build();
        kvClient = client.getKVClient();
    }

    /**
     * This is the only method for CPS as managers should only need to get
     * properties from the CPS and not set or watch any.
     * 
     * @param key The property to get
     */
    @Override
    public @Null String getProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        ByteSequence bsKey = ByteSequence.from(key, UTF_8);
        CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey);
        try {
            GetResponse response = getFuture.get();
            List<KeyValue> kvs = response.getKvs();
            if (kvs.isEmpty()) {
                return null;
            }
            return kvs.get(0).getValue().toString(UTF_8);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConfigurationPropertyStoreException("Could not retrieve key, interrupted", e);
        } catch (ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ConfigurationPropertyStoreException("Could not retrieve key", e);
        }
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        
        HashMap<String, String> returnValues = new HashMap<>();
        
        ByteSequence bsKey = ByteSequence.from(prefix, UTF_8);
        GetOption option = GetOption.newBuilder().withPrefix(bsKey).build();
        CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey, option);  
        try {
            GetResponse response = getFuture.get();
            List<KeyValue> kvs = response.getKvs();
            for(KeyValue kv : kvs) {
                returnValues.put(kv.getKey().toString(UTF_8), kv.getValue().toString(UTF_8));
            }

            return returnValues;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConfigurationPropertyStoreException("Could not retrieve key, interrupted", e);
        } catch (ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ConfigurationPropertyStoreException("Could not retrieve key", e);
        }
    }

    @Override
    public void shutdown() throws ConfigurationPropertyStoreException {
        kvClient.close();
        client.close();
    }

    @Override
    public void setProperty(@NotNull String key, @NotNull String value) throws ConfigurationPropertyStoreException {
        ByteSequence bytesKey = ByteSequence.from(key, UTF_8);
        ByteSequence bytesValue = ByteSequence.from(value, UTF_8);
        try {
            kvClient.put(bytesKey, bytesValue).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ConfigurationPropertyStoreException("Could not set key and value.", e);
        }
    }
    
    @Override
    public void deleteProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        ByteSequence bytesKey = ByteSequence.from(key, StandardCharsets.UTF_8);
        try {
            kvClient.delete(bytesKey).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ConfigurationPropertyStoreException("Could not delete key.", e);
        }
    }

    @Override
    public Map<String, String> getPropertiesFromNamespace(String namespace) throws ConfigurationPropertyStoreException {
        ByteSequence bsNamespace = ByteSequence.from(namespace + ".", UTF_8);
        GetOption option = GetOption.newBuilder()
                .withSortField(GetOption.SortTarget.KEY)
                .withSortOrder(GetOption.SortOrder.DESCEND)
                .withRange(bsNamespace)
                .withPrefix(ByteSequence.from(namespace + ".", UTF_8))
                .build();

        CompletableFuture<GetResponse> futureResponse = client.getKVClient().get(bsNamespace, option);
        Map<String, String> results = new HashMap<>();
        try {
            GetResponse response = futureResponse.get();
            List<KeyValue> kvs = response.getKvs();
            for(KeyValue kv : kvs) {
                results.put(kv.getKey().toString(UTF_8), kv.getValue().toString(UTF_8));
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
        return results;
    }

    @Override
    public List<String> getNamespaces() throws ConfigurationPropertyStoreException {
        ByteSequence empty = ByteSequence.from("\0", UTF_8);
        GetOption option = GetOption.newBuilder()
                .withSortField(GetOption.SortTarget.KEY)
                .withSortOrder(GetOption.SortOrder.DESCEND)
                .withRange(empty)
                .build();

        CompletableFuture<GetResponse> futureResponse = client.getKVClient().get(empty, option);
        List<String> results = new ArrayList<>();
        try {
            GetResponse response = futureResponse.get();
            List<KeyValue> kvs = response.getKvs();
            for(KeyValue kv : kvs) {
                String key = kv.getKey().toString(UTF_8);
                // Ignore any etcd keys which don't have a '.' character.
                int indexOfFirstDot = key.indexOf(".");
                if (indexOfFirstDot >= 0) {
                    String namespace = key.substring(0,indexOfFirstDot);
                    if(!results.contains(namespace)) {
                        results.add(namespace);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
        return results;
    }



}
