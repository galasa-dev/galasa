/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest.mocks;

import dev.galasa.cps.rest.JwtProvider;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

public class MockJwtProvider implements JwtProvider {

    String jwt ;

    public MockJwtProvider(String jwtToSupply) {
        jwt = jwtToSupply;
    }

    @Override
    public String getJwt() throws ConfigurationPropertyStoreException {
        return this.jwt;
    }
    
}
