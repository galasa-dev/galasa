/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.net.URI;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.AuthStoreException;

/**
 * This interface provides methods to register additional stores
 * used only by Galasa's API server and should server initialisation.
 */
public interface IApiServerInitialisation extends IFrameworkInitialisation {

    URI getAuthStoreUri();

    /**
     * Register an Auth Store Service, which allows the framework to retrieve user
     * and token information.
     *
     * @param authStore the auth store service to be registered
     * @throws AuthStoreException if there is a problem registering the service
     */
    void registerAuthStore(@NotNull IAuthStore authStore) throws AuthStoreException;
}
