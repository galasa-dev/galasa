/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.tags;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.spi.tags.Tag;

public class TestTagsService {

    @Test
    public void testCanGetAllTags() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put("TAG1.description", "this is a test tag");
        propertiesToSet.put("TAG1.priority", "100");
        propertiesToSet.put("TAG2.description", "this is another test tag");
        propertiesToSet.put("TAG2.priority", "5");
        propertiesToSet.put("TAG3.description", "new tag");
        propertiesToSet.put("TAG3.priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        // When...
        List<Tag> tagsGotBack = tagsService.getTags();

        // Then...
        assertThat(tagsGotBack).hasSize(3);
        assertThat(tagsGotBack).extracting(Tag::getName)
            .containsExactlyInAnyOrder("TAG1", "TAG2", "TAG3");
        assertThat(tagsGotBack).extracting(Tag::getDescription)
            .containsExactlyInAnyOrder("this is a test tag", "this is another test tag", "new tag");
        assertThat(tagsGotBack).extracting(Tag::getPriority)
            .containsExactlyInAnyOrder(100, 5, 1);
    }

    @Test
    public void testCanGetTagByName() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put("TAG1.description", "this is a test tag");
        propertiesToSet.put("TAG1.priority", "100");
        propertiesToSet.put("TAG2.description", "this is another test tag");
        propertiesToSet.put("TAG2.priority", "5");
        propertiesToSet.put("TAG3.description", "new tag");
        propertiesToSet.put("TAG3.priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        // When...
        Tag tagGotBack = tagsService.getTagByName("TAG2");

        // Then...
        assertThat(tagGotBack.getName()).isEqualTo("TAG2");
        assertThat(tagGotBack.getDescription()).isEqualTo("this is another test tag");
        assertThat(tagGotBack.getPriority()).isEqualTo(5);
    }

    @Test
    public void testGetUnknownTagReturnsNull() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put("TAG1.description", "this is a test tag");
        propertiesToSet.put("TAG1.priority", "100");
        propertiesToSet.put("TAG2.description", "this is another test tag");
        propertiesToSet.put("TAG2.priority", "5");
        propertiesToSet.put("TAG3.description", "new tag");
        propertiesToSet.put("TAG3.priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        // When...
        Tag tagGotBack = tagsService.getTagByName("Unknown");

        // Then...
        assertThat(tagGotBack).isNull();
    }

    @Test
    public void testCanDeleteTagByName() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put("TAG1.description", "this is a test tag");
        propertiesToSet.put("TAG1.priority", "100");
        propertiesToSet.put("TAG2.description", "this is another test tag");
        propertiesToSet.put("TAG2.priority", "5");
        propertiesToSet.put("TAG3.description", "new tag");
        propertiesToSet.put("TAG3.priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        // When...
        tagsService.deleteTag("TAG2");

        // Then...
        assertThat(mockCpsService.getAllProperties().keySet()).doesNotContain("TAG2.description", "TAG2.priority");
    }

    @Test
    public void testCanCreateTag() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put("TAG1.description", "this is a test tag");
        propertiesToSet.put("TAG1.priority", "100");
        propertiesToSet.put("TAG2.description", "this is another test tag");
        propertiesToSet.put("TAG2.priority", "5");
        propertiesToSet.put("TAG3.description", "new tag");
        propertiesToSet.put("TAG3.priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        Tag tag = new Tag("NEWTAG");
        tag.setDescription("Create me!");
        tag.setPriority(42);

        // When...
        tagsService.setTag(tag);

        // Then...
        assertThat(mockCpsService.getAllProperties().keySet()).contains("NEWTAG.description", "NEWTAG.priority");
        assertThat(mockCpsService.getProperty("NEWTAG", "description")).isEqualTo("Create me!");
        assertThat(mockCpsService.getProperty("NEWTAG", "priority")).isEqualTo("42");
    }

    @Test
    public void testCanUpdateTag() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put("TAG1.description", "this is a test tag");
        propertiesToSet.put("TAG1.priority", "100");
        propertiesToSet.put("TAG2.description", "this is another test tag");
        propertiesToSet.put("TAG2.priority", "5");
        propertiesToSet.put("TAG3.description", "new tag");
        propertiesToSet.put("TAG3.priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        Tag tag = new Tag("TAG1");
        tag.setDescription("Updated!");
        tag.setPriority(42);

        // When...
        tagsService.setTag(tag);

        // Then...
        assertThat(mockCpsService.getProperty("TAG1", "description")).isEqualTo("Updated!");
        assertThat(mockCpsService.getProperty("TAG1", "priority")).isEqualTo("42");
    }
}
