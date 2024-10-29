/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl;

import java.net.URL;

import org.junit.Test;

import dev.galasa.plugin.common.PluginCommonFactory;
import dev.galasa.plugin.common.test.MockException;

public class PluginCommonFactoryImplTest {
    
    @Test
    public void testCanCreateAuthenticationService() throws Exception {
        PluginCommonFactory<MockException> f = new PluginCommonFactoryImpl<>();
        MockHttpClient client = new MockHttpClient();
        f.newAuthenticationService(new URL("http://fakeurl"), "my:token", client);
    }
}
