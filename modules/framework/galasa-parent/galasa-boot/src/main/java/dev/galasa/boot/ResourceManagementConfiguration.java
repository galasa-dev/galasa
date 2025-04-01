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
    
    private String stream;
    private List<String> includesGlobPatterns = new ArrayList<>();
    private List<String> excludesGlobPatterns = new ArrayList<>();

    private static final String STREAM_ENV_VAR = "GALASA_MONITOR_STREAM";
    private static final String INCLUDES_ENV_VAR = "GALASA_MONITOR_INCLUDES_GLOB_PATTERNS";
    private static final String EXCLUDES_ENV_VAR = "GALASA_MONITOR_EXCLUDES_GLOB_PATTERNS";

    public ResourceManagementConfiguration(Environment env) {
        this.stream = env.getenv(STREAM_ENV_VAR);
        String commaSeparatedIncludes = env.getenv(INCLUDES_ENV_VAR);
        String commaSeparatedExcludes = env.getenv(EXCLUDES_ENV_VAR);

        if (commaSeparatedIncludes != null && !commaSeparatedIncludes.isBlank()) {
            this.includesGlobPatterns = Arrays.asList(commaSeparatedIncludes.split(","));
        }

        if (commaSeparatedExcludes != null && !commaSeparatedExcludes.isBlank()) {
            this.excludesGlobPatterns = Arrays.asList(commaSeparatedExcludes.split(","));
        }
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
