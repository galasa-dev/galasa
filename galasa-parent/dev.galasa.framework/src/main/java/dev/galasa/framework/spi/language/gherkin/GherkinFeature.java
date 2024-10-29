/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.util.ArrayList;
import java.util.List;

/** 
 * A class which represents the gherkin feature.
 */
public class GherkinFeature {

    private List<GherkinMethod> scenarios = new ArrayList<>();

    private String name ;

    private GherkinVariables variables = new GherkinVariables();

    public List<GherkinMethod> getScenarios() {
        return this.scenarios;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getName() {
        return this.name;
    }

    public GherkinVariables getVariables() {
        return this.variables;
    }

    public void setVariables(GherkinVariables variables) {
        this.variables = variables;
    }
}
