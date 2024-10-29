/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.xform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.GherkinFeature;
import dev.galasa.framework.spi.language.gherkin.GherkinMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinVariables;
import dev.galasa.framework.spi.language.gherkin.parser.ParseToken;

/**
 * Visits a parse tree, and populates the meaning into a test structure.
 */
public class ParseTreeTransform extends ParseTreeVisitorBase {

    private List<ParseToken> steps ;

    private GherkinFeature feature;

    private GherkinVariables variables;

    public ParseTreeTransform() {
    }

    public GherkinFeature getFeature() {
        return this.feature;
    }

    @Override
    public void visit(ParseToken token) throws TestRunException {
        ParseTreeNavigator navigator = new ParseTreeNavigator(this);
        navigator.visit(token);
    }

    @Override
    public void visitFeature(ParseToken token) throws TestRunException {

        // <feature> ::= FEATURE_START <scenarioPartList> 
        GherkinFeature feature = new GherkinFeature();
        this.feature = feature ;

        // The <feature> tag inherits the text from the FEATURE_START token.
        feature.setName(token.getText());
    }

    @Override
    public void visitScenario(ParseToken token) throws TestRunException {
        // <scenario> ::= SCENARIO_START <stepList>
        this.steps = new ArrayList<ParseToken>();
    }

    @Override
    public void postVisitScenario(ParseToken token) throws TestRunException {
        // <scenario> ::= SCENARIO_START <stepList>
        String scenarioName = token.getText();
        GherkinMethod scenario = new GherkinMethod(scenarioName,feature.getName());

        this.feature.getScenarios().add(scenario);

        // Add the steps to the scenario.
        for (ParseToken stepToken : steps) {
            scenario.addStep(stepToken.getText());
        }

        this.steps = null ;
    }

    @Override
    public void visitScenarioOutline(ParseToken token) throws TestRunException {
        // <scenarioOutline> ::= SCENARIO_OUTLINE_START <stepList> EXAMPLES_START <dataTable>
        this.variables = new GherkinVariables();
        this.steps = new ArrayList<ParseToken>();
    }

    @Override
    public void postVisitScenarioOutline(ParseToken token) throws TestRunException {
        // <scenarioOutline> ::= SCENARIO_OUTLINE_START <stepList> EXAMPLES_START <dataTable>

        // Turn the scenario outline into a number of scenarios, substituting the variable
        // values into the steps as we go.
        int instancesToProcess = this.variables.getNumberOfInstances();
        for( int instance = 0 ; instance < instancesToProcess ; instance +=1 ) {
            // The scenario is instance 'n'. One scenario for each line in the data table.
            String instanceScenarioName = token.getText() + "-"+Integer.toString(instance);
            GherkinMethod scenario = new GherkinMethod(instanceScenarioName,feature.getName());

            // Add the steps to the scenario.
            for (ParseToken stepToken : steps) {

                String stepText = stepToken.getText();

                Map<String, Object> variableMapInstance = this.variables.getVariableInstance(instance);
                for( Entry<String,Object> entry : variableMapInstance.entrySet()) {
                    String variableName = "<"+entry.getKey()+">";

                    // Get the value. This is horrible. It's really a string, but the variables class says it's an object.
                    String variableValue = (String)entry.getValue();

                    stepText = stepText.replaceAll(variableName,variableValue);
                }
                scenario.addStep(stepText);
            }

            this.feature.getScenarios().add(scenario);
        }
        
        this.variables = null ;

        this.steps = null ;
    }

    @Override
    public void visitStep(ParseToken token) throws TestRunException {
        this.steps.add(token);
    }

    @Override
    public void visitDataTableHeader(ParseToken token) throws TestRunException {
        this.variables.processHeaderLine(token.getText());
    }

    @Override 
    public void visitDataTableLineList(ParseToken token) throws TestRunException {
        // <dataTableValuesLineList> ::= null
        // <dataTableValuesLineList> ::= DATA_LINE <dataTableValuesLineList>
        if (token.getChildren().size() > 0 ) {
            // Add the DATA_LINE token to the list of data rows. 
            // To turn it into a form we can easily iterate over.
            this.variables.processDataLine(token.getText());
        }
    }

}
