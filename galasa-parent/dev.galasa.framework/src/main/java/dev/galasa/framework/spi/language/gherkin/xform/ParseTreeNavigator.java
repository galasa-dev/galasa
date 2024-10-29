/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.xform;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.parser.ParseToken;

/**
 * Give this parse tree navigator a sub-visitor, and it will handle the visiting of all children
 * for all types of things for you, and call teh sub-visitor pre- during- and post- encountering 
 * a node in the parse tree, as it navigates around.
 */
public class ParseTreeNavigator extends ParseTreeVisitorBase {

    private ParseTreeVisitor subVisitor ;

    public ParseTreeNavigator(ParseTreeVisitor subVisitor) {
        this.subVisitor = subVisitor;
    }

    @Override
    public void postVisit(ParseToken token) throws TestRunException {
        subVisitor.postVisit(token);

        switch(token.getType()) {
            case FEATURE_START:
                subVisitor.postVisitFeatureStart(token);
            break;

            case SCENARIO_START:
                subVisitor.postVisitScenarioStart(token);
            break;

            case SCENARIO_OUTLINE_START:
                subVisitor.postVisitScenarioOutlineStart(token);
            break;

            case EXAMPLES_START:
                subVisitor.postVisitExamplesStart(token);
            break;

            case STEP:
                subVisitor.postVisitStep(token);
            break;

            case DATA_LINE:
                subVisitor.postVisitDataLine(token);
            break;
            
            case END_OF_FILE:
                subVisitor.postVisitEndOfFile(token);
            break;

            case FEATURE:
                subVisitor.postVisitFeature(token);
            break;

            case SCENARIO_OUTLINE:
                subVisitor.postVisitScenarioOutline(token);
            break;
            
            case SCENARIO:
                subVisitor.postVisitScenario(token);
            break;

            case SCENARIO_PART_LIST:
                subVisitor.postVisitScenarioPartList(token);
            break;

            case SCENARIO_PART:
                subVisitor.postVisitScenarioPart(token);
            break;

            case DATA_TABLE:
                subVisitor.postVisitDataTable(token);
            break;

            case DATA_TABLE_HEADER:
                subVisitor.postVisitDataTableHeader(token);
            break;

            case DATA_TABLE_LINE_LIST:
                subVisitor.postVisitDataTableLineList(token);
            break;

            case STEP_LIST:
                subVisitor.postVisitStepList(token);
            break;

            default:
                throw new TestRunException("Unexpected token in visitor "+token);
        }
    }

    @Override
    public void preVisit(ParseToken token) throws TestRunException {
        subVisitor.preVisit(token);

        switch(token.getType()) {
            case FEATURE_START:
                subVisitor.preVisitFeatureStart(token);
            break;

            case SCENARIO_START:
                subVisitor.preVisitScenarioStart(token);
            break;

            case SCENARIO_OUTLINE_START:
                subVisitor.preVisitScenarioOutlineStart(token);
            break;

            case EXAMPLES_START:
                subVisitor.preVisitExamplesStart(token);
            break;

            case STEP:
                subVisitor.preVisitStep(token);
            break;

            case DATA_LINE:
                subVisitor.preVisitDataLine(token);
            break;
            
            case END_OF_FILE:
                subVisitor.preVisitEndOfFile(token);
            break;

            case FEATURE:
                subVisitor.preVisitFeature(token);
            break;

            case SCENARIO_OUTLINE:
                subVisitor.preVisitScenarioOutline(token);
            break;
            
            case SCENARIO:
                subVisitor.preVisitScenario(token);
            break;

            case SCENARIO_PART_LIST:
                subVisitor.preVisitScenarioPartList(token);
            break;

            case SCENARIO_PART:
                subVisitor.preVisitScenarioPart(token);
            break;

            case DATA_TABLE:
                subVisitor.preVisitDataTable(token);
            break;

            case DATA_TABLE_HEADER:
                subVisitor.preVisitDataTableHeader(token);
            break;

            case DATA_TABLE_LINE_LIST:
                subVisitor.preVisitDataTableLineList(token);
            break;

            case STEP_LIST:
                subVisitor.preVisitStepList(token);
            break;

            default:
                throw new TestRunException("Unexpected token in visitor "+token);
        }
    }

