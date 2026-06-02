/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.creds.os.internal.OsCredentialsException;

public class WindowsCredentialManagerTest {

    private MockCredentialManagerJNA mockJNA;
    private TestableWindowsCredentialManager credentialManager;

    class TestableWindowsCredentialManager extends WindowsCredentialManager {
        private final MockCredentialManagerJNA mockJNA;

        public TestableWindowsCredentialManager(MockCredentialManagerJNA mockJNA) {
            super(mockJNA);
            this.mockJNA = mockJNA;
        }

        @Override
        int getLastErrorCode() {
            // Return the mock error code instead of calling Native.getLastError()
            return mockJNA.getLastError();
        }
    }

    @Before
    public void setUp() {
        mockJNA = new MockCredentialManagerJNA();
        credentialManager = new TestableWindowsCredentialManager(mockJNA);
    }

    @After
    public void tearDown() {
        mockJNA.clear();
    }

    @Test
    public void testReadCredentialWithUsernameAndPassword() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.TEST";
        String username = "testuser";
        String password = "testpass";
        mockJNA.addCredential(targetName, username, password);

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(password);
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialWithUsernameOnly() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.USERONLY";
        String username = "testuser";
        mockJNA.addCredential(targetName, username, "");

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEmpty();
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialWithPasswordOnly() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.PASSONLY";
        String password = "testpass";
        mockJNA.addCredential(targetName, "", password);

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEmpty();
        assertThat(result.getPassword()).isEqualTo(password);
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialNotFound() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.NOTFOUND";
        // No credential added

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testReadCredentialWithSpecialCharactersInPassword() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.SPECIAL";
        String username = "user";
        String password = "p@ssw0rd!#$%^&*()";
        mockJNA.addCredential(targetName, username, password);

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(password);
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialWithLongPassword() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.LONG";
        String username = "user";
        String password = "a".repeat(1000); // 1000 character password
        mockJNA.addCredential(targetName, username, password);

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(password);
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialWithNullTargetName() {
        // When/Then
        assertThatThrownBy(() -> credentialManager.readCredential(null))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Target name cannot be null or empty");
    }

    @Test
    public void testReadCredentialWithEmptyTargetName() {
        // When/Then
        assertThatThrownBy(() -> credentialManager.readCredential(""))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Target name cannot be null or empty");
    }

    @Test
    public void testReadCredentialWithWhitespaceTargetName() {
        // When/Then
        assertThatThrownBy(() -> credentialManager.readCredential("   "))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Target name cannot be null or empty");
    }

    @Test
    public void testReadCredentialWithInvalidCharacters() {
        // When/Then
        assertThatThrownBy(() -> credentialManager.readCredential("galasa/credentials/TEST"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID contains invalid characters");
    }

    @Test
    public void testReadCredentialWithInvalidCharactersBackslash() {
        // When/Then
        assertThatThrownBy(() -> credentialManager.readCredential("galasa\\credentials\\TEST"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID contains invalid characters");
    }

    @Test
    public void testReadCredentialWithInvalidCharactersSpace() {
        // When/Then
        assertThatThrownBy(() -> credentialManager.readCredential("galasa credentials TEST"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID contains invalid characters");
    }

    @Test
    public void testReadCredentialWithValidSpecialCharacters() throws OsCredentialsException {
        // Given - dots, underscores, hyphens, and @ symbols are allowed
        String targetName = "galasa.credentials.TEST-USER_123@domain";
        String username = "user";
        String password = "pass";
        mockJNA.addCredential(targetName, username, password);

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(password);
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialWithNoLogonSession() {
        // Given
        String targetName = "galasa.credentials.TEST";
        mockJNA.setLastError(WindowsCredentialManager.ERROR_NO_SUCH_LOGON_SESSION);

        // When/Then
        assertThatThrownBy(() -> credentialManager.readCredential(targetName))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("No logon session exists");
    }

    @Test
    public void testReadCredentialWithInvalidParameter() {
        // Given
        String targetName = "galasa.credentials.TEST";
        mockJNA.setLastError(WindowsCredentialManager.ERROR_INVALID_PARAMETER);

        // When/Then
        assertThatThrownBy(() -> credentialManager.readCredential(targetName))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Invalid parameter when reading credential");
    }

    @Test
    public void testReadCredentialWithUnknownError() {
        // Given
        String targetName = "galasa.credentials.TEST";
        mockJNA.setLastError(999); // Unknown error code

        // When/Then
        assertThatThrownBy(() -> credentialManager.readCredential(targetName))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Failed to read credential from Windows Credential Manager")
            .hasMessageContaining("Error code: 999");
    }

    @Test
    public void testReadCredentialWithEmptyUsername() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.EMPTYUSER";
        mockJNA.addCredential(targetName, "", "password");

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEmpty();
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialWithEmptyPassword() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.EMPTYPASS";
        mockJNA.addCredential(targetName, "username", "");

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("username");
        assertThat(result.getPassword()).isEmpty();
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialMultipleTimes() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.MULTI";
        String username = "user";
        String password = "pass";
        mockJNA.addCredential(targetName, username, password);

        // When - read the same credential multiple times
        CredentialItem result1 = credentialManager.readCredential(targetName);
        CredentialItem result2 = credentialManager.readCredential(targetName);
        CredentialItem result3 = credentialManager.readCredential(targetName);

        // Then - all reads should succeed and memory should be freed each time
        assertThat(result1).isNotNull();
        assertThat(result1.getUsername()).isEqualTo(username);
        assertThat(result1.getPassword()).isEqualTo(password);

        assertThat(result2).isNotNull();
        assertThat(result2.getUsername()).isEqualTo(username);
        assertThat(result2.getPassword()).isEqualTo(password);

        assertThat(result3).isNotNull();
        assertThat(result3.getUsername()).isEqualTo(username);
        assertThat(result3.getPassword()).isEqualTo(password);

        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadDifferentCredentials() throws OsCredentialsException {
        // Given
        mockJNA.addCredential("galasa.credentials.CRED1", "user1", "pass1");
        mockJNA.addCredential("galasa.credentials.CRED2", "user2", "pass2");
        mockJNA.addCredential("galasa.credentials.CRED3", "user3", "pass3");

        // When
        CredentialItem result1 = credentialManager.readCredential("galasa.credentials.CRED1");
        CredentialItem result2 = credentialManager.readCredential("galasa.credentials.CRED2");
        CredentialItem result3 = credentialManager.readCredential("galasa.credentials.CRED3");

        // Then
        assertThat(result1.getUsername()).isEqualTo("user1");
        assertThat(result1.getPassword()).isEqualTo("pass1");

        assertThat(result2.getUsername()).isEqualTo("user2");
        assertThat(result2.getPassword()).isEqualTo("pass2");

        assertThat(result3.getUsername()).isEqualTo("user3");
        assertThat(result3.getPassword()).isEqualTo("pass3");

        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialWithNumericUsername() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.NUMERIC";
        String username = "12345";
        String password = "pass";
        mockJNA.addCredential(targetName, username, password);

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(password);
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }

    @Test
    public void testReadCredentialWithNumericPassword() throws OsCredentialsException {
        // Given
        String targetName = "galasa.credentials.NUMPASS";
        String username = "user";
        String password = "123456789";
        mockJNA.addCredential(targetName, username, password);

        // When
        CredentialItem result = credentialManager.readCredential(targetName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(password);
        assertThat(mockJNA.isMemoryFreed()).isTrue();
    }
}
