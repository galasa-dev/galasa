/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import dev.galasa.cps.rest.mocks.MockJwtProvider;
import dev.galasa.extensions.common.mocks.MockFrameworkInitialisation;
import dev.galasa.extensions.common.mocks.MockHttpClientFactory;
import dev.galasa.extensions.common.mocks.MockLogFactory;
import dev.galasa.framework.spi.IConfigurationPropertyStore;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


public class TestRestCPSRegistration {
    
    @Rule
    public TestName testName = new TestName();

    @Test
    public void testCanCreateARegistrationOK() {
        new RestCPSRegistration();
    }

    @Test
    public void testCanInitialiseARegistrationOK() throws Exception {
        URI uri = new URI("galasacps://my.server/api");
        RestCPSRegistration registration = new RestCPSRegistration(
            new MockHttpClientFactory(null),
            new MockJwtProvider("myJWT"),
            new MockLogFactory()
        );

        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation(uri);

        System.setProperty("GALASA_JWT", "not a valid token");
        registration.initialise(mockFrameworkInit);

        List<IConfigurationPropertyStore> stores = mockFrameworkInit.getRegisteredConfigurationPropertyStores();
        assertThat(stores).isNotNull().hasSize(1);
        assertThat(stores.get(0)).isInstanceOf(CacheCPS.class);
    }

    @Test
    public void testInitialiseARegistrationWithWrongSchemaGetsIgnoredSilently() throws Exception {
        URI uri = new URI("notrest:http://my.server/api"); // Wrong schema in this URL.
        RestCPSRegistration registration = new RestCPSRegistration();

        System.setProperty("GALASA_JWT", "not a valid token");
        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation(uri);

        registration.initialise(mockFrameworkInit);

        List<IConfigurationPropertyStore> stores = mockFrameworkInit.getRegisteredConfigurationPropertyStores();
        assertThat(stores).isNotNull().hasSize(0);
    }

}
