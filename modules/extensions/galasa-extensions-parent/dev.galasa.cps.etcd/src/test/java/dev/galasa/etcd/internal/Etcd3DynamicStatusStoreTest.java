/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import dev.galasa.cps.etcd.internal.Etcd3DynamicStatusStore;
import dev.galasa.etcd.internal.mocks.MockEtcdClient;
import dev.galasa.etcd.internal.mocks.MockEtcdKvClient;
import dev.galasa.etcd.internal.mocks.MockEtcdLeaseClient;
import dev.galasa.etcd.internal.mocks.MockTransaction;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.op.Op.PutOp;

import static org.assertj.core.api.Assertions.*;

public class Etcd3DynamicStatusStoreTest {

    @Test
    public void testPutPropertyWithTimeToLiveCreatesExpectedLeaseOk() throws Exception {
        // Given...
        Map<String, String> mockProps = new HashMap<>();

        MockEtcdLeaseClient mockLeaseClient = new MockEtcdLeaseClient();
        MockEtcdClient mockClient = new MockEtcdClient(mockProps);
        mockClient.setLeaseClient(mockLeaseClient);

        Etcd3DynamicStatusStore store = new Etcd3DynamicStatusStore(mockClient);

        String keyToAdd = "key1";
        String valueToAdd = "value1";
        long timeToLiveSecs = 5;

        // When...
        store.put(keyToAdd, valueToAdd, timeToLiveSecs);

        // Then...
        assertThat(mockProps).hasSize(1);
        assertThat(mockProps.get(keyToAdd)).isEqualTo(valueToAdd);
        assertThat(mockLeaseClient.getLeases()).hasSize(1);
        assertThat(mockLeaseClient.getLeases().get(0).getTTL()).isEqualTo(timeToLiveSecs);
    }

    @Test
    public void testPutMultiplePropertiesWithTimeToLiveCreatesExpectedLeaseOk() throws Exception {
        // Given...
        Map<String, String> mockProps = new HashMap<>();

        MockEtcdLeaseClient mockLeaseClient = new MockEtcdLeaseClient();
        MockEtcdClient mockClient = new MockEtcdClient(mockProps);
        mockClient.setLeaseClient(mockLeaseClient);

        Etcd3DynamicStatusStore store = new Etcd3DynamicStatusStore(mockClient);

        long timeToLiveSecs = 5;

        Map<String, String> keyValuesToAdd = new HashMap<>();
        String keyToAdd1 = "key1";
        String valueToAdd1 = "value1";

        String keyToAdd2 = "key2";
        String valueToAdd2 = "value2";

        keyValuesToAdd.put(keyToAdd1, valueToAdd1);
        keyValuesToAdd.put(keyToAdd2, valueToAdd2);

        // When...
        store.put(keyValuesToAdd, timeToLiveSecs);

        // Then...
        assertThat(mockLeaseClient.getLeases()).hasSize(1);
        assertThat(mockLeaseClient.getLeases().get(0).getTTL()).isEqualTo(timeToLiveSecs);

        MockEtcdKvClient mockKvClient = (MockEtcdKvClient) mockClient.getKVClient();
        MockTransaction mockTransaction = mockKvClient.getTransaction();
        assertThat(mockTransaction).isNotNull();

        List<Op> thenOperations = mockTransaction.getThenOperations();
        assertThat(thenOperations).hasSize(2);
        assertThat(thenOperations).hasOnlyElementsOfType(PutOp.class);
    }

    @Test
    public void testCreateAnEtcd3DynamicStatusStoreWithDefaultgRPCMessageSizeIsOK() throws Exception {
        // Given...
        URI uri = new URI("http://mydss.com");
        int defaultMaxgRPCMessageSize = 4194304;

        // When...
        new Etcd3DynamicStatusStore(uri, defaultMaxgRPCMessageSize);

        // Then...
        // We should have been able to create a DSS okay.
    }

    @Test
    public void testCreateAnEtcd3DynamicStatusStoreWithIntegerMaxValueIsOK() throws Exception {
        // Given...
        URI uri = new URI("http://mydss.com");

        // When...
        new Etcd3DynamicStatusStore(uri, Integer.MAX_VALUE);

        // Then...
        // We should have been able to create a DSS okay.
    }

    @Test
    public void testCreateAnEtcd3DynamicStatusStoreWithZeroValueIsOK() throws Exception {
        // Given...
        URI uri = new URI("http://mydss.com");

        // When...
        new Etcd3DynamicStatusStore(uri, 0);

        // Then...
        // We should have been able to create a DSS okay.
    }

    @Test
    public void testCreateAnEtcd3DynamicStatusStoreWithNegativeValueThrowsException() throws Exception {
        // Given...
        URI uri = new URI("http://mydss.com");

        // When...
        Exception thrown = catchThrowableOfType(() -> new Etcd3DynamicStatusStore(uri, -1), Exception.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("negative max");
    }

}
