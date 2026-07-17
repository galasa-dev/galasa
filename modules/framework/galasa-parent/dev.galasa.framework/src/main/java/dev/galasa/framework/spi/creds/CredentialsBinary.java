/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;

import dev.galasa.ICredentialsBinary;

/**
 * Implementation of ICredentialsBinary that stores an opaque binary value
 * (e.g. a licence JAR file) in the Galasa Credentials Store.
 *
 * The binary data is always stored and transmitted as a base64-encoded string.
 */
public class CredentialsBinary extends AbstractCredentials implements ICredentialsBinary {

    private final byte[] data;
    private final String encodedData;

    /**
     * Constructor for plain-text creation (the value is already base64-encoded).
     *
     * @param base64Data the base64-encoded binary content
     * @throws CredentialsException if the provided string is not valid base64
     */
    public CredentialsBinary(String base64Data) throws CredentialsException {
        this.encodedData = base64Data;
        this.data = decodeAndValidate(base64Data);
    }

    /**
     * Constructor for credentials loaded from storage (file/etcd).
     *
     * When loaded from etcd the value may be encrypted; this constructor
     * decrypts it first then base64-decodes to obtain the raw bytes.
     *
     * @param key        the encryption key for decrypting etcd-stored credentials
     * @param base64Data the binary data (encrypted from etcd, or plain base64 from file)
     * @throws CredentialsException if decryption or base64 decoding fails
     */
    public CredentialsBinary(SecretKeySpec key, String base64Data) throws CredentialsException {
        super(key);

        String decrypted = decryptToString(base64Data);
        if (decrypted == null) {
            // Not encrypted – treat as plain base64
            this.encodedData = base64Data;
        } else {
            this.encodedData = decrypted;
        }

        this.data = decodeAndValidate(this.encodedData);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getData() {
        return this.data;
    }

    /** {@inheritDoc} */
    @Override
    public String getEncodedData() {
        return this.encodedData;
    }

    /**
     * {@inheritDoc}
     *
     * Stores the binary value base64-encoded under the property key
     * {@code secure.credentials.<credentialsId>.data}.
     */
    @Override
    public Properties toProperties(String credentialsId) {
        Properties props = new Properties();
        props.setProperty(CREDS_PROPERTY_PREFIX + credentialsId + ".data", this.encodedData);
        return props;
    }

    // -------------------------------------------------------------------------

    private byte[] decodeAndValidate(String value) throws CredentialsException {
        if (value == null || value.isBlank()) {
            throw new CredentialsException("Binary credential data cannot be null or empty");
        }
        try {
            return base64(value);
        } catch (IllegalArgumentException e) {
            throw new CredentialsException("Failed to base64-decode binary credential data", e);
        }
    }
}
