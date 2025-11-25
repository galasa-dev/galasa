/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import dev.galasa.framework.api.common.EnvironmentVariables;

public class FilledMockEnvironment {
    public static MockEnvironment createTestEnvironment() {
        MockEnvironment mockEnv = new MockEnvironment();

        mockEnv.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, "http://my-api.server/api");
        mockEnv.setenv(EnvironmentVariables.GALASA_EXTERNAL_WEBUI_URL, "http://my-api.server");
        mockEnv.setenv(EnvironmentVariables.GALASA_DEX_ISSUER, "http://my-dex.issuer/dex");
        mockEnv.setenv(EnvironmentVariables.GALASA_DEX_GRPC_HOSTNAME, "dex-grpc:1234");
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username,name,sub");

        return mockEnv;
    }
}
