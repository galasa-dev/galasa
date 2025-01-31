/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.xform;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.language.gherkin.GherkinFeature;
import dev.galasa.framework.spi.language.gherkin.GherkinMethod;
import dev.galasa.framework.spi.language.gherkin.parser.*;

import java.util.List;
import org.junit.Test;

public class TestParseTreeTransform {

    @Test
    public void testCanInstantiateVisitor() {
        new ParseTreeTransform();
    }

    @Test
    public void testAnEmptyFeatureCreatesNoTestMethods() throws Exception {
        // Given...
        List<String> lines = List.of(
            "Feature: my feature"
        );
        ParseToken rootToken = getParseTreeRootToken(lines);

        ParseTreeTransform visitor = new ParseTreeTransform();

        // When...
        visitor.visit(rootToken);
        GherkinFeature feature = visitor.getFeature();

        // Then...
        assertThat(feature).isNotNull();
        assertThat(feature.getName()).isEqualTo("my feature");
    }

    @Test
    public void testFeatureWithOneEmptyScenario() throws Exception {
        // Given...
        List<String> lines = List.of(
            "Feature: my feature",
            "Scenario: scenario1"
        );
        ParseToken rootToken = getParseTreeRootToken(lines);

        ParseTreePrinter printer = new ParseTreePrinter();
        printer.print(rootToken,System.out);

        ParseTreeTransform visitor = new ParseTreeTransform();

        // When...
        visitor.visit(rootToken);
        GherkinFeature feature = visitor.getFeature();

        // Then...
        assertThat(feature).isNotNull();
        List<GherkinMethod> scenarios = feature.getScenarios();
        assertThat(scenarios).isNotNull().hasSize(1);
    }

    @Test
    public void testFeatureWithOneComplexScenario() throws Exception {
        // Given...
        List<String> lines = List.of(
            "Feature: my feature",
            "Scenario: scenario1",
            "  Given a",
            "  and b",
            "  then c"
        );
        ParseToken rootToken = getParseTreeRootToken(lines);

        ParseTreePrinter printer = new ParseTreePrinter();
        printer.print(rootToken,System.out);

        ParseTreeTransform visitor = new ParseTreeTransform();

        // When...
        visitor.visit(rootToken);
        GherkinFeature feature = visitor.getFeature();

        // Then...
        assertThat(feature).isNotNull();
        List<GherkinMethod> scenarios = feature.getScenarios();
        assertThat(scenarios).isNotNull().hasSize(1);

        GherkinMethod scenario1 = scenarios.get(0);
        assertThat(scenario1.getName()).isEqualTo("scenario1");
        List<IGherkinExecutable> executables = scenario1.getExecutables();
        assertThat(executables).hasSize(3);
    }

    @Test
    public void testFeatureWithTwoComplexScenarios() throws Exception {
        // Given...
        List<String> lines = List.of(
            "Feature: my feature",
            "Scenario: scenario1",
            "  Given a",
            "  and b",
            "  then c",
            "Scenario: scenario2",
            "  Given a",
            "  and b",
            "  then c"
        );
        ParseToken rootToken = getParseTreeRootToken(lines);

        ParseTreeTransform visitor = new ParseTreeTransform();

        // When...
        visitor.visit(rootToken);
        GherkinFeature feature = visitor.getFeature();

        // Then...
        assertThat(feature).isNotNull();
        List<GherkinMethod> scenarios = feature.getScenarios();
        assertThat(scenarios).isNotNull().hasSize(2);

        assertThat(scenarios.get(0).getName()).isEqualTo("scenario1");

        assertThat(scenarios.get(1).getName()).isEqualTo("scenario2");
    }

    private ParseToken getParseTreeRootToken( List<String> lines) throws Exception {
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        dev.galasa.framework.spi.language.gherkin.parser.GherkinParser parser = new dev.galasa.framework.spi.language.gherkin.parser.GherkinParser(lexer);
        ParseToken rootToken = parser.Parse();
        return rootToken ;
    }
}
