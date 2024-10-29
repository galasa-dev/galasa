/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

/**
 * Something which can provide the caller with a valid JWT to use with authenticated HTTP traffic.
 */
public interface JwtProvider {
    String getJwt() throws ConfigurationPropertyStoreException;
}
