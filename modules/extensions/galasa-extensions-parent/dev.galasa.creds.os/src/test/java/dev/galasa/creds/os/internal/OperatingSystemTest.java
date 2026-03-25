/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Test;

import dev.galasa.extensions.common.mocks.MockEnvironment;
import dev.galasa.framework.spi.SystemEnvironment;

/**
 * Unit tests for the OperatingSystem enum.
 */
public class OperatingSystemTest {

    @After
    public void tearDown() {
        // Reset to real environment after each test
        OperatingSystem.setEnvironment(new SystemEnvironment());
    }

    @Test
    public void testFromStringMacOS() {
        assertThat(OperatingSystem.fromString("macOS")).as("macOS should be recognized").isEqualTo(OperatingSystem.MACOS);
        assertThat(OperatingSystem.fromString("macos")).as("macos (lowercase) should be recognized").isEqualTo(OperatingSystem.MACOS);
        assertThat(OperatingSystem.fromString("Mac")).as("Mac should be recognized").isEqualTo(OperatingSystem.MACOS);
        assertThat(OperatingSystem.fromString("darwin")).as("darwin should be recognized").isEqualTo(OperatingSystem.MACOS);
    }

    @Test
    public void testFromStringWindows() {
        assertThat(OperatingSystem.fromString("Windows")).as("Windows should be recognized").isEqualTo(OperatingSystem.WINDOWS);
        assertThat(OperatingSystem.fromString("windows")).as("windows (lowercase) should be recognized").isEqualTo(OperatingSystem.WINDOWS);
        assertThat(OperatingSystem.fromString("Win")).as("Win should be recognized").isEqualTo(OperatingSystem.WINDOWS);
    }

    @Test
    public void testFromStringLinux() {
        assertThat(OperatingSystem.fromString("Linux")).as("Linux should be recognized").isEqualTo(OperatingSystem.LINUX);
        assertThat(OperatingSystem.fromString("linux")).as("linux (lowercase) should be recognized").isEqualTo(OperatingSystem.LINUX);
    }

    @Test
    public void testFromStringAuto() {
        OperatingSystem detected = OperatingSystem.fromString("auto");
        assertThat(detected).as("auto should detect an OS").isNotEqualTo(OperatingSystem.UNKNOWN);
    }

    @Test
    public void testFromStringUnknown() {
        assertThat(OperatingSystem.fromString("invalid")).as("invalid should return UNKNOWN").isEqualTo(OperatingSystem.UNKNOWN);
        assertThat(OperatingSystem.fromString("")).as("empty string should return UNKNOWN").isEqualTo(OperatingSystem.UNKNOWN);
        assertThat(OperatingSystem.fromString(null)).as("null should return UNKNOWN").isEqualTo(OperatingSystem.UNKNOWN);
    }

    @Test
    public void testDetectMacOS() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("os.name", "Mac OS X");
        OperatingSystem.setEnvironment(mockEnv);

        // When
        OperatingSystem detected = OperatingSystem.detect();

        // Then
        assertThat(detected).as("Should detect macOS").isEqualTo(OperatingSystem.MACOS);
    }

    @Test
    public void testDetectWindows() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("os.name", "Windows 10");
        OperatingSystem.setEnvironment(mockEnv);

        // When
        OperatingSystem detected = OperatingSystem.detect();

        // Then
        assertThat(detected).as("Should detect Windows").isEqualTo(OperatingSystem.WINDOWS);
    }

    @Test
    public void testDetectLinux() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("os.name", "Linux");
        OperatingSystem.setEnvironment(mockEnv);

        // When
        OperatingSystem detected = OperatingSystem.detect();

        // Then
        assertThat(detected).as("Should detect Linux").isEqualTo(OperatingSystem.LINUX);
    }

    @Test
    public void testDetectUnknown() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("os.name", "FreeBSD");
        OperatingSystem.setEnvironment(mockEnv);

        // When
        OperatingSystem detected = OperatingSystem.detect();

        // Then
        assertThat(detected).as("Should return UNKNOWN for unsupported OS").isEqualTo(OperatingSystem.UNKNOWN);
    }

    @Test
    public void testDetectNullOsName() {
        // Given
        MockEnvironment mockEnv = new MockEnvironment();
        // Don't set os.name property
        OperatingSystem.setEnvironment(mockEnv);

        // When
        OperatingSystem detected = OperatingSystem.detect();

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
