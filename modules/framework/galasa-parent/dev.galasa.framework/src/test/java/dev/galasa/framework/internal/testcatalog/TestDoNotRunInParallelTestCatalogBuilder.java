/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.testcatalog;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.DoNotRunInParallel;

public class TestDoNotRunInParallelTestCatalogBuilder {

    // A test class that carries @DoNotRunInParallel
    @DoNotRunInParallel
    private static class SerialTestClass {}

    // A test class that does NOT carry @DoNotRunInParallel
    private static class NonSerialTestClass {}

    @Test
    public void testSerialClassHasIsTrueInCatalog() {
        // Given...
        DoNotRunInParallelTestCatalogBuilder builder = new DoNotRunInParallelTestCatalogBuilder();
        JsonObject jsonRoot = new JsonObject();
        JsonObject jsonTestClass = new JsonObject();

        // When...
        builder.appendTestCatalog(jsonRoot, jsonTestClass, SerialTestClass.class);

        // Then...
        assertThat(jsonTestClass.has("isSerial")).isTrue();
        assertThat(jsonTestClass.get("isSerial").getAsBoolean()).isTrue();
    }

    @Test
    public void testNonSerialClassHasNoIsSerialFieldInCatalog() {
        // Given...
        DoNotRunInParallelTestCatalogBuilder builder = new DoNotRunInParallelTestCatalogBuilder();
        JsonObject jsonRoot = new JsonObject();
        JsonObject jsonTestClass = new JsonObject();

        // When...
        builder.appendTestCatalog(jsonRoot, jsonTestClass, NonSerialTestClass.class);

        // Then...
        assertThat(jsonTestClass.has("isSerial")).isFalse();
    }

    @Test
    public void testSharedEnvironmentMethodDoesNothing() {
        // Given...
        DoNotRunInParallelTestCatalogBuilder builder = new DoNotRunInParallelTestCatalogBuilder();
        JsonObject jsonSharedEnvironmentClass = new JsonObject();

        // When...
        builder.appendTestCatalogForSharedEnvironment(jsonSharedEnvironmentClass, SerialTestClass.class);

        // Then... the object is unchanged
        assertThat(jsonSharedEnvironmentClass.size()).isZero();
    }
}
