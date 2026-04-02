/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsKeyStore;

/**
 * Implementation of ICredentialsKeyStore that stores a Java KeyStore
 * containing certificates and private keys for client authentication.
 *
 * This class handles KeyStore credentials stored in base64 encoding.
 * The KeyStore bytes must be base64 encoded when stored in the credentials store.
 *
 * Supported KeyStore types:
 *   PKCS12 - Industry standard format (recommended)
 *   JKS - Java KeyStore format (legacy)
 */
public class CredentialsKeyStore extends AbstractCredentials implements ICredentialsKeyStore {

    public static final String KEYSTORE_TYPE_JKS = "JKS";
    public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";
    public static final List<String> SUPPORTED_KEYSTORE_TYPES = List.of(
        KEYSTORE_TYPE_PKCS12, KEYSTORE_TYPE_JKS
    );
    
    private KeyStore keyStore;
    private byte[] keyStoreBytes;
    private String keyStoreString;
    private String keyStorePassword;
    private String keyStoreType;
    
    /**
     * Constructor for plain-text KeyStore (programmatic creation/testing).
     *
     * This constructor is used when creating credentials programmatically
     * or for testing. The KeyStore data must be base64 encoded (without "base64:" prefix).
     *
     * @param keyStore The base64-encoded KeyStore bytes (without "base64:" prefix)
     * @param keyStorePassword The password for the KeyStore
     * @param keyStoreType The type of KeyStore ("PKCS12" or "JKS", defaults to "PKCS12" if null)
     * @throws CredentialsException if decoding fails or format is invalid
     * @throws IllegalArgumentException if keyStoreType is not "PKCS12" or "JKS"
     */
    public CredentialsKeyStore(String keyStore, String keyStorePassword, String keyStoreType)
            throws CredentialsException {
        this.keyStoreString = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.keyStoreType = normalizeAndValidateKeyStoreType(keyStoreType);
        this.keyStoreBytes = decodeBase64(keyStore);
        validateKeyStoreCanLoad();
    }

    /**
     * Constructor for KeyStore credentials loaded from storage (file/etcd).
     *
     * This constructor is used when loading credentials from the Credentials Store.
     * It handles both encrypted (from etcd) and encoded (from file) KeyStore data.
     *
     * For etcd storage:
     *   - The KeyStore, password, and type values are encrypted
     *   - This constructor decrypts them using the provided encryption key
     *   - The decrypted KeyStore data is base64 encoded (without "base64:" prefix)
     *
     * For file storage:
     *   - KeyStore value is base64 encoded
     *   - Password and type are plain text
     *
     * @param key The encryption key for decrypting etcd-stored credentials
     * @param keyStore The KeyStore data (encrypted from etcd, or base64 from file)
     * @param keyStorePassword The password (encrypted from etcd, or plain text from file)
     * @param keyStoreType The KeyStore type (encrypted from etcd, or plain text from file)
     * @throws CredentialsException if decryption/decoding fails or format is invalid
     * @throws IllegalArgumentException if keyStoreType is not "PKCS12" or "JKS"
     */
    public CredentialsKeyStore(SecretKeySpec key, String keyStore, String keyStorePassword, String keyStoreType)
            throws CredentialsException {
        super(key);

        // Decrypt if encrypted (from etcd), otherwise use as-is (from file)
        this.keyStoreString = decryptToString(keyStore);
        this.keyStorePassword = decryptToString(keyStorePassword);
        this.keyStoreType = decryptToString(keyStoreType);

        // If decryption returned null, use the original value
        if (this.keyStoreString == null) {
            this.keyStoreString = keyStore;
        }
        
        // Decode the base64 KeyStore data to get actual KeyStore bytes
        this.keyStoreBytes = decodeBase64(this.keyStoreString);

        // Decode password if it was encrypted
        if (this.keyStorePassword == null) {
            this.keyStorePassword = new String(decode(keyStorePassword), StandardCharsets.UTF_8);
        }

        // Decode type if it was encrypted
        if (this.keyStoreType == null) {
            if (keyStoreType != null) {
                this.keyStoreType = new String(decode(keyStoreType), StandardCharsets.UTF_8);
            }
        }

        // Validate and normalize the KeyStore type
        this.keyStoreType = normalizeAndValidateKeyStoreType(this.keyStoreType);
        
        // Validate that the KeyStore can be loaded
        validateKeyStoreCanLoad();
    }
    
