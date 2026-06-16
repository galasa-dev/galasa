/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

/**
 * Constants used throughout the JMeter Manager implementation
 */
public final class JMeterConstants {
    
    // File extensions
    public static final String JMX_EXTENSION = ".jmx";
    public static final String JTL_EXTENSION = ".jtl";
    public static final String LOG_EXTENSION = ".log";
    // Timeout values
    public static final int DEFAULT_TIMEOUT_SECONDS = 60;
    
    
    // Directory names
    public static final String JMETER_DIRECTORY = "jmeter";
    
    // RAS file names
    public static final String RAS_RESULTS_FILE = "results.jtl";
    public static final String RAS_LOG_FILE = "jmeter.log";
    public static final String RAS_JMX_FILE = "test-plan.jmx";
    
    // Velocity template name
    public static final String VELOCITY_TEMPLATE_NAME = "VelocityRenderer";
    
    // Private constructor to prevent instantiation
    private JMeterConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
