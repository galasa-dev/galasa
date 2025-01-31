/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal;

import org.junit.Test;

import dev.galasa.extensions.common.impl.HttpRequestFactoryImpl;
import dev.galasa.extensions.common.mocks.MockFrameworkInitialisation;
import dev.galasa.extensions.common.mocks.MockHttpClientFactory;
import dev.galasa.extensions.common.mocks.MockLogFactory;
import dev.galasa.extensions.common.mocks.couchdb.MockCouchdbValidator;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStore;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class TestCouchdbAuthStoreRegistration {

    @Test
    public void testCanCreateARegistrationOK() {
        new CouchdbAuthStoreRegistration();
    }

    @Test
    public void testCanInitialiseARegistrationOK() throws Exception {
        // Given...
        URI uri = new URI("couchdb:https://my.server:5984");
        CouchdbAuthStoreRegistration registration = new CouchdbAuthStoreRegistration(
                new MockHttpClientFactory(null),
                new HttpRequestFactoryImpl(),
                new MockLogFactory(),
                new MockCouchdbValidator());

        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation();
        mockFrameworkInit.setAuthStoreUri(uri);

        // When...
        registration.initialise(mockFrameworkInit);

        // Then...
        List<IAuthStore> stores = mockFrameworkInit.getRegisteredAuthStores();
        assertThat(stores).isNotNull().hasSize(1);
        assertThat(stores.get(0)).isInstanceOf(CouchdbAuthStore.class);
    }

    @Test
    public void testInitialiseARegistrationWithWrongSchemaGetsIgnoredSilently() throws Exception {
        // Given...
        // Wrong schema in this URL.
        URI uri = new URI("notcouchdb:http://my.server/blah");
        CouchdbAuthStoreRegistration registration = new CouchdbAuthStoreRegistration(
                new MockHttpClientFactory(null),
                new HttpRequestFactoryImpl(),
                new MockLogFactory(),
                new MockCouchdbValidator());

        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation();
        mockFrameworkInit.setAuthStoreUri(uri);

        // When...
        registration.initialise(mockFrameworkInit);

        // Then...
        // The auth store shouldn't have been added to the list of registered user stores
        List<IAuthStore> stores = mockFrameworkInit.getRegisteredAuthStores();
        assertThat(stores).isNotNull().hasSize(0);
    }

    @Test
    public void testInitialiseRegistrationWithFailingValidationReturnsError() throws Exception {
        // Given...
        URI uri = new URI("couchdb:http://my.server:1234");
        MockCouchdbValidator mockCouchdbValidator = new MockCouchdbValidator();
        mockCouchdbValidator.setThrowException(true);

        CouchdbAuthStoreRegistration registration = new CouchdbAuthStoreRegistration(
                new MockHttpClientFactory(null),
                new HttpRequestFactoryImpl(),
                new MockLogFactory(),
                mockCouchdbValidator);

        MockFrameworkInitialisation mockFrameworkInit = new MockFrameworkInitialisation();
        mockFrameworkInit.setAuthStoreUri(uri);

        // When...
        AuthStoreException thrown = catchThrowableOfType(() -> registration.initialise(mockFrameworkInit), AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6103E", "Failed to initialise the Galasa CouchDB auth store");
    }
}
