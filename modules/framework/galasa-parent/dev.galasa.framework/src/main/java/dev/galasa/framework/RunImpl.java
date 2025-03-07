/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.time.Instant;
import java.util.Map;

import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IRun;

public class RunImpl implements IRun {

    private final String  name;
    private final Instant heartbeat;
    private final String  type;
    private final String  group;
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

    public RunImpl(String name, IDynamicStatusStoreService dss) throws DynamicStatusStoreException {
        this.name = name;

        String prefix = "run." + name + ".";

        Map<String, String> runProperties = dss.getPrefix("run." + this.name);

        String sHeartbeat = runProperties.get(prefix + "heartbeat");
        if (sHeartbeat != null) {
            this.heartbeat = Instant.parse(sHeartbeat);
        } else {
            this.heartbeat = null;
        }

        type = runProperties.get(prefix + "request.type");
        test = runProperties.get(prefix + "test");
        status = runProperties.get(prefix + "status");
        result = runProperties.get(prefix + "result");
        requestor = runProperties.get(prefix + "requestor");
        stream = runProperties.get(prefix + "stream");
        repo = runProperties.get(prefix + "repository");
        obr = runProperties.get(prefix + "obr");
        group = runProperties.get(prefix + "group");
        rasRunId = runProperties.get(prefix + "rasrunid");
        local = Boolean.parseBoolean(runProperties.get(prefix + "local"));
        trace = Boolean.parseBoolean(runProperties.get(prefix + "trace"));
        sharedEnvironment = Boolean.parseBoolean(runProperties.get(prefix + "shared.environment"));
        gherkin = runProperties.get(prefix + "gherkin");

        String sQueued = runProperties.get(prefix + "queued");
        if (sQueued != null) {
            this.queued = Instant.parse(sQueued);
        } else {
            if ("queued".equals(this.status)) {
                this.queued = Instant.now();
            } else {
                this.queued = null;
            }
        }

        String sFinished = runProperties.get(prefix + "finished");
        if (sFinished != null) {
            this.finished = Instant.parse(sFinished);
        } else {
            this.finished = null;
        }

        String sWaitUntil = runProperties.get(prefix + "wait.until");
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
                finished, waitUntil, requestor, stream, repo, obr, local, trace, rasRunId);
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
    
    @Override
    public String getRasRunId() {
        return this.rasRunId;
    }

}
