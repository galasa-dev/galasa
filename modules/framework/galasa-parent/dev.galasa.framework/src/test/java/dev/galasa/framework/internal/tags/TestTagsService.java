/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.tags;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64.Encoder;

import org.junit.Test;

import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.spi.tags.Tag;

public class TestTagsService {

    @Test
    public void testCanGetAllTags() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String tag1Name = encoder.encodeToString("TAG1".getBytes(StandardCharsets.UTF_8));
        String tag2Name = encoder.encodeToString("TAG2".getBytes(StandardCharsets.UTF_8));
        String tag3Name = encoder.encodeToString("TAG3".getBytes(StandardCharsets.UTF_8));

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put(tag1Name + ".description", "this is a test tag");
        propertiesToSet.put(tag1Name + ".priority", "100");
        propertiesToSet.put(tag2Name + ".description", "this is another test tag");
        propertiesToSet.put(tag2Name + ".priority", "5");
        propertiesToSet.put(tag3Name + ".description", "new tag");
        propertiesToSet.put(tag3Name + ".priority", "1");

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
    public void testGetAllTagsSkipsTagWithBadName() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String tag1Name = encoder.encodeToString("TAG1".getBytes(StandardCharsets.UTF_8));
        String tag2Name = encoder.encodeToString("TAG2".getBytes(StandardCharsets.UTF_8));

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put(tag1Name + ".description", "this is a test tag");
        propertiesToSet.put(tag1Name + ".priority", "100");
        propertiesToSet.put(tag2Name + ".description", "this is another test tag");
        propertiesToSet.put(tag2Name + ".priority", "5");
        propertiesToSet.put("not a valid base64 tag!.description", "new tag");
        propertiesToSet.put("not a valid base64 tag!.priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        // When...
        List<Tag> tagsGotBack = tagsService.getTags();

        // Then...
        assertThat(tagsGotBack).hasSize(2);
        assertThat(tagsGotBack).extracting(Tag::getName)
            .containsExactlyInAnyOrder("TAG1", "TAG2");
        assertThat(tagsGotBack).extracting(Tag::getDescription)
            .containsExactlyInAnyOrder("this is a test tag", "this is another test tag");
        assertThat(tagsGotBack).extracting(Tag::getPriority)
            .containsExactlyInAnyOrder(100, 5);
    }

    @Test
    public void testCanGetTagByName() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String tag1Name = encoder.encodeToString("TAG1".getBytes(StandardCharsets.UTF_8));
        String tag2Name = encoder.encodeToString("TAG2".getBytes(StandardCharsets.UTF_8));
        String tag3Name = encoder.encodeToString("TAG3".getBytes(StandardCharsets.UTF_8));

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put(tag1Name + ".description", "this is a test tag");
        propertiesToSet.put(tag1Name + ".priority", "100");
        propertiesToSet.put(tag2Name + ".description", "this is another test tag");
        propertiesToSet.put(tag2Name + ".priority", "5");
        propertiesToSet.put(tag3Name + ".description", "new tag");
        propertiesToSet.put(tag3Name + ".priority", "1");

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

        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String tag1Name = encoder.encodeToString("TAG1".getBytes(StandardCharsets.UTF_8));
        String tag2Name = encoder.encodeToString("TAG2".getBytes(StandardCharsets.UTF_8));
        String tag3Name = encoder.encodeToString("TAG3".getBytes(StandardCharsets.UTF_8));

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put(tag1Name + ".description", "this is a test tag");
        propertiesToSet.put(tag1Name + ".priority", "100");
        propertiesToSet.put(tag2Name + ".description", "this is another test tag");
        propertiesToSet.put(tag2Name + ".priority", "5");
        propertiesToSet.put(tag3Name + ".description", "new tag");
        propertiesToSet.put(tag3Name + ".priority", "1");

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

        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String tag1Name = encoder.encodeToString("TAG1".getBytes(StandardCharsets.UTF_8));
        String tag2Name = encoder.encodeToString("TAG2".getBytes(StandardCharsets.UTF_8));
        String tag3Name = encoder.encodeToString("TAG3".getBytes(StandardCharsets.UTF_8));

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put(tag1Name + ".description", "this is a test tag");
        propertiesToSet.put(tag1Name + ".priority", "100");
        propertiesToSet.put(tag2Name + ".description", "this is another test tag");
        propertiesToSet.put(tag2Name + ".priority", "5");
        propertiesToSet.put(tag3Name + ".description", "new tag");
        propertiesToSet.put(tag3Name + ".priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        // When...
        tagsService.deleteTag("TAG2");

        // Then...
        assertThat(mockCpsService.getAllProperties().keySet()).doesNotContain(tag2Name + ".description", tag2Name + ".priority");
    }

