/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;

import org.junit.Test;

import dev.galasa.extensions.common.mocks.MockEnvironment;
import dev.galasa.extensions.common.mocks.MockFrameworkInitialisation;
import dev.galasa.framework.spi.creds.ICredentialsStore;

/**
 * Unit tests for OsCredentialsStoreRegistration.
 */
public class OsCredentialsStoreRegistrationTest {

    @Test
    public void testInitialiseWithOsAutoUri() throws Exception {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("os.name", "MacOS");
        OperatingSystemDetector detector = new OperatingSystemDetector(mockEnv);

        URI uri = new URI("os:auto");
        MockFrameworkInitialisation mockInit = new MockFrameworkInitialisation();
        mockInit.setCredentialsStoreUri(uri);
        OsCredentialsStoreRegistration registration = new OsCredentialsStoreRegistration(detector);

        // When
        registration.initialise(mockInit);

        // Then
        ICredentialsStore registeredStore = mockInit.getRegisteredCredentialsStore();
        assertThat(registeredStore).as("Credentials store should be registered").isNotNull();
        assertThat(registeredStore).as("Should be OsCredentialsStore").isInstanceOf(OsCredentialsStore.class);
        
        OsCredentialsStore osStore = (OsCredentialsStore) registeredStore;
        assertThat(osStore.getOperatingSystem()).as("Should detect OS").isNotEqualTo(OperatingSystem.UNKNOWN);
    }

    @Test
    public void testInitialiseWithOsMacOSUri() throws Exception {
        // Given
        OperatingSystemDetector detector = new OperatingSystemDetector();
        URI uri = new URI("os:macOS");
        MockFrameworkInitialisation mockInit = new MockFrameworkInitialisation();
        mockInit.setCredentialsStoreUri(uri);
        OsCredentialsStoreRegistration registration = new OsCredentialsStoreRegistration(detector);

        // When
        registration.initialise(mockInit);

        // Then
        ICredentialsStore registeredStore = mockInit.getRegisteredCredentialsStore();
        assertThat(registeredStore).as("Credentials store should be registered").isNotNull();
        assertThat(registeredStore).as("Should be OsCredentialsStore").isInstanceOf(OsCredentialsStore.class);
        
        OsCredentialsStore osStore = (OsCredentialsStore) registeredStore;
        assertThat(osStore.getOperatingSystem()).as("Should be macOS").isEqualTo(OperatingSystem.MACOS);
    }

    @Test
    public void testInitialiseWithNonOsUri() throws Exception {
        // Given
        OperatingSystemDetector detector = new OperatingSystemDetector();
        URI uri = new URI("file:///some/path");
        MockFrameworkInitialisation mockInit = new MockFrameworkInitialisation();
        mockInit.setCredentialsStoreUri(uri);
        OsCredentialsStoreRegistration registration = new OsCredentialsStoreRegistration(detector);

        // When
        registration.initialise(mockInit);

        // Then
        ICredentialsStore registeredStore = mockInit.getRegisteredCredentialsStore();
        assertThat(registeredStore).as("No credentials store should be registered for non-os URI").isNull();
    }

    @Test
    public void testInitialiseWithInvalidOsUri() throws Exception {
        // Given
        OperatingSystemDetector detector = new OperatingSystemDetector();
        URI uri = new URI("os:invalid");
        MockFrameworkInitialisation mockInit = new MockFrameworkInitialisation();
        mockInit.setCredentialsStoreUri(uri);
        OsCredentialsStoreRegistration registration = new OsCredentialsStoreRegistration(detector);

        // When/Then
        assertThatThrownBy(() -> registration.initialise(mockInit))
            .as("Should throw exception for invalid OS")
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Unable to determine operating system");
    }

    @Test
    public void testInitialiseWithWindowsUri() throws Exception {
        // Given
        OperatingSystemDetector detector = new OperatingSystemDetector();
        URI uri = new URI("os:windows");
        MockFrameworkInitialisation mockInit = new MockFrameworkInitialisation();
        mockInit.setCredentialsStoreUri(uri);
        OsCredentialsStoreRegistration registration = new OsCredentialsStoreRegistration(detector);

        // When/Then
        assertThatThrownBy(() -> registration.initialise(mockInit))
            .as("Should throw exception for unsupported Windows")
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Windows Credential Manager is not yet implemented");
    }

    @Test
    public void testInitialiseWithLinuxUri() throws Exception {
        // Given
        OperatingSystemDetector detector = new OperatingSystemDetector();
        URI uri = new URI("os:linux");
        MockFrameworkInitialisation mockInit = new MockFrameworkInitialisation();
        mockInit.setCredentialsStoreUri(uri);
        OsCredentialsStoreRegistration registration = new OsCredentialsStoreRegistration(detector);

        // When/Then
        assertThatThrownBy(() -> registration.initialise(mockInit))
            .as("Should throw exception for unsupported Linux")
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Linux Secret Service is not yet implemented");
    }
}

// Made with Bob
