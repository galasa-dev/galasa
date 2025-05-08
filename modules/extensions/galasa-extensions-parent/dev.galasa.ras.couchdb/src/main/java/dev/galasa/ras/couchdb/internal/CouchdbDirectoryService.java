/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import static dev.galasa.extensions.common.Errors.*;
import static dev.galasa.ras.couchdb.internal.CouchdbRasStore.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasRunResultPage;
import dev.galasa.framework.spi.ras.RasSortField;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.ras.ResultArchiveStoreFileStore;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.extensions.common.api.LogFactory;
import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.extensions.common.couchdb.pojos.Row;
import dev.galasa.extensions.common.couchdb.pojos.ViewResponse;
import dev.galasa.extensions.common.couchdb.pojos.ViewRow;
import dev.galasa.extensions.common.api.HttpRequestFactory;
import dev.galasa.ras.couchdb.internal.pojos.Find;
import dev.galasa.ras.couchdb.internal.pojos.FoundRuns;
import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public class CouchdbDirectoryService implements IResultArchiveStoreDirectoryService {

    private final Log logger;
    private final LogFactory logFactory;
    private final HttpRequestFactory requestFactory;

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private final CouchdbRasStore store;
    private final GalasaGson gson;

    private static final int COUCHDB_RESULTS_LIMIT_PER_QUERY = 100;
    private final CouchdbRasQueryBuilder rasQueryBuilder = new CouchdbRasQueryBuilder();

    public CouchdbDirectoryService(CouchdbRasStore store, LogFactory logFactory, HttpRequestFactory requestFactory) {
        this.store = store;
        this.logFactory = logFactory;
        this.logger = logFactory.getLog(getClass());
        this.requestFactory = requestFactory;
        this.gson = store.getGson();
    }

    @Override
    public @NotNull String getName() {
        return "CouchDB - " + store.getCouchdbUri();
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    private CouchdbRasFileSystemProvider createFileSystemProvider() {
        ResultArchiveStoreFileStore fileStore = new ResultArchiveStoreFileStore();
        return new CouchdbRasFileSystemProvider(fileStore, store, logFactory);
    }

    public Path getRunArtifactPath(TestStructureCouchdb ts) throws CouchdbRasException {
        CouchdbRasFileSystemProvider runProvider = createFileSystemProvider();
        if (ts.getArtifactRecordIds() == null || ts.getArtifactRecordIds().isEmpty()) {
            return runProvider.getRoot();
        }

        for (String artifactRecordId : ts.getArtifactRecordIds()) {
            HttpGet httpGet = requestFactory
                    .getHttpGetRequest(store.getCouchdbUri() + "/galasa_artifacts/" + artifactRecordId);

            try (CloseableHttpResponse response = store.getHttpClient().execute(httpGet)) {
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_NOT_FOUND) { // TODO Ignore it for now
                    continue;
                }
                if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                    throw new CouchdbRasException("Unable to find artifacts - " + statusLine.toString());
                }

                HttpEntity entity = response.getEntity();
                String responseEntity = EntityUtils.toString(entity);
                JsonObject artifactRecord = gson.fromJson(responseEntity, JsonObject.class);

                JsonElement attachmentsElement = artifactRecord.get("_attachments");

                if (attachmentsElement != null) {
                    if (attachmentsElement instanceof JsonObject) {
                        JsonObject attachments = (JsonObject) attachmentsElement;
                        Set<Entry<String, JsonElement>> entries = attachments.entrySet();
                        if (entries != null) {
                            for (Entry<String, JsonElement> entry : entries) {
                                JsonElement elem = entry.getValue();
                                if (elem instanceof JsonObject) {
                                    runProvider.addPath(new CouchdbArtifactPath(runProvider.getActualFileSystem(),
                                            entry.getKey(), (JsonObject) elem, artifactRecordId));
                                }
                            }
                        }
                    }
                }
            } catch (CouchdbRasException e) {
                throw e;
            } catch (Exception e) {
                throw new CouchdbRasException("Unable to find runs", e);
            }
        }

        return runProvider.getRoot();
    }

    private @NotNull List<IRunResult> getAllRuns() throws ResultArchiveStoreException {

        ArrayList<IRunResult> runs = new ArrayList<>();

        HttpGet httpGet = requestFactory.getHttpGetRequest(store.getCouchdbUri() + "/" + RUNS_DB + "/_all_docs");

        try (CloseableHttpResponse response = store.getHttpClient().execute(httpGet)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new CouchdbRasException("Unable to find runs - " + statusLine.toString());
            }

            HttpEntity entity = response.getEntity();
            String responseEntity = EntityUtils.toString(entity);
            FoundRuns found = gson.fromJson(responseEntity, FoundRuns.class);
            if (found.rows == null) {
                throw new CouchdbRasException("Unable to find rows - Invalid JSON response");
            }

            if (found.warning != null) {
                logger.warn("CouchDB warning detected - " + found.warning);
            }

            for (Row row : found.rows) {
                CouchdbRunResult cdbrr = fetchRun(row.id);
                if (cdbrr != null) {
                    if (cdbrr.getTestStructure() != null && cdbrr.getTestStructure().isValid()) {
                        runs.add(cdbrr);
                    }
                }
            }
        } catch (CouchdbRasException e) {
            throw e;
        } catch (Exception e) {
            throw new ResultArchiveStoreException("Unable to find runs", e);
        }

        return runs;
    }

    private CouchdbRunResult fetchRun(String id) throws ParseException, IOException, ResultArchiveStoreException {
        CouchdbRunResult runResult = null;
        HttpGet httpGet = requestFactory.getHttpGetRequest(store.getCouchdbUri() + "/" + RUNS_DB + "/" + id);

        try (CloseableHttpResponse response = store.getHttpClient().execute(httpGet)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }

            HttpEntity entity = response.getEntity();
            String responseEntity = EntityUtils.toString(entity);
            TestStructureCouchdb ts = gson.fromJson(responseEntity, TestStructureCouchdb.class);

            runResult = new CouchdbRunResult(store, ts, logFactory);
        }
        return runResult;
    }

    @Override
    public @NotNull List<String> getRequestors() throws ResultArchiveStoreException {
        ArrayList<String> requestors = new ArrayList<>();

        HttpGet httpGet = requestFactory.getHttpGetRequest(
                store.getCouchdbUri() + "/" + RUNS_DB + "/_design/docs/_view/" + REQUESTORS_VIEW_NAME + "?group=true");

        try (CloseableHttpResponse response = store.getHttpClient().execute(httpGet)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new CouchdbRasException("Unable to find runs - " + statusLine.toString());
            }

            HttpEntity entity = response.getEntity();
            String responseEntity = EntityUtils.toString(entity);
            ViewResponse view = gson.fromJson(responseEntity, ViewResponse.class);
            if (view.rows == null) {
                throw new CouchdbRasException("Unable to find requestors - Invalid JSON response");
            }

            for (ViewRow row : view.rows) {
                requestors.add(row.key);
            }
        } catch (CouchdbRasException e) {
            throw e;
        } catch (Exception e) {
            throw new ResultArchiveStoreException("Unable to find requestors", e);
        }

        return requestors;
    }

    @Override
    public @NotNull List<String> getResultNames() throws ResultArchiveStoreException {
        ArrayList<String> results = new ArrayList<>();

        HttpGet httpGet = requestFactory.getHttpGetRequest(
                store.getCouchdbUri() + "/" + RUNS_DB + "/_design/docs/_view/" + RESULT_VIEW_NAME + "?group=true");

        try (CloseableHttpResponse response = store.getHttpClient().execute(httpGet)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new CouchdbRasException("Unable to find results - " + statusLine.toString());
            }

            HttpEntity entity = response.getEntity();
            String responseEntity = EntityUtils.toString(entity);
            ViewResponse view = gson.fromJson(responseEntity, ViewResponse.class);
            if (view.rows == null) {
                throw new CouchdbRasException("Unable to find results - Invalid JSON response");
            }

            for (ViewRow row : view.rows) {
                if (row.key != null) {
                    results.add(row.key);
                }
            }
        } catch (CouchdbRasException e) {
            throw e;
        } catch (Exception e) {
            throw new ResultArchiveStoreException("Unable to find results", e);
        }

        return results;

    }

    @Override
    public @NotNull List<RasTestClass> getTests() throws ResultArchiveStoreException {
        ArrayList<RasTestClass> tests = new ArrayList<>();

        HttpGet httpGet = requestFactory.getHttpGetRequest(
                store.getCouchdbUri() + "/" + RUNS_DB + "/_design/docs/_view/" + BUNDLE_TESTNAMES_VIEW_NAME
                        + "?group=true");

        try (CloseableHttpResponse response = store.getHttpClient().execute(httpGet)) {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new CouchdbRasException("Unable to find tests - " + statusLine.toString());
            }

            HttpEntity entity = response.getEntity();
            String responseEntity = EntityUtils.toString(entity);
            ViewResponse view = gson.fromJson(responseEntity, ViewResponse.class);
            if (view.rows == null) {
                throw new CouchdbRasException("Unable to find rows - Invalid JSON response");
            }

            for (ViewRow row : view.rows) {
                String bundleTestname = row.key;
                if (bundleTestname == null) {
                    continue;
                }
                if ("undefined/undefined".equals(bundleTestname)) {
                    continue;
                }

                int posSlash = bundleTestname.indexOf('/');
                if (posSlash < 0) {
                    continue;
                }

                String bundleName = bundleTestname.substring(0, posSlash);
                String testName = bundleTestname.substring(posSlash + 1);

                RasTestClass rasTestClass = new RasTestClass(testName, bundleName);
                tests.add(rasTestClass);
            }
        } catch (CouchdbRasException e) {
            throw e;
        } catch (Exception e) {
            throw new ResultArchiveStoreException("Unable to find tests", e);
        }

        return tests;
    }

    @Override
    public List<IRunResult> getRunsByGroupName(@NotNull String groupName) throws ResultArchiveStoreException {

        List<IRunResult> runs = getRunsFromViewByKey(RUN_GROUP_VIEW_NAME, groupName);
        
        return runs;
    }

    @Override
    public List<IRunResult> getRunsByRunName(@NotNull String runName) throws ResultArchiveStoreException {
         
        List<IRunResult> runs = getRunsFromViewByKey(RUN_NAMES_VIEW_NAME, runName);
        return runs;

    }

    @Override
    public @NotNull RasRunResultPage getRunsPage(int maxResults, RasSortField primarySort, String pageToken,
            @NotNull IRasSearchCriteria... searchCriterias)
            throws ResultArchiveStoreException {

        HttpPost httpPost = requestFactory.getHttpPostRequest(store.getCouchdbUri() + "/" + RUNS_DB + "/_find");

        Find find = new Find();
        find.selector = rasQueryBuilder.buildGetRunsQuery(searchCriterias);
        find.execution_stats = true;
        find.limit = maxResults;
        find.bookmark = pageToken;
        if (primarySort != null) {
            find.sort = buildQuerySortJson(primarySort);
        }

        return getRunsPageFromCouchdb(httpPost, find);
    }

    private RasRunResultPage getRunsPageFromCouchdb(HttpPost httpPost, Find query) throws ResultArchiveStoreException {
        ArrayList<IRunResult> runs = new ArrayList<>();
        RasRunResultPage runsPage = null;
        String requestContent = gson.toJson(query);
        httpPost.setEntity(new StringEntity(requestContent, UTF8));

        try (CloseableHttpResponse response = store.getHttpClient().execute(httpPost)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String responseEntity = EntityUtils.toString(entity);

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new CouchdbRasException("Unable to find runs - " + statusLine.toString());
            }

            FoundRuns found = gson.fromJson(responseEntity, FoundRuns.class);
            if (found.docs == null) {
                throw new CouchdbRasException("Unable to find runs - Invalid JSON response");
            }

            if (found.warning != null) {
                logger.warn("CouchDB warning detected - " + found.warning);
            }

            for (TestStructureCouchdb ts : found.docs) {
                if (ts.isValid()) {

                    // Don't load the artifacts for the found runs, just set a root location for artifacts
                    // and add this run to the results
                    runs.add(new CouchdbRunResult(store, ts, logFactory));
                }
            }

            // CouchDB sometimes returns a 'nil' string as a bookmark to indicate no
            // bookmark,
            // so turn it into an actual null value
            if (found.bookmark != null && found.bookmark.equals("nil")) {
                found.bookmark = null;
            }

            runsPage = new RasRunResultPage(runs, found.bookmark);
        } catch (CouchdbRasException e) {
            throw e;
        } catch (Exception e) {
            throw new ResultArchiveStoreException("Unable to find runs", e);
        }
        return runsPage;
    }

    private JsonArray buildQuerySortJson(@NotNull RasSortField primarySort) {
        JsonArray sort = new JsonArray();

        JsonObject primarySortJson = new JsonObject();
        primarySortJson.addProperty(primarySort.getFieldName(), primarySort.getSortDirection());

        sort.add(primarySortJson);
        return sort;
    }

    @Override
    public @NotNull List<IRunResult> getRuns(@NotNull IRasSearchCriteria... searchCriterias)
            throws ResultArchiveStoreException {

        if (searchCriterias.length == 0) {
            return getAllRuns();
        }

        ArrayList<IRunResult> runs = new ArrayList<>();

        HttpPost httpPost = requestFactory.getHttpPostRequest(store.getCouchdbUri() + "/" + RUNS_DB + "/_find");

        Find find = new Find();
        find.selector = rasQueryBuilder.buildGetRunsQuery(searchCriterias);
        find.execution_stats = true;
        find.limit = COUCHDB_RESULTS_LIMIT_PER_QUERY;

        while (true) {
            RasRunResultPage runsPage = getRunsPageFromCouchdb(httpPost, find);

            List<IRunResult> returnedRuns = runsPage.getRuns();
            if (!returnedRuns.isEmpty()) {
                runs.addAll(returnedRuns);
            } else {
                // No runs were found, so we've reached the end
                break;
            }

            find.bookmark = runsPage.getNextCursor();
        }

        return runs;
    }

    @Override
    public IRunResult getRunById(@NotNull String runId) throws ResultArchiveStoreException {
        if (!runId.startsWith("cdb-")) {
            return null;
        }

        runId = runId.substring(4);

        try {
            return fetchRun(runId);
        } catch (Exception e) {
            return null; // This runid may not belong to this RAS, so ignore all errors
        }
    }

    private List<IRunResult> getRunsFromViewByKey(String viewName, String criteriaValue) throws ResultArchiveStoreException{

        List<IRunResult> runs = new ArrayList<>();

        try {
            boolean includeDocuments = true;
            ViewResponse viewResponse = store.getDocumentsFromDatabaseViewByKey(
                    RUNS_DB,
                    viewName,
                    criteriaValue,
                    includeDocuments);

            if (viewResponse.rows == null) {
                String errorMessage = ERROR_FAILED_TO_GET_VIEW_DOCUMENTS_FROM_DATABASE.getMessage(viewName,
                        RUNS_DB);
                throw new ResultArchiveStoreException(errorMessage);
            }

            for (ViewRow row : viewResponse.rows) {
                if (row.doc != null) {
                    JsonObject testStructureJson = gson.toJsonTree(row.doc).getAsJsonObject();
                    TestStructureCouchdb testStructure = gson.fromJson(testStructureJson, TestStructureCouchdb.class);
                    runs.add(new CouchdbRunResult(store, testStructure, logFactory));
                }
            }
        } catch (CouchdbException e) {
            // This error has a custom message, so pass it up
            throw new ResultArchiveStoreException(e);
        }

        return runs;
    }
}
