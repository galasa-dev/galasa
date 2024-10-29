/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.testcatalog;

import com.google.gson.JsonObject;

import dev.galasa.Summary;
import dev.galasa.framework.spi.ITestCatalogBuilder;
import dev.galasa.framework.spi.TestCatalogBuilder;

@TestCatalogBuilder
public class BasicTestCatalogBuilder implements ITestCatalogBuilder {

    @Override
    public void appendTestCatalog(JsonObject jsonRoot, JsonObject jsonTestClass, Class<?> testClass) {
        Summary summary = testClass.getAnnotation(Summary.class);
        if (summary != null) {
            String text = summary.value();
            if (text != null) {
                text = text.trim();
                if (!text.isEmpty()) {
                    jsonTestClass.addProperty("summary", text);
                }
            }
        }
    }

    @Override
    public void appendTestCatalogForSharedEnvironment(JsonObject jsonSharedEnvironmentClass,
            Class<?> sharedEnvironmentClass) {
        Summary summary = sharedEnvironmentClass.getAnnotation(Summary.class);
        if (summary != null) {
            String text = summary.value();
            if (text != null) {
                text = text.trim();
                if (!text.isEmpty()) {
                    jsonSharedEnvironmentClass.addProperty("summary", text);
                }
            }
        }
    }

}
