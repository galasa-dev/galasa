/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.galasa.framework.spi.tags.ITagsService;
import dev.galasa.framework.spi.tags.Tag;
import dev.galasa.framework.spi.tags.TagsException;

public class MockTagsService implements ITagsService {

    private Map<String, Tag> tags;

    public MockTagsService() {
        this(new HashMap<>());
    }

    public MockTagsService(Map<String, Tag> tags) {
        this.tags = tags;
    }

    @Override
    public List<Tag> getTags() throws TagsException {
        return tags.values().stream().toList();
    }

    @Override
    public Tag getTagByName(String tagName) throws TagsException {
        return tags.get(tagName);
    }

    @Override
    public void setTag(Tag tag) throws TagsException {
        tags.put(tag.getName(), tag);
    }

    @Override
    public void deleteTag(String tagName) throws TagsException {
        tags.remove(tagName);
    }

}
