/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import java.util.List;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.resources.ResourceAction;

public interface IGalasaResourceProcessor {
    /**
     * Performs a given action on a provided Galasa resource.
     * The action could be to create, update, or delete the given resource.
     * 
     * @param resourceJson the resource to perform an action on
     * @param action the action to perform
     * @return a list of validation errors encountered when processing the given JSON payload
     * @throws InternalServletException if there was an issue processing the resource
     */
    List<String> processResource(JsonObject resourceJson, ResourceAction action) throws InternalServletException;
}
