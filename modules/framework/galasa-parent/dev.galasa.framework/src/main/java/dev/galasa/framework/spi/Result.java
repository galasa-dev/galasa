/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.IResult;

public class Result implements IResult {

    public static final String CANCELLED   = "Cancelled";

    private static final String IGNORED     = "Ignored";
    private static final String PASSED      = "Passed";
    private static final String FAILED      = "Failed";
    private static final String ENVFAIL     = "EnvFail";

    private String              name;
    private String              reason;
    private String              iconUri     = "internalicon:unknown";

    private Throwable           throwable;

    private boolean             passed      = false;
    private boolean             failed      = false;
    private boolean             defects     = false;
    private boolean             ignored     = false;
    private boolean             cancelled   = false;
    private boolean             environment = false;
    private boolean             resources   = false;
    private boolean             fullStop    = false;

    private Result() {
    }

    public static List<String> getDefaultResultNames(){
        // Returns a list of the default result values a test can have
        List<String> resultNames = new ArrayList<String>();
        resultNames.add(IGNORED);
        resultNames.add(PASSED);
        resultNames.add(FAILED);
        resultNames.add(ENVFAIL);
        resultNames.add(CANCELLED);
        return resultNames;
    }

    public static Result passed() {
        Result result = new Result();
        result.name = PASSED;
        result.passed = true;
        result.iconUri = "internalicon:passed";

        return result;
    }

    public static Result failed(Throwable t) {
        Result result = new Result();
        result.name = FAILED;
        result.failed = true;
        result.fullStop = true;
        result.iconUri = "internalicon:failed";
        result.reason = t.getMessage();
        result.throwable = t;

        return result;
    }

    public static Result envfail(Throwable t) {
        Result result = new Result();
        result.name = ENVFAIL;
        result.failed = true;
        result.environment = true;
        result.fullStop = true;
        result.iconUri = "internalicon:envfail";
        result.reason = t.getMessage();
        result.throwable = t;

        return result;
    }

    public static Result failed(String reason) {
        Result result = new Result();
        result.name = FAILED;
        result.failed = true;
        result.fullStop = true;
        result.iconUri = "internalicon:failed";
        result.reason = reason;

        return result;
    }

    public static Result ignore(String reason) {
        Result result = new Result();
        result.name = IGNORED;
        result.ignored = true;
        result.iconUri = "internalicon:ignored";
        result.reason = reason;

        return result;
    }

    public static Result cancelled(String reason) {
        Result result = new Result();
        result.name = CANCELLED;
        result.cancelled = true;
        result.iconUri = "internalicon:cancelled";
        result.reason = reason;

        return result;
    }

    public static Result custom(String name, 
            boolean passed, 
            boolean failed, 
            boolean defects, 
            boolean ignored, 
            boolean environment, 
            boolean resources, 
            boolean fullStop, 
            String reason) {
        Result result = new Result();
        result.name        = name;
        result.passed      = passed;
        result.failed      = failed;
        result.defects     = defects;
        result.ignored     = ignored;
        result.environment = environment;
        result.resources   = resources;
        result.fullStop    = fullStop;
        result.iconUri     = "internalicon:custom";
        result.reason      = reason;

        return result;
    }

    public @NotNull String getName() {
        return this.name;
    }
    
    public String getReason() {
        return this.reason;
    }

    @Override
    public boolean isPassed() {
        return this.passed;
    }

    @Override
    public boolean isFailed() {
        return this.failed;
    }

    public boolean isDefects() {
        return this.defects;
    }

    public boolean isEnvFail() {
        return this.environment;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isFullStop() {
        return this.fullStop;
    }

    public boolean isIgnored() {
        return this.ignored;
    }

    public boolean isResources() {
        return this.resources;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

}