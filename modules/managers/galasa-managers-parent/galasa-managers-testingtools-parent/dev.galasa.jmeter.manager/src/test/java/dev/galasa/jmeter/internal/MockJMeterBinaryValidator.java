/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.nio.file.Path;

import dev.galasa.framework.IFileSystem;
import dev.galasa.jmeter.JMeterManagerException;

/**
 * Mock implementation of IJMeterBinaryValidator for testing.
 * This validator uses MockFileSystem to perform actual validation logic
 * without OS-specific dependencies like Files.isExecutable().
 */
public class MockJMeterBinaryValidator implements IJMeterBinaryValidator {

    private final IFileSystem fileSystem;

    public MockJMeterBinaryValidator(IFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    
    @Override
    public void validateBinaryPath(Path binaryPath) throws JMeterManagerException {
        if (!fileSystem.exists(binaryPath)) {
            throw new JMeterManagerException("JMeter binary not found at: " + binaryPath);
        }
        
        if (fileSystem.isDirectory(binaryPath)) {
            throw new JMeterManagerException(
                "JMeter binary path must point to the JMeter binary file, not a directory: " + binaryPath);
        }
    }
    
    @Override
    public void validateBinaryExecutable(Path binaryPath) throws JMeterManagerException {
        // Skip executable check in tests - this is OS-specific and cannot be mocked
        // In real usage, JMeterBinaryValidator uses Files.isExecutable()
    }
    
    @Override
    public void validate(Path binaryPath) throws JMeterManagerException {
        validateBinaryPath(binaryPath);
        validateBinaryExecutable(binaryPath);
    }
}
