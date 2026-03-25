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
 * Simulates the behavior of the macOS security command-line tool.
 */
public class MockCommandExecutor implements CommandExecutor {

    private final Map<String, KeychainEntry> storage = new HashMap<>();
    private boolean shouldCancelAccess = false;
    private boolean shouldFailAuth = false;

    private static class KeychainEntry {
        final String accountName;
        final String password;

        KeychainEntry(String accountName, String password) {
            this.accountName = accountName;
            this.password = password;
        }
    }

    @Override
    public CommandResult execute(String... command) throws OsCredentialsException {
        if (command.length < 2) {
            throw new OsCredentialsException("Invalid command");
        }

        String tool = command[0];
        String subcommand = command[1];

        if (!"security".equals(tool)) {
            throw new OsCredentialsException("Unknown command: " + tool);
        }

        if (shouldCancelAccess) {
            return new CommandResult(128, "");
        }

        if (shouldFailAuth) {
            return new CommandResult(-25293, "");
        }

        switch (subcommand) {
            case "find-generic-password":
                return handleFindPassword(command);
            case "delete-generic-password":
                return handleDeletePassword(command);
            default:
                throw new OsCredentialsException("Unknown subcommand: " + subcommand);
        }
    }

    private CommandResult handleFindPassword(String[] command) {
        // Parse the service name from the command
        String serviceName = null;
        for (int i = 0; i < command.length - 1; i++) {
            if ("-s".equals(command[i])) {
                serviceName = command[i + 1];
                break;
            }
        }

        if (serviceName == null) {
            return new CommandResult(1, "Error: service name not specified");
        }

        KeychainEntry entry = storage.get(serviceName);
        if (entry == null) {
            return new CommandResult(44, ""); // errSecItemNotFound
        }

        // Format output like the real security command
        StringBuilder output = new StringBuilder();
        output.append("keychain: \"/Users/test/Library/Keychains/login.keychain-db\"\n");
        output.append("class: \"genp\"\n");
        output.append("attributes:\n");
        output.append("    0x00000007 <blob>=\"").append(serviceName).append("\"\n");
        output.append("    \"acct\"<blob>=\"").append(entry.accountName).append("\"\n");
        output.append("    \"svce\"<blob>=\"").append(serviceName).append("\"\n");
        output.append("password: \"").append(entry.password).append("\"\n");

        return new CommandResult(0, output.toString());
    }

    private CommandResult handleDeletePassword(String[] command) {
        // Parse the service name from the command
        String serviceName = null;
        for (int i = 0; i < command.length - 1; i++) {
            if ("-s".equals(command[i])) {
                serviceName = command[i + 1];
                break;
            }
        }

        if (serviceName == null) {
            return new CommandResult(1, "Error: service name not specified");
        }

        KeychainEntry entry = storage.remove(serviceName);
        if (entry == null) {
            return new CommandResult(44, ""); // errSecItemNotFound
        }

        return new CommandResult(0, "");
    }

    // Test helper methods

    public void addPassword(String serviceName, String accountName, String password) {
        storage.put(serviceName, new KeychainEntry(accountName, password));
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
        shouldCancelAccess = false;
        shouldFailAuth = false;
    }
}
