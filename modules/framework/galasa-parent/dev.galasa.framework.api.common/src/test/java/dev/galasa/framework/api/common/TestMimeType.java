/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.api.common.MimeType.*;

import org.junit.Test;

public class TestMimeType {

    @Test
    public void testMimeTypeToStringReturnExpectedString() throws Exception {
        assertThat(APPLICATION_JSON.toString()).isEqualTo("application/json");
        assertThat(APPLICATION_YAML.toString()).isEqualTo("application/yaml");
        assertThat(TEXT_PLAIN.toString()).isEqualTo("text/plain");
    }

    @Test
    public void testMimeTypeJsonReturnTrue() throws Exception {
        assertThat(APPLICATION_JSON.matchesType("application/json")).isTrue();
        assertThat(APPLICATION_JSON.matchesType("application/*")).isTrue();
        assertThat(APPLICATION_JSON.matchesType("*/*")).isTrue();
    }

    @Test
    public void testMimeTypeYamlReturnTrue() throws Exception {
        assertThat(APPLICATION_YAML.matchesType("application/yaml")).isTrue();
        assertThat(APPLICATION_YAML.matchesType("application/*")).isTrue();
        assertThat(APPLICATION_YAML.matchesType("*/*")).isTrue();
    }

    @Test
    public void testMimeTypeTextReturnTrue() throws Exception {
        assertThat(TEXT_PLAIN.matchesType("text/plain")).isTrue();
        assertThat(TEXT_PLAIN.matchesType("text/plain   ")).isTrue();
        assertThat(TEXT_PLAIN.matchesType(" text/plain   ")).isTrue();
        assertThat(TEXT_PLAIN.matchesType("text/*")).isTrue();
        assertThat(TEXT_PLAIN.matchesType("*/*")).isTrue();
    }

    @Test
    public void testJsonMimeTypeWithInvalidTypeReturnFalse() throws Exception {
        assertThat(MimeType.APPLICATION_JSON.matchesType("application/  json")).isFalse();
        assertThat(MimeType.APPLICATION_JSON.matchesType("application/json/not/good!")).isFalse();
        assertThat(MimeType.APPLICATION_JSON.matchesType("application/*/not/good!")).isFalse();
        assertThat(MimeType.APPLICATION_JSON.matchesType("json/application")).isFalse();
    }

    @Test
    public void testYamlMimeTypeWithInvalidTypeReturnFalse() throws Exception {
        assertThat(MimeType.APPLICATION_YAML.matchesType("application/  yaml")).isFalse();
        assertThat(MimeType.APPLICATION_YAML.matchesType("application/yaml/not/good!")).isFalse();
        assertThat(MimeType.APPLICATION_YAML.matchesType("application/*/not/good!")).isFalse();
        assertThat(MimeType.APPLICATION_YAML.matchesType("yaml/application")).isFalse();
    }

    @Test
    public void testTextMimeTypeWithInvalidTypeReturnFalse() throws Exception {
        assertThat(MimeType.TEXT_PLAIN.matchesType("text/ plain")).isFalse();
        assertThat(MimeType.TEXT_PLAIN.matchesType("text/plain/not/good!")).isFalse();
        assertThat(MimeType.TEXT_PLAIN.matchesType("text/*//")).isFalse();
        assertThat(MimeType.TEXT_PLAIN.matchesType("plain/text")).isFalse();
    }
}
