/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.SystemEnvironment;

/**
 * Enumeration of supported operating systems for OS-native credentials storage.
 */
public enum OperatingSystem {
    MACOS("macOS"),
    WINDOWS("Windows"),
    LINUX("Linux"),
    UNKNOWN("Unknown");

    private final String displayName;
    private static Environment environment = new SystemEnvironment();

    OperatingSystem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the environment to use for OS detection. This is primarily for testing purposes.
     *
     * @param env the environment to use
     */
    static void setEnvironment(Environment env) {
        environment = env;
    }

    /**
     * Detects the current operating system from system properties.
     *
     * @return the detected operating system
     */
    public static OperatingSystem detect() {
        String osName = environment.getProperty("os.name");
        if (osName == null) {
            return UNKNOWN;
        }

        if (osName.startsWith("Mac")) {
            return MACOS;
        } else if (osName.startsWith("Win")) {
            return WINDOWS;
        } else if (osName.startsWith("Linux")) {
            return LINUX;
        }

        return UNKNOWN;
    }

    /**
     * Parses an operating system from a string value.
     *
     * @param value the string value (case-insensitive)
     * @return the parsed operating system, or UNKNOWN if not recognized
     */
    public static OperatingSystem fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNKNOWN;
        }

        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "macos":
            case "mac":
            case "darwin":
                return MACOS;
            case "windows":
            case "win":
                return WINDOWS;
            case "linux":
                return LINUX;
            case "auto":
                return detect();
            default:
                return UNKNOWN;
        }
    }
}
