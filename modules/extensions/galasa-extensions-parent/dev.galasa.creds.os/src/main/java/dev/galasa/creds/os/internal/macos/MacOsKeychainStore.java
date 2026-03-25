/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.jna.ptr.PointerByReference;

import dev.galasa.ICredentials;
import dev.galasa.creds.os.internal.OsCredentialsException;
import dev.galasa.framework.spi.creds.CredentialsException;
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

    private final SecurityFramework security;
    private final CommandExecutor commandExecutor;

    /**
     * Internal class to hold keychain item data.
     */
    private static class KeychainItem {
        final String accountName;
        final String password;

        KeychainItem(String accountName, String password) {
            this.accountName = accountName;
            this.password = password;
        }
    }

    /**
     * Enumeration of supported credential types.
     */
    private enum CredentialType {
        USERNAME_PASSWORD,
        USERNAME,
        TOKEN,
        USERNAME_TOKEN
    }

    public MacOsKeychainStore() {
        this(SecurityFramework.INSTANCE, new SystemCommandExecutor());
    }

    // Package-private constructor for testing
    MacOsKeychainStore(SecurityFramework security, CommandExecutor commandExecutor) {
        this.security = security;
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
        CredentialType type = detectCredentialType(item.accountName, item.password);
        
        switch (type) {
            case TOKEN:
                return new CredentialsToken(item.password);
            
            case USERNAME:
                String username = extractUsername(item.accountName);
                return new CredentialsUsername(username);
            
            case USERNAME_TOKEN:
                String usernameForToken = extractUsername(item.accountName);
                return new CredentialsUsernameToken(usernameForToken, item.password);
            
            case USERNAME_PASSWORD:
            default:
                return new CredentialsUsernamePassword(item.accountName, item.password);
        }
    }

    @Override
    public Map<String, ICredentials> getAllCredentials() throws CredentialsException {
        throw new OsCredentialsException("Method not implemented for Mac OS Keychain");
    }

    @Override
    public void setCredentials(String credsId, ICredentials credentials) throws CredentialsException {
        if (credsId == null || credsId.trim().isEmpty()) {
            throw new OsCredentialsException("Credentials ID cannot be null or empty");
        }
        if (credentials == null) {
            throw new OsCredentialsException("Credentials cannot be null");
        }

        String serviceName = SERVICE_PREFIX + credsId;
        String accountName;
        String password;

        // Format account name and password based on credential type
        if (credentials instanceof CredentialsUsernamePassword) {
            CredentialsUsernamePassword creds = (CredentialsUsernamePassword) credentials;
            accountName = creds.getUsername();
            password = creds.getPassword();
        } else if (credentials instanceof CredentialsUsernameToken) {
            CredentialsUsernameToken creds = (CredentialsUsernameToken) credentials;
            accountName = USERNAME_TOKEN_PREFIX + creds.getUsername();
            password = new String(creds.getToken(), StandardCharsets.UTF_8);
        } else if (credentials instanceof CredentialsUsername) {
            CredentialsUsername creds = (CredentialsUsername) credentials;
            accountName = USERNAME_PREFIX + creds.getUsername();
            password = ""; // No password for username-only credentials
        } else if (credentials instanceof CredentialsToken) {
            accountName = TOKEN_ACCOUNT;
            CredentialsToken creds = (CredentialsToken) credentials;
            password = new String(creds.getToken(), StandardCharsets.UTF_8);
        } else {
            throw new OsCredentialsException("Unsupported credentials type: " + credentials.getClass().getName());
        }

        // Delete any existing credential with this service name first
        tryDeleteKeychainItemByService(serviceName);
        
        // Add the new credential
        addPasswordToKeychain(serviceName, accountName, password);
    }

    @Override
    public void deleteCredentials(String credsId) throws CredentialsException {
        if (credsId == null || credsId.trim().isEmpty()) {
            throw new OsCredentialsException("Credentials ID cannot be null or empty");
        }

        String serviceName = SERVICE_PREFIX + credsId;

        boolean deleted = tryDeleteKeychainItemByService(serviceName);
        if (!deleted) {
            throw new OsCredentialsException("Credentials not found: " + credsId);
        }
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
        
        return CredentialType.USERNAME_PASSWORD;
    }

    /**
     * Extracts the username from an account name that has a prefix.
     *
     * @param accountName the account name (e.g., "username:myuser" or "username-token:myuser")
     * @return the extracted username (e.g., "myuser")
     */
    private String extractUsername(String accountName) {
        if (accountName.startsWith(USERNAME_TOKEN_PREFIX)) {
            return accountName.substring(USERNAME_TOKEN_PREFIX.length());
        }
        if (accountName.startsWith(USERNAME_PREFIX)) {
            return accountName.substring(USERNAME_PREFIX.length());
        }
        return accountName;
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

    private void addPasswordToKeychain(String serviceName, String accountName, String password) throws CredentialsException {
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        PointerByReference itemRef = new PointerByReference();

        int status = security.SecKeychainAddGenericPassword(
            null,
            serviceName.length(),
            serviceName,
            accountName.length(),
            accountName,
            passwordBytes.length,
            passwordBytes,
            itemRef
        );

        if (status == SecurityFramework.errSecDuplicateItem) {
            throw new OsCredentialsException("Credentials already exist. Delete them first before adding new ones.");
        }

        if (status == SecurityFramework.errSecUserCanceled) {
            throw new OsCredentialsException("User cancelled keychain access");
        }

        if (status == SecurityFramework.errSecAuthFailed) {
            throw new OsCredentialsException("Authorization failed for keychain access");
        }

        if (status != SecurityFramework.errSecSuccess) {
            throw new OsCredentialsException("Failed to add credentials to keychain. Status code: " + status);
        }
    }

    /**
     * Tries to delete a keychain item by service name using the security command-line tool.
     *
     * @param serviceName the service name
     * @return true if deleted, false if not found
     * @throws CredentialsException if there's an error deleting the item
     */
    private boolean tryDeleteKeychainItemByService(String serviceName) throws CredentialsException {
        // Use the security command-line tool to delete the password
        CommandExecutor.CommandResult result = commandExecutor.execute(
            "security",
            "delete-generic-password",
            "-s", serviceName
        );
        
        int exitCode = result.getExitCode();
        
        if (exitCode == 44) {
            // Item not found (errSecItemNotFound)
            return false;
        }

        if (exitCode == 128) {
            // User cancelled
            throw new OsCredentialsException("User cancelled keychain access");
        }

        if (exitCode != 0) {
            throw new OsCredentialsException("Failed to delete credentials from keychain. Exit code: " + exitCode);
        }

        return true;
    }
}
