/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceManagementConfiguration {
    public static final String STREAM_ENV_VAR = "GALASA_MONITOR_STREAM";
    public static final String INCLUDES_ENV_VAR = "GALASA_MONITOR_INCLUDES_GLOB_PATTERNS";
    public static final String EXCLUDES_ENV_VAR = "GALASA_MONITOR_EXCLUDES_GLOB_PATTERNS";
    
    private String stream;
    private List<String> includesGlobPatterns = new ArrayList<>();
    private List<String> excludesGlobPatterns = new ArrayList<>();


    public ResourceManagementConfiguration(
        List<String> includesGlobPatterns,
        List<String> excludesGlobPatterns,
        Environment env
    ) {
        this.stream = env.getenv(STREAM_ENV_VAR);
        this.includesGlobPatterns = initialiseGlobPatternsList(includesGlobPatterns, env, INCLUDES_ENV_VAR);
        this.excludesGlobPatterns = initialiseGlobPatternsList(excludesGlobPatterns, env, EXCLUDES_ENV_VAR);
    }

    private List<String> initialiseGlobPatternsList(List<String> possiblySuppliedPatterns, Environment env, String envVar) {
        List<String> globPatterns = new ArrayList<>();

        if (possiblySuppliedPatterns == null || possiblySuppliedPatterns.isEmpty()) {
            String commaSeparatedPatterns = env.getenv(envVar);

            if (commaSeparatedPatterns != null && !commaSeparatedPatterns.isBlank()) {
                globPatterns = Arrays.asList(commaSeparatedPatterns.split(","));
            }
        } else {
            globPatterns = possiblySuppliedPatterns;
        }
        return globPatterns;
    }

    public String getStream() {
        return stream;
    }

    public List<String> getIncludesGlobPatterns() {
        return includesGlobPatterns;
    }

    public List<String> getExcludesGlobPatterns() {
        return excludesGlobPatterns;
    }
}
