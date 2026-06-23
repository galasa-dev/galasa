/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.jmeter.JMeterManagerException;

/**
 * Unit tests for JMeterBinaryValidator (production code).
 * Tests use MockFileSystem to avoid OS dependencies.
 * Note: validateBinaryExecutable() uses Files.isExecutable() which cannot be mocked,
 * so those tests verify the method doesn't throw exceptions with mock filesystem.
 */
public class JMeterBinaryValidatorTest {

    private MockFileSystem mockFileSystem;
    private JMeterBinaryValidator validator;

    @Before
    public void setup() {
        mockFileSystem = new MockFileSystem();
        validator = new JMeterBinaryValidator(mockFileSystem);
    }

    @Test
    public void testValidateBinaryPathThrowsExceptionWhenBinaryDoesNotExist() throws Exception {
        // Given...
        String nonExistentPath = "/opt/does/not/exist";
        Path binaryPath = mockFileSystem.getPath(nonExistentPath);
        // Don't create the file in mockFileSystem
        
        // When...
        JMeterManagerException thrown = catchThrowableOfType(() -> {
            validator.validateBinaryPath(binaryPath);
        }, JMeterManagerException.class);
        
        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage())
            .contains("JMeter binary not found")
            .contains(nonExistentPath);
    }

    @Test
    public void testValidateBinaryPathThrowsExceptionWhenBinaryPathIsDirectory() throws Exception {
        // Given...
        String directoryPath = "/opt/jmeter/bin";
        Path binPath = mockFileSystem.getPath(directoryPath);
        mockFileSystem.createDirectories(binPath);
        
        // When...
        JMeterManagerException thrown = catchThrowableOfType(() -> {
            validator.validateBinaryPath(binPath);
        }, JMeterManagerException.class);
        
        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage())
            .contains("must point to the JMeter binary file, not a directory")
            .contains(directoryPath);
    }

    @Test
    public void testValidateBinaryPathSucceedsWithValidFile() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        Path jmeterPath = mockFileSystem.getPath(binaryPath);
        mockFileSystem.createDirectories(jmeterPath.getParent());
        mockFileSystem.createFile(jmeterPath);
        
        // When
        validator.validateBinaryPath(jmeterPath);

        // Then...
        // Should not throw exception
    }

    @Test
    public void testValidateBinaryExecutableSucceedsWithExistingFile() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        Path jmeterPath = mockFileSystem.getPath(binaryPath);
        mockFileSystem.createDirectories(jmeterPath.getParent());
        mockFileSystem.createFile(jmeterPath);
        
        // When...
        // MockFileSystemProvider.checkAccess() now returns success for existing files
        // This allows Files.isExecutable() to work in tests
        validator.validateBinaryExecutable(jmeterPath);
        
        // Then...
        // Should not throw exception - file exists and is considered executable
    }

    @Test
    public void testValidateBinaryExecutableThrowsWhenFileDoesNotExist() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/nonexistent";
        Path jmeterPath = mockFileSystem.getPath(binaryPath);
        
        // When...
        JMeterManagerException thrown = catchThrowableOfType(() -> {
            validator.validateBinaryExecutable(jmeterPath);
        }, JMeterManagerException.class);
        
        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("not executable");
    }

    @Test
    public void testValidateThrowsExceptionWhenBinaryDoesNotExist() throws Exception {
        // Given...
        String nonExistentPath = "/opt/does/not/exist";
        Path binaryPath = mockFileSystem.getPath(nonExistentPath);
        
        // When...
        JMeterManagerException thrown = catchThrowableOfType(() -> {
            validator.validate(binaryPath);
        }, JMeterManagerException.class);
        
        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("JMeter binary not found");
    }

    @Test
    public void testValidateThrowsExceptionWhenBinaryIsDirectory() throws Exception {
        // Given...
        String directoryPath = "/opt/jmeter/bin";
        Path binPath = mockFileSystem.getPath(directoryPath);
        mockFileSystem.createDirectories(binPath);
        
        // When...
        JMeterManagerException thrown = catchThrowableOfType(() -> {
            validator.validate(binPath);
        }, JMeterManagerException.class);
        
        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("must point to the JMeter binary file, not a directory");
    }

    @Test
    public void testValidateSucceedsWithValidBinaryFile() throws Exception {
        // Given...
        String binaryPath = "/opt/jmeter/bin/jmeter";
        Path jmeterPath = mockFileSystem.getPath(binaryPath);
        mockFileSystem.createDirectories(jmeterPath.getParent());
        mockFileSystem.createFile(jmeterPath);
        
        // When...
        validator.validate(jmeterPath);

        // Then...
        // Should not throw exception
    }

    @Test
    public void testValidateHandlesWindowsPath() throws Exception {
        // Given...
        String binaryPath = "C:\\apache-jmeter\\bin\\jmeter.bat";
        Path jmeterPath = mockFileSystem.getPath(binaryPath);
        mockFileSystem.createDirectories(jmeterPath.getParent());
        mockFileSystem.createFile(jmeterPath);
        
        // When...
        validator.validate(jmeterPath);

        // Then...
        // Should not throw exception
    }
}
