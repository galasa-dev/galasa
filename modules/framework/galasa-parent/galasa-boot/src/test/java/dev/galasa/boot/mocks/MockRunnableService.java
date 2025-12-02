/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.mocks;

import java.util.List;
import java.util.Properties;

/**
 * This is a class containing the "run" methods used by the FelixFramework to start various services
 * like the API server and k8s controller.
 */
public class MockRunnableService {
    
    // run method used to invoke the API server startup
    public void run(Properties bootstrapProperties, Properties overrideProperties, List<String> extraBundles) {}

    // run method used to invoke resource management
    public void run(
        Properties bootstrapProperties,
        Properties overrideProperties,
        String stream,
        List<String> includesGlobPatterns,
        List<String> excludesGlobPatterns
    ) {}
}
