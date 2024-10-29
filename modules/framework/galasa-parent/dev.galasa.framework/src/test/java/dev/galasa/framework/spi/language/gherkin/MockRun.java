/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.time.Instant;


import dev.galasa.api.run.Run;
import dev.galasa.framework.spi.IRun;

public class MockRun implements IRun {

    private String gherkinFeatureText;

    @Override
    public String getGherkin() {
        return gherkinFeatureText;
    }

    public void setGherkin(String featureText) {
        this.gherkinFeatureText = featureText;
    }

    @Override
    public String getName() {
               throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public Instant getHeartbeat() {
               throw new UnsupportedOperationException("Unimplemented method 'getHeartbeat'");
    }

    @Override
    public String getType() {
               throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

    @Override
    public String getTest() {
               throw new UnsupportedOperationException("Unimplemented method 'getTest'");
    }

    @Override
    public String getStatus() {
               throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
    }

    @Override
    public String getRequestor() {
               throw new UnsupportedOperationException("Unimplemented method 'getRequestor'");
    }

    @Override
    public String getStream() {
               throw new UnsupportedOperationException("Unimplemented method 'getStream'");
    }

    @Override
    public String getTestBundleName() {
               throw new UnsupportedOperationException("Unimplemented method 'getTestBundleName'");
    }

    @Override
    public String getTestClassName() {
               throw new UnsupportedOperationException("Unimplemented method 'getTestClassName'");
    }

    @Override
    public boolean isLocal() {
               throw new UnsupportedOperationException("Unimplemented method 'isLocal'");
    }

    @Override
    public String getGroup() {
               throw new UnsupportedOperationException("Unimplemented method 'getGroup'");
    }

    @Override
    public Instant getQueued() {
               throw new UnsupportedOperationException("Unimplemented method 'getQueued'");
    }

    @Override
    public String getRepository() {
               throw new UnsupportedOperationException("Unimplemented method 'getRepository'");
    }

    @Override
    public String getOBR() {
               throw new UnsupportedOperationException("Unimplemented method 'getOBR'");
    }

    @Override
    public boolean isTrace() {
               throw new UnsupportedOperationException("Unimplemented method 'isTrace'");
    }

    @Override
    public Instant getFinished() {
               throw new UnsupportedOperationException("Unimplemented method 'getFinished'");
    }

    @Override
    public Instant getWaitUntil() {
               throw new UnsupportedOperationException("Unimplemented method 'getWaitUntil'");
    }

    @Override
    public Run getSerializedRun() {
               throw new UnsupportedOperationException("Unimplemented method 'getSerializedRun'");
    }

    @Override
    public String getResult() {
               throw new UnsupportedOperationException("Unimplemented method 'getResult'");
    }

    @Override
    public boolean isSharedEnvironment() {
               throw new UnsupportedOperationException("Unimplemented method 'isSharedEnvironment'");
    }


    
}
