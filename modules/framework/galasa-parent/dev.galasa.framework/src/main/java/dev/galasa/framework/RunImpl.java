/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DssPropertyKeyRunNameSuffix;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

public class RunImpl implements IRun {

    private final String  name;
    private final Instant heartbeat;
    private final String  type;
    private final String  group;
    private final String  submissionId;
    private final String  test;
    private final String  bundleName;
    private final String  testName;
    private final String  gherkin;
    private final String  status;
    private final String  result;
    private final Instant queued;
    private final Instant finished;
    private final Instant waitUntil;
    private final String  requestor;
    private final String  stream;
    private final String  repo;
    private final String  obr;
    private final boolean local;
    private final boolean trace;
    private final boolean sharedEnvironment;
    private final String  rasRunId;
    private final String  interruptReason;
    private final Instant interruptedAt;
    private final Instant allocatedTimeout;
    private List<RunRasAction> rasActions = new ArrayList<>();
    private final Set<String> tags;

    private static final Log logger = LogFactory.getLog(RunImpl.class);
    private static final GalasaGson gson = new GalasaGson();

    public RunImpl(String name, IDynamicStatusStoreService dss) throws DynamicStatusStoreException {
        this.name = name;

        String prefix = "run." + name + ".";

        Map<String, String> runProperties = dss.getPrefix("run." + this.name);

        String sHeartbeat = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.HEARTBEAT);
        if (sHeartbeat != null) {
            this.heartbeat = Instant.parse(sHeartbeat);
        } else {
            this.heartbeat = null;
        }

