/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dev.galasa.creds.os.internal.OsCredentialsException;

/**
 * A class that provides functions to exceute macOS security command-line tool commands.
 */
public class SecurityCommand {

    public static final int SECURITY_CLI_SUCCESS_CODE = 0;
    public static final int SECURITY_CLI_ERROR_ITEM_NOT_FOUND_CODE = 44;
    public static final int SECURITY_CLI_ERROR_USER_CANCELLED_CODE = 128;

    private static final Pattern VALID_SERVICE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._@-]+$");

    private final CommandExecutor commandExecutor;

    public SecurityCommand() {
        this(new SystemCommandExecutor());
    }

    public SecurityCommand(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public CommandResult findGenericPassword(String serviceName) throws OsCredentialsException {
        validateServiceName(serviceName);

        List<String> commandParts = new ArrayList<>();
        commandParts.add("security");
        commandParts.add("find-generic-password");
        commandParts.add("-s");
        commandParts.add(serviceName);
        commandParts.add("-g");

        return commandExecutor.execute(commandParts.toArray(new String[0]));
    }

    /**
     * Validates that a service name contains only safe characters.
     * Allowed characters: alphanumeric, dot (.), underscore (_), hyphen (-), and at (@).
     *
     * @param serviceName the service name to validate
     * @throws OsCredentialsException if the service name is invalid
     */
    private void validateServiceName(String serviceName) throws OsCredentialsException {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new OsCredentialsException("Service name cannot be null or empty");
        }

        if (!VALID_SERVICE_NAME_PATTERN.matcher(serviceName).matches()) {
            throw new OsCredentialsException(
                "Credentials ID contains invalid characters. "+
                "Only alphanumeric characters, dots (.), underscores (_), hyphens (_), and at symbols (@) are allowed");
        }
    }
}
