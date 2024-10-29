/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonSyntaxException;


import dev.galasa.extensions.common.api.LogFactory;

import dev.galasa.extensions.common.couchdb.CouchdbBaseValidator;
import dev.galasa.extensions.common.couchdb.CouchdbClashingUpdateException;
import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.extensions.common.couchdb.RetryableCouchdbUpdateOperationProcessor;
import dev.galasa.auth.couchdb.internal.beans.*;
import dev.galasa.extensions.common.api.HttpRequestFactory;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.framework.spi.utils.ITimeService;

import static dev.galasa.auth.couchdb.internal.Errors.*;

public class CouchdbAuthStoreValidator extends CouchdbBaseValidator {

    private final Log logger ;
    private final GalasaGson gson = new GalasaGson();
    private final LogFactory logFactory;

    // A couchDB view, it gets all the access tokens of a the user based on the loginId provided.
    public static final String DB_TABLE_TOKENS_DESIGN = "function (doc) { if (doc.owner && doc.owner.loginId) {emit(doc.owner.loginId, doc); } }";
    public static final String DB_TABLE_USERS_DESIGN = "function (doc) { if (doc['login-id']) { emit(doc['login-id'], doc); } }";

    public CouchdbAuthStoreValidator() {
        this(new LogFactory(){
            @Override
            public Log getLog(Class<?> clazz) {
                return org.apache.commons.logging.LogFactory.getLog(clazz);
            }
        });
    }

    public CouchdbAuthStoreValidator(LogFactory logFactory) {
        this.logFactory = logFactory;
        this.logger = logFactory.getLog(getClass());
    }

    @Override
    public void checkCouchdbDatabaseIsValid(
        URI couchdbUri, 
        CloseableHttpClient httpClient,
        HttpRequestFactory httpRequestFactory,
        ITimeService timeService
    ) throws CouchdbException {

        // Perform the base CouchDB checks
        super.checkCouchdbDatabaseIsValid(couchdbUri, httpClient, httpRequestFactory, timeService);

        RetryableCouchdbUpdateOperationProcessor retryProcessor = new RetryableCouchdbUpdateOperationProcessor(timeService, this.logFactory);
        
        retryProcessor.retryCouchDbUpdateOperation( 
            ()->{ 
                tryToCheckAndUpdateCouchDBTokenView(couchdbUri, httpClient, httpRequestFactory); 
        });
        
        logger.debug("Auth Store CouchDB at " + couchdbUri.toString() + " validated");
    }

    private void tryToCheckAndUpdateCouchDBTokenView(URI couchdbUri, CloseableHttpClient httpClient,
            HttpRequestFactory httpRequestFactory) throws CouchdbException {
       
        validateDatabasePresent(couchdbUri, CouchdbAuthStore.TOKENS_DATABASE_NAME);
        validateDatabasePresent(couchdbUri, CouchdbAuthStore.USERS_DATABASE_NAME);
        checkTokensDesignDocument(httpClient, couchdbUri, 1, CouchdbAuthStore.TOKENS_DATABASE_NAME);
        checkTokensDesignDocument(httpClient, couchdbUri, 1, CouchdbAuthStore.USERS_DATABASE_NAME);
    }

    public void checkTokensDesignDocument(CloseableHttpClient httpClient, URI couchdbUri, int attempts, String dbName)
            throws CouchdbException {

        // Get the design document from couchdb
        String docJson = getDatabaseDesignDocument(httpClient, couchdbUri, attempts, dbName);

        AuthDBNameViewDesign tableDesign = parseTokenDesignFromJson(docJson);

        boolean isDesignUpdated = updateDesignDocToDesiredDesignDoc(tableDesign, dbName);

        if (isDesignUpdated) {
            updateTokenDesignDocument(httpClient, couchdbUri, attempts, tableDesign, dbName);
        }
    }

