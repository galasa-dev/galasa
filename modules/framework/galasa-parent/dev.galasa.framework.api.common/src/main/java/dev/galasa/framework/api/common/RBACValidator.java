/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import javax.servlet.http.HttpServletResponse;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;

public class RBACValidator {

    private RBACService rbacService;

    public RBACValidator(RBACService rbacService) {
        this.rbacService = rbacService;
    }

    public void validateActionPermitted(BuiltInAction action, String loginId) throws InternalServletException {
        if (!isActionPermitted(action, loginId)) {
            ServletError error = new ServletError(GAL5125_ACTION_NOT_PERMITTED, action.getAction().getId());
            throw new InternalServletException(error, HttpServletResponse.SC_FORBIDDEN);
        }
    }

    public boolean isActionPermitted(BuiltInAction action, String loginId) throws InternalServletException {
        boolean isActionPermitted = false;
        try {
            isActionPermitted = rbacService.isActionPermitted(loginId, action.getAction().getId());
        } catch (RBACException e) {
            ServletError error = new ServletError(GAL5126_INTERNAL_RBAC_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return isActionPermitted;
    }
}
