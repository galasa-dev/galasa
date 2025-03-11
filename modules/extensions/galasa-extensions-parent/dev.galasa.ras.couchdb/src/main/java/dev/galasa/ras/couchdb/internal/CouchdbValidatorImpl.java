/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import static dev.galasa.ras.couchdb.internal.CouchdbRasStore.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.extensions.common.couchdb.pojos.Welcome;
import dev.galasa.extensions.common.api.HttpRequestFactory;
import dev.galasa.extensions.common.couchdb.CouchDbVersion;
import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.extensions.common.couchdb.CouchdbValidator;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.framework.spi.utils.ITimeService;

public class CouchdbValidatorImpl implements CouchdbValidator {

    private static final String REQUESTORS_VIEW_FUNCTION       = "function (doc) { emit(doc.requestor, 1); }";
    private static final String RESULT_VIEW_FUNCTION           = "function (doc) { emit(doc.result, 1); }";
    private static final String TEST_NAMES_VIEW_FUNCTION       = "function (doc) { emit(doc.testName, 1); }";
    private static final String BUNDLE_TESTNAMES_VIEW_FUNCTION = "function (doc) { emit(doc.bundle + '/' + doc.testName, 1); }";
    private static final String RUN_NAMES_VIEW_FUNCTION        = "function (doc) { emit(doc.runName, 1); }";
    private static final String GROUP_VIEW_FUNCTION            = "function (doc) { if (doc.group !== undefined && doc.group !== null) { emit(doc.group, doc); } }";
    private static final String COUNT_REDUCE                   = "_count";
    
    private final GalasaGson                         gson               = new GalasaGson();
    private final Log                          logger             = LogFactory.getLog(getClass());
    private       HttpRequestFactory requestFactory;

    private static final CouchDbVersion minCouchDbVersion = new CouchDbVersion(3,3,3);

