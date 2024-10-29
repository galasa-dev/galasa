/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal.mocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.protobuf.ByteString;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.api.KeyValue;
import io.etcd.jetcd.api.KeyValue.Builder;
import io.etcd.jetcd.api.RangeResponse;
import io.etcd.jetcd.kv.CompactResponse;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.CompactOption;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;

public class MockEtcdKvClient implements KV {

    Map<String, String> kvContents = new HashMap<>();

    public MockEtcdKvClient(Map<String, String> kvContents) {
        this.kvContents = kvContents;
    }

    @Override
    public CompletableFuture<GetResponse> get(ByteSequence key) {
        String keyStr = key.toString();
        String value = kvContents.get(keyStr);

        RangeResponse rangeResponse;
        if (value == null) {
            rangeResponse = RangeResponse.newBuilder().build();
        } else {
            ByteString keyByteStr = ByteString.copyFromUtf8(keyStr);
            Builder builder = KeyValue.newBuilder().setKey(keyByteStr);
            ByteString valueByteStr = ByteString.copyFromUtf8(value);
            builder = builder.setValue(valueByteStr);
            KeyValue kv = builder.build();
            rangeResponse = RangeResponse.newBuilder().addKvs(kv).build();
        }
        GetResponse mockResponse = new GetResponse(rangeResponse, key);
        return CompletableFuture.completedFuture(mockResponse);
    }

    @Override
    public CompletableFuture<PutResponse> put(ByteSequence key, ByteSequence value) {
        String keyStr = key.toString();
        String valueStr = value.toString();
        kvContents.put(keyStr, valueStr);

        return CompletableFuture.completedFuture(null);
    }


    @Override
    public CompletableFuture<DeleteResponse> delete(ByteSequence key, DeleteOption options) {
        String keyStr = key.toString();

        if (options.isPrefix()) {
            Set<String> keysToRemove = new HashSet<>();
            Set<String> existingKeySet = kvContents.keySet();

            for (String existingKey : existingKeySet) {
                if (existingKey.startsWith(keyStr)) {
                    keysToRemove.add(existingKey);
                }
            }

            existingKeySet.removeAll(keysToRemove);
        } else {
            kvContents.remove(keyStr);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<CompactResponse> compact(long key) {
        throw new UnsupportedOperationException("Unimplemented method 'compact'");
    }

    @Override
    public CompletableFuture<CompactResponse> compact(long key, CompactOption options) {
        throw new UnsupportedOperationException("Unimplemented method 'compact'");
    }

    @Override
    public CompletableFuture<DeleteResponse> delete(ByteSequence key) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public CompletableFuture<GetResponse> get(ByteSequence key, GetOption options) {
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public CompletableFuture<PutResponse> put(ByteSequence key, ByteSequence value, PutOption options) {
        throw new UnsupportedOperationException("Unimplemented method 'put'");
    }

    @Override
    public Txn txn() {
        throw new UnsupportedOperationException("Unimplemented method 'txn'");
    }
    
}
