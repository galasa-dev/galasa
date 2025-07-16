/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import dev.galasa.cps.etcd.internal.Etcd3DynamicStatusStore;
import dev.galasa.etcd.internal.mocks.MockEtcdClient;
import dev.galasa.etcd.internal.mocks.MockEtcdLeaseClient;
import dev.galasa.framework.spi.auth.AuthStoreException;

import static org.assertj.core.api.Assertions.*;

public class Etcd3DynamicStatusStoreTest {

    private static final int DEFAULT_MAX_GRPC_MESSAGE_SIZE = 4194304;

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
    public void testCreateAnEtcd3DynamicStatusStoreWithDefaultgRPCMessageSizeIsOK() throws Exception {
        // Given...
        URI uri = new URI("http://mydss.com");

        // When...
        new Etcd3DynamicStatusStore(uri, DEFAULT_MAX_GRPC_MESSAGE_SIZE);

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
    public void testCreateAnEtcd3DynamicStatusStoreWithBiggerThanIntegerMaxValueThrowsException() throws Exception {
        // Given...
        URI uri = new URI("http://mydss.com");

        // When...
        Exception thrown = catchThrowableOfType(() -> new Etcd3DynamicStatusStore(uri, Integer.MAX_VALUE + 1),
                IllegalArgumentException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("negative max");
    }
}