    @Test
    public void testCanCreateTag() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String tag1Name = encoder.encodeToString("TAG1".getBytes(StandardCharsets.UTF_8));
        String tag2Name = encoder.encodeToString("TAG2".getBytes(StandardCharsets.UTF_8));
        String tag3Name = encoder.encodeToString("TAG3".getBytes(StandardCharsets.UTF_8));

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put(tag1Name + ".description", "this is a test tag");
        propertiesToSet.put(tag1Name + ".priority", "100");
        propertiesToSet.put(tag2Name + ".description", "this is another test tag");
        propertiesToSet.put(tag2Name + ".priority", "5");
        propertiesToSet.put(tag3Name + ".description", "new tag");
        propertiesToSet.put(tag3Name + ".priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        Tag tag = new Tag("NEWTAG");
        tag.setDescription("Create me!");
        tag.setPriority(42);

        // When...
        tagsService.setTag(tag);

        // Then...
        String encodedNewTagName = encoder.encodeToString("NEWTAG".getBytes(StandardCharsets.UTF_8));
        assertThat(mockCpsService.getAllProperties().keySet()).contains(encodedNewTagName + ".description", encodedNewTagName + ".priority");
        assertThat(mockCpsService.getProperty(encodedNewTagName, "description")).isEqualTo("Create me!");
        assertThat(mockCpsService.getProperty(encodedNewTagName, "priority")).isEqualTo("42");
    }

    @Test
    public void testCanCreateTagWithSpecialCharactersInItsName() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String tag1Name = encoder.encodeToString("TAG1".getBytes(StandardCharsets.UTF_8));
        String tag2Name = encoder.encodeToString("TAG2".getBytes(StandardCharsets.UTF_8));
        String tag3Name = encoder.encodeToString("TAG3".getBytes(StandardCharsets.UTF_8));

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put(tag1Name + ".description", "this is a test tag");
        propertiesToSet.put(tag1Name + ".priority", "100");
        propertiesToSet.put(tag2Name + ".description", "this is another test tag");
        propertiesToSet.put(tag2Name + ".priority", "5");
        propertiesToSet.put(tag3Name + ".description", "new tag");
        propertiesToSet.put(tag3Name + ".priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        String tagName = "this is a new tag!@#$%^&*()_+";
        Tag tag = new Tag(tagName);
        tag.setDescription("Create me!");
        tag.setPriority(42);

        // When...
        tagsService.setTag(tag);

        // Then...
        String encodedNewTagName = encoder.encodeToString(tagName.getBytes(StandardCharsets.UTF_8));
        assertThat(mockCpsService.getAllProperties().keySet()).contains(encodedNewTagName + ".description", encodedNewTagName + ".priority");
        assertThat(mockCpsService.getProperty(encodedNewTagName, "description")).isEqualTo("Create me!");
        assertThat(mockCpsService.getProperty(encodedNewTagName, "priority")).isEqualTo("42");
    }

    @Test
    public void testCanUpdateTag() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCpsService = new MockIConfigurationPropertyStoreService();

        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String tag1Name = encoder.encodeToString("TAG1".getBytes(StandardCharsets.UTF_8));
        String tag2Name = encoder.encodeToString("TAG2".getBytes(StandardCharsets.UTF_8));
        String tag3Name = encoder.encodeToString("TAG3".getBytes(StandardCharsets.UTF_8));

        Map<String, String> propertiesToSet = new HashMap<>();
        propertiesToSet.put(tag1Name + ".description", "this is a test tag");
        propertiesToSet.put(tag1Name + ".priority", "100");
        propertiesToSet.put(tag2Name + ".description", "this is another test tag");
        propertiesToSet.put(tag2Name + ".priority", "5");
        propertiesToSet.put(tag3Name + ".description", "new tag");
        propertiesToSet.put(tag3Name + ".priority", "1");

        mockCpsService.setProperties(propertiesToSet);

        TagsService tagsService = new TagsService(mockCpsService);

        Tag tag = new Tag("TAG1");
        tag.setDescription("Updated!");
        tag.setPriority(42);

        // When...
        tagsService.setTag(tag);

        // Then...
        assertThat(mockCpsService.getProperty(tag1Name, "description")).isEqualTo("Updated!");
        assertThat(mockCpsService.getProperty(tag1Name, "priority")).isEqualTo("42");
    }
}
