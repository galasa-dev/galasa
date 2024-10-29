/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal;

import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;

public interface IInternalEcosystem {

    Object getTag();

    void build() throws GalasaEcosystemManagerException;

    void stop();

    void discard();

}
