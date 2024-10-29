/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.framework.ITestRunManagers;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.teststructure.TestGherkinMethod;

/**
 * A GherkinMethod is really a Gherkin Scenario.
 * It has a name, and a number of Statements(steps) which can be executed.
 */
public class GherkinMethod {

    private Log logger  = LogFactory.getLog(GherkinMethod.class);

    private String name;
    private transient List<IGherkinExecutable> executables;
    private String testName;

    private Result result;
    
    private TestGherkinMethod testStructureMethod;

    public GherkinMethod(String name, String testName) {
        this.name = name;
        this.executables = new ArrayList<>();
        this.testName = testName;
        
        this.testStructureMethod = new TestGherkinMethod();
        this.testStructureMethod.setMethodName(name);
    }

    public void addStep(String statement) throws TestRunException {
        this.executables.add(GherkinStatement.get(statement));
    }

    public String getName() {
        return this.name;
    }

    public List<IGherkinExecutable> getExecutables() {
        return this.executables;
    }

    public void invoke(ITestRunManagers managers, Map<String, Object> testVariables) throws TestRunException {
        try {
            managers.startOfTestMethod(new GalasaMethod(this));

            logger.info(GherkinTest.LOG_STARTING + GherkinTest.LOG_START_LINE + GherkinTest.LOG_ASTERS
                    + GherkinTest.LOG_START_LINE + "*** Start of test method " + this.testName + "#"
                    + this.name + GherkinTest.LOG_START_LINE
                    + GherkinTest.LOG_ASTERS);
            testStructureMethod.setStartTime(Instant.now());
            testStructureMethod.setStatus("started");
            
            for(IGherkinExecutable executable : this.executables) {
                try {
                    logger.info("Executing Statement: " + executable.getKeyword() + " " + executable.getValue());
                    executable.execute(testVariables);
                } catch (ManagerException e) {
                    this.result = Result.failed(e);
                    break;
                }
            }

            if(this.result == null) {
                this.result = Result.passed();
            }

            this.testStructureMethod.setResult(this.result.getName());
            Result overrideResult = managers.endOfTestMethod(new GalasaMethod(this), this.result, this.result.getThrowable());
            if (overrideResult != null) {
                this.result = overrideResult;
            }

            if (this.result.getThrowable() != null) {
                Throwable t = this.result.getThrowable();
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    t.printStackTrace(pw);
                    this.testStructureMethod.setException(sw.toString());
                } catch (Exception e) {
                    this.testStructureMethod.setException("Unable to report exception because of " + e.getMessage());
                }
            }

            if (this.result.isPassed()) {
                String resname = this.result.getName();
                logger.info(GherkinTest.LOG_ENDING + GherkinTest.LOG_START_LINE + GherkinTest.LOG_ASTERS
                        + GherkinTest.LOG_START_LINE + "*** " + resname + " - Test method " + this.testName
                        + "#" + this.name + GherkinTest.LOG_START_LINE
                        + GherkinTest.LOG_ASTERS);
            } else {
                String exception = "";
                if (this.testStructureMethod.getException() != null) {
                    exception = "\n" + this.testStructureMethod.getException();
                }
                logger.info(GherkinTest.LOG_ENDING + GherkinTest.LOG_START_LINE + GherkinTest.LOG_ASTERS
                        + GherkinTest.LOG_START_LINE + "*** " + this.result.getName() + " - Test method "
                        + this.testName + "#" + this.name
                        + GherkinTest.LOG_START_LINE + GherkinTest.LOG_ASTERS + exception);
            }

            testStructureMethod.setEndTime(Instant.now());
            testStructureMethod.setStatus("finished");
        } catch (FrameworkException e) {
            throw new TestRunException("There was a problem with the framework, please check stacktrace", e);
        }
    }

    public boolean fullStop() {
        return this.result.isFailed();
    }

    public Result getResult() {
        return this.result;
    }
   
    public TestGherkinMethod getStructure() {
        return this.testStructureMethod;
    }
}