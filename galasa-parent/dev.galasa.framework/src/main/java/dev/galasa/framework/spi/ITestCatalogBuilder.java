/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import com.google.gson.JsonObject;

public interface ITestCatalogBuilder {

    void appendTestCatalog(JsonObject jsonRoot, JsonObject jsonTestClass, Class<?> testClass);
    void appendTestCatalogForSharedEnvironment(JsonObject jsonSharedEnvironmentClass, Class<?> sharedEnvironmentClass);

}
