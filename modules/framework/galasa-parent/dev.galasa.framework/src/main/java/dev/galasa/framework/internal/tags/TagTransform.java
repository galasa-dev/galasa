/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.tags;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.tags.Tag;
import dev.galasa.framework.spi.utils.StringValidator;

public class TagTransform {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String TAG_DESCRIPTION_SUFFIX = "description";
    private static final String TAG_PRIORITY_SUFFIX    = "priority";

    private Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();
    private Decoder base64UrlDecoder = Base64.getUrlDecoder();

    private StringValidator stringValidator = new StringValidator();

    public Map<String, String> getPropertiesFromTag(Tag tag) {
        Map<String, String> properties = new HashMap<>();
        
        // Tag names could contain special characters like spaces and dots, so the property keys
        // need to be able to handle these special characters.
        // Encode the tag name into Base64 URL format to ensure safe storage as a property key since
        // Base64 URL encoding uses alphanumeric characters, hyphens (-), and underscores (_) only.
        String tagName = tag.getName();
        String encodedTagName = encodeTagName(tagName);

        String description = tag.getDescription();
        if (description != null) {
            properties.put(getTagPropertyKey(encodedTagName, TAG_DESCRIPTION_SUFFIX), description);
        }

        int priority = tag.getPriority();
        properties.put(getTagPropertyKey(encodedTagName, TAG_PRIORITY_SUFFIX), Integer.toString(priority));

        return properties;
    }

    public Tag getTagFromProperties(Map<String, String> properties, String encodedTagName) {
        Tag tag = null;
        try {
            String tagName = decodeTagName(encodedTagName);
            if (!stringValidator.isLatin1(tagName)) {
                logger.warn("Decoded tag name contains characters that are not in the Latin-1 character set, returning null tag");
            } else {
                tag = new Tag(tagName);
    
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
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to decode tag name, returning null tag");
        }
        return tag;
    }

    public String encodeTagName(String tagName) {
        return base64UrlEncoder.encodeToString(tagName.getBytes(StandardCharsets.UTF_8));
    }

    private String decodeTagName(String encodedTagName) {
        byte[] decodedBytes = base64UrlDecoder.decode(encodedTagName);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private String getTagPropertyKey(String encodedTagName, String suffix) {
        return encodedTagName + "." + suffix;
    }
}
