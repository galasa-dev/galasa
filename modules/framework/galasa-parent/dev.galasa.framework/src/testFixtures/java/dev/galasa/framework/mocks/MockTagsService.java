/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.tags.ITagsService;
import dev.galasa.framework.spi.tags.Tag;
import dev.galasa.framework.spi.tags.TagsException;

public class MockTagsService implements ITagsService {

    private List<Tag> tags;

    public MockTagsService() {
        this(new ArrayList<>());
    }

    public MockTagsService(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public List<Tag> getTags() throws TagsException {
        return tags;
    }

    @Override
    public Tag getTagByName(String tagName) throws TagsException {
        Tag tagToReturn = null;
        for (Tag tag : tags) {
            if (tag.getName().equals(tagName)) {
                tagToReturn = tag;
                break;
            }
        }
        return tagToReturn;
    }

    @Override
    public void setTag(Tag tag) throws TagsException {
        tags.remove(tag);
        tags.add(tag);
    }

    @Override
    public void deleteTag(String tagName) throws TagsException {
        Tag tagToDelete = null;
        for (Tag tag : tags) {
            if (tag.getName().equals(tagName)) {
                tagToDelete = tag;
                break;
            }
        }
        if (tagToDelete != null) {
            tags.remove(tagToDelete);
        }
    }
    
}
