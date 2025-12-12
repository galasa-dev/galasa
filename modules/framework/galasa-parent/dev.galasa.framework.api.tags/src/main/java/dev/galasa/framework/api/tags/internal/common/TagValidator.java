/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.internal.common;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.utils.StringValidator;

public class TagValidator {

    private StringValidator stringValidator = new StringValidator();

    public void validateTagName(String tagName) throws InternalServletException {
        if (tagName == null || tagName.isBlank() || !stringValidator.isLatin1(tagName)) {
            ServletError error = new ServletError(GAL5443_INVALID_TAG_NAME_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public void validateDescription(String description) throws InternalServletException {
        if (description != null && (description.isBlank() || !stringValidator.isLatin1(description))) {
            ServletError error = new ServletError(GAL5444_INVALID_TAG_DESCRIPTION_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
