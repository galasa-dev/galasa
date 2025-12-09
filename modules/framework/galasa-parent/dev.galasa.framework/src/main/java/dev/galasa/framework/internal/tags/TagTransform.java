/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.tags;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.tags.Tag;

public class TagTransform {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String TAG_DESCRIPTION_SUFFIX = "description";
    private static final String TAG_PRIORITY_SUFFIX    = "priority";

    public Map<String, String> getPropertiesFromTag(Tag tag) {
        Map<String, String> properties = new HashMap<>();

        String tagName = tag.getName();
        String description = tag.getDescription();
        if (description != null) {
            properties.put(getTagPropertyKey(tagName, TAG_DESCRIPTION_SUFFIX), description);
        }

        int priority = tag.getPriority();
        properties.put(getTagPropertyKey(tagName, TAG_PRIORITY_SUFFIX), Integer.toString(priority));

        return properties;
    }

    public Tag getTagFromProperties(Map<String, String> properties, String tagName) {
        Tag tag = new Tag(tagName);

        String description = properties.get(TAG_DESCRIPTION_SUFFIX);
        tag.setDescription(description);

        String priorityString = properties.get(TAG_PRIORITY_SUFFIX);
        int priority = 0;
        if (priorityString != null) {
            try {
                priority = Integer.parseInt(priorityString);
            } catch (NumberFormatException e) {
                logger.warn("Invalid priority value for tag " + tagName + ". Defaulting to " + priority);
            }
        }
        tag.setPriority(priority);

        return tag;
    }

    private String getTagPropertyKey(String tagName, String suffix) {
        return tagName + "." + suffix;
    }
}
