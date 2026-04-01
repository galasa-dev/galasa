/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.parsers;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.creds.os.internal.OsCredentialsException;
import dev.galasa.framework.spi.creds.CredentialType;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsKeyStore;

/**
 * Parser for JSON-based KeyStore credentials.
 * 
 * <p>Expected JSON structure:
 * <pre>
 * {
 *   "keystore": "base64-encoded-keystore-content",
 *   "password": "keystore-password",
 *   "type": "JKS|PKCS12"
 * }
 * </pre>
 * 
 * <p>All three fields are required. The keystore content must be base64-encoded.
 */
public class KeyStoreJsonParser implements JsonCredentialParser {
    
    private static final String FIELD_KEYSTORE = "keystore";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_TYPE = "type";
    
    @Override
    public boolean canParse(JsonObject json) {
        return json.has(FIELD_KEYSTORE);
    }
    
    @Override
    public ICredentials parse(JsonObject json) throws CredentialsException {
        // Validate required fields
        if (!json.has(FIELD_PASSWORD)) {
            throw new OsCredentialsException(
                "JSON KeyStore credential missing '" + FIELD_PASSWORD + "' field");
        }
        
        // Extract values
        String keystoreContent = json.get(FIELD_KEYSTORE).getAsString();
        String keystorePassword = json.get(FIELD_PASSWORD).getAsString();

        // Determine keystore type, defaulting to PKCS12 if a type is not provided
        String keystoreType = CredentialsKeyStore.KEYSTORE_TYPE_PKCS12;
        if (json.has(FIELD_TYPE)) {
            keystoreType = json.get(FIELD_TYPE).getAsString();
        }
        
        // Create and return KeyStore credentials
        return new CredentialsKeyStore(keystoreContent, keystorePassword, keystoreType);
    }
    
    @Override
    public String getCredentialType() {
        return CredentialType.KEYSTORE.toString();
    }
}
