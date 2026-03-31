/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.parsers;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.creds.CredentialsException;

/**
 * Interface for parsing JSON-based credentials from macOS Keychain.
 * 
 * <p>Each implementation handles a specific credential type (e.g., KeyStore, Certificate, etc.)
 * and is responsible for:
 * <ul>
 *   <li>Detecting if a JSON object matches its credential type</li>
 *   <li>Validating the JSON structure</li>
 *   <li>Parsing the JSON and creating the appropriate ICredentials implementation</li>
 * </ul>
 * 
 * <p>This design allows new credential types to be added without modifying
 * the core parsing logic in MacOsKeychainStore.
 */
public interface JsonCredentialParser {
    
    /**
     * Checks if this parser can handle the given JSON structure.
     * 
     * @param json the JSON object to check
     * @return true if this parser can handle the JSON structure, false otherwise
     */
    boolean canParse(JsonObject json);
    
    /**
     * Parses the JSON object and creates the appropriate ICredentials implementation.
     * 
     * @param json the JSON object containing credential data
     * @return the parsed credentials
     * @throws CredentialsException if the JSON structure is invalid or parsing fails
     */
    ICredentials parse(JsonObject json) throws CredentialsException;
    
    /**
     * Gets a description of the credential type this parser handles.
     * Used for error messages and documentation.
     * 
     * @return a description of the credential type (e.g., "KeyStore", "Certificate")
     */
    String getCredentialType();
}
