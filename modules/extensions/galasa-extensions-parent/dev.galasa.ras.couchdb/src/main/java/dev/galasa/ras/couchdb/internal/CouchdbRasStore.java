/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.commons.logging.Log;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.SystemEnvironment;
import dev.galasa.framework.spi.ras.ResultArchiveStoreFileStore;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.utils.SystemTimeService;
import dev.galasa.extensions.common.api.HttpClientFactory;
import dev.galasa.extensions.common.api.LogFactory;
import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.extensions.common.couchdb.CouchdbStore;
import dev.galasa.extensions.common.couchdb.CouchdbValidator;
import dev.galasa.extensions.common.couchdb.RetryableCouchdbUpdateOperationProcessor;
import dev.galasa.extensions.common.couchdb.pojos.PutPostResponse;
import dev.galasa.extensions.common.impl.HttpClientFactoryImpl;
import dev.galasa.extensions.common.impl.HttpRequestFactoryImpl;
import dev.galasa.extensions.common.api.HttpRequestFactory;
import dev.galasa.extensions.common.impl.LogFactoryImpl;
import dev.galasa.ras.couchdb.internal.pojos.Artifacts;
import dev.galasa.ras.couchdb.internal.pojos.LogLines;
import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public class CouchdbRasStore extends CouchdbStore implements IResultArchiveStoreService {

    private static final String COUCHDB_AUTH_ENV_VAR = "GALASA_RAS_TOKEN";
    private static final String COUCHDB_AUTH_TYPE    = "Basic";

    public static final String ARTIFACTS_DB         = "galasa_artifacts";
    public static final String RUNS_DB              = "galasa_run";
    public static final String LOG_DB               = "galasa_log";

    public static final String REQUESTORS_VIEW_NAME       = "requestors-view";
    public static final String RESULT_VIEW_NAME           = "result-view";
    public static final String TEST_NAMES_VIEW_NAME       = "testnames-view";
    public static final String BUNDLE_TESTNAMES_VIEW_NAME = "bundle-testnames-view";
    public static final String RUN_NAMES_VIEW_NAME        = "runnames-view";
    public static final String RUN_GROUP_VIEW_NAME        = "group-view";

    public static final String COUCHDB_RUN_ID_PREFIX = "cdb-";

    private final Log                          logger            ;

    private final IFramework                   framework;                                         // NOSONAR

    private boolean                            shutdown           = false;

    private final GalasaGson                   gson               = new GalasaGson();

    private final CouchdbRasFileSystemProvider provider;

    private final IRun                         run;
    private String                             runDocumentId;
    private String                             runDocumentRevision;

    private long                               logOrder           = 0;

    private final ArrayList<String>            logCache           = new ArrayList<>(100);

    private ArrayList<String>                  logIds             = new ArrayList<>();
    private ArrayList<String>                  artifactDocumentId = new ArrayList<>();;
    private String                             artifactDocumentRev;

    private TestStructure                      lastTestStructure;
    private ITimeService timeService ;

    private LogFactory logFactory;

    public CouchdbRasStore(IFramework framework, URI rasUri) throws CouchdbException, CouchdbRasException {
        this(
            framework,
            rasUri,
            new HttpClientFactoryImpl(),
            new CouchdbValidatorImpl(),
            new LogFactoryImpl(),
            new HttpRequestFactoryImpl(COUCHDB_AUTH_TYPE, new SystemEnvironment().getenv(COUCHDB_AUTH_ENV_VAR)),
            new SystemTimeService()
        );
    }

    // Note: We use logFactory here so we can propogate it downwards during unit testing.
    public CouchdbRasStore(IFramework framework, URI rasUri, HttpClientFactory httpFactory , CouchdbValidator validator,
        LogFactory logFactory, HttpRequestFactory requestFactory, ITimeService timeService
    ) throws CouchdbException {
        super(rasUri, requestFactory, httpFactory);
        this.logFactory = logFactory;
        this.logger = logFactory.getLog(getClass());
        this.framework = framework;
        this.timeService = timeService;
         // *** Validate the connection to the server and it's version

        validator.checkCouchdbDatabaseIsValid(this.storeUri,this.httpClient, this.httpRequestFactory, timeService);

        this.run = this.framework.getTestRun();

        // *** If this is a run, ensure we can create the run document
        if (this.run != null) {
            lastTestStructure = new TestStructure();
            lastTestStructure.setRunName(this.run.getName());
            try {
                updateTestStructure(lastTestStructure);
            } catch (ResultArchiveStoreException e) {
                throw new CouchdbException("Validation failed - unable to create initial run document", e);
            }

            createArtifactDocument();
        }

        ResultArchiveStoreFileStore fileStore = new ResultArchiveStoreFileStore();
        this.provider = new CouchdbRasFileSystemProvider(fileStore, this, this.logFactory);
    }

    // Protected so that we can create artifact documents from elsewhere.
    protected void createArtifactDocument() throws CouchdbException {
        Artifacts artifacts = new Artifacts();
        createArtifactDocument(artifacts);
    }

    protected void createArtifactDocument(Artifacts artifacts) throws CouchdbException {

        artifacts.runId = this.runDocumentId;
        artifacts.runName = this.run.getName();

        String jsonArtifacts = gson.toJson(artifacts);
        PutPostResponse putPostResponse = createDocument(ARTIFACTS_DB, jsonArtifacts);
        this.artifactDocumentId.add(putPostResponse.id);
        this.artifactDocumentRev = putPostResponse.rev;
    }

    @Override
    public void writeLog(@NotNull String message) throws ResultArchiveStoreException {
        if (this.run == null) {
            throw new ResultArchiveStoreException("Not a run");
        }

        String[] lines = message.split("\r\n?|\n");

        synchronized (logCache) {
            logCache.addAll(Arrays.asList(lines));
            if (logCache.size() >= 100) {
                flushLogCache();
            }
        }
    }

    private void flushLogCache() throws ResultArchiveStoreException {
        LogLines logLines = new LogLines();
        synchronized (logCache) {
            if (logCache.isEmpty()) {
                return;
            }
            logLines.lines = new ArrayList<>(logCache);
            logOrder++;
            logLines.order = logOrder;

            logCache.clear();
        }
        logLines.runName = this.run.getName();
        logLines.runId = this.runDocumentId;

        String jsonStructure = gson.toJson(logLines);

        HttpPost request = httpRequestFactory.getHttpPostRequest(this.storeUri + "/"+LOG_DB);
        request.setEntity(new StringEntity(jsonStructure, StandardCharsets.UTF_8));

        try{
            String entity = sendHttpRequest(request, HttpStatus.SC_CREATED);
            PutPostResponse putPostResponse = gson.fromJson(entity, PutPostResponse.class);
            if (putPostResponse.id == null || putPostResponse.rev == null) {
                throw new CouchdbException("Unable to store the test structure - Invalid JSON response");
            }

            this.logIds.add(putPostResponse.id);
            this.updateTestStructure(lastTestStructure);
        } catch (CouchdbException e) {
            throw new ResultArchiveStoreException(e);
        } catch (Exception e) {
            throw new ResultArchiveStoreException("Unable to store the test log", e);
        }
    }

    @Override
    public void writeLog(@NotNull List<String> messages) throws ResultArchiveStoreException {
        if (this.run == null) {
            throw new ResultArchiveStoreException("Not a run");
        }

        for (String message : messages) {
            writeLog(message);
        }
    }

    @Override
    public synchronized void updateTestStructure(@NotNull String runId, @NotNull TestStructure testStructure)
            throws ResultArchiveStoreException {

        TestStructureCouchdb couchdbTestStructure = (TestStructureCouchdb) testStructure;

        String documentId = runId;
        if (runId.startsWith(COUCHDB_RUN_ID_PREFIX)) {
            documentId = runId.substring(COUCHDB_RUN_ID_PREFIX.length());
        }

        String revision = couchdbTestStructure._rev;
        if (revision == null) {
            throw new ResultArchiveStoreException("Failed to get run document revision");
        }

        String jsonStructure = gson.toJson(testStructure);
        HttpEntityEnclosingRequestBase request = httpRequestFactory.getHttpPutRequest(this.storeUri + "/"+RUNS_DB+"/" + documentId);
        request.setHeader("If-Match", revision);
        request.setEntity(new StringEntity(jsonStructure, StandardCharsets.UTF_8));

        RetryableCouchdbUpdateOperationProcessor retryProcessor = new RetryableCouchdbUpdateOperationProcessor(timeService, logFactory);
        try {
            retryProcessor.retryCouchDbUpdateOperation(() -> {            
                String entity = sendHttpRequest(request, HttpStatus.SC_CREATED);
                PutPostResponse putPostResponse = gson.fromJson(entity, PutPostResponse.class);
                if (putPostResponse.id == null || putPostResponse.rev == null) {
                    throw new CouchdbException("Unable to update the test structure - Invalid JSON response");
                }
            });
        } catch (CouchdbException e) {
            throw new ResultArchiveStoreException("Failed to update test structure", e);
        }
    }

    @Override
    public synchronized void updateTestStructure(@NotNull TestStructure testStructure)
            throws ResultArchiveStoreException {
        if (this.run == null) {
            throw new ResultArchiveStoreException("Not a run");
        }

        this.lastTestStructure = testStructure;
        this.lastTestStructure.setLogRecordIds(this.logIds);
        this.lastTestStructure.setArtifactRecordIds(this.artifactDocumentId);
        this.lastTestStructure.normalise();

        String jsonStructure = gson.toJson(testStructure);

        HttpEntityEnclosingRequestBase request;
        if (runDocumentId == null) {
            request = httpRequestFactory.getHttpPostRequest(this.storeUri + "/"+RUNS_DB);
        } else {
            request = httpRequestFactory.getHttpPutRequest(this.storeUri + "/"+RUNS_DB+"/" + runDocumentId);
            request.setHeader("If-Match", runDocumentRevision);
        }
        request.setEntity(new StringEntity(jsonStructure, StandardCharsets.UTF_8));

        try{
            String entity = sendHttpRequest(request, HttpStatus.SC_CREATED);
            PutPostResponse putPostResponse = gson.fromJson(entity, PutPostResponse.class);
                if (putPostResponse.id == null || putPostResponse.rev == null) {
                    throw new CouchdbException("Unable to store the test structure - Invalid JSON response");
                }
                this.runDocumentId = putPostResponse.id;
                this.runDocumentRevision = putPostResponse.rev;
        } catch (CouchdbException e){
            throw new ResultArchiveStoreException(e);
        }
    }

    public void retrieveArtifact(CouchdbArtifactPath path, Path cachePath) throws CouchdbException {
        String artifactRecordId = path.getArtifactRecordId();
        String encodedPath;
        try {
            encodedPath = URLEncoder.encode(path.toString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new CouchdbException("Problem encoding artifact path", e);
        }

        String artifactURI= this.storeUri + "/"+ARTIFACTS_DB+"/" + artifactRecordId + "/" + encodedPath;
        retrieveArtifactFromDatabase(artifactURI, cachePath,StandardCopyOption.REPLACE_EXISTING);
    }

    public String getLog(TestStructure ts) throws ResultArchiveStoreException {
        StringBuilder sb = new StringBuilder();

        for (String logRecordId : ts.getLogRecordIds()) {
            HttpGet httpGet = httpRequestFactory.getHttpGetRequest(this.storeUri + "/"+LOG_DB+"/" + logRecordId);

            try{
                String entity = sendHttpRequest(httpGet, HttpStatus.SC_OK);
                LogLines logLines = gson.fromJson(entity, LogLines.class);
                if (logLines.lines != null) {
                    for (String line : logLines.lines) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(line);
                    }
                }
            } catch (CouchdbException e) {
                throw new ResultArchiveStoreException(e);
            } catch (Exception e) {
                throw new ResultArchiveStoreException("Unable to find runs", e);
            }
        }
        return sb.toString();
    }

    @Override
    public Path getStoredArtifactsRoot() {
        if (this.run == null) {
            return null;
        }
        return provider.getActualFileSystem().getPath("/");
    }

    @Override
    public void flush() {
        try {
            flushLogCache();
        } catch (ResultArchiveStoreException e) {
            logger.error("Error with heartbeat flush", e);
        }
    }

    @Override
    public void shutdown() {
        this.shutdown = true;
        try {
            flushLogCache();
        } catch (ResultArchiveStoreException e) {
            logger.error("Error with shutdown flush", e);
        }

        try {
            this.httpClient.close();
        } catch (IOException e) {
        }
    }

    protected boolean isShutdown() {
        return this.shutdown;
    }

    public CloseableHttpClient getHttpClient() {
        return this.httpClient;
    }

    public String getArtifactDocumentId() {
        return this.artifactDocumentId.get(0);
    }

    public String getArtifactDocumentRev() {
        return this.artifactDocumentRev;
    }

    public URI getCouchdbUri() {
        return this.storeUri;
    }

    public GalasaGson getGson() {
        return this.gson;
    }

    public void updateArtifactDocumentRev(String newArtifactDocumentRev) {
        this.artifactDocumentRev = newArtifactDocumentRev;
    }

    @Override
    public @NotNull List<IResultArchiveStoreDirectoryService> getDirectoryServices() {
        ArrayList<IResultArchiveStoreDirectoryService> dirs = new ArrayList<>();
        dirs.add(new CouchdbDirectoryService(this, this.logFactory, this.httpRequestFactory));
        return dirs;
    }

    @Override
    public String calculateRasRunId() {

        if (this.runDocumentId == null) {
            return null;
        }
        return "cdb-" + this.runDocumentId;
    }

   public HttpRequestFactory getRequestFactory() {
     return this.httpRequestFactory;
   }
}
