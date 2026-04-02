/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

/**
 * Enumeration of supported operating systems for OS-native credentials storage.
 */
public enum OperatingSystem {
    MACOS("macOS"),
    WINDOWS("Windows"),
    LINUX("Linux"),
    UNKNOWN("Unknown");

    private final String displayName;

    private OperatingSystem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
