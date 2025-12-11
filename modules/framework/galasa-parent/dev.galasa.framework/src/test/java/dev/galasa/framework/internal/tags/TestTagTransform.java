/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.tags;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.junit.Test;

import dev.galasa.framework.spi.tags.Tag;

public class TestTagTransform {

    @Test
    public void testCanEncodeTagNameIntoBase64() throws Exception {
        // Given...
        String tagName = "sampleTag";
        TagTransform transform = new TagTransform();

        // When...
        String encodedTagName = transform.encodeTagName(tagName);

        // Then...
        String expectedTagName = Base64.getUrlEncoder().withoutPadding().encodeToString(tagName.getBytes(StandardCharsets.UTF_8));
        assertThat(encodedTagName).isEqualTo(expectedTagName);

        String decodedTagName = new String(Base64.getUrlDecoder().decode(encodedTagName), StandardCharsets.UTF_8);
        assertThat(decodedTagName).isEqualTo(tagName);
    }

    @Test
    public void testCanConvertTagToProperties() throws Exception {
        // Given...
        TagTransform transform = new TagTransform();
        Tag tag = new Tag("sampleTag");
        tag.setDescription("This is a sample tag");
        tag.setPriority(5);

        // When...
        Map<String, String> properties = transform.getPropertiesFromTag(tag);

        // Then...
        String encodedTagName = transform.encodeTagName("sampleTag");
        assertThat(properties).hasSize(2);
        assertThat(properties).containsEntry(encodedTagName + ".description", "This is a sample tag");
        assertThat(properties).containsEntry(encodedTagName + ".priority", "5");
    }

    @Test
    public void testCanConvertTagToPropertiesWithoutDescription() throws Exception {
        // Given...
        TagTransform transform = new TagTransform();
        Tag tag = new Tag("sampleTag");
        tag.setPriority(5);

        // When...
        Map<String, String> properties = transform.getPropertiesFromTag(tag);

        // Then...
        String encodedTagName = transform.encodeTagName("sampleTag");
        assertThat(properties).hasSize(1);
        assertThat(properties).doesNotContainKey(encodedTagName + ".description");
        assertThat(properties).containsEntry(encodedTagName + ".priority", "5");
    }

    @Test
    public void testCanConvertPropertiesToTag() throws Exception {
        // Given...
        TagTransform transform = new TagTransform();
        Map<String, String> properties = Map.of(
            "description", "This is a sample tag",
            "priority", "5"
        );

        String encodedTagName = transform.encodeTagName("sampleTag");

        // When...
        Tag tagGotBack = transform.getTagFromProperties(properties, encodedTagName);

        // Then...
        assertThat(tagGotBack.getName()).isEqualTo("sampleTag");
        assertThat(tagGotBack.getDescription()).isEqualTo("This is a sample tag");
        assertThat(tagGotBack.getPriority()).isEqualTo(5);
    }

    @Test
    public void testCanConvertPropertiesToTagWithoutDescription() throws Exception {
        // Given...
        TagTransform transform = new TagTransform();
        Map<String, String> properties = Map.of(
            "priority", "5"
        );

        String encodedTagName = transform.encodeTagName("sampleTag");

        // When...
        Tag tagGotBack = transform.getTagFromProperties(properties, encodedTagName);

        // Then...
        assertThat(tagGotBack.getName()).isEqualTo("sampleTag");
        assertThat(tagGotBack.getDescription()).isNull();
        assertThat(tagGotBack.getPriority()).isEqualTo(5);
    }

    @Test
    public void testCanConvertPropertiesToTagWithInvalidPriorityDefaultsToZero() throws Exception {
        // Given...
        TagTransform transform = new TagTransform();
        Map<String, String> properties = Map.of(
            "description", "This is a sample tag",
            "priority", "not a valid priority!"
        );

        String encodedTagName = transform.encodeTagName("sampleTag");

        // When...
        Tag tagGotBack = transform.getTagFromProperties(properties, encodedTagName);

        // Then...
        assertThat(tagGotBack.getName()).isEqualTo("sampleTag");
        assertThat(tagGotBack.getDescription()).isEqualTo("This is a sample tag");
        assertThat(tagGotBack.getPriority()).isEqualTo(0);
    }

    @Test
    public void testDecodingBadTagNameReturnsNull() throws Exception {
        // Given...
        TagTransform transform = new TagTransform();
        Map<String, String> properties = Map.of(
            "description", "This is a sample tag",
            "priority", "not a valid priority!"
        );

        // When...
        Tag tagGotBack = transform.getTagFromProperties(properties, "not a valid base64 string!!!");

        // Then...
        assertThat(tagGotBack).isNull();
    }
}
