/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.internal.routes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;

public class TagsServletTest extends BaseServletTest {
    
    protected JsonObject generateExpectedTagJson(String tagName, String description, int priority) {
        JsonObject tagObject = new JsonObject();
        tagObject.addProperty("apiVersion", GalasaResourceValidator.DEFAULT_API_VERSION);

        JsonObject metadata = new JsonObject();
        tagObject.add("metadata", metadata);

        String encodedName = Base64.getUrlEncoder().withoutPadding().encodeToString(tagName.getBytes(StandardCharsets.UTF_8));
        metadata.addProperty("url", "http://my-api.server/api/tags/" + encodedName);

        metadata.addProperty("name", tagName);
        metadata.addProperty("id", encodedName);

        if (description != null) {
            metadata.addProperty("description", description);
        }

        JsonObject data = new JsonObject();
        tagObject.add("data", data);
        data.addProperty("priority", priority);

        tagObject.addProperty("kind", "GalasaTag");

        return tagObject;
    }
}
