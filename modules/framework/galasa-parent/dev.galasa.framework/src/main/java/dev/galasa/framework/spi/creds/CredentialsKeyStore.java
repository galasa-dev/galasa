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
    
    private KeyStore keyStore;
    private byte[] keyStoreBytes;
    private String keyStoreString;
    private String keyStorePassword;
    private String keyStoreType;
    
    /**
     * Constructor for plain-text KeyStore (programmatic creation/testing).
     *
     * This constructor is used when creating credentials programmatically
     * or for testing. The KeyStore data must be base64 encoded with "base64:" prefix.
     *
     * @param keyStore The base64-encoded KeyStore bytes (must start with "base64:")
     * @param keyStorePassword The password for the KeyStore
     * @param keyStoreType The type of KeyStore ("PKCS12" or "JKS", defaults to "PKCS12" if null)
     * @throws CredentialsException if decoding fails or format is invalid
     * @throws IllegalArgumentException if keyStoreType is not "PKCS12" or "JKS"
     */
    public CredentialsKeyStore(String keyStore, String keyStorePassword, String keyStoreType)
            throws CredentialsException {
        this.keyStoreString = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.keyStoreType = keyStoreType;
        this.keyStoreBytes = decode(keyStore);
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
     *   - The decrypted KeyStore data must have "base64:" prefix
     *
     * For file storage:
     *   - Values are stored with prefixes like "base64:" or as plain text
     *   - The decode() method handles these formats
     *
     * @param key The encryption key for decrypting etcd-stored credentials
     * @param keyStore The KeyStore data (encrypted from etcd, or "base64:..." from file)
     * @param keyStorePassword The password (encrypted from etcd, or plain text from file)
     * @param keyStoreType The KeyStore type (encrypted from etcd, or plain text from file)
     * @throws CredentialsException if decryption/decoding fails or format is invalid
     * @throws IllegalArgumentException if keyStoreType is not "PKCS12" or "JKS"
     */
    public CredentialsKeyStore(SecretKeySpec key, String keyStore, String keyStorePassword, String keyStoreType)
            throws CredentialsException {
        super(key);

        this.keyStoreString = decryptToString(keyStore);
        this.keyStorePassword = decryptToString(keyStorePassword);
        this.keyStoreType = decryptToString(keyStoreType);

        // This should get the keystore with a prefix of "base64:" followed by the base64-encoded KeyStore bytes
        if (this.keyStoreString == null) {
            // We want to keep the keyStore base64 encoded so don't call decode here
            this.keyStoreString = keyStore;
        }
        if (!this.keyStoreString.startsWith("base64:")) {
            throw new CredentialsException("KeyStore data must be base64 encoded with 'base64:' prefix");
        }
        // We then decode to get the keyStore bytes
        this.keyStoreBytes = decode(keyStore);
        

        if (this.keyStorePassword == null) {
            this.keyStorePassword = new String(decode(keyStorePassword), StandardCharsets.UTF_8);
        }
        

        if (this.keyStoreType == null) {
            if (keyStoreType != null) {
                this.keyStoreType = new String(decode(keyStoreType), StandardCharsets.UTF_8);
            }
        }
        // Validate the KeyStore type or default to PKCS12
        if (this.keyStoreType == null) {
            this.keyStoreType = "PKCS12";
        } else if ("PKCS12".equalsIgnoreCase(this.keyStoreType) || "JKS".equalsIgnoreCase(this.keyStoreType)) {
            this.keyStoreType = this.keyStoreType.toUpperCase();
        } else {
            throw new IllegalArgumentException("Unsupported KeyStore type: " + keyStoreType +
                ". Only PKCS12 and JKS are supported.");
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * The KeyStore is lazily loaded on first access and cached for
     * subsequent calls. If the KeyStore cannot be loaded, a RuntimeException
     * is thrown with details of the failure.
     */
    @Override
    public KeyStore getKeyStore() {
        if (this.keyStore == null) {
            try {
                this.keyStore = KeyStore.getInstance(this.keyStoreType);
                this.keyStore.load(new ByteArrayInputStream(this.keyStoreBytes), this.keyStorePassword.toCharArray());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load KeyStore of type " 
                                            + this.keyStoreType + ": " + e.getMessage(), e);
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
     * Returns the KeyStore data in base64-encoded format with "base64:" prefix.
     * The keyStoreString field already contains the decrypted value (if it was
     * encrypted in etcd) or the original value (if from file storage).
     */
    public String getEncodedKeyStore() {
        return this.keyStoreString;
    }

    /**
     * Convert this KeyStore credential to Properties format for storage.
     *
     * The KeyStore bytes are base64 encoded with "base64:" prefix for storage.
     * The password is stored as plain text.
     *
     * @param credentialsId The ID to use as a prefix for property keys
     * @return Properties object containing the KeyStore data
     */
    @Override
    public Properties toProperties(String credentialsId) {
        String keyPrefix = CREDS_PROPERTY_PREFIX + credentialsId;
        Properties credsProperties = new Properties();
        
        // Encode KeyStore bytes as base64 with prefix
        String encodedKeyStore = "base64:" + Base64.getEncoder().encodeToString(this.keyStoreBytes);
        credsProperties.setProperty(keyPrefix + ".keystore", encodedKeyStore);

        credsProperties.setProperty(keyPrefix + ".password", this.keyStorePassword);
        credsProperties.setProperty(keyPrefix + ".type", this.keyStoreType);
        
        return credsProperties;
    }
}
