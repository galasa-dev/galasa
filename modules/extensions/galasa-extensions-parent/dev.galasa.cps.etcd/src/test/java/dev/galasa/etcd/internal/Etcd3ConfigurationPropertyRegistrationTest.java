/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import dev.galasa.cps.etcd.internal.Etcd3ConfigurationPropertyRegistration;
import dev.galasa.cps.etcd.internal.Etcd3ConfigurationPropertyStore;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.extensions.common.mocks.MockFrameworkInitialisation;
import dev.galasa.etcd.internal.mocks.MockEtcdClient;
import dev.galasa.etcd.internal.mocks.MockEtcdKvClient;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;

public class Etcd3ConfigurationPropertyRegistrationTest {

    /**
     * Subclass that creates the store using a responsive MockEtcdClient, bypassing
     * any real network connection while still exercising the probe path.
     */
    private static class RegistrationWithMockedStore extends Etcd3ConfigurationPropertyRegistration {
        @Override
        protected Etcd3ConfigurationPropertyStore createStore(URI uri) {
            return new Etcd3ConfigurationPropertyStore(new MockEtcdClient(new HashMap<>()));
        }
    }

    /**
     * Subclass that creates the store using a KV client whose get() future
     * completes exceptionally, so the probe always fails immediately.
     */
    private static class RegistrationWithFailingStore extends Etcd3ConfigurationPropertyRegistration {
        @Override
        protected Etcd3ConfigurationPropertyStore createStore(URI uri) {
            KV failingKvClient = new MockEtcdKvClient(new HashMap<>()) {
                @Override
                public CompletableFuture<GetResponse> get(ByteSequence key) {
                    CompletableFuture<GetResponse> future = new CompletableFuture<>();
                    future.completeExceptionally(new Exception("simulating a connection error"));
                    return future;
                }
            };
            return new Etcd3ConfigurationPropertyStore(new MockEtcdClient(new HashMap<>()) {
                @Override
                public KV getKVClient() { return failingKvClient; }
            });
        }
    }

    @Test
    public void testCanCreateARegistrationOk() {
        new Etcd3ConfigurationPropertyRegistration();
    }

    @Test
    public void testWhenRemoteRunCanInitialiseARegistrationOK() throws Exception {
        // Given...
        Etcd3ConfigurationPropertyRegistration registration = new RegistrationWithMockedStore();

        URI cps = new URI("etcd://my.server/api");
        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation(cps);

        // When...
        registration.initialise(mockFrameworkInit);

        // Then...
        assertThat(mockFrameworkInit.getBootstrapConfigurationPropertyStore()).isEqualTo(cps);
    }

    @Test
    public void testWhenEtcdUnreachableInitialisationThrowsConfigurationPropertyStoreException() throws Exception {
        // Given...
        Etcd3ConfigurationPropertyRegistration registration = new RegistrationWithFailingStore();

        URI cps = new URI("etcd://my.server/api");
        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation(cps);

        // When / Then...
        assertThatThrownBy(() -> registration.initialise(mockFrameworkInit))
            .isInstanceOf(ConfigurationPropertyStoreException.class)
            .hasMessageContaining("Failed to connect to etcd");
    }
}
