/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest;

import org.junit.Test;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

import static org.assertj.core.api.Assertions.*;


public class TestJwtProviderSystemProp {
    
    @Test
    public void testCanGetJwtFromSystemPropertyOK() throws Exception {
        String dummyJwt = "A fake jwt";
        System.setProperty("GALASA_JWT", dummyJwt);
        JwtProvider provider = new JwtProviderSystemProp();
        String jwtGot = provider.getJwt();
        assertThat(jwtGot).isEqualTo(dummyJwt);
    }

    @Test
    public void testJwtFromSystemPropertyWithQuotesHasTheQuotesRemoved() throws Exception {
        String dummyJwt = "A fake jwt";
        String dummyJwtWithQuotes = "\""+dummyJwt+"\"";
        System.setProperty("GALASA_JWT", dummyJwtWithQuotes);
        JwtProvider provider = new JwtProviderSystemProp();
        String jwtGot = provider.getJwt();
        assertThat(jwtGot).isEqualTo(dummyJwt);
    }

    @Test
    public void testMissingJwtFromSystemPropertyFails() throws Exception {
        // No GALASA_JWT set in the system properties.
        System.clearProperty("GALASA_JWT");
        JwtProvider provider = new JwtProviderSystemProp();
        ConfigurationPropertyStoreException ex = catchThrowableOfType( ()-> provider.getJwt(), ConfigurationPropertyStoreException.class );
        assertThat(ex).isNotNull().hasMessageContaining("GAL7005E");
    }
}
