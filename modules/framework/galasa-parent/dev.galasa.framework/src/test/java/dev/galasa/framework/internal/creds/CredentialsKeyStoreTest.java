/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.creds;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsKeyStore;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsKeyStore;

/**
 * Test class for CredentialsKeyStore functionality.
 * 
 * This class tests the creation, storage, and retrieval of KeyStore
 * credentials. Uses a simple KeyStore with a secret key for testing
 * to avoid complex certificate generation.
 */
public class CredentialsKeyStoreTest {

    private File fileCPS;
    private File fileCREDS;
    private KeyStore testKeyStore;
    private String testPassword = "testPassword123"; //pragma: allowlist secret
    private byte[] testKeyStoreBytes;
    private String encodedKeyStore;

    @Before
    public void setup() throws Exception {
        fileCPS = File.createTempFile("galasacps_", ".properties");
        fileCREDS = File.createTempFile("galasacreds_", ".properties");
        
        // Create a simple test KeyStore with a secret key (no certificates needed)
        testKeyStore = createSimpleTestKeyStore();
        
        // Convert KeyStore to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        testKeyStore.store(baos, testPassword.toCharArray());
        testKeyStoreBytes = baos.toByteArray();

        encodedKeyStore = "base64:" + Base64.getEncoder().encodeToString(testKeyStoreBytes);
    }

    @After
    public void teardown() throws Exception {
        if (fileCPS.exists()) {
            fileCPS.delete();
        }
        if (fileCREDS.exists()) {
            fileCREDS.delete();
        }
    }

    /**
     * Test creating a plain-text KeyStore credential.
     */
    @Test
    public void testCreatePlainTextKeyStoreCredential() throws Exception {
        // Create credentials
        CredentialsKeyStore creds = new CredentialsKeyStore(encodedKeyStore, testPassword, "PKCS12");

        // Verify properties
        assertThat(creds.getEncodedKeyStore()).isEqualTo(encodedKeyStore);
        assertThat(creds.getKeyStorePassword()).isEqualTo(testPassword);
        assertThat(creds.getKeyStoreType()).isEqualTo("PKCS12");
        
        // Verify KeyStore can be loaded and contains the test key
        KeyStore loadedKeyStore = creds.getKeyStore();
        assertThat(loadedKeyStore).isNotNull();
        assertThat(loadedKeyStore.containsAlias("test-secret-key")).isTrue();
    }

    /**
     * Test that KeyStore defaults to PKCS12 when type is null.
     */
    @Test
    public void testKeyStoreTypeDefaultsToPKCS12() throws Exception {
        CredentialsKeyStore creds = new CredentialsKeyStore(null, encodedKeyStore, testPassword, null);
        assertThat(creds.getKeyStoreType()).isEqualTo("PKCS12");
    }

    /**
     * Test converting KeyStore credentials to Properties format.
     */
    @Test
    public void testToProperties() throws Exception {
        CredentialsKeyStore creds = new CredentialsKeyStore(encodedKeyStore, testPassword, "PKCS12");
        
        Properties props = creds.toProperties("TESTCREDS");
        
        assertThat(props).isNotNull();
        
        // Verify the keystore is base64 encoded with "base64:" prefix
        String encodedKeyStore = props.getProperty("secure.credentials.TESTCREDS.keystore");
        assertThat(encodedKeyStore).startsWith("base64:");
        
        // Remove prefix and decode
        String base64Data = encodedKeyStore.substring("base64:".length());
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        assertThat(decodedBytes).isEqualTo(testKeyStoreBytes);
        
        assertThat(props.getProperty("secure.credentials.TESTCREDS.password")).isEqualTo(testPassword);
        assertThat(props.getProperty("secure.credentials.TESTCREDS.type")).isEqualTo("PKCS12");
    }

