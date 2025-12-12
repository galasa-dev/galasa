/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.tags.ITagsService;
import dev.galasa.framework.spi.tags.Tag;
import dev.galasa.framework.spi.tags.TagsException;

public class TagsService implements ITagsService {

    private IConfigurationPropertyStoreService cpsService;
    private TagTransform tagTransform = new TagTransform();

    public TagsService(IConfigurationPropertyStoreService cpsService) {
        this.cpsService = cpsService;
    }

    @Override
    public List<Tag> getTags() throws TagsException {
        List<Tag> tags = new ArrayList<>();
        try {
            Map<String, String> allTagProperties = cpsService.getPrefixedProperties("");

            tags = getTagsFromCpsProperties(allTagProperties);

        } catch (ConfigurationPropertyStoreException e) {
            throw new TagsException("Failed to get tags from the CPS", e);
        }
        return tags;
    }

    @Override
    public Tag getTagByName(String tagName) throws TagsException {
        Tag tag = null;
        try {
            String encodedTagName = tagTransform.encodeTagName(tagName);
            Map<String, String> tagProperties = cpsService.getPrefixedProperties(encodedTagName + ".");
            List<Tag> tags = getTagsFromCpsProperties(tagProperties);

            if (!tags.isEmpty()) {
                tag = tags.get(0);
            }

        } catch (ConfigurationPropertyStoreException e) {
            throw new TagsException("Failed to get tag " + tagName + " from the CPS", e);
        }
        return tag;
    }

    private List<Tag> getTagsFromCpsProperties(Map<String, String> tagProperties) throws ConfigurationPropertyStoreException {
        List<Tag> tags = new ArrayList<>();
        Map<String, Map<String, String>> groupedTagProperties = new HashMap<>();

        // Tag properties are returned from the CPS with the format:
        // base64URLEncodedTagName.suffix
        for (Map.Entry<String, String> entry : tagProperties.entrySet()) {
            String fullKey = entry.getKey();
            String value = entry.getValue();

            // Get the index of the first dot to separate tag name and suffix
            int dotIndex = fullKey.indexOf('.');
            if (dotIndex > 0) {
                String encodedTagName = fullKey.substring(0, dotIndex);
                String suffix = fullKey.substring(dotIndex + 1);

                groupedTagProperties
                    .computeIfAbsent(encodedTagName, k -> new HashMap<>())
                    .put(suffix, value);
            }
        }

        for (Map.Entry<String, Map<String, String>> entry : groupedTagProperties.entrySet()) {
            String encodedTagName = entry.getKey();
            Map<String, String> properties = entry.getValue();

            Tag tag = tagTransform.getTagFromProperties(properties, encodedTagName);
            if (tag != null) {
                tags.add(tag);
            }
        }

        return tags;
    }

    @Override
    public void setTag(Tag tag) throws TagsException {
        try {
            Map<String, String> properties = tagTransform.getPropertiesFromTag(tag);
            cpsService.setProperties(properties);

        } catch (ConfigurationPropertyStoreException e) {
            throw new TagsException("Failed to set tag " + tag.getName() + " in the CPS", e);
        }
    }

    @Override
    public void deleteTag(String tagName) throws TagsException {
        try {
            String encodedTagName = tagTransform.encodeTagName(tagName);
            cpsService.deletePrefixedProperties(encodedTagName + ".");
        } catch (ConfigurationPropertyStoreException e) {
            throw new TagsException("Failed to delete tag " + tagName + " from the CPS", e);
        }
    }
}
