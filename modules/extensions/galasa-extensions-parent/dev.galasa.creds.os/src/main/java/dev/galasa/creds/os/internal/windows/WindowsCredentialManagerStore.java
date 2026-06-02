/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import dev.galasa.ICredentials;
import dev.galasa.creds.os.internal.OsCredentialsException;
import dev.galasa.creds.os.internal.parsers.JsonCredentialParser;
import dev.galasa.creds.os.internal.parsers.KeyStoreJsonParser;
import dev.galasa.framework.spi.creds.CredentialType;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.creds.ICredentialsStore;

/**
 * Windows Credential Manager implementation of the credentials store using JNA
 * to access the Windows Credential Manager API.
 *
 * <p>This store supports the following credential types, determined by the username format:
 * <ul>
 *   <li><b>CredentialsUsernamePassword</b>: Username = plain username, Password = password</li>
 *   <li><b>CredentialsUsername</b>: Username = "username:{username}", Password = empty</li>
 *   <li><b>CredentialsToken</b>: Username = "token", Password = token value</li>
 *   <li><b>CredentialsUsernameToken</b>: Username = "username-token:{username}", Password = token value</li>
 *   <li><b>JSON-based credentials</b>: Username = "JSON", Password = JSON object with credential properties</li>
 * </ul>
 *
 * <p><b>Creating credentials manually in Windows Credential Manager:</b>
 * <ol>
 *   <li>Open Control Panel → User Accounts → Credential Manager</li>
 *   <li>Click "Add a generic credential"</li>
 *   <li>Set "Internet or network address" to: galasa.credentials.{CREDENTIALS-ID}</li>
 *   <li>Set "User name" and "Password" according to the credential type</li>
 * </ol>
 *
 * <p>The implementation automatically detects the credential type when retrieving
 * credentials based on the username format and password presence.
 */
public class WindowsCredentialManagerStore implements ICredentialsStore {

    private static final String TARGET_PREFIX = "galasa.credentials.";
    private static final String USERNAME_PREFIX = "username:";
    private static final String USERNAME_TOKEN_PREFIX = "username-token:";
    private static final String TOKEN_USERNAME = "token";
    private static final String JSON_USERNAME = "JSON";

    private final List<JsonCredentialParser> jsonParsers;
    private final ICredentialManager credentialManager;

    /**
     * Creates a new Windows Credential Manager store with the default command executor.
     */
    public WindowsCredentialManagerStore() {
        this(new WindowsCredentialManager());
    }

    /**
     * Creates a new Windows Credential Manager store with a custom command executor.
     *
     * @param credentialManager the credential manager command executor
     */
    public WindowsCredentialManagerStore(ICredentialManager credentialManager) {
        this.credentialManager = credentialManager;
        this.jsonParsers = initializeJsonParsers();
    }

    /**
     * Initializes the list of JSON credential parsers.
     * New parser implementations should be added here.
     *
     * @return list of JSON credential parsers
     */
    private List<JsonCredentialParser> initializeJsonParsers() {
        List<JsonCredentialParser> parsers = new ArrayList<>();
        parsers.add(new KeyStoreJsonParser());
        return parsers;
    }

    @Override
    public ICredentials getCredentials(String credsId) throws CredentialsException {
        if (credsId == null || credsId.trim().isEmpty()) {
            throw new OsCredentialsException("Credentials ID cannot be null or empty");
        }

        String targetName = TARGET_PREFIX + credsId;
        ICredentials credentialsToReturn = null;

        CredentialItem item = credentialManager.readCredential(targetName);
        if (item != null) {
            String username = item.getUsername();
            String password = item.getPassword();

            if (JSON_USERNAME.equalsIgnoreCase(username)) {
                credentialsToReturn = parseJsonCredentials(password);
            } else {
                CredentialType type = detectCredentialType(username, password);

                switch (type) {
                    case TOKEN:
                        credentialsToReturn = new CredentialsToken(password);
                        break;

                    case USERNAME:
                        String extractedUsername = extractUsername(username);
                        credentialsToReturn = new CredentialsUsername(extractedUsername);
                        break;

                    case USERNAME_TOKEN:
                        String usernameForToken = extractUsername(username);
                        credentialsToReturn = new CredentialsUsernameToken(usernameForToken, password);
                        break;

                    case USERNAME_PASSWORD:
                    default:
                        credentialsToReturn = new CredentialsUsernamePassword(username, password);
                        break;
                }
            }
        }

        return credentialsToReturn;
    }

