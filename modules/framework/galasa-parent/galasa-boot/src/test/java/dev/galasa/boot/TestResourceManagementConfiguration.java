/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;

import dev.galasa.boot.mocks.MockEnvironment;

public class TestResourceManagementConfiguration {

    @Test
    public void testCanLoadConfigFromEnvironmentVariables() throws Exception {
        // Given...
        List<String> includesList = List.of(
            "dev.galasa.*",
            "my.other.manager.*"
        );

        List<String> excludesList = List.of(
            "dev.galasa.core.*",
            "a.different.manager.provider"
        );

        String stream = "mystream";

        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setenv(ResourceManagementConfiguration.INCLUDES_ENV_VAR, String.join(",", includesList));
        mockEnvironment.setenv(ResourceManagementConfiguration.EXCLUDES_ENV_VAR, String.join(",", excludesList));
        mockEnvironment.setenv(ResourceManagementConfiguration.STREAM_ENV_VAR, stream);

        // When...
        ResourceManagementConfiguration config = new ResourceManagementConfiguration(null, null, mockEnvironment);

        // Then...
        assertThat(config.getStream()).isEqualTo(stream);
        assertThat(config.getIncludesGlobPatterns()).isEqualTo(includesList);
        assertThat(config.getExcludesGlobPatterns()).isEqualTo(excludesList);
    }

    @Test
    public void testCanLoadConfigFromParameters() throws Exception {
        // Given...
        List<String> includesList = List.of(
            "dev.galasa.*",
            "my.other.manager.*"
        );

        List<String> excludesList = List.of(
            "dev.galasa.core.*",
            "a.different.manager.provider"
        );

        MockEnvironment mockEnvironment = new MockEnvironment();

        // When...
        ResourceManagementConfiguration config = new ResourceManagementConfiguration(includesList, excludesList, mockEnvironment);

        // Then...
        assertThat(config.getIncludesGlobPatterns()).isEqualTo(includesList);
        assertThat(config.getExcludesGlobPatterns()).isEqualTo(excludesList);
    }

    @Test
    public void testConfigFromParametersOverridesEnvironmentVariables() throws Exception {
        // Given...
        String envIncludesList = "one,two,three";
        String envExcludesList = "four,five,six";

        List<String> includesList = List.of(
            "dev.galasa.*",
            "my.other.manager.*"
        );

        List<String> excludesList = List.of(
            "dev.galasa.core.*",
            "a.different.manager.provider"
        );

        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setenv(ResourceManagementConfiguration.INCLUDES_ENV_VAR, envIncludesList);
        mockEnvironment.setenv(ResourceManagementConfiguration.EXCLUDES_ENV_VAR, envExcludesList);

        // When...
        ResourceManagementConfiguration config = new ResourceManagementConfiguration(includesList, excludesList, mockEnvironment);

        // Then...
        assertThat(config.getIncludesGlobPatterns()).isEqualTo(includesList);
        assertThat(config.getExcludesGlobPatterns()).isEqualTo(excludesList);
    }
}
