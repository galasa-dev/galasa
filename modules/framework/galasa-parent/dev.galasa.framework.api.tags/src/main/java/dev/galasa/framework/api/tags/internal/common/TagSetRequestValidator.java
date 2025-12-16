/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.internal.common;

import dev.galasa.framework.api.beans.generated.TagSetRequest;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;

public class TagSetRequestValidator extends GalasaResourceValidator<TagSetRequest> {

    private TagValidator tagValidator = new TagValidator();

    @Override
    public void validate(TagSetRequest request) throws InternalServletException {
        tagValidator.validateDescription(request.getdescription());
    }
}