    private AuthDBNameViewDesign parseTokenDesignFromJson(String docJson) throws CouchdbException {
        AuthDBNameViewDesign tableDesign;
        try {
            tableDesign = gson.fromJson(docJson, AuthDBNameViewDesign.class);
        } catch (JsonSyntaxException ex) {
            throw new CouchdbException(ERROR_FAILED_TO_PARSE_COUCHDB_DESIGN_DOC.getMessage(ex.getMessage()), ex);
        }

        if (tableDesign == null) {
            tableDesign = new AuthDBNameViewDesign();
        }
        return tableDesign;
    }

    private boolean updateDesignDocToDesiredDesignDoc(AuthDBNameViewDesign tableDesign, String dbName) {
        boolean isUpdated = false;

        if (tableDesign.views == null) {
            isUpdated = true;
            tableDesign.views = new AuthStoreDBViews();
        }

        if (tableDesign.views.loginIdView == null) {
            isUpdated = true;
            tableDesign.views.loginIdView = new AuthStoreDBLoginView();
        }

        if (tableDesign.views.loginIdView.map == null) {
            isUpdated = true;
            if(dbName.equals(DB_TABLE_TOKENS_DESIGN)){
                tableDesign.views.loginIdView.map = DB_TABLE_TOKENS_DESIGN;
            }
            else{
                tableDesign.views.loginIdView.map = DB_TABLE_USERS_DESIGN;
            }
        }

        if (tableDesign.language == null || !tableDesign.language.equals("javascript")) {
            isUpdated = true;
            tableDesign.language = "javascript";
        }

        return isUpdated;
    }

    private String getDatabaseDesignDocument(CloseableHttpClient httpClient, URI couchdbUri, int attempts, String dbName)
            throws CouchdbException {
        HttpRequestFactory requestFactory = super.getRequestFactory();
        HttpGet httpGet = requestFactory.getHttpGetRequest(couchdbUri + "/" + dbName +"/_design/docs");

        String docJson = null;
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {

            StatusLine statusLine = response.getStatusLine();

            docJson = EntityUtils.toString(response.getEntity());
            if (statusLine.getStatusCode() != HttpStatus.SC_OK
                    && statusLine.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
                throw new CouchdbException(
                        "Validation failed of database " + dbName + " design document - " + statusLine.toString());
            }
            if (statusLine.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                docJson = "{}";
            }

            return docJson;

        } catch (CouchdbException e) {
            throw e;
        } catch (Exception e) {
            throw new CouchdbException("Validation failed", e);
        }
    }

    private void updateTokenDesignDocument(CloseableHttpClient httpClient, URI couchdbUri, int attempts,
            AuthDBNameViewDesign tokenViewDesign, String dbName) throws CouchdbException {
        HttpRequestFactory requestFactory = super.getRequestFactory();

        logger.info("Updating the " + dbName + " design document");

        HttpEntity entity = new StringEntity(gson.toJson(tokenViewDesign), ContentType.APPLICATION_JSON);

        HttpPut httpPut = requestFactory.getHttpPutRequest(couchdbUri + "/" + dbName +"/_design/docs");
        httpPut.setEntity(entity);

        if (tokenViewDesign._rev != null) {
            httpPut.addHeader("ETaq", "\"" + tokenViewDesign._rev + "\"");
        }

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            EntityUtils.consumeQuietly(response.getEntity());

            if (statusCode == HttpStatus.SC_CONFLICT) {
                // Someone possibly updated the document while we were thinking about it.
                // It was probably another instance of this exact code.
                throw new CouchdbClashingUpdateException(ERROR_FAILED_TO_UPDATE_COUCHDB_DESING_DOC_CONFLICT.toString());
            }

            if (statusCode != HttpStatus.SC_CREATED) {

                throw new CouchdbException(
                        "Update of " + dbName +  " design document failed on CouchDB server - " + statusLine.toString());
            }

        } catch (CouchdbException e) {
            throw e;
        } catch (Exception e) {
            throw new CouchdbException("Update of " +dbName +  " design document failed", e);
        }
    }
}
