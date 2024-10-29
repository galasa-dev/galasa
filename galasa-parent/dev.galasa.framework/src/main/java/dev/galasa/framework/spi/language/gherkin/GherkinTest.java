/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.net.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.language.gherkin.parser.*;
import dev.galasa.framework.spi.language.gherkin.xform.ParseTreeTransform;
import dev.galasa.framework.spi.language.gherkin.xform.ParseTreeVisitorPrinter;
import dev.galasa.framework.FileSystem;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.ITestRunManagers;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.teststructure.TestGherkinMethod;
import dev.galasa.framework.spi.teststructure.TestStructure;

/**
 * A GherkinTest is a complete Gherkin feature.
 * It can have multiple scenarios, each of which has a number of steps.
 */
public class GherkinTest {

    private Log logger = LogFactory.getLog(GherkinTest.class);

    private GherkinFeature feature;

    private URI gherkinUri;
    private TestStructure testStructure;
    private Result result;

    // Logger statics
    public static final String  LOG_STARTING   = "Starting";
    public static final String  LOG_ENDING     = "Ending";
    public static final String  LOG_START_LINE = "\n" + StringUtils.repeat("-", 23) + " ";
    public static final String  LOG_ASTERS     = StringUtils.repeat("*", 100);



    public GherkinTest(IRun run, TestStructure testStructure) throws TestRunException {
        this(run,testStructure, new FileSystem() );
    }

    public GherkinTest(IRun run, TestStructure testStructure, IFileSystem fs) throws TestRunException {

        this.testStructure = testStructure;

        IFileLineReader reader = new FileLineReader(fs);

        List<String> lines = getGherkinFeatureTextLines(run,reader);

        this.feature = parseFeature(lines);

        List<TestGherkinMethod> structureMethods = new ArrayList<TestGherkinMethod>(this.feature.getScenarios().size());
        for(GherkinMethod scenario : this.feature.getScenarios()) {
            structureMethods.add(scenario.getStructure());
        }

        this.testStructure.setTestName(this.feature.getName());
        this.testStructure.setTestShortName(this.feature.getName());
        this.testStructure.setGherkinMethods(structureMethods);
    }

    private GherkinFeature parseFeature(List<String> lines) throws TestRunException {
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        GherkinParser parser = new GherkinParser(lexer);
        ParseToken rootToken = parser.Parse();

        // Log the parse tree.
        ParseTreeVisitorPrinter printer = new ParseTreeVisitorPrinter();
        printer.visit(rootToken);
        String parseTreeText = printer.getResults();
        logger.info(parseTreeText);

        ParseTreeTransform transform = new ParseTreeTransform();
        transform.visit(rootToken);
        GherkinFeature feature = transform.getFeature();
        return feature;
    }

    public String getName() {
        return this.feature.getName();
    }

    public List<GherkinMethod> getMethods() {
        return this.feature.getScenarios();
    }

    public List<IGherkinExecutable> getAllExecutables() {
        List<IGherkinExecutable> allExecutables = new ArrayList<>();
        for(GherkinMethod scenario : this.feature.getScenarios()) {
            allExecutables.addAll(scenario.getExecutables());
        }
        return allExecutables;
    }

    public Boolean allMethodsRegistered() {
        Boolean allRegistered = true;
        for(IGherkinExecutable executable : getAllExecutables()) {
            if(executable.getRegisteredManager() == null) {
                allRegistered = false;
            }
        }
        return allRegistered;
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void runTestMethods(ITestRunManagers managers) throws TestRunException {
        String report = this.testStructure.gherkinReport(LOG_START_LINE);
        logger.trace("Test Class structure:-" + report);

        logger.info(LOG_STARTING + LOG_START_LINE + LOG_ASTERS + LOG_START_LINE + "*** Start of feature file: "
                + this.feature.getName() + LOG_START_LINE + LOG_ASTERS);

        try {
            managers.startOfTestClass();
        } catch (FrameworkException e) {
            throw new TestRunException("Unable to inform managers of start of test class", e);
        }

        for (GherkinMethod method : this.feature.getScenarios()) {
            if(this.feature.getVariables().getNumberOfInstances() >= 1){
                method.invoke(managers, this.feature.getVariables().getVariableInstance(0));
            } else{
                method.invoke(managers, this.feature.getVariables().getVariablesOriginal());
            }
            
            if(method.fullStop()) {
                break;
            }
        }

        for (GherkinMethod method : this.feature.getScenarios()) {
            Result methodResult = method.getResult();
            if (methodResult != null && methodResult.isFailed()) {
                this.result = Result.failed("A Test failed");
                break;
            }
        }

        if (this.result == null) {
            this.result = Result.passed();
        }

        try {
            Result newResult = managers.endOfTestClass(this.result, null); 
            if (newResult != null) {
                logger.info("Result of test run overridden to " + newResult);
                this.result = newResult;
            }
        } catch (FrameworkException e) {
            throw new TestRunException("Problem with end of test class", e);
        }

        // Test result
        logger.info(LOG_ENDING + LOG_START_LINE + LOG_ASTERS + LOG_START_LINE + "*** " + this.result.getName()
        + " - Test class " + this.feature.getName() +  LOG_START_LINE + LOG_ASTERS);

        this.testStructure.setResult(this.result.getName());

        managers.testClassResult(this.result, null);

        String postReport = this.testStructure.gherkinReport(LOG_START_LINE);
        logger.trace("Finishing Test Class structure:-" + postReport);
    }


    private List<String> getGherkinFeatureTextLines(IRun run, IFileLineReader fileReader) throws TestRunException {

        String gherkinUriString = run.getGherkin();
        if (gherkinUriString == null) {
            throw new TestRunException("Gherkin URI is not set");
        }

        try {
            gherkinUri = new URI(gherkinUriString);
        } catch (URISyntaxException e) {
            throw new TestRunException("Unable to parse gherkin test URI", e);
        }

        String schema = gherkinUri.getScheme();
        if (schema == null) {
            throw new TestRunException("Gherkin URI " + gherkinUri + " does not contain a schema");
        }

        if (!"file".equals(schema)) {
            throw new TestRunException("Gherkin URI scheme " + schema + " is not supported");
        }

        List<String> lines =  fileReader.readLines(gherkinUri);
        
        return lines ;
    }
}