/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

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
 * macOS Keychain implementation of the credentials store using the <code>security</code> CLI tool
 * to access the keychain.
 *
 * <p>This store supports the following credential types, determined by the account name format:
 * <ul>
 *   <li><b>CredentialsUsernamePassword</b>: Account = username, Password = password</li>
 *   <li><b>CredentialsUsername</b>: Account = "username:{username}", Password = empty</li>
 *   <li><b>CredentialsToken</b>: Account = "token", Password = token value</li>
 *   <li><b>CredentialsUsernameToken</b>: Account = "username-token:{username}", Password = token value</li>
 *   <li><b>JSON-based credentials</b>: Account = "JSON", Password = JSON object with credential properties</li>
 * </ul>
 *
 * <p><b>Creating credentials manually in Keychain Access.app:</b>
 * <ol>
 *   <li>Open Keychain Access.app</li>
 *   <li>Click the "+" button to add a new password item</li>
 *   <li>Set "Where" (Service Name) to: galasa.credentials.{CREDENTIALS-ID}</li>
 *   <li>Set "Account Name" and "Password" according to the credential type</li>
 * </ol>
 *
 * <p>The implementation automatically detects the credential type when retrieving
 * credentials based on the account name format and password presence.
 */
public class MacOsKeychainStore implements ICredentialsStore {

    private static final String SERVICE_PREFIX = "galasa.credentials.";
    private static final String USERNAME_PREFIX = "username:";
    private static final String USERNAME_TOKEN_PREFIX = "username-token:";
    private static final String TOKEN_ACCOUNT = "token";
    private static final String JSON_ACCOUNT = "JSON";
    
    private static final int SECURITY_CLI_SUCCESS_CODE = 0;
    private static final int SECURITY_CLI_ERROR_ITEM_NOT_FOUND_CODE = 44;
    private static final int SECURITY_CLI_ERROR_USER_CANCELLED_CODE = 128;
    
    private static final String SECURITY_ACCOUNT_NAME_FIELD = "\"acct\"";
    private static final String SECURITY_PASSWORD_FIELD_PREFIX = "password:";


    private final CommandExecutor commandExecutor;
    private final List<JsonCredentialParser> jsonParsers;

    public MacOsKeychainStore() {
        this(new SystemCommandExecutor());
    }

