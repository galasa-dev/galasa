/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.xform;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.parser.ParseToken;

/**
 * Anything interested in visiting the parse implements this interface.
 */
public interface ParseTreeVisitor {
    public void visit(ParseToken rootNode) throws TestRunException ;
    public void visitFeature(ParseToken token) throws TestRunException ;
    public void visitScenarioPartList(ParseToken token) throws TestRunException ;
    public void visitScenarioPart(ParseToken token) throws TestRunException;
    public void visitScenario(ParseToken token) throws TestRunException;
    public void visitStepList(ParseToken token) throws TestRunException;
    public void visitFeatureStart(ParseToken token) throws TestRunException;
    public void visitDataTable(ParseToken token) throws TestRunException ;
    public void visitDataTableHeader(ParseToken token) throws TestRunException ;
    public void visitDataTableLineList(ParseToken token) throws TestRunException;
    public void visitScenarioOutline(ParseToken token) throws TestRunException;
    public void visitEndOfFile(ParseToken token) throws TestRunException ;
    public void visitDataLine(ParseToken token) throws TestRunException ;
    public void visitScenarioStart(ParseToken token) throws TestRunException;
    public void visitStep(ParseToken token) throws TestRunException;
    public void visitScenarioOutlineStart(ParseToken token) throws TestRunException;
    public void visitExamplesStart(ParseToken token) throws TestRunException;

    public void postVisit(ParseToken token) throws TestRunException;
    public void postVisitFeature(ParseToken token) throws TestRunException ;
    public void postVisitScenarioPartList(ParseToken token) throws TestRunException ;
    public void postVisitScenarioPart(ParseToken token) throws TestRunException;
    public void postVisitScenario(ParseToken token) throws TestRunException;
    public void postVisitStepList(ParseToken token) throws TestRunException;
    public void postVisitFeatureStart(ParseToken token) throws TestRunException;
    public void postVisitDataTable(ParseToken token) throws TestRunException ;
    public void postVisitDataTableHeader(ParseToken token) throws TestRunException ;
    public void postVisitDataTableLineList(ParseToken token) throws TestRunException;
    public void postVisitScenarioOutline(ParseToken token) throws TestRunException;
    public void postVisitEndOfFile(ParseToken token) throws TestRunException ;
    public void postVisitDataLine(ParseToken token) throws TestRunException ;
    public void postVisitScenarioStart(ParseToken token) throws TestRunException;
    public void postVisitStep(ParseToken token) throws TestRunException;
    public void postVisitScenarioOutlineStart(ParseToken token) throws TestRunException;
    public void postVisitExamplesStart(ParseToken token) throws TestRunException;
    
    public void preVisit(ParseToken token) throws TestRunException;
    public void preVisitFeature(ParseToken token) throws TestRunException ;
    public void preVisitScenarioPartList(ParseToken token) throws TestRunException ;
    public void preVisitScenarioPart(ParseToken token) throws TestRunException;
    public void preVisitScenario(ParseToken token) throws TestRunException;
    public void preVisitStepList(ParseToken token) throws TestRunException;
    public void preVisitFeatureStart(ParseToken token) throws TestRunException;
    public void preVisitDataTable(ParseToken token) throws TestRunException ;
    public void preVisitDataTableHeader(ParseToken token) throws TestRunException ;
    public void preVisitDataTableLineList(ParseToken token) throws TestRunException;
    public void preVisitScenarioOutline(ParseToken token) throws TestRunException;
    public void preVisitEndOfFile(ParseToken token) throws TestRunException ;
    public void preVisitDataLine(ParseToken token) throws TestRunException ;
    public void preVisitScenarioStart(ParseToken token) throws TestRunException;
    public void preVisitStep(ParseToken token) throws TestRunException;
    public void preVisitScenarioOutlineStart(ParseToken token) throws TestRunException;
    public void preVisitExamplesStart(ParseToken token) throws TestRunException;

}
