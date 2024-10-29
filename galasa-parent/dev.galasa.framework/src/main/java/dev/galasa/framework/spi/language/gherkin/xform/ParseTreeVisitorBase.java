/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.xform;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.parser.ParseToken;

/**
 * A base class which implements the interface for visiting a parse tree.
 * 
 * None of the methods do anything, but are here so that actual visitor logic 
 * doesn't have to implement pre- post- and during- visitor methods themselves.
 */
public class ParseTreeVisitorBase implements ParseTreeVisitor {

    @Override
    public void visit(ParseToken rootNode) throws TestRunException {
    }

    @Override
    public void visitFeature(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitScenarioPartList(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitScenarioPart(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitScenario(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitStepList(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitFeatureStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitDataTableHeader(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitDataTableLineList(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitDataTable(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitScenarioOutline(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitEndOfFile(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitDataLine(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitScenarioStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitStep(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitScenarioOutlineStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void visitExamplesStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisit(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisit(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitFeature(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitScenarioPartList(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitScenarioPart(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitScenario(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitStepList(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitFeatureStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitDataTable(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitDataTableHeader(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitDataTableLineList(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitScenarioOutline(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitEndOfFile(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitDataLine(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitScenarioStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitStep(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitScenarioOutlineStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void postVisitExamplesStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitFeature(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitScenarioPartList(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitScenarioPart(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitScenario(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitStepList(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitFeatureStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitDataTable(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitDataTableHeader(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitDataTableLineList(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitScenarioOutline(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitEndOfFile(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitDataLine(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitScenarioStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitStep(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitScenarioOutlineStart(ParseToken token) throws TestRunException {
    }

    @Override
    public void preVisitExamplesStart(ParseToken token) throws TestRunException {
    }
    
}
