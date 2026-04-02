/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.SystemEnvironment;

/**
 * Detects the current operating system from system properties.
 */
public class OperatingSystemDetector {

    private final Environment env;

    /**
     * Creates a detector using the system environment.
     */
    public OperatingSystemDetector() {
        this(new SystemEnvironment());
    }

    /**
     * Creates a detector using the specified environment.
     * This constructor is primarily for testing purposes.
     *
     * @param environment the environment to use for OS detection
     */
    public OperatingSystemDetector(Environment environment) {
        this.env = environment;
    }

    /**
     * Detects the current operating system from the os.name system property.
     *
     * @return the detected operating system
     */
    public OperatingSystem detect() {
        OperatingSystem operatingSystem = OperatingSystem.UNKNOWN;
        String osName = env.getProperty("os.name");

        if (osName != null) {
            if (osName.startsWith("Mac")) {
                operatingSystem = OperatingSystem.MACOS;
            } else if (osName.startsWith("Win")) {
                operatingSystem = OperatingSystem.WINDOWS;
            } else if (osName.startsWith("Linux")) {
                operatingSystem = OperatingSystem.LINUX;
            }
        }
        return operatingSystem;
    }

    /**
     * Parses an operating system from a string value.
     *
     * @param value the string value (case-insensitive)
     * @return the parsed operating system, or UNKNOWN if not recognized
     */
    public OperatingSystem fromString(String value) {
        OperatingSystem operatingSystem = OperatingSystem.UNKNOWN;

        if (value != null && !value.trim().isEmpty()) {
            String normalized = value.trim().toLowerCase();
            switch (normalized) {
                case "macos":
                case "mac":
                case "darwin":
                    operatingSystem = OperatingSystem.MACOS;
                    break;
                case "windows":
                case "win":
                    operatingSystem = OperatingSystem.WINDOWS;
                    break;
                case "linux":
                    operatingSystem = OperatingSystem.LINUX;
                    break;
                case "auto":
                    operatingSystem = detect();
                    break;
                default:
                    operatingSystem = OperatingSystem.UNKNOWN;
            }
        }

        return operatingSystem;
    }
}
