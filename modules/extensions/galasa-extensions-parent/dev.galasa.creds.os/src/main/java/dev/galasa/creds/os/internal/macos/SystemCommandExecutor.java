/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import dev.galasa.creds.os.internal.OsCredentialsException;

/**
 * Real implementation of CommandExecutor that executes system commands.
 */
public class SystemCommandExecutor implements CommandExecutor {

    @Override
    public CommandResult execute(String... command) throws OsCredentialsException {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            return new CommandResult(exitCode, output.toString());

        } catch (IOException e) {
            throw new OsCredentialsException("Failed to execute command: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OsCredentialsException("Interrupted while executing command", e);
        }
    }
}
