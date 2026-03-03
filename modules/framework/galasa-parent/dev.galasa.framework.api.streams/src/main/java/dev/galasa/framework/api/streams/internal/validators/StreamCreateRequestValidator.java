/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.internal.validators;

import dev.galasa.framework.api.beans.generated.StreamCreateRequest;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.resources.StreamValidator;

/**
 * Validator for StreamCreateRequest objects used in POST /streams endpoint.
 * Validates that all required fields are present and properly formed.
 */
public class StreamCreateRequestValidator {

    private final StreamValidator streamValidator;

    public StreamCreateRequestValidator() {
        this.streamValidator = new StreamValidator();
    }

    /**
     * Validates a StreamCreateRequest object.
     *
     * @param createRequest the request to validate
     * @throws InternalServletException if validation fails
     */
    public void validate(StreamCreateRequest createRequest) throws InternalServletException {
        streamValidator.validateStreamName(createRequest.getname());

        String repositoryUrl = null;
        if (createRequest.getrepository() != null) {
            repositoryUrl = createRequest.getrepository().geturl();
        }

        String testCatalogUrl = null;
        if (createRequest.getTestCatalog() != null) {
            testCatalogUrl = createRequest.getTestCatalog().geturl();
        }

        streamValidator.validateRepositoryUrl(repositoryUrl, true);
        streamValidator.validateTestCatalogUrl(testCatalogUrl, true);
        streamValidator.validateObrs(createRequest.getobrs(), true);
    }
}