    /**
     * Test retrieving KeyStore credentials from FileCredentialsStore.
     *
     * The KeyStore bytes must be stored with the "base64:" prefix so the
     * decode() method knows to base64-decode them.
     */
    @Test
    public void testGetKeyStoreCredentialsFromFile() throws Exception {
        Properties propsCPS = new Properties();
        saveProperties(propsCPS, fileCPS);
        
        Properties propsCREDS = new Properties();
        propsCREDS.setProperty("secure.credentials.TESTCREDSID.keystore", encodedKeyStore);
        propsCREDS.setProperty("secure.credentials.TESTCREDSID.password", testPassword);
        propsCREDS.setProperty("secure.credentials.TESTCREDSID.type", "PKCS12");
        saveProperties(propsCREDS, fileCREDS);

        IFramework framework = new MockFramework(fileCPS);
        FileCredentialsStore fileCreds = new FileCredentialsStore(fileCREDS.toURI(), framework);

        ICredentials creds = fileCreds.getCredentials("TESTCREDSID");

        assertThat(creds).isNotNull();
        assertThat(creds).isInstanceOf(ICredentialsKeyStore.class);

        ICredentialsKeyStore keyStoreCreds = (ICredentialsKeyStore) creds;
        assertThat(keyStoreCreds.getKeyStorePassword()).isEqualTo(testPassword);
        assertThat(keyStoreCreds.getKeyStoreType()).isEqualTo("PKCS12");
        
        // Verify KeyStore can be loaded
        KeyStore loadedKeyStore = keyStoreCreds.getKeyStore();
        assertThat(loadedKeyStore).isNotNull();
        assertThat(loadedKeyStore.containsAlias("test-secret-key")).isTrue();
    }

    /**
     * Test that invalid KeyStore bytes throw an exception when loading.
     */
    @Test
    public void testInvalidKeyStoreBytesThrowsException() throws Exception {
        byte[] invalidBytes = "not a valid keystore".getBytes();
        CredentialsKeyStore creds = new CredentialsKeyStore("base64:" + Base64.getEncoder().encodeToString(invalidBytes), testPassword, "PKCS12");

        assertThatThrownBy(() -> creds.getKeyStore())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to load KeyStore");
    }

    /**
     * Test that KeyStore is cached after first load.
     */
    @Test
    public void testKeyStoreIsCached() throws Exception {
        CredentialsKeyStore creds = new CredentialsKeyStore(encodedKeyStore, testPassword, "PKCS12");
        
        KeyStore keyStore1 = creds.getKeyStore();
        KeyStore keyStore2 = creds.getKeyStore();
        
        // Should return the same instance (cached)
        assertThat(keyStore1).isSameAs(keyStore2);
    }

    /**
     * Test metadata properties (description, last updated).
     */
    @Test
    public void testMetadataProperties() throws Exception {
        CredentialsKeyStore creds = new CredentialsKeyStore(encodedKeyStore, testPassword, "PKCS12");
        
        // Set metadata
        creds.setDescription("Test KeyStore for Docker TLS");
        creds.setLastUpdatedByUser("testuser");
        
        // Verify metadata
        assertThat(creds.getDescription()).isEqualTo("Test KeyStore for Docker TLS");
        assertThat(creds.getLastUpdatedByUser()).isEqualTo("testuser");
        
        // Verify metadata in properties
        Properties metaProps = creds.getMetadataProperties("testcreds");
        assertThat(metaProps.getProperty("secure.credentials.testcreds.description"))
                    .isEqualTo("Test KeyStore for Docker TLS");
        assertThat(metaProps.getProperty("secure.credentials.testcreds.lastUpdated.user"))
                    .isEqualTo("testuser");
    }

    /**
     * Test that KeyStore with wrong password throws exception.
     */
    @Test
    public void testKeyStoreWithWrongPasswordThrowsException() throws Exception {
        CredentialsKeyStore creds = new CredentialsKeyStore(encodedKeyStore, "wrongPassword", "PKCS12");

        assertThatThrownBy(() -> creds.getKeyStore())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to load KeyStore");
    }

    /**
     * Create a simple test KeyStore with a secret key.
     * This avoids the complexity of certificate generation.
     */
    private KeyStore createSimpleTestKeyStore() throws Exception {
        // Create an empty PKCS12 KeyStore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        
        // Generate a simple AES secret key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();
        
        // Store the secret key in the KeyStore
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        KeyStore.ProtectionParameter protectionParam = 
            new KeyStore.PasswordProtection(testPassword.toCharArray());
        keyStore.setEntry("test-secret-key", secretKeyEntry, protectionParam);

        return keyStore;
    }

    private void saveProperties(Properties properties, File file) throws Exception {
        FileOutputStream out = new FileOutputStream(file);
        properties.store(out, null);
        out.close();
    }
}
