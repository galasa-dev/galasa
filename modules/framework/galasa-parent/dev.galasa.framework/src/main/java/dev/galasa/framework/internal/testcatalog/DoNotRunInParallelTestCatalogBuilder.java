/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.testcatalog;

import com.google.gson.JsonObject;

import dev.galasa.DoNotRunInParallel;
import dev.galasa.framework.spi.ITestCatalogBuilder;
import dev.galasa.framework.spi.TestCatalogBuilder;

/**
 * Writes the {@code isSerial} field into the test catalog entry for any test
 * class annotated with {@link DoNotRunInParallel}.
 *
 * A class without the annotation gets no field written; consumers must treat
 * the absence of the field as {@code false}.
 */
@TestCatalogBuilder
public class DoNotRunInParallelTestCatalogBuilder implements ITestCatalogBuilder {

    @Override
    public void appendTestCatalog(JsonObject jsonRoot, JsonObject jsonTestClass, Class<?> testClass) {
        if (testClass.getAnnotation(DoNotRunInParallel.class) != null) {
            jsonTestClass.addProperty("isSerial", true);
        }
    }

    @Override
    public void appendTestCatalogForSharedEnvironment(JsonObject jsonSharedEnvironmentClass,
            Class<?> sharedEnvironmentClass) {
        // Shared-environment runs are out of scope — nothing to do.
    }

}
