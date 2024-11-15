/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks.couchdb;

import java.net.URI;

import org.apache.http.impl.client.CloseableHttpClient;

import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.extensions.common.couchdb.CouchdbValidator;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.extensions.common.api.HttpRequestFactory;

public class MockCouchdbValidator implements CouchdbValidator {

    private boolean throwException = false;

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    @Override
    public void checkCouchdbDatabaseIsValid(URI couchdbUri, CloseableHttpClient httpClient, HttpRequestFactory requestFactory, ITimeService timeService) throws CouchdbException {
        if (throwException) {
            throw new CouchdbException("simulating a validation failure!");
        }
    }
}
