/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.tags;

import java.util.List;

public interface ITagsService {

    /**
     * Get all tags from the CPS.
     * @return List of Tag objects
     */
    List<Tag> getTags() throws TagsException;

    /**
     * Get a tag by its name from the CPS.
     *
     * @param tagName The name of the tag to retrieve
     * @return The Tag object, or null if not found
     */
    Tag getTagByName(String tagName) throws TagsException;

    /**
     * Create or update a tag.
     *
     * @param tag The Tag object to set
     */
    void setTag(Tag tag) throws TagsException;

    /**
     * Delete a tag with the given name.
     *
     * @param tagName The name of the tag to delete
     */
    void deleteTag(String tagName) throws TagsException;
}