    public MacOsKeychainStore(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
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

        String serviceName = SERVICE_PREFIX + credsId;
        ICredentials credentialsToReturn = null;

        // Try to get the keychain item by service name only
        KeychainItem item = getKeychainItem(serviceName);
        if (item != null) {
            // Detect credential type and return appropriate implementation
            String accountName = item.getAccountName();
            String password = item.getPassword();

            // Check for JSON-based credentials first
            if (JSON_ACCOUNT.equalsIgnoreCase(accountName)) {
                credentialsToReturn = parseJsonCredentials(password);
            } else {
                CredentialType type = detectCredentialType(accountName, password);

                switch (type) {
                    case TOKEN:
                        credentialsToReturn = new CredentialsToken(password);
                        break;

                    case USERNAME:
                        String username = extractUsername(accountName);
                        credentialsToReturn = new CredentialsUsername(username);
                        break;

                    case USERNAME_TOKEN:
                        String usernameForToken = extractUsername(accountName);
                        credentialsToReturn = new CredentialsUsernameToken(usernameForToken, password);
                        break;

                    case USERNAME_PASSWORD:
                    default:
                        credentialsToReturn = new CredentialsUsernamePassword(accountName, password);
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

    /**
     * Detects the credential type based on the account name format and password presence.
     *
     * @param accountName the account name from the keychain item
     * @param password the password from the keychain item
     * @return the detected credential type
     */
    private CredentialType detectCredentialType(String accountName, String password) {
        CredentialType type = CredentialType.USERNAME_PASSWORD;

        if (TOKEN_ACCOUNT.equalsIgnoreCase(accountName)) {
            type = CredentialType.TOKEN;
        } else if (accountName.startsWith(USERNAME_TOKEN_PREFIX)) {
            type = CredentialType.USERNAME_TOKEN;
        } else if (accountName.startsWith(USERNAME_PREFIX)) {
            type = CredentialType.USERNAME;
        }

        return type;
    }

    /**
     * Extracts the username from an account name that has a prefix.
     *
     * @param accountName the account name (e.g., "username:myuser" or "username-token:myuser")
     * @return the extracted username (e.g., "myuser")
     */
    private String extractUsername(String accountName) {
        String username = accountName;
        if (accountName.startsWith(USERNAME_TOKEN_PREFIX)) {
            username = accountName.substring(USERNAME_TOKEN_PREFIX.length());
        } else if (accountName.startsWith(USERNAME_PREFIX)) {
            username = accountName.substring(USERNAME_PREFIX.length());
        }
        return username;
    }

    /**
     * Parses JSON-based credentials from the password field using registered parsers.
     *
     * <p>This method iterates through all registered JSON credential parsers
     * and uses the first one that can handle the JSON structure.
     *
     * <p>To add support for new JSON credential types:
     * <ol>
     *   <li>Create a new class implementing {@link JsonCredentialParser}</li>
     *   <li>Add an instance to the {@link #initializeJsonParsers()} method</li>
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

            // Try each registered parser
            for (JsonCredentialParser parser : jsonParsers) {
                if (parser.canParse(json)) {
                    return parser.parse(json);
                }
            }

            // No parser could handle this JSON structure
            List<String> supportedTypes = jsonParsers.stream()
                .map(parser -> parser.getCredentialType())
                .collect(Collectors.toList());

            throw new OsCredentialsException(
                "Unknown JSON credential structure. Supported types: " + String.join(", ", supportedTypes));

        } catch (JsonSyntaxException e) {
            throw new OsCredentialsException("Invalid JSON in credentials: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new OsCredentialsException("Invalid JSON structure in credentials: " + e.getMessage(), e);
        }
    }

    public void shutdown() throws CredentialsException {
        // No resources to clean up
    }

    /**
     * Retrieves a keychain item by service name only using the security command-line tool.
     * Searches for the first item matching the service name and returns both
     * the account name and password.
     *
     * @param serviceName the service name to search for
     * @return KeychainItem containing account name and password, or null if not found
     * @throws CredentialsException if there's an error accessing the keychain
     */
    private KeychainItem getKeychainItem(String serviceName) throws CredentialsException {
        // Use the security command-line tool to find the password
        // The -g flag outputs the password to stderr
        CommandExecutor.CommandResult result = commandExecutor.execute(
            "security",
            "find-generic-password",
            "-s", serviceName,
            "-g"
        );

        int exitCode = result.getExitCode();

        if (exitCode == SECURITY_CLI_ERROR_ITEM_NOT_FOUND_CODE) {
            return null;
        }

        if (exitCode == SECURITY_CLI_ERROR_USER_CANCELLED_CODE) {
            throw new OsCredentialsException("User cancelled keychain access");
        }

        if (exitCode != SECURITY_CLI_SUCCESS_CODE) {
            throw new OsCredentialsException("Failed to retrieve credentials from keychain. Exit code: " + exitCode);
        }

        // Parse the output to extract account name and password
        String output = result.getOutput();
        String accountName = extractAccountName(output);
        String password = extractPassword(output);

        if (accountName == null) {
            throw new OsCredentialsException("Failed to extract account name from keychain output");
        }

        return new KeychainItem(accountName, password != null ? password : "");
    }

    /**
     * Extracts the account name from security command output.
     * Example line: "acct"<blob>="username"
     *
     * @param output the command output
     * @return the account name, or null if not found
     */
    private String extractAccountName(String output) {
        // Look for the account attribute line
        String accountName = null;
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.contains(SECURITY_ACCOUNT_NAME_FIELD)) {

                // Extract the value after the equals sign
                int equalsIndex = line.indexOf('=');
                if (equalsIndex != -1) {
                    String value = line.substring(equalsIndex + 1).trim();

                    // Remove quotes if present
                    accountName = stripQuotes(value);
                    if (accountName != null) {
                        break;
                    }

                    // Handle hex blob format: 0x... "value"
                    int quoteStart = value.indexOf('"');
                    int quoteEnd = value.lastIndexOf('"');
                    if (quoteStart != -1 && quoteEnd != -1 && quoteStart < quoteEnd) {
                        accountName = value.substring(quoteStart + 1, quoteEnd);
                        break;
                    }
                }
            }
        }
        return accountName;
    }

    /**
     * Extracts the password from security command output.
     * Example line: password: "mypassword"
     *
     * @param output the command output
     * @return the password, or null if not found
     */
    private String extractPassword(String output) {
        // Look for the password line
        String password = null;
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.startsWith(SECURITY_PASSWORD_FIELD_PREFIX)) {

                // Extract the value after the colon
                String value = line.substring(SECURITY_PASSWORD_FIELD_PREFIX.length()).trim();

                // Remove quotes if present
                password = stripQuotes(value);
                if (password != null) {
                    break;
                }
            }
        }
        return password;
    }

    private String stripQuotes(String value) {
        String strippedValue = value.trim();
        if (strippedValue.startsWith("\"") && strippedValue.endsWith("\"")) {
            strippedValue = strippedValue.substring(1, strippedValue.length() - 1);
        }
        return strippedValue;
    }
}