    @Override
    public void visit(ParseToken token) throws TestRunException {
        
        preVisit(token);

        switch(token.getType()) {
            case FEATURE_START:
                visitFeatureStart(token);
            break;

            case SCENARIO_START:
                visitScenarioStart(token);
            break;

            case SCENARIO_OUTLINE_START:
                visitScenarioOutlineStart(token);
            break;

            case EXAMPLES_START:
                visitExamplesStart(token);
            break;

            case STEP:
                visitStep(token);
            break;

            case DATA_LINE:
                visitDataLine(token);
            break;
            
            case END_OF_FILE:
                visitEndOfFile(token);
            break;

            case FEATURE:
                visitFeature(token);
            break;

            case SCENARIO_OUTLINE:
                visitScenarioOutline(token);
            break;
            
            case SCENARIO:
                visitScenario(token);
            break;

            case SCENARIO_PART_LIST:
                visitScenarioPartList(token);
            break;

            case SCENARIO_PART:
                visitScenarioPart(token);
            break;

            case DATA_TABLE:
                visitDataTable(token);
            break;

            case DATA_TABLE_HEADER:
                visitDataTableHeader(token);
            break;

            case DATA_TABLE_LINE_LIST:
                visitDataTableLineList(token);
            break;

            case STEP_LIST:
                visitStepList(token);
            break;

            default:
                throw new TestRunException("Unexpected token in visitor "+token);
        }

        postVisit(token);
    }
    
    @Override 
    public void visitScenarioStart(ParseToken token) throws TestRunException {
        this.subVisitor.visitScenarioStart(token);
        visitChildren(token);
    }

    @Override 
    public void visitScenarioOutlineStart(ParseToken token) throws TestRunException {
        this.subVisitor.visitScenarioOutlineStart(token);
        visitChildren(token);
    }

    @Override 
    public void visitExamplesStart(ParseToken token) throws TestRunException {
        this.subVisitor.visitExamplesStart(token);
        visitChildren(token);
    }

    @Override 
    public void visitStep(ParseToken token) throws TestRunException {
        this.subVisitor.visitStep(token);
        visitChildren(token);
    }

    @Override 
    public void visitDataTable(ParseToken token) throws TestRunException {
        this.subVisitor.visitDataTable(token);
        visitChildren(token);
    }

    @Override 
    public void visitDataLine(ParseToken token) throws TestRunException {
        this.subVisitor.visitDataLine(token);
        visitChildren(token);
    }

    @Override 
    public void visitEndOfFile(ParseToken token) throws TestRunException {
        this.subVisitor.visitEndOfFile(token);
        visitChildren(token);
    }

    @Override 
    public void visitScenarioOutline(ParseToken token) throws TestRunException {
        this.subVisitor.visitScenarioOutline(token);
        visitChildren(token);
    }

    @Override 
    public void visitDataTableLineList(ParseToken token) throws TestRunException {
        this.subVisitor.visitDataTableLineList(token);
        visitChildren(token);
    }

    @Override 
    public void visitDataTableHeader(ParseToken token) throws TestRunException {
        this.subVisitor.visitDataTableHeader(token);
        visitChildren(token);
    }

    @Override
    public void visitFeatureStart(ParseToken token) throws TestRunException {
        this.subVisitor.visitFeatureStart(token);
        visitChildren(token);
    }

    @Override
    public void visitFeature(ParseToken token) throws TestRunException {
        this.subVisitor.visitFeature(token);
        visitChildren(token);
    }

    @Override
    public void visitScenarioPartList(ParseToken token) throws TestRunException {
        this.subVisitor.visitScenarioPartList(token);
        visitChildren(token);
    }

    @Override
    public void visitScenarioPart(ParseToken token) throws TestRunException {
        this.subVisitor.visitScenarioPart(token);
        visitChildren(token);
    }

    @Override
    public void visitScenario(ParseToken token) throws TestRunException {
        this.subVisitor.visitScenario(token);
        visitChildren(token);
    }

    @Override
    public void visitStepList(ParseToken token) throws TestRunException {
        this.subVisitor.visitStepList(token);
        visitChildren(token);
    }

    private void visitChildren(ParseToken token) throws TestRunException {
        for( ParseToken child: token.getChildren()) {
            visit(child);
        }
    }
    
}