    /**
     * {@inheritDoc}
     *
     * The KeyStore is lazily loaded on first access and cached for
     * subsequent calls. If the KeyStore cannot be loaded, a RuntimeException
     * is thrown with details of the failure.
     */
    @Override
    public KeyStore getKeyStore() throws CredentialsException {
        if (this.keyStore == null) {
            try {
                this.keyStore = KeyStore.getInstance(this.keyStoreType);
                this.keyStore.load(new ByteArrayInputStream(this.keyStoreBytes), this.keyStorePassword.toCharArray());
            } catch (Exception e) {
                throw new CredentialsException("Failed to load KeyStore of type " + this.keyStoreType);
            }
        }
        return this.keyStore;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getKeyStorePassword() {
        return this.keyStorePassword;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getKeyStoreType() {
        return this.keyStoreType;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the KeyStore data in base64-encoded format.
     * The keyStoreString field already contains the decrypted value (if it was
     * encrypted in etcd) or the original value (if from file storage).
     */
    public String getEncodedKeyStore() {
        return this.keyStoreString;
    }

    /**
     * Convert this KeyStore credential to Properties format for storage.
     *
     * The KeyStore bytes are base64 encoded for storage.
     * The password is stored as plain text.
     *
     * @param credentialsId The ID to use as a prefix for property keys
     * @return Properties object containing the KeyStore data
     */
    @Override
    public Properties toProperties(String credentialsId) {
        String keyPrefix = CREDS_PROPERTY_PREFIX + credentialsId;
        Properties credsProperties = new Properties();
        
        // Encode KeyStore bytes as base64
        String encodedKeyStore = Base64.getEncoder().encodeToString(this.keyStoreBytes);
        credsProperties.setProperty(keyPrefix + ".keystore", encodedKeyStore);

        credsProperties.setProperty(keyPrefix + ".password", this.keyStorePassword);
        credsProperties.setProperty(keyPrefix + ".type", this.keyStoreType);
        
        return credsProperties;
    }

    /**
     * Decodes a base64-encoded string to bytes.
     *
     * @param base64String The base64-encoded string
     * @return The decoded bytes
     * @throws CredentialsException if decoding fails
     */
    private byte[] decodeBase64(String base64String) throws CredentialsException {
        if (base64String == null) {
            throw new CredentialsException("KeyStore data cannot be null");
        }
        
        try {
            return Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            throw new CredentialsException("Failed to decode base64 KeyStore data", e);
        }
    }

    /**
     * Validates and normalizes the KeyStore type.
     *
     * @param type The KeyStore type to validate
     * @return The normalized KeyStore type (uppercase)
     * @throws IllegalArgumentException if the type is not supported
     */
    private String normalizeAndValidateKeyStoreType(String type) {
        String validatedType;
        
        if (type == null) {
            validatedType = KEYSTORE_TYPE_PKCS12;
        } else {
            String upperType = type.toUpperCase();
            if (KEYSTORE_TYPE_PKCS12.equals(upperType) || KEYSTORE_TYPE_JKS.equals(upperType)) {
                validatedType = upperType;
            } else {
                throw new IllegalArgumentException("Unsupported KeyStore type: " + type +
                    ". Supported types are: " + String.join(", ", SUPPORTED_KEYSTORE_TYPES));
            }
        }
        
        return validatedType;
    }

    /**
     * Validates that the KeyStore can be loaded with the provided password.
     * This provides eager validation to catch errors early.
     *
     * @throws CredentialsException if the KeyStore cannot be loaded
     */
    private void validateKeyStoreCanLoad() throws CredentialsException {
        try {
            KeyStore testKeyStore = KeyStore.getInstance(this.keyStoreType);
            testKeyStore.load(new ByteArrayInputStream(this.keyStoreBytes), this.keyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new CredentialsException("Failed to load KeyStore of type " + this.keyStoreType +
                ". Please verify the KeyStore data and password are correct.", e);
        }
    }
}
