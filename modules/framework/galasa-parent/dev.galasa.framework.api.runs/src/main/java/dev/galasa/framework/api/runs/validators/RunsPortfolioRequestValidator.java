/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.validators;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.RunsPortfolioRequest;
import dev.galasa.framework.api.beans.generated.RunsPortfolioSelection;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.ResourceNameValidator;

/**
 * Validates a {@link RunsPortfolioRequest} payload for POST /runs/portfolios.
 */
public class RunsPortfolioRequestValidator {

    private final ResourceNameValidator nameValidator = new ResourceNameValidator();

    /**
     * Validates the deserialized request bean.
     *
     * @param request the deserialized request; may be null if the body was empty or not a JSON object
     * @throws InternalServletException with 400 Bad Request if validation fails
     */
    public void validate(RunsPortfolioRequest request) throws InternalServletException {
        if (request == null) {
            ServletError error = new ServletError(GAL5465_RUNS_PORTFOLIO_SELECTIONS_EMPTY);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        RunsPortfolioSelection[] selections = request.getselections();
        if (selections == null || selections.length == 0) {
            ServletError error = new ServletError(GAL5465_RUNS_PORTFOLIO_SELECTIONS_EMPTY);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        for (RunsPortfolioSelection selection : selections) {
            String stream = selection.getstream();
            if (stream == null || stream.isBlank()) {
                ServletError error = new ServletError(GAL5468_RUNS_PORTFOLIO_STREAM_NOT_FOUND, "");
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    /**
     * Validates that all keys in the parsed overrides map are valid CPS property names.
     *
     * @param overrides the parsed overrides map; may be null or empty
     * @throws InternalServletException with 400 Bad Request if any key is invalid
     */
    public void validateOverrideKeys(Map<String, String> overrides) throws InternalServletException {
        if (overrides != null) {
            for (String key : overrides.keySet()) {
                try {
                    nameValidator.assertPropertyNameCharPatternIsValid(key);
                } catch (InternalServletException e) {
                    ServletError error = new ServletError(GAL5470_RUNS_PORTFOLIO_INVALID_OVERRIDE_KEY, key);
                    throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }
}