        type = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.REQUEST_TYPE);
        test = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.TEST);
        status = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.STATUS);
        result = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.RESULT);
        requestor = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.REQUESTOR);
        stream = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.STREAM);
        repo = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.REPOSITORY);
        obr = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.OBR);
        group = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.GROUP);
        submissionId = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.SUBMISSION_ID);
        rasRunId = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.RAS_RUN_ID);
        interruptReason = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.INTERRUPT_REASON);
        local = Boolean.parseBoolean(runProperties.get(prefix + DssPropertyKeyRunNameSuffix.LOCAL));
        trace = Boolean.parseBoolean(runProperties.get(prefix + DssPropertyKeyRunNameSuffix.TRACE));
        sharedEnvironment = Boolean.parseBoolean(runProperties.get(prefix + DssPropertyKeyRunNameSuffix.SHARED_ENVIRONMENT));
        gherkin = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.GHERKIN);
        tags = getTagsFromDss(runProperties, prefix);
        interruptedAt = getInterruptedAtTimeFromDss(runProperties, prefix);
        allocatedTimeout = getAllocatedTimeoutFromDss(runProperties, prefix);

        String encodedRasActions = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.RAS_ACTIONS);
        if (encodedRasActions != null) {
            this.rasActions = getRasActionsFromEncodedString(encodedRasActions);
        }

        String sQueued = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.QUEUED);
        if (sQueued != null) {
            this.queued = Instant.parse(sQueued);
        } else {
            if ("queued".equals(this.status)) {
                this.queued = Instant.now();
            } else {
                this.queued = null;
            }
        }

        String sFinished = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.FINISHED_DATETIME);
        if (sFinished != null) {
            this.finished = Instant.parse(sFinished);
        } else {
            this.finished = null;
        }

        String sWaitUntil = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.WAIT_UNTIL);
        if (sWaitUntil != null) {
            this.waitUntil = Instant.parse(sWaitUntil);
        } else {
            this.waitUntil = null;
        }

        if (test != null) {
            if(gherkin != null) {
                this.bundleName = null;
                this.testName = null;
            } else {
                String[] split = test.split("/");
                this.bundleName = split[0];
                this.testName = split[1];
            }
        } else {
            this.bundleName = null;
            this.testName = null;
        }

        logger.info("RunImpl created: "+this.toString());
    }

    private Instant getInterruptedAtTimeFromDss(Map<String, String> runProperties, String prefix) {
        return getTimeValueFromDss(runProperties, prefix + DssPropertyKeyRunNameSuffix.INTERRUPTED_AT);
    }

    private Instant getAllocatedTimeoutFromDss(Map<String, String> runProperties, String prefix) {
        return getTimeValueFromDss(runProperties, prefix + DssPropertyKeyRunNameSuffix.ALLOCATE_TIMEOUT);
    }

    private Instant getTimeValueFromDss(Map<String, String> runProperties, String propertyKey) {
        Instant timeToReturn = null;
        String timeAsStr = runProperties.get(propertyKey);
        if (timeAsStr != null) {
            timeToReturn = Instant.parse(timeAsStr);
        }
        return timeToReturn;
    }

    private Set<String> getTagsFromDss(Map<String, String> runProperties, String prefix) {
        Set<String> tags = new HashSet<String>();
        try {
            String tagsAsString = runProperties.get(prefix + DssPropertyKeyRunNameSuffix.TAGS);
            if (tagsAsString!= null && !tagsAsString.trim().isEmpty()) {
                HashSet<?> tagSetOfObj = gson.fromJson(tagsAsString, HashSet.class);
                for( Object entry : tagSetOfObj) {
                    // Tags are always going to be strings, so we can safely cast as a string,
                    // but do an instanceof check to keep the compiler happy.
                    if( entry instanceof String) {
                        tags.add((String)entry);
                    }
                }
            }
        } catch( Exception ex) {
            logger.error("Failed to de-serialise tags from dss. ",ex);
            // We don't want to fail the entire run because of this, so 
            // we will forget any tags which may have been in the dss test structure.
        }
        logger.info("test tags retrieved from dss: "+tags.toString());
        return tags;
    }

    private List<RunRasAction> getRasActionsFromEncodedString(String encodedRasActions) {
        byte[] rasActionsJsonBytes = Base64.getDecoder().decode(encodedRasActions);
        String rasActionsJsonStr = new String(rasActionsJsonBytes, StandardCharsets.UTF_8);
        RunRasAction[] rasActionsArr = gson.fromJson(rasActionsJsonStr, RunRasAction[].class);
        return Arrays.asList(rasActionsArr);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Instant getHeartbeat() {
        return this.heartbeat;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getTest() {
        return test;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getRequestor() {
        return requestor;
    }

    @Override
    public String getStream() {
        return stream;
    }

    @Override
    public String getTestBundleName() {
        return this.bundleName;
    }

    @Override
    public String getTestClassName() {
        return this.testName;
    }

    @Override
    public boolean isLocal() {
        return this.local;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public String getSubmissionId() {
        return this.submissionId;
    }

    @Override
    public Set<String> getTags() {
        return this.tags;
    }

    @Override
    public Instant getQueued() {
        return this.queued;
    }

    @Override
    public String getRepository() {
        return this.repo;
    }

    @Override
    public String getOBR() {
        return this.obr;
    }

    @Override
    public boolean isTrace() {
        return this.trace;
    }

    @Override
    public Instant getFinished() {
        return this.finished;
    }

    @Override
    public Instant getWaitUntil() {
        return this.waitUntil;
    }

    @Override
    public Run getSerializedRun() {
        return new Run(name, heartbeat, type, group, test, bundleName, testName, status, result, queued,
                finished, waitUntil, requestor, stream, repo, obr, local, trace, rasRunId, submissionId, tags);
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public boolean isSharedEnvironment() {
        return this.sharedEnvironment;
    }

    @Override
    public String getGherkin() {
        return this.gherkin;
    }
    
    public String getRasRunId() {
        return this.rasRunId;
    }

    @Override
    public String getInterruptReason() {
        return this.interruptReason;
    }

    @Override
    public Instant getInterruptedAt() {
        return this.interruptedAt;
    }

    @Override
    public Instant getAllocatedTimeout() {
        return allocatedTimeout;
    }

    @Override
    public List<RunRasAction> getRasActions() {
        return this.rasActions;
    }
    public String toString() {
        ByteArrayOutputStream buffArray = new ByteArrayOutputStream();
        PrintWriter buff = new PrintWriter(buffArray);

        buff.append("Run:");

        if (this.name==null) {
            buff.append(" name: null");
        } else {
            buff.append(" name: "+this.name);
        }

        if (this.heartbeat == null) {
            buff.append(" heartbeat: null");
        } else {
            buff.append(" heartbeat: "+heartbeat.toString());
        }

        buff.append(" type: "+type);
        buff.append(" group: "+group);
        buff.append(" submissionId: "+submissionId);
        buff.append(" test: "+test);
        buff.append(" bundleName: "+bundleName);


        buff.append(" testName: "+testName);
        buff.append(" gherkin: "+gherkin);
        buff.append(" status: "+status);
        buff.append(" result: "+result);


        if (this.queued == null) {
            buff.append(" queued: null");
        } else {
            buff.append(" queued: "+queued.toString());
        }

        if (this.finished == null) {
            buff.append(" finished: null");
        } else {
            buff.append(" finished: "+finished.toString());
        }

        if (this.waitUntil == null) {
            buff.append(" waitUntil: null");
        } else {
            buff.append(" waitUntil: "+waitUntil.toString());
        }

        if (this.interruptedAt == null) {
            buff.append(" interruptedAt: null");
        } else {
            buff.append(" interruptedAt: "+interruptedAt.toString());
        }

        buff.append(" interruptReason: "+interruptReason);
        buff.append(" requestor: "+requestor);
        buff.append(" stream: "+stream);
        buff.append(" repo: "+repo);
        buff.append(" obr: "+obr);
        buff.append(" local: "+Boolean.toString(local));
        buff.append(" trace: "+Boolean.toString(trace));
        buff.append(" sharedEnvironment: "+Boolean.toString(sharedEnvironment));
        buff.append(" rasRunId: "+rasRunId);
        buff.append(" bundleName: "+bundleName);
        
        buff.append(" tags: [");
        boolean isFirst = true;
        for( String tag : tags ) {
            if (isFirst) {
                isFirst = false ;
            } else {
                buff.append(",");
            }
            buff.append(tag);
        }
        buff.append("]");

        buff.flush();
        return buffArray.toString();
    }

    /**
     * Create a new test structure, and populate it with as much information as we can from the run.
     * @return A TestStructure which is written into the RAS eventually.
     */
    @Override
    public TestStructure toTestStructure() {
        TestStructure testStructure = new TestStructure();

        String bundleName = getTestBundleName();
        String testName = getTestClassName();
        String runName = getName();
        String group = getGroup();
        String submissionId = getSubmissionId();
        Instant queuedAt = getQueued();
        String requestor = AbstractManager.defaultString(getRequestor(), "unknown");

        if (testName != null) {
            // The test name is in the form "package.class", so get the class after the last "."
            String trimmedTestName = testName.trim();
            int lastDotIndex = trimmedTestName.lastIndexOf(".");
            if (lastDotIndex != -1 && (lastDotIndex + 1) < trimmedTestName.length()) {
                String testShortName = testName.substring(lastDotIndex + 1);
                testStructure.setTestShortName(testShortName);
            }
        }

        testStructure.setBundle(bundleName);
        testStructure.setTestName(testName);
        testStructure.setQueued(queuedAt);
        testStructure.setRunName(runName);
        testStructure.setRequestor(requestor);
        testStructure.setGroup(group);
        testStructure.setSubmissionId(submissionId);
        testStructure.setLogRecordIds(new ArrayList<>());
        testStructure.setArtifactRecordIds(new ArrayList<>());
        testStructure.normalise();

        for (String tag : getTags()) {
            testStructure.addTag(tag);
        }

        return testStructure;
    }
}
