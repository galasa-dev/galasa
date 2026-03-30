/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import java.util.Map;

import dev.galasa.ICredentials;
import dev.galasa.creds.os.internal.OsCredentialsException;
import dev.galasa.framework.spi.creds.CredentialType;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsKeyStore;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.creds.ICredentialsStore;

/**
 * macOS Keychain implementation of the credentials store using JNA to access
 * the Security Framework.
 *
 * <p>This store supports four credential types, determined by the account name format:
 * <ul>
 *   <li><b>CredentialsUsernamePassword</b>: Account = username, Password = password</li>
 *   <li><b>CredentialsUsername</b>: Account = "username:{username}", Password = empty</li>
 *   <li><b>CredentialsToken</b>: Account = "token", Password = token value</li>
 *   <li><b>CredentialsUsernameToken</b>: Account = "username-token:{username}", Password = token value</li>
 * </ul>
 *
 * <p><b>Creating credentials manually in Keychain Access.app:</b>
 * <ol>
 *   <li>Open Keychain Access.app</li>
 *   <li>Click the "+" button to add a new password item</li>
 *   <li>Set "Where" (Service Name) to: galasa.credentials.{CREDENTIALS-ID}</li>
 *   <li>Set "Account Name" and "Password" according to the credential type:</li>
 * </ol>
 *
 * <p><b>Examples:</b>
 * <table border="1">
 *   <tr>
 *     <th>Credential Type</th>
 *     <th>Account Name</th>
 *     <th>Password</th>
 *   </tr>
 *   <tr>
 *     <td>Username + Password</td>
 *     <td>myuser</td>
 *     <td>mypassword</td>
 *   </tr>
 *   <tr>
 *     <td>Username only</td>
 *     <td>username:myuser</td>
 *     <td>(leave empty)</td>
 *   </tr>
 *   <tr>
 *     <td>Token only</td>
 *     <td>token</td>
 *     <td>abc123token</td>
 *   </tr>
 *   <tr>
 *     <td>Username + Token</td>
 *     <td>username-token:myuser</td>
 *     <td>abc123token</td>
 *   </tr>
 * </table>
 *
 * <p>The implementation automatically detects the credential type when retrieving
 * credentials based on the account name format and password presence.
 */
public class MacOsKeychainStore implements ICredentialsStore {

    private static final String SERVICE_PREFIX = "galasa.credentials.";
    private static final String USERNAME_PREFIX = "username:";
    private static final String USERNAME_TOKEN_PREFIX = "username-token:";
    private static final String TOKEN_ACCOUNT = "token";
    private static final String KEYSTORE_PREFIX = "keystore:";

    private final CommandExecutor commandExecutor;

    public MacOsKeychainStore() {
        this(new SystemCommandExecutor());
    }

    public MacOsKeychainStore(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public ICredentials getCredentials(String credsId) throws CredentialsException {
        if (credsId == null || credsId.trim().isEmpty()) {
            throw new OsCredentialsException("Credentials ID cannot be null or empty");
        }

        String serviceName = SERVICE_PREFIX + credsId;

        // Try to get the keychain item by service name only
        KeychainItem item = getKeychainItem(serviceName);
        if (item == null) {
            return null;
        }

        // Detect credential type and return appropriate implementation
        String accountName = item.getAccountName();
        String password = item.getPassword();
        CredentialType type = detectCredentialType(accountName, password);


        ICredentials credentialsToReturn = null;
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

            case KEYSTORE:
                credentialsToReturn = extractKeyStore(accountName, password);
                break;

            case USERNAME_PASSWORD:
            default:
                credentialsToReturn = new CredentialsUsernamePassword(accountName, password);
                break;
        }
        return credentialsToReturn;
    }

    @Override
    public Map<String, ICredentials> getAllCredentials() throws CredentialsException {
        throw new OsCredentialsException("Method not implemented for Mac OS Keychain");
    }

    @Override
    public void setCredentials(String credsId, ICredentials credentials) throws CredentialsException {
        throw new OsCredentialsException("Method not implemented for Mac OS Keychain");
    }

    @Override
    public void deleteCredentials(String credsId) throws CredentialsException {
        throw new OsCredentialsException("Method not implemented for Mac OS Keychain");
    }

    /**
     * Detects the credential type based on the account name format and password presence.
     *
     * @param accountName the account name from the keychain item
     * @param password the password from the keychain item
     * @return the detected credential type
     */
    private CredentialType detectCredentialType(String accountName, String password) {
        if (TOKEN_ACCOUNT.equals(accountName)) {
            return CredentialType.TOKEN;
        }

        // Check for username-token BEFORE username to avoid false matches
        if (accountName.startsWith(USERNAME_TOKEN_PREFIX)) {
            return CredentialType.USERNAME_TOKEN;
        }

        if (accountName.startsWith(USERNAME_PREFIX)) {
            return CredentialType.USERNAME;
        }

        if (accountName.startsWith(KEYSTORE_PREFIX)) {
            return CredentialType.KEYSTORE;
        }

        return CredentialType.USERNAME_PASSWORD;
    }

    /**
     * Extracts a keystore from an account name that has a prefix and password.
     *
     * @param accountName the account name (e.g., "keystore:jks:myuser")
     * @return the extracted keystore (e.g., "myuser")
     */
    private ICredentials extractKeyStore(String accountName, String password) throws CredentialsException {
        CredentialsKeyStore keystore = null;

        String keystoreWithType = accountName.substring(KEYSTORE_PREFIX.length());
        String[] parts = keystoreWithType.split(":");
        if (parts.length == 2) {
            String type = parts[0];
            String keystoreContent = parts[1];
            keystore = new CredentialsKeyStore(keystoreContent, password, type);
        } else {
            throw new CredentialsException("Invalid Keystore credential provided. Keystore item must be in the format 'keystore:type:content'");
        }
        return keystore;
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

        if (exitCode == 44) {
            // Item not found (errSecItemNotFound)
            return null;
        }

        if (exitCode == 128) {
            // User cancelled
            throw new OsCredentialsException("User cancelled keychain access");
        }

        if (exitCode != 0) {
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
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.contains("\"acct\"")) {
                // Extract the value after the equals sign
                int equalsIndex = line.indexOf('=');
                if (equalsIndex != -1) {
                    String value = line.substring(equalsIndex + 1).trim();
                    // Remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        return value.substring(1, value.length() - 1);
                    }
                    // Handle hex blob format: 0x... "value"
                    int quoteStart = value.indexOf('"');
                    int quoteEnd = value.lastIndexOf('"');
                    if (quoteStart != -1 && quoteEnd != -1 && quoteStart < quoteEnd) {
                        return value.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }
        }
        return null;
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
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.startsWith("password:")) {
                // Extract the value after the colon
                String value = line.substring("password:".length()).trim();
                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    return value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
        return null;
    }
}
