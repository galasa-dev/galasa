/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import dev.galasa.cps.etcd.internal.Etcd3CredentialsStore;
import dev.galasa.cps.etcd.internal.Etcd3CredentialsStoreRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.extensions.common.mocks.MockFrameworkInitialisation;
import dev.galasa.etcd.internal.mocks.MockEtcdClient;
import dev.galasa.etcd.internal.mocks.MockEtcdKvClient;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;

public class Etcd3CredentialsStoreRegistrationTest {

    /**
     * Subclass that creates the store using a responsive MockEtcdClient, bypassing
     * any real network connection while still exercising the probe path.
     */
    private static class RegistrationWithMockedStore extends Etcd3CredentialsStoreRegistration {
        @Override
        protected Etcd3CredentialsStore createStore(IFrameworkInitialisation frameworkInitialisation, URI uri) throws CredentialsException {
            return new Etcd3CredentialsStore(null, null, new MockEtcdClient(new HashMap<>()));
        }
    }

    /**
     * Subclass that creates the store using a KV client whose get() future
     * completes exceptionally, so the probe always fails immediately.
     */
    private static class RegistrationWithFailingStore extends Etcd3CredentialsStoreRegistration {
        @Override
        protected Etcd3CredentialsStore createStore(IFrameworkInitialisation frameworkInitialisation, URI uri) throws CredentialsException {
            KV failingKvClient = new MockEtcdKvClient(new HashMap<>()) {
                @Override
                public CompletableFuture<GetResponse> get(ByteSequence key) {
                    CompletableFuture<GetResponse> future = new CompletableFuture<>();
                    future.completeExceptionally(new RuntimeException("connection refused"));
                    return future;
                }
            };
            return new Etcd3CredentialsStore(null, null, new MockEtcdClient(new HashMap<>()) {
                @Override
                public KV getKVClient() { return failingKvClient; }
            });
        }
    }

    @Test
    public void testCanCreateARegistrationOk() {
        new Etcd3CredentialsStoreRegistration();
    }

    @Test
    public void testWhenRemoteRunCanInitialiseARegistrationOK() throws Exception {
        // Given...
        Etcd3CredentialsStoreRegistration registration = new RegistrationWithMockedStore();

        URI cps = new URI("etcd://my.server/api");
        URI creds = new URI("etcd://my.server/api");
        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation(cps);
        mockFrameworkInit.setCredentialsStoreUri(creds);

        // When...
        registration.initialise(mockFrameworkInit);

        // Then... no exception - store was registered successfully
    }

    @Test
    public void testWhenEtcdUnreachableInitialisationThrowsCredentialsException() throws Exception {
        // Given...
        Etcd3CredentialsStoreRegistration registration = new RegistrationWithFailingStore();

        URI cps = new URI("etcd://my.server/api");
        URI creds = new URI("etcd://my.server/api");
        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation(cps);
        mockFrameworkInit.setCredentialsStoreUri(creds);

        // When / Then...
        assertThatThrownBy(() -> registration.initialise(mockFrameworkInit))
            .isInstanceOf(CredentialsException.class)
            .hasMessageContaining("Failed to connect to etcd");
    }
}
