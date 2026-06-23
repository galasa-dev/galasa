/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.nio.file.Files;
import java.nio.file.Path;

import dev.galasa.framework.IFileSystem;
import dev.galasa.jmeter.JMeterManagerException;

public class JMeterBinaryValidator implements IJMeterBinaryValidator {
    
    private final IFileSystem fileSystem;
    
    /**
     * Constructor with dependency injection
     *
     * @param fileSystem The filesystem abstraction to use for file operations
     */
    public JMeterBinaryValidator(IFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    
    @Override
    public void validateBinaryPath(Path binaryPath) throws JMeterManagerException {
        // Validate binary exists using IFileSystem
        if (!fileSystem.exists(binaryPath)) {
            throw new JMeterManagerException(
                "JMeter binary not found at: " + binaryPath +
                ". Please ensure jmeter.binary.path points to the JMeter binary file.\n" +
                "Examples:\n" +
                "  - Unix/Linux/Mac: /opt/apache-jmeter-5.6.3/bin/jmeter\n" +
                "  - Windows: C:\\apache-jmeter-5.6.3\\bin\\jmeter.bat"
            );
        }
        
        // Validate it's a file, not a directory using IFileSystem
        if (!fileSystem.isRegularFile(binaryPath)) {
            throw new JMeterManagerException(
                "jmeter.binary.path must point to the JMeter binary file, not a directory: " + binaryPath +
                "\nExamples:\n" +
                "  - Unix/Linux/Mac: /opt/apache-jmeter-5.6.3/bin/jmeter\n" +
                "  - Windows: C:\\apache-jmeter-5.6.3\\bin\\jmeter.bat"
            );
        }
    }
    
    @Override
    public void validateBinaryExecutable(Path binaryPath) throws JMeterManagerException {
        // Check executability - Files.isExecutable is OS-specific
        if (!Files.isExecutable(binaryPath)) {
            throw new JMeterManagerException(
                "JMeter binary is not executable: " + binaryPath +
                ". Please check file permissions."
            );
        }
    }
    
    @Override
    public void validate(Path binaryPath) throws JMeterManagerException {
        validateBinaryPath(binaryPath);
        validateBinaryExecutable(binaryPath);
    }
}
