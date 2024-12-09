/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.couchdb;

import static dev.galasa.extensions.common.Errors.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.extensions.common.couchdb.pojos.Welcome;
import dev.galasa.extensions.common.api.HttpRequestFactory;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.framework.spi.utils.ITimeService;

public abstract class CouchdbBaseValidator implements CouchdbValidator {

    private final GalasaGson gson = new GalasaGson();
    private final Log logger = LogFactory.getLog(getClass());

    private HttpRequestFactory requestFactory;
    private CloseableHttpClient httpClient;

    @Override
    public void checkCouchdbDatabaseIsValid(URI couchdbUri, CloseableHttpClient httpClient, HttpRequestFactory httpRequestFactory, ITimeService timeService) throws CouchdbException {
        this.requestFactory = httpRequestFactory;
        this.httpClient = httpClient;

        HttpGet httpGet = this.requestFactory.getHttpGetRequest(couchdbUri.toString());

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                String errorMessage = ERROR_FAILED_TO_ACCESS_COUCHDB_SERVER.getMessage(statusCode);
                throw new CouchdbException(errorMessage);
            }

            HttpEntity entity = response.getEntity();
            String welcomePayload = EntityUtils.toString(entity);
            Welcome welcome = gson.fromJson(welcomePayload, Welcome.class);
            if (!"Welcome".equals(welcome.couchdb) || welcome.version == null) {
                throw new CouchdbException(ERROR_INVALID_COUCHDB_WELCOME_RESPONSE.getMessage());
            }

            checkVersion(welcome.version);
            logger.debug("CouchDB is at the correct version");

        } catch (ParseException | IOException e) {
            String errorMessage = ERROR_FAILED_TO_VALIDATE_COUCHDB_SERVER.getMessage(e.getMessage());
            throw new CouchdbException(errorMessage, e);
        }
    }

    public HttpRequestFactory getRequestFactory() {
        return requestFactory;
    }

    /**
     * Checks if a database with the given name exists in the CouchDB server. If not, the database is created.
     *
     * @param couchdbUri the URI of the CouchDB server
     * @param dbName the name of the database that should exist
     * @throws CouchdbException if there was an issue checking the existence of the database or creating it
     */
    protected void validateDatabasePresent(URI couchdbUri, String dbName) throws CouchdbException {
        if (!isDatabasePresent(couchdbUri, dbName)) {
            createDatabase(couchdbUri, dbName);
        }
    }

    /**
     * Checks if a database with the given name exists, returning true if so and false otherwise.
     *
     * @param couchdbUri the URI of the CouchDB server
     * @param dbName the name of the database to check for
     * @return true if the database with the given name exists, false otherwise
     * @throws CouchdbException if there was an issue checking for the database
     */
    private boolean isDatabasePresent(URI couchdbUri, String dbName) throws CouchdbException {
        HttpHead httpHead = requestFactory.getHttpHeadRequest(couchdbUri + "/" + dbName);

        boolean databaseExists = false;
        try (CloseableHttpResponse response = httpClient.execute(httpHead)) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                databaseExists = true;
            } else if (statusCode != HttpStatus.SC_NOT_FOUND) {
                String errorMessage = ERROR_FAILED_TO_VALIDATE_COUCHDB_DATABASE.getMessage(dbName, statusCode);
                throw new CouchdbException(errorMessage);
            }
        } catch (IOException e) {
            String errorMessage = ERROR_FAILED_TO_VALIDATE_COUCHDB_SERVER.getMessage(e.getMessage());
            throw new CouchdbException(errorMessage, e);
        }
        return databaseExists;
    }

    /**
     * Creates a new database with the given name, throwing a CouchdbException if
     * the database could not be created.
     *
     * @param dbName the name of the database to be created
     * @throws CouchdbException if there was a problem accessing the CouchDB server
     *                          or creating the database
     */
    private synchronized void createDatabase(URI couchdbUri, String dbName) throws CouchdbException {
        HttpPut httpPut = requestFactory.getHttpPutRequest(couchdbUri + "/" + dbName);
        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                EntityUtils.consumeQuietly(response.getEntity());

                String errorMessage = ERROR_FAILED_TO_CREATE_COUCHDB_DATABASE.getMessage(dbName, statusCode);
                throw new CouchdbException(errorMessage);

            } else {
                // The database was successfully created
                EntityUtils.consumeQuietly(response.getEntity());
            }
        } catch (IOException e) {
            String errorMessage = ERROR_FAILED_TO_CREATE_COUCHDB_DATABASE.getMessage(dbName, e.getMessage());
            throw new CouchdbException(errorMessage, e);
        }
    }

    /**
     * Determines whether or not the given CouchDB version meets the minimum required CouchDB version
     * for Galasa.
     *
     * @param actualVersion the version that the CouchDB server is running at
     * @throws CouchdbException if the version of CouchDB is older than the minimum required version
     */
    private void checkVersion(String actualVersion) throws CouchdbException {

        CouchDbVersion actualCouchDbVersion = new CouchDbVersion(actualVersion);

        // Check if the actual version is older than the minimum version
        // If the actual version matches the minimum version, then this loop will continue until
        // all parts of the versions have been compared
        if ( actualCouchDbVersion.compareTo(CouchDbVersion.COUCHDB_MIN_VERSION) < 0) {

            // The minimum CouchDB version is later than the actual version, so throw an error
            String errorMessage = ERROR_OUTDATED_COUCHDB_VERSION.getMessage(actualVersion, CouchDbVersion.COUCHDB_MIN_VERSION);
            throw new CouchdbException(errorMessage);
        }
    }
}
