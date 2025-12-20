/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.internal.common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import dev.galasa.framework.api.beans.generated.GalasaTag;
import dev.galasa.framework.api.beans.generated.GalasaTagdata;
import dev.galasa.framework.api.beans.generated.GalasaTagmetadata;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.spi.tags.Tag;
import dev.galasa.framework.spi.utils.GalasaGson;

public class TagsBeanTransform {

    private static final GalasaGson gson = new GalasaGson();

    /**
     * Creates a Tag bean based on the provided tag.
     */
    public GalasaTag createTagBean(Tag tag, String apiServerUrl) {

        GalasaTag tagBean = new GalasaTag();
        tagBean.setApiVersion(GalasaResourceValidator.DEFAULT_API_VERSION);

        // Build metadata
        GalasaTagmetadata metadata = createTagMetadata(tag, apiServerUrl);
        tagBean.setmetadata(metadata);

        // Build data section
        GalasaTagdata data = createTagData(tag);
        tagBean.setdata(data);

        return tagBean;
    }

    /**
     * Creates the metadata section of the Tag bean.
     */
    private GalasaTagmetadata createTagMetadata(Tag tag, String apiServerUrl) {
        GalasaTagmetadata metadata = new GalasaTagmetadata();

        String tagName = tag.getName();
        metadata.setname(tagName);

        String description = tag.getDescription();
        if (description != null && !description.isBlank()) {
            metadata.setdescription(description);
        }

        String encodedTagName = Base64.getUrlEncoder().withoutPadding().encodeToString(tagName.getBytes(StandardCharsets.UTF_8));
        metadata.setid(encodedTagName);
        metadata.seturl(apiServerUrl + "/tags/" + encodedTagName);

        return metadata;
    }

    /**
     * Creates the data section of the Tag bean.
     */
    private GalasaTagdata createTagData(Tag tag) {
        GalasaTagdata data = new GalasaTagdata();
        data.setpriority(tag.getPriority());

        return data;
    }

    public String getTagsAsJsonString(List<Tag> tagsToConvert, String baseServletUrl) {
        List<GalasaTag> tags = new ArrayList<>();

        for (Tag tagToConvert : tagsToConvert) {
            GalasaTag tagBean = createTagBean(tagToConvert, baseServletUrl);
            tags.add(tagBean);
        }
        return gson.toJson(tags);
    }
}
