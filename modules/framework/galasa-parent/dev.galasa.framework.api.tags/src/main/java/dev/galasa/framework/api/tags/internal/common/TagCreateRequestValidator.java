/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.internal.common;

import dev.galasa.framework.api.beans.generated.TagCreateRequest;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;

public class TagCreateRequestValidator extends GalasaResourceValidator<TagCreateRequest> {

    private TagValidator tagValidator = new TagValidator();

    @Override
    public void validate(TagCreateRequest request) throws InternalServletException {
        // Check that the tag has been given a name
        tagValidator.validateTagName(request.getname());
        tagValidator.validateDescription(request.getdescription());
    }
}
