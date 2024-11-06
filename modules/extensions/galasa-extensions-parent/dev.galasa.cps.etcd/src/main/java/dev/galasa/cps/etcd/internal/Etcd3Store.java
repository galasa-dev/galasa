/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.validation.constraints.NotNull;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;

/**
 * Abstract class containing common methods used to interact with etcd, like getting, setting,
 * and deleting properties.
 */
public abstract class Etcd3Store {

    protected final Client client;
    protected final KV kvClient;

    public Etcd3Store(Client client) {
        this.client = client;
        this.kvClient = client.getKVClient();
    }

    public Etcd3Store(URI etcdUri) {
        this(Client.builder().endpoints(etcdUri).build());
    }

    protected String get(String key) throws InterruptedException, ExecutionException {
        ByteSequence bsKey = ByteSequence.from(key, UTF_8);
        CompletableFuture<GetResponse> getFuture = kvClient.get(bsKey);
        GetResponse response = getFuture.get();
        List<KeyValue> kvs = response.getKvs();

        String retrievedKey = null;
        if (!kvs.isEmpty()) {
            retrievedKey = kvs.get(0).getValue().toString(UTF_8);
        }
        return retrievedKey;
    }

    protected Map<String, String> getPrefix(String keyPrefix) throws InterruptedException, ExecutionException {
        Map<String, String> keyValues = new HashMap<>();

        ByteSequence bsPrefix = ByteSequence.from(keyPrefix, UTF_8);
        GetOption options = GetOption.builder().isPrefix(true).build();
        CompletableFuture<GetResponse> getFuture = kvClient.get(bsPrefix, options);

        GetResponse response = getFuture.get();
        List<KeyValue> kvs = response.getKvs();

        for (KeyValue kv : kvs) {
            // jetcd's getKey() method strips off the given prefix from matching keys, so add them back in
            String key = kv.getKey().toString(UTF_8);
            if (!key.startsWith(keyPrefix)) {
                key = keyPrefix + key;
            }
            keyValues.put(key, kv.getValue().toString(UTF_8));
        }

        return keyValues;
    }

    protected void put(String key, String value) throws InterruptedException, ExecutionException {
        ByteSequence bytesKey = ByteSequence.from(key, UTF_8);
        ByteSequence bytesValue = ByteSequence.from(value, UTF_8);
        kvClient.put(bytesKey, bytesValue).get();
    }

    protected void delete(@NotNull String key) throws InterruptedException, ExecutionException {
        ByteSequence bytesKey = ByteSequence.from(key, StandardCharsets.UTF_8);
        kvClient.delete(bytesKey).get();
    }

    protected void deletePrefix(@NotNull String keyPrefix) throws InterruptedException, ExecutionException {
        ByteSequence bsKey = ByteSequence.from(keyPrefix, UTF_8);
        DeleteOption options = DeleteOption.builder().isPrefix(true).build();
        kvClient.delete(bsKey, options).get();
    }

    protected void shutdownStore() {
        kvClient.close();
        client.close();
    }
}
