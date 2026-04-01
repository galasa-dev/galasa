/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import java.util.HashMap;
import java.util.Map;

import dev.galasa.creds.os.internal.OsCredentialsException;

/**
 * Mock implementation of CommandExecutor for testing.
 */
public class MockCommandExecutor implements CommandExecutor {

    private final Map<String, KeychainItem> storage = new HashMap<>();
    private boolean shouldCancelAccess = false;
    private boolean shouldFailAuth = false;

    private String serviceName;

    @Override
    public CommandResult execute(String... command) throws OsCredentialsException {
        CommandResult result = null;
        if (shouldCancelAccess) {
            result = new CommandResult(SecurityCommand.SECURITY_CLI_ERROR_USER_CANCELLED_CODE, "");
        } else if (shouldFailAuth) {
            result = new CommandResult(-1, "");
        } else {
            result = handleFindPassword();
        }
        return result;
    }

    private CommandResult handleFindPassword() {
        CommandResult result = null;
        KeychainItem entry = storage.get(serviceName);

        if (entry == null) {
            result = new CommandResult(SecurityCommand.SECURITY_CLI_ERROR_ITEM_NOT_FOUND_CODE, "");
        } else {
            // Format output like the real security command
            StringBuilder output = new StringBuilder();
            output.append("keychain: \"login.keychain-db\"\n");
            output.append("class: \"genp\"\n");
            output.append("attributes:\n");
            output.append("    0x00000007 <blob>=\"").append(serviceName).append("\"\n");
            output.append("    \"acct\"<blob>=\"").append(entry.getAccountName()).append("\"\n");
            output.append("    \"svce\"<blob>=\"").append(serviceName).append("\"\n");
            output.append("password: \"").append(entry.getPassword()).append("\"\n");

            result = new CommandResult(SecurityCommand.SECURITY_CLI_SUCCESS_CODE, output.toString());
        }
        return result;
    }

    // Test helper methods

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void addPassword(String serviceName, String accountName, String password) {
        storage.put(serviceName, new KeychainItem(accountName, password));
    }

    public void removePassword(String serviceName) {
        storage.remove(serviceName);
    }

    public boolean hasPassword(String serviceName) {
        return storage.containsKey(serviceName);
    }

    public void setShouldCancelAccess(boolean shouldCancel) {
        this.shouldCancelAccess = shouldCancel;
    }

    public void setShouldFailAuth(boolean shouldFail) {
        this.shouldFailAuth = shouldFail;
    }

    public void clear() {
        storage.clear();
        serviceName = null;
        shouldCancelAccess = false;
        shouldFailAuth = false;
    }
}
