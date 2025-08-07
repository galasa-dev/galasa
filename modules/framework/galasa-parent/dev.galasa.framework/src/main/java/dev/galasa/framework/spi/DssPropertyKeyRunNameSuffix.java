package dev.galasa.framework.spi;


/**
 * An enum of Dss property suffixes pertaining to a single run.
 * 
 * These are used by the Galasa framework.
 * 
 * The general format of the whole dss key is "run.{runName}.{suffix}"
 * 
 * This is a list of all the suffixes used.
 */
public enum DssPropertyKeyRunNameSuffix{
    ALLOCATE_TIMEOUT("allocate.timeout"),
    ALLOCATED("allocated"),
    CONTROLLER("controller"),
    FINISHED_DATETIME("finished"),
    GHERKIN("gherkin"),
    GROUP("group"),
    HEARTBEAT("heartbeat"),
    INTERRUPT_REASON("interruptReason"),
    INTERRUPTED_AT("interruptedAt"),
    LOCAL("local"),
    METHOD_CURRENT("method.current"),
    METHOD_NAME("method.name"),
    METHOD_TOTAL("method.total"),
    OBR("obr"),
    OVERRIDES("overrides"),
    RAS_ACTIONS("rasActions"),
    RAS_RUN_ID("rasrunid"),
    REPOSITORY("repository"),
    RESULT("result"),
    REQUEST_TYPE("request.type"),
    REQUESTOR("requestor"),
    QUEUED("queued"),
    SHARED_ENVIRONMENT("shared.environment"),
    STATUS("status"),
    STREAM("stream"),
    SUBMISSION_ID("submissionId"),
    TAGS("tags"),
    TEST("test"), 
    TEST_BUNDLE("testbundle"),
    TEST_CLASS("testclass"),
    TRACE("trace"),
    WAIT_UNTIL("wait.until"),
    ;

    private String name ;

    private DssPropertyKeyRunNameSuffix(String name) {
        this.name = name ;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return name ;
    }

}