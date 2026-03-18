/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import java.security.KeyStore;

/**
 * Represents credentials stored as a Java KeyStore containing certificates
 * and private keys for client authentication.
 *
 * This credential type is used to store SSL/TLS certificates that can be
 * used for authenticating to services that require client certificates, such
 * as Docker engines with TLS protection.
 *
 * The KeyStore can contain:
 *   Client certificate and private key for authentication
 *   CA certificates for validating server certificates
 *   Multiple certificate chains if needed
 *
 * Supported KeyStore types:
 *   PKCS12 - Industry standard format (recommended)
 *   JKS - Java KeyStore format (legacy)
 * 
 *
 * The KeyStore data must be base64 encoded when stored in the credentials store.
 */
public interface ICredentialsKeyStore extends ICredentials {
    
    /**
     * Get the KeyStore containing certificates and keys.
     *
     * The KeyStore is loaded from the stored bytes using the configured
     * password. The KeyStore can then be used to configure SSL contexts
     * for secure connections.
     *
     * @return KeyStore object containing certificates and keys
     */
    KeyStore getKeyStore();
    
    /**
     * Get the password for the KeyStore.
     * 
     * This password is used to unlock the KeyStore and access the
     * private keys within it. The password should be kept secure and
     * is typically encrypted when stored.
     * 
     * @return KeyStore password as a String
     */
    String getKeyStorePassword();
    
    /**
     * Get the type of KeyStore.
     *
     * The KeyStore type determines the format and capabilities of the
     * KeyStore. Supported types are:
     *   PKCS12 - Industry standard format (recommended)
     *   JKS - Java KeyStore format (legacy)
     * 
     * @return KeyStore type identifier ("PKCS12" or "JKS")
     */
    String getKeyStoreType();

    /**
     * Get the base64-encoded KeyStore data.
     *
     * Returns the KeyStore bytes in base64-encoded format with the "base64:" prefix.
     * This is the format used for storing and transmitting KeyStore data in the
     * credentials store and via the Secrets API.
     *
     * When credentials are stored in etcd, the value may be encrypted. This method
     * handles decryption automatically and returns the decrypted "base64:..." value.
     * For file-based credentials, it returns the value as stored.
     *
     * @return The base64-encoded KeyStore data with "base64:" prefix
     */
    String getEncodedKeyStore();
}
