/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

public interface IResultArchiveStoreRegistration {

    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws ResultArchiveStoreException;
}
