/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.IFrameworkInitialisation;

public interface IAuthStoreRegistration {

    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws AuthStoreException;
}
