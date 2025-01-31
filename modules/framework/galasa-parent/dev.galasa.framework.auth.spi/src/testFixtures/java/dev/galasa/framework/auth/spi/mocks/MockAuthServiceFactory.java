/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.auth.spi.mocks;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.IAuthServiceFactory;

public class MockAuthServiceFactory implements IAuthServiceFactory {

    private IAuthService authService;

    public MockAuthServiceFactory(IAuthService authService) {
        this.authService = authService;
    }

    @Override
    public IAuthService getAuthService() throws InternalServletException {
        return authService;
    }
}