    @Override
    public Map<String, ICredentials> getAllCredentials() throws CredentialsException {
        throw new OsCredentialsException("Getting all credentials is not enabled for OS credentials stores");
    }

    @Override
    public void setCredentials(String credsId, ICredentials credentials) throws CredentialsException {
        throw new OsCredentialsException("Setting credentials is not enabled for OS credentials stores");
    }

    @Override
    public void deleteCredentials(String credsId) throws CredentialsException {
        throw new OsCredentialsException("Deleting credentials is not enabled for OS credentials stores");
    }

    @Override
    public void shutdown() throws CredentialsException {
        // No resources to clean up
    }

    /**
     * Detects the credential type based on the username format and password presence.
     *
     * @param username the username from the credential item
     * @param password the password from the credential item
     * @return the detected credential type
     */
    private CredentialType detectCredentialType(String username, String password) {
        CredentialType type = CredentialType.USERNAME_PASSWORD;

        if (TOKEN_USERNAME.equalsIgnoreCase(username)) {
            type = CredentialType.TOKEN;
        } else if (username.startsWith(USERNAME_TOKEN_PREFIX)) {
            type = CredentialType.USERNAME_TOKEN;
        } else if (username.startsWith(USERNAME_PREFIX)) {
            type = CredentialType.USERNAME;
        }

        return type;
    }

    /**
     * Extracts the username from a username field that has a prefix.
     *
     * @param username the username field (e.g., "username:myuser" or "username-token:myuser")
     * @return the extracted username (e.g., "myuser")
     */
    private String extractUsername(String username) {
        String extractedUsername = username;
        if (username.startsWith(USERNAME_TOKEN_PREFIX)) {
            extractedUsername = username.substring(USERNAME_TOKEN_PREFIX.length());
        } else if (username.startsWith(USERNAME_PREFIX)) {
            extractedUsername = username.substring(USERNAME_PREFIX.length());
        }
        return extractedUsername;
    }

    /**
     * Parses JSON-based credentials from the password field using registered parsers.
     *
     * <p>This method iterates through all registered JSON credential parsers
     * and uses the first one that can handle the JSON structure.
     *
     * <p>To add support for new JSON credential types:
     * <ol>
     *   <li>Create a new class implementing {@link JsonCredentialParser} interface</li>
     *   <li>Add an instance of your parser to the {@link #initializeJsonParsers()} method</li>
     * </ol>
     *
     * @param jsonPassword the JSON string containing credential data
     * @return the parsed credentials object
     * @throws CredentialsException if JSON parsing fails or no parser can handle the structure
     */
    private ICredentials parseJsonCredentials(String jsonPassword) throws CredentialsException {
        if (jsonPassword == null || jsonPassword.trim().isEmpty()) {
            throw new OsCredentialsException("JSON credentials cannot be empty");
        }

        try {
            JsonObject json = JsonParser.parseString(jsonPassword).getAsJsonObject();

            for (JsonCredentialParser parser : jsonParsers) {
                if (parser.canParse(json)) {
                    return parser.parse(json);
                }
            }

            List<String> supportedTypes = jsonParsers.stream()
                .map(parser -> parser.getCredentialType())
                .collect(Collectors.toList());

            throw new OsCredentialsException(
                "Unknown JSON credential structure. Supported types: " + String.join(", ", supportedTypes)
            );

        } catch (JsonSyntaxException e) {
            throw new OsCredentialsException("Invalid JSON in credentials: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new OsCredentialsException("Invalid JSON structure in credentials: " + e.getMessage(), e);
        }
    }
}
