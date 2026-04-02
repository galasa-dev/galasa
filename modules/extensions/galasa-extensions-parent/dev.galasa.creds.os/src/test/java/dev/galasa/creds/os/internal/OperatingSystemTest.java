/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import dev.galasa.extensions.common.mocks.MockEnvironment;

/**
 * Unit tests for the OperatingSystemDetector class.
 */
public class OperatingSystemTest {

    @Test
    public void testFromStringMacOS() {
        OperatingSystemDetector detector = new OperatingSystemDetector();
        assertThat(detector.fromString("macOS")).as("macOS should be recognized").isEqualTo(OperatingSystem.MACOS);
        assertThat(detector.fromString("macos")).as("macos (lowercase) should be recognized").isEqualTo(OperatingSystem.MACOS);
        assertThat(detector.fromString("Mac")).as("Mac should be recognized").isEqualTo(OperatingSystem.MACOS);
        assertThat(detector.fromString("darwin")).as("darwin should be recognized").isEqualTo(OperatingSystem.MACOS);
    }

    @Test
    public void testFromStringWindows() {
        OperatingSystemDetector detector = new OperatingSystemDetector();
        assertThat(detector.fromString("Windows")).as("Windows should be recognized").isEqualTo(OperatingSystem.WINDOWS);
        assertThat(detector.fromString("windows")).as("windows (lowercase) should be recognized").isEqualTo(OperatingSystem.WINDOWS);
        assertThat(detector.fromString("Win")).as("Win should be recognized").isEqualTo(OperatingSystem.WINDOWS);
    }

    @Test
    public void testFromStringLinux() {
        OperatingSystemDetector detector = new OperatingSystemDetector();
        assertThat(detector.fromString("Linux")).as("Linux should be recognized").isEqualTo(OperatingSystem.LINUX);
        assertThat(detector.fromString("linux")).as("linux (lowercase) should be recognized").isEqualTo(OperatingSystem.LINUX);
    }

    @Test
    public void testFromStringAuto() {
        OperatingSystemDetector detector = new OperatingSystemDetector();
        OperatingSystem detected = detector.fromString("auto");
        assertThat(detected).as("auto should detect an OS").isNotEqualTo(OperatingSystem.UNKNOWN);
    }

    @Test
    public void testFromStringUnknown() {
        OperatingSystemDetector detector = new OperatingSystemDetector();
        assertThat(detector.fromString("invalid")).as("invalid should return UNKNOWN").isEqualTo(OperatingSystem.UNKNOWN);
        assertThat(detector.fromString("")).as("empty string should return UNKNOWN").isEqualTo(OperatingSystem.UNKNOWN);
        assertThat(detector.fromString(null)).as("null should return UNKNOWN").isEqualTo(OperatingSystem.UNKNOWN);
    }

    @Test
    public void testDetectMacOS() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("os.name", "Mac OS X");
        OperatingSystemDetector detector = new OperatingSystemDetector(mockEnv);

        // When
        OperatingSystem detected = detector.detect();

        // Then
        assertThat(detected).as("Should detect macOS").isEqualTo(OperatingSystem.MACOS);
    }

    @Test
    public void testDetectWindows() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("os.name", "Windows 10");
        OperatingSystemDetector detector = new OperatingSystemDetector(mockEnv);

        // When
        OperatingSystem detected = detector.detect();

        // Then
        assertThat(detected).as("Should detect Windows").isEqualTo(OperatingSystem.WINDOWS);
    }

    @Test
    public void testDetectLinux() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("os.name", "Linux");
        OperatingSystemDetector detector = new OperatingSystemDetector(mockEnv);

        // When
        OperatingSystem detected = detector.detect();

        // Then
        assertThat(detected).as("Should detect Linux").isEqualTo(OperatingSystem.LINUX);
    }

    @Test
    public void testDetectUnknown() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("os.name", "FreeBSD");
        OperatingSystemDetector detector = new OperatingSystemDetector(mockEnv);

        // When
        OperatingSystem detected = detector.detect();

        // Then
        assertThat(detected).as("Should return UNKNOWN for unsupported OS").isEqualTo(OperatingSystem.UNKNOWN);
    }

    @Test
    public void testDetectNullOsName() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        // Don't set os.name property
        OperatingSystemDetector detector = new OperatingSystemDetector(mockEnv);

        // When
        OperatingSystem detected = detector.detect();

        // Then
        assertThat(detected).as("Should return UNKNOWN when os.name is null").isEqualTo(OperatingSystem.UNKNOWN);
    }

    @Test
    public void testGetDisplayName() {
        assertThat(OperatingSystem.MACOS.getDisplayName()).as("macOS display name").isEqualTo("macOS");
        assertThat(OperatingSystem.WINDOWS.getDisplayName()).as("Windows display name").isEqualTo("Windows");
        assertThat(OperatingSystem.LINUX.getDisplayName()).as("Linux display name").isEqualTo("Linux");
        assertThat(OperatingSystem.UNKNOWN.getDisplayName()).as("Unknown display name").isEqualTo("Unknown");
    }
}
