/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import dev.galasa.cps.etcd.internal.Etcd3ConfigurationPropertyStore;
import dev.galasa.etcd.internal.mocks.MockEtcdClient;
import dev.galasa.etcd.internal.mocks.MockEtcdKvClient;
import dev.galasa.etcd.internal.mocks.MockTransaction;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.op.Op.PutOp;

import static org.assertj.core.api.Assertions.*;

public class Etcd3ConfigurationPropertyStoreTest {

    @Test
    public void testSetPropertiesCreatesTransactionOk() throws Exception {
        // Given...
        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put("my.test.property", "myvalue");
        propertiesToSet.put("another.test.property", "anothervalue");
        propertiesToSet.put("more.test.properties", "lastvalue");

        Map<String, String> kvClientContents = new HashMap<>();
        MockEtcdClient mockClient = new MockEtcdClient(kvClientContents);
        Etcd3ConfigurationPropertyStore store = new Etcd3ConfigurationPropertyStore(mockClient);

        // When...
        store.setProperties(propertiesToSet);

        // Then...
        MockEtcdKvClient mockKvClient = (MockEtcdKvClient) mockClient.getKVClient();
        MockTransaction mockTransaction = mockKvClient.getTransaction();
        assertThat(mockTransaction).isNotNull();

        List<Op> thenOperations = mockTransaction.getThenOperations();
        assertThat(thenOperations).hasSize(3);
        assertThat(thenOperations).hasOnlyElementsOfType(PutOp.class);
    }
}