    @Override
    public void checkCouchdbDatabaseIsValid( 
        URI rasUri, 
        CloseableHttpClient httpClient , 
        HttpRequestFactory httpRequestFactory, 
        ITimeService timeService
    ) throws CouchdbException {

       this.requestFactory = httpRequestFactory;
        HttpGet httpGet = requestFactory.getHttpGetRequest(rasUri.toString());

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {

            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new CouchdbRasException("Validation failed to CouchDB server - " + statusLine.toString());
            }

            HttpEntity entity = response.getEntity();
            String welcomePayload = EntityUtils.toString(entity);
            Welcome welcome = gson.fromJson(welcomePayload, Welcome.class);
            if (!"Welcome".equals(welcome.couchdb) || welcome.version == null) {
                throw new CouchdbRasException("Validation failed to CouchDB server - invalid json response");
            }

            checkVersion(welcome.version, minCouchDbVersion);
            checkDatabasePresent(httpClient, rasUri, 1, "galasa_run", timeService);
            checkDatabasePresent(httpClient, rasUri, 1, "galasa_log", timeService);
            checkDatabasePresent(httpClient, rasUri, 1, "galasa_artifacts", timeService);

            checkRunDesignDocument(httpClient, rasUri,1, timeService);

            checkIndex(httpClient, rasUri, 1, "galasa_run", "runName",timeService);
            checkIndex(httpClient, rasUri, 1, "galasa_run", "requestor",timeService);
            checkIndex(httpClient, rasUri, 1, "galasa_run", "queued",timeService);
            checkIndex(httpClient, rasUri, 1, "galasa_run", "startTime", timeService);
            checkIndex(httpClient, rasUri, 1, "galasa_run", "endTime", timeService);
            checkIndex(httpClient, rasUri, 1, "galasa_run", "testName", timeService);
            checkIndex(httpClient, rasUri, 1, "galasa_run", "bundle", timeService);
            checkIndex(httpClient, rasUri, 1, "galasa_run", "result", timeService);
            checkIndex(httpClient, rasUri, 1, "galasa_run", "group", timeService);
            checkIndex(httpClient, rasUri, 1, "galasa_run", "submissionId", timeService);

            logger.debug("RAS CouchDB at " + rasUri.toString() + " validated");
        } catch (CouchdbException e) {
            throw e;
        } catch (Exception e) {
            throw new CouchdbException("Validation failed "+ e);
        }
    }



    private void checkDatabasePresent( CloseableHttpClient httpClient, URI rasUri, int attempts, String dbName, ITimeService timeService) throws CouchdbException {
        HttpHead httpHead = requestFactory.getHttpHeadRequest(rasUri + "/" + dbName);

        try (CloseableHttpResponse response = httpClient.execute(httpHead)) {
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                return;
            }
            if (statusLine.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
                throw new CouchdbException(
                        "Validation failed of database " + dbName + " - " + statusLine.toString());
            }
        } catch (CouchdbException e) {
            throw e;
        } catch (Exception e) {
            throw new CouchdbException("Validation failed", e);
        }

        logger.info("CouchDB database " + dbName + " is missing,  creating");

        HttpPut httpPut = requestFactory.getHttpPutRequest(rasUri + "/" + dbName);
        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == HttpStatus.SC_CONFLICT) {
                // Someone possibly updated
                attempts++;
                if (attempts > 10) {
                    throw new CouchdbException(
                            "Create Database " + dbName + " failed on CouchDB server due to conflicts, attempted 10 times");
                }
                timeService.sleepMillis(1000 + new Random().nextInt(3000));
                checkDatabasePresent(httpClient, rasUri, attempts, dbName, timeService);
                return;
            }

            if (statusLine.getStatusCode() != HttpStatus.SC_CREATED) {
                EntityUtils.consumeQuietly(response.getEntity());
                throw new CouchdbException(
                        "Create Database " + dbName + " failed on CouchDB server - " + statusLine.toString());
            }

            EntityUtils.consumeQuietly(response.getEntity());
        } catch (CouchdbException e) {
            throw e;
        } catch (Exception e) {
            throw new CouchdbException("Create database " + dbName + " failed", e);
        }
    }

    private void checkRunDesignDocument( CloseableHttpClient httpClient , URI rasUri , int attempts, ITimeService timeService) throws CouchdbException {
        HttpGet httpGet = requestFactory.getHttpGetRequest(rasUri + "/" + RUNS_DB + "/_design/docs");

        String docJson = null;
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            StatusLine statusLine = response.getStatusLine();
            docJson = EntityUtils.toString(response.getEntity());
            if (statusLine.getStatusCode() != HttpStatus.SC_OK
                    && statusLine.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
                throw new CouchdbException(
                        "Validation failed of database galasa_run designdocument - " + statusLine.toString());
            }
            if (statusLine.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                docJson = "{}";
            }
        } catch (CouchdbException e) {
            throw e;
        } catch (Exception e) {
            throw new CouchdbException("Validation failed", e);
        }

        boolean updated = false;

        JsonObject doc = gson.fromJson(docJson, JsonObject.class);
        doc.remove("_id");
        String rev = null;
        if (doc.has("_rev")) {
            rev = doc.get("_rev").getAsString();
        }

        JsonObject views = doc.getAsJsonObject("views");
        if (views == null) {
            updated = true;
            views = new JsonObject();
            doc.add("views", views);
        }

        if (checkForViewUpdates(views, REQUESTORS_VIEW_NAME, REQUESTORS_VIEW_FUNCTION, COUNT_REDUCE)) {
            updated = true;
        }

        if (checkForViewUpdates(views, RESULT_VIEW_NAME, RESULT_VIEW_FUNCTION, COUNT_REDUCE)) {
            updated = true;
        }

        if (checkForViewUpdates(views, TEST_NAMES_VIEW_NAME, TEST_NAMES_VIEW_FUNCTION, COUNT_REDUCE)) {
            updated = true;
        }

        if (checkForViewUpdates(views, BUNDLE_TESTNAMES_VIEW_NAME, BUNDLE_TESTNAMES_VIEW_FUNCTION, COUNT_REDUCE)) {
            updated = true;
        }

        if (checkForViewUpdates(views, RUN_NAMES_VIEW_NAME, RUN_NAMES_VIEW_FUNCTION)) {
            updated = true;
        }

        if (checkForViewUpdates(views, RUN_GROUP_VIEW_NAME, GROUP_VIEW_FUNCTION)) {
            updated = true;
        }

        if (updated) {
            logger.info("Updating the galasa_run design document");

            HttpEntity entity = new StringEntity(gson.toJson(doc), ContentType.APPLICATION_JSON);

            HttpPut httpPut = requestFactory.getHttpPutRequest(rasUri + "/" + RUNS_DB +"/_design/docs");
            httpPut.setEntity(entity);

            if (rev != null) {
                httpPut.addHeader("ETaq", "\"" + rev + "\"");
            }

            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == HttpStatus.SC_CONFLICT) {
                    // Someone possibly updated
                    attempts++;
                    if (attempts > 10) {
                        throw new CouchdbException(
                                "Update of galasa_run design document failed on CouchDB server due to conflicts, attempted 10 times");
                    }
                    timeService.sleepMillis(1000 + new Random().nextInt(3000));
                    checkRunDesignDocument(httpClient, rasUri, attempts, timeService);
                    return;
                }
                
                if (statusCode != HttpStatus.SC_CREATED) {
                    EntityUtils.consumeQuietly(response.getEntity());
                    throw new CouchdbException(
                            "Update of galasa_run design document failed on CouchDB server - " + statusLine.toString());
                }

                EntityUtils.consumeQuietly(response.getEntity());
            } catch (CouchdbException e) {
                throw e;
            } catch (Exception e) {
                throw new CouchdbException("Update of galasa_run design document faile", e);
            }
        }
    }

    private boolean checkForViewUpdates(JsonObject views, String viewName, String viewFunction) {
        return checkForViewUpdates(views, viewName, viewFunction, null);
    }

    private boolean checkForViewUpdates(JsonObject views, String viewName, String viewFunction, String targetReduce) {
        boolean updated = false;
        JsonObject view = views.getAsJsonObject(viewName);
        if (view == null) {
            updated = true;
            view = new JsonObject();
            views.add(viewName, view);
        }
        
        if (checkView(view, viewFunction, targetReduce)) {
            updated = true;
        }

        return updated;
    }

    private boolean checkView(JsonObject view, String targetMap, String targetReduce) {

        boolean updated = false;

        if (checkViewString(view, "map", targetMap)) {
            updated = true;
        }
        if (checkViewString(view, "reduce", targetReduce)) {
            updated = true;
        }
        if (checkViewString(view, "language", "javascript")) {
            updated = true;
        }

        return updated;
    }

    private boolean checkViewString(JsonObject view, String field, String value) {
        boolean isUpdated = false;
        JsonElement element = view.get(field);

        // Remove the existing field if no value is given
        if (element != null && value == null) {
            view.remove(field);
            isUpdated = true;

        } else if (value != null) {
            if (element == null || !element.isJsonPrimitive() || !((JsonPrimitive) element).isString()) {
                view.addProperty(field, value);
                isUpdated = true;

            } else {
                String actualValue = element.getAsString();
                if (!value.equals(actualValue)) {
                    view.addProperty(field, value);
                    isUpdated = true;
                }
            }
        }

        return isUpdated;
    }

    private void checkVersion(String version, CouchDbVersion minVersion)
            throws CouchdbException {

        CouchDbVersion actualCouchVersion = new CouchDbVersion(version);

        if ( actualCouchVersion.compareTo(minVersion) < 0) {
            throw new CouchdbException("CouchDB version " + version + " is below minimum " + minVersion);
        }

        return;
    }



    private void checkIndex(CloseableHttpClient httpClient, URI rasUri , int attempts, String dbName, String field, ITimeService timeService) throws CouchdbException {
        HttpGet httpGet = requestFactory.getHttpGetRequest(rasUri + "/galasa_run/_index");

        String idxJson = null;
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            StatusLine statusLine = response.getStatusLine();
            idxJson = EntityUtils.toString(response.getEntity());
            if (statusLine.getStatusCode() != HttpStatus.SC_OK
                    && statusLine.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
                throw new CouchdbException("Validation failed of database indexes - " + statusLine.toString());
            }
            if (statusLine.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                idxJson = "{}";
            }
        } catch (CouchdbException e) {
            throw e;
        } catch (Exception e) {
            throw new CouchdbException("Validation failed", e);
        }

        JsonObject idx = gson.fromJson(idxJson, JsonObject.class);
        boolean create = true;

        String idxName = field + "-index";

        JsonArray idxs = idx.getAsJsonArray("indexes");
        if (idxs != null) {
            for (int i = 0; i < idxs.size(); i++) {
                JsonElement elem = idxs.get(i);
                if (elem.isJsonObject()) {
                    JsonObject o = (JsonObject) elem;

                    JsonElement name = o.get("name");
                    if (name != null) {
                        if (name.isJsonPrimitive() && ((JsonPrimitive) name).isString()) {
                            if (idxName.equals(name.getAsString())) {
                                create = false;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (create) {
            logger.info("Updating the galasa_run index " + idxName);

            JsonObject doc = new JsonObject();
            doc.addProperty("name", idxName);
            doc.addProperty("type", "json");

            JsonObject index = new JsonObject();
            doc.add("index", index);
            JsonArray fields = new JsonArray();
            index.add("fields", fields);

            JsonObject field1 = new JsonObject();
            fields.add(field1);
            field1.addProperty(field, "asc");

            HttpEntity entity = new StringEntity(gson.toJson(doc), ContentType.APPLICATION_JSON);

            HttpPost httpPost = requestFactory.getHttpPostRequest(rasUri + "/galasa_run/_index");
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                StatusLine statusLine = response.getStatusLine();
                EntityUtils.consumeQuietly(response.getEntity());
                int statusCode = statusLine.getStatusCode();
                if (statusCode == HttpStatus.SC_CONFLICT) {
                    // Someone possibly updated
                    attempts++;
                    if (attempts > 10) {
                        throw new CouchdbException(
                                "Update of galasa_run index failed on CouchDB server due to conflicts, attempted 10 times");
                    }
                    timeService.sleepMillis(1000 + new Random().nextInt(3000));
                    checkIndex(httpClient, rasUri, attempts, dbName, field, timeService);
                    return;
                }

                if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                    throw new CouchdbException(
                            "Update of galasa_run index failed on CouchDB server - " + statusLine.toString());
                }

            } catch (CouchdbException e) {
                throw e;
            } catch (Exception e) {
                throw new CouchdbException("Update of galasa_run index faile", e);
            }
        }

    }

}
