/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.internal.validators;

import dev.galasa.framework.api.beans.generated.StreamUpdateRequest;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.resources.StreamValidator;

/**
 * Validator for StreamUpdateRequest objects used in PUT /streams/{streamName} endpoint.
 * Validates that provided fields are properly formatted. Unlike create requests,
 * update requests support partial updates so not all fields are required.
 */
public class StreamUpdateRequestValidator {

    private final StreamValidator streamValidator;

    public StreamUpdateRequestValidator() {
        this.streamValidator = new StreamValidator();
    }

    /**
     * Validates a StreamUpdateRequest object.
     *
     * @param updateRequest the request to validate
     * @throws InternalServletException if validation fails
     */
    public void validate(StreamUpdateRequest updateRequest, boolean isCreatingNewStream) throws InternalServletException {
        // For update requests, we validate what's provided but don't require all fields
        String repositoryUrl = null;
        if (updateRequest.getrepository() != null) {
            repositoryUrl = updateRequest.getrepository().geturl();
        }

        String testCatalogUrl = null;
        if (updateRequest.getTestCatalog() != null) {
            testCatalogUrl = updateRequest.getTestCatalog().geturl();
        }

        streamValidator.validateRepositoryUrl(repositoryUrl, isCreatingNewStream);
        streamValidator.validateTestCatalogUrl(testCatalogUrl, isCreatingNewStream);
        streamValidator.validateObrs(updateRequest.getobrs(), isCreatingNewStream);
    }
}
