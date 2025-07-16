/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.Test;

import dev.galasa.cps.etcd.internal.Etcd3DynamicStatusStoreRegistration;
import dev.galasa.extensions.common.mocks.MockEnvironment;
import dev.galasa.extensions.common.mocks.MockFrameworkInitialisation;

public class Etcd3DynamicStatusStoreRegistrationTest {

    private static final String MAX_GRPC_MESSAGE_SIZE_ENV_VAR = "MAX_GRPC_MESSAGE_SIZE";

    @Test
    public void testCanCreateARegistrationOK() {
        new Etcd3DynamicStatusStoreRegistration();
    }

    @Test
    public void testWhenRemoteRunCanInitialiseARegistrationOK() throws Exception {
        // Given...
        Etcd3DynamicStatusStoreRegistration registration = new Etcd3DynamicStatusStoreRegistration();

        URI cps = new URI("etcd://my.server/api");
        URI dss = new URI("etcd://my.server/api");
        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation(cps, dss);

        // When...
        registration.initialise(mockFrameworkInit);

        // Then...
        assertThat(mockFrameworkInit.getDynamicStatusStoreUri()).isEqualTo(dss);
    }

    @Test
    public void testCanInitialiseARegistrationWithValidMaxgRPCMessageSizeFromEnvironmentOK() throws Exception {
        // Given...
        MockEnvironment mockEnvironment = new MockEnvironment();
        String validValue = Integer.toString(Integer.MAX_VALUE);
        mockEnvironment.setenv(MAX_GRPC_MESSAGE_SIZE_ENV_VAR, validValue);

        Etcd3DynamicStatusStoreRegistration registration = new Etcd3DynamicStatusStoreRegistration(mockEnvironment);

        URI cps = new URI("etcd://my.server/api");
        URI dss = new URI("etcd://my.server/api");
        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation(cps, dss);

        // When...
        registration.initialise(mockFrameworkInit);

        // Then...
        // We should have been able to initialise the registration OK.
    }

    @Test
    public void testCanInitialiseARegistrationWithInvalidMaxgRPCMessageSizeFromEnvironmentOK() throws Exception {
        // Given...
        MockEnvironment mockEnvironment = new MockEnvironment();
        String invalidValue = "2147483648"; // This is an invalid integer as bigger than 2147483647.
        mockEnvironment.setenv(MAX_GRPC_MESSAGE_SIZE_ENV_VAR, invalidValue);

        Etcd3DynamicStatusStoreRegistration registration = new Etcd3DynamicStatusStoreRegistration(mockEnvironment);

        URI cps = new URI("etcd://my.server/api");
        URI dss = new URI("etcd://my.server/api");
        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation(cps, dss);

        // When...
        registration.initialise(mockFrameworkInit);

        // Then...
        // We should have been able to initialise the registration OK still,
        // as the env var should be ignored and default if it is invalid.
    }

}
