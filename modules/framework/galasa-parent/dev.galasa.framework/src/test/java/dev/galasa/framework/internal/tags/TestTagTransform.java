/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.tags;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.Test;

import dev.galasa.framework.spi.tags.Tag;

public class TestTagTransform {

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
        assertThat(properties).hasSize(2);
        assertThat(properties).containsEntry("sampleTag.description", "This is a sample tag");
        assertThat(properties).containsEntry("sampleTag.priority", "5");
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
        assertThat(properties).hasSize(1);
        assertThat(properties).doesNotContainKey("sampleTag.description");
        assertThat(properties).containsEntry("sampleTag.priority", "5");
    }

    @Test
    public void testCanConvertPropertiesToTag() throws Exception {
        // Given...
        TagTransform transform = new TagTransform();
        Map<String, String> properties = Map.of(
            "description", "This is a sample tag",
            "priority", "5"
        );

        // When...
        Tag tagGotBack = transform.getTagFromProperties(properties, "sampleTag");

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

        // When...
        Tag tagGotBack = transform.getTagFromProperties(properties, "sampleTag");

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

        // When...
        Tag tagGotBack = transform.getTagFromProperties(properties, "sampleTag");

        // Then...
        assertThat(tagGotBack.getName()).isEqualTo("sampleTag");
        assertThat(tagGotBack.getDescription()).isEqualTo("This is a sample tag");
        assertThat(tagGotBack.getPriority()).isEqualTo(0);
    }
}
