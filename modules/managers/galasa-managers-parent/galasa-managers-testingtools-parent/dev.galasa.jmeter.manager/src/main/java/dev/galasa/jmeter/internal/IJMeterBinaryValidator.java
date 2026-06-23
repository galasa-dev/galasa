/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.nio.file.Path;

import dev.galasa.jmeter.JMeterManagerException;

/**
 * Interface for validating JMeter binary paths.
 */
public interface IJMeterBinaryValidator {
    
    /**
     * Validate that the binary path exists and is a regular file
     *
     * @param binaryPath The path to validate
     * @throws JMeterManagerException if validation fails
     */
    void validateBinaryPath(Path binaryPath) throws JMeterManagerException;
    
    /**
     * Validate that the binary is executable
     *
     * @param binaryPath The path to validate
     * @throws JMeterManagerException if the file is not executable
     */
    void validateBinaryExecutable(Path binaryPath) throws JMeterManagerException;
    
    /**
     * Perform complete validation of the binary path
     * Calls both validateBinaryPath() and validateBinaryExecutable()
     *
     * @param binaryPath The path to validate
     * @throws JMeterManagerException if any validation fails
     */
    void validate(Path binaryPath) throws JMeterManagerException;
}
