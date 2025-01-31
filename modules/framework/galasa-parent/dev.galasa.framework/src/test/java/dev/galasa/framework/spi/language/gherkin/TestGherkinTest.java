/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.spi.teststructure.TestStructure;


public class TestGherkinTest {

    static class MockGherkinFileSystem extends MockFileSystem {

        private List<String> lines;

        public void setFeatureText(String gherkinFeatureText) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(gherkinFeatureText.getBytes());
            this.lines = IOUtils.readLines(inputStream, "UTF-8");
        }

        @Override
        public List<String> readLines(URI gherkinUri) throws IOException {
            return lines ;
        }
    }

    @Test
    public void testGherkinTestFailsWellWithNullGherkinUri() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();
        TestRunException ex = catchThrowableOfType( ()-> new GherkinTest(run,testStructure,gherkinReader),
            TestRunException.class );
        assertThat(ex).hasMessageContaining("Gherkin URI is not set");
    }

    @Test
    public void testGherkinTestWithInvalidGherkinUriMissingSchemaFailsWell() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String INVALID_URI_NO_SCHEMA = "asd+23d3ddadw";
        run.setGherkin(INVALID_URI_NO_SCHEMA);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();
        TestRunException ex = catchThrowableOfType( ()-> new GherkinTest(run,testStructure,gherkinReader),
            TestRunException.class );
        assertThat(ex).hasMessageContaining("does not contain a schema");
    }

    @Test
    public void testGherkinTestWithInvalidGherkinUriNotAFileFailsWell() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String INVALID_URI_NON_FILE_SCHEMA = "http://asd23d3ddadw";
        run.setGherkin(INVALID_URI_NON_FILE_SCHEMA);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();
        TestRunException ex = catchThrowableOfType( ()-> new GherkinTest(run,testStructure,gherkinReader),
            TestRunException.class );
        assertThat(ex).hasMessageContaining("Gherkin URI scheme","is not suported");
    }

    @Test
    public void testGherkinTestCreatableWithSimpleFeature() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();
        gherkinReader.setFeatureText("");

        TestRunException ex = catchThrowableOfType(
            ()-> { new GherkinTest(run,testStructure,gherkinReader); }, 
            TestRunException.class);
        assertThat(ex)
            .isNotNull()
            .hasMessageContaining("GHER002")
            ;
    }

    @Test
    public void testGherkinTestCreatableWithDataExampleTableSingleLine() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")
            .append("Scenario Outline: Browse a Product from catalog and place an order\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"myString\" in any terminal field\n")
            .append("And type \"myString\" on terminal\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")
            .append("\n")
            .append("Examples:\n")
            .append("| username | password |\n")
            .append("| X123455  | YXXXXXXX |\n")
            .append("\n")
            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        GherkinTest test = new GherkinTest(run,testStructure,gherkinReader);
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo("Browse the catalog and order");
        assertThat(test.getMethods()).hasSize(1);
    }

    @Test
    public void testGherkinTestCreatableWithDataExampleTableInScenarioShowsError() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")
            .append("Scenario: Browse a Product from catalog and place an order\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"myString\" in any terminal field\n")
            .append("And type \"myString\" on terminal\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")
            .append("\n")
            .append("Examples:\n")
            .append("| username | password |\n")
            .append("| X123455  | YXXXXXXX |\n")
            .append("\n")
            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        TestRunException ex = catchThrowableOfType( ()-> new GherkinTest(run,testStructure,gherkinReader),
            TestRunException.class );
        assertThat(ex).hasMessageContaining("Example given outside of a Scenario Outline.");
    }

    @Test
    public void testGherkinTestCreatableWithDataExampleMissingFromScenarioOutline() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")
            .append("Scenario Outline: Browse a Product from catalog and place an order\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"myString\" in any terminal field\n")
            .append("And type \"myString\" on terminal\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")
            .append("\n")
            .append("\n")
            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        TestRunException ex = catchThrowableOfType( ()-> new GherkinTest(run,testStructure,gherkinReader),
            TestRunException.class );
        assertThat(ex).hasMessageContaining("'Scenario Outline:' used without an 'Examples:' section.");
    }

    @Test
    public void testGherkinTestCreatableWithDataExampleTableThreeLines() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")
            .append("Scenario Outline: Browse a Product from catalog and place an order\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"myString\" in any terminal field\n")
            .append("And type \"myString\" on terminal\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")
            .append("\n")
            .append("Examples:\n")
            .append("| field1 | field2 |\n")
            .append("| X123455  | YXXXXXXX |\n")
            .append("| Xiwhdoi  | uqhwdhjq |\n")
            .append("| asdasda  | asdasdas |\n")
            .append("\n")
            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        GherkinTest test = new GherkinTest(run,testStructure,gherkinReader);
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo("Browse the catalog and order");

        // TODO: Explain why there is only one method, despite the table having two rows ? 
        assertThat(test.getMethods()).hasSize(3);
    }

    @Test
    public void testGherkinTestCreatableWithSingleScenario() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")
            .append("Scenario: Scenario 1\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"myString\" in any terminal field\n")
            .append("And type \"myString\" on terminal\n")
            .append("And press terminal key ENTER\n")
            .append("And wait for terminal keyboard\n")
            .append("Then wait for \"Signon to CICS\" in any terminal field\n")
            .append("\n")
            .append("And wait for terminal keyboard\n")
            .append("\n")
            .append("And type \"username\" on terminal\n")
            .append("And press terminal key TAB\n")
            .append("And type \"password\" on terminal\n")
            .append("\n")
            .append("And press terminal key ENTER\n")
            .append("\n")
            .append("And wait for terminal keyboard\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")
            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        GherkinTest test = new GherkinTest(run,testStructure,gherkinReader);
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo("Browse the catalog and order");
        assertThat(test.getMethods()).hasSize(1);
        assertThat(test.getMethods().get(0).getName()).isEqualTo("Scenario 1");
    }

    @Test
    public void testGherkinTestCreatableWithTwoScenarios() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")
            .append("Scenario: Scenario 1\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"me\" in any terminal field\n")
            .append("And type \"me\" on terminal\n")
            .append("And wait for terminal keyboard\n")
            .append("And type \"hello\" on terminal\n")
            .append("\n")
            .append("And press terminal key ENTER\n")
            .append("\n")
            .append("And wait for terminal keyboard\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")

            .append("Scenario: Scenario 2\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"me\" in any terminal field\n")
            .append("And type \"me\" on terminal\n")
            .append("And wait for terminal keyboard\n")
            .append("And type \"hello\" on terminal\n")
            .append("\n")
            .append("And press terminal key ENTER\n")
            .append("\n")
            .append("And wait for terminal keyboard\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")

            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        GherkinTest test = new GherkinTest(run,testStructure,gherkinReader);
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo("Browse the catalog and order");
        assertThat(test.getMethods()).hasSize(2);
        assertThat(test.getMethods().get(0).getName()).isEqualTo("Scenario 1");
        assertThat(test.getMethods().get(1).getName()).isEqualTo("Scenario 2");
    }

    @Test
    public void testGherkinTestCreatableWithScenarioAndScenarioOutline() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")

            .append("Scenario: Scenario 1\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"me\" in any terminal field\n")
            .append("And type \"me\" on terminal\n")
            .append("And wait for terminal keyboard\n")
            .append("And type \"hello\" on terminal\n")
            .append("\n")
            .append("And press terminal key ENTER\n")
            .append("\n")
            .append("And wait for terminal keyboard\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")

            .append("Scenario Outline: Scenario outline 1\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"myString\" in any terminal field\n")
            .append("And type \"myString\" on terminal\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")
            .append("\n")
            .append("Examples:\n")
            .append("| field1 | field2 |\n")
            .append("| X123455  | YXXXXXXX |\n")
            .append("| Xiwhdoi  | uqhwdhjq |\n")
            .append("| asdasda  | asdasdas |\n")
            .append("\n")

            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        GherkinTest test = new GherkinTest(run,testStructure,gherkinReader);
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo("Browse the catalog and order");
        assertThat(test.getMethods()).hasSize(4);
        assertThat(test.getMethods().get(0).getName()).isEqualTo("Scenario 1");
        assertThat(test.getMethods().get(1).getName()).isEqualTo("Scenario outline 1-0");
        assertThat(test.getMethods().get(2).getName()).isEqualTo("Scenario outline 1-1");
        assertThat(test.getMethods().get(3).getName()).isEqualTo("Scenario outline 1-2");
    }

    @Test
    public void testGherkinTestCreatableWithScenarioOutlineAndScenarioOutline() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")

            .append("Scenario Outline: Scenario outline 1\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"myString\" in any terminal field\n")
            .append("And type \"myString\" on terminal\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")
            .append("\n")
            .append("Examples:\n")
            .append("| field1 | field2 |\n")
            .append("| X123455  | YXXXXXXX |\n")
            .append("| Xiwhdoi  | uqhwdhjq |\n")
            .append("| asdasda  | asdasdas |\n")
            .append("\n")

            .append("Scenario Outline: Scenario outline 2\n")
            .append("\n")
            .append("Given a terminal 2\n")
            .append("Then wait for \"myString\" in any terminal field 2\n")
            .append("And type \"myString\" on terminal 2 \n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal 2\n")
            .append("\n")
            .append("Examples:\n")
            .append("| field12 | field22 |\n")
            .append("| X1234552  | YXXXXXXX2 |\n")
            .append("| Xiwhdoi2  | uqhwdhjq2 |\n")
            .append("| asdasda2  | asdasdas2 |\n")
            .append("\n")

            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        GherkinTest test = new GherkinTest(run,testStructure,gherkinReader);
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo("Browse the catalog and order");
        assertThat(test.getMethods()).hasSize(6);
        assertThat(test.getMethods().get(0).getName()).isEqualTo("Scenario outline 1-0");
        assertThat(test.getMethods().get(1).getName()).isEqualTo("Scenario outline 1-1");
        assertThat(test.getMethods().get(2).getName()).isEqualTo("Scenario outline 1-2");
        assertThat(test.getMethods().get(3).getName()).isEqualTo("Scenario outline 2-0");
        assertThat(test.getMethods().get(4).getName()).isEqualTo("Scenario outline 2-1");
        assertThat(test.getMethods().get(5).getName()).isEqualTo("Scenario outline 2-2");
    }

    @Test
    public void testGherkinTestCreatableWithScenarioOutlineAndScenario() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")

            .append("Scenario Outline: Scenario outline 1\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"myString\" in any terminal field\n")
            .append("And type \"myString\" on terminal\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")
            .append("\n")
            .append("Examples:\n")
            .append("| field1 | field2 |\n")
            .append("| X123455  | YXXXXXXX |\n")
            .append("| Xiwhdoi  | uqhwdhjq |\n")
            .append("| asdasda  | asdasdas |\n")
            .append("\n")

            .append("Scenario: Scenario 1\n")
            .append("\n")
            .append("Given a terminal\n")
            .append("Then wait for \"me\" in any terminal field\n")
            .append("And type \"me\" on terminal\n")
            .append("And wait for terminal keyboard\n")
            .append("And type \"hello\" on terminal\n")
            .append("\n")
            .append("And press terminal key ENTER\n")
            .append("\n")
            .append("And wait for terminal keyboard\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")

            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        GherkinTest test = new GherkinTest(run,testStructure,gherkinReader);
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo("Browse the catalog and order");
        assertThat(test.getMethods()).hasSize(4);
        assertThat(test.getMethods().get(0).getName()).isEqualTo("Scenario outline 1-0");
        assertThat(test.getMethods().get(1).getName()).isEqualTo("Scenario outline 1-1");
        assertThat(test.getMethods().get(2).getName()).isEqualTo("Scenario outline 1-2");
        assertThat(test.getMethods().get(3).getName()).isEqualTo("Scenario 1");
    }

    @Test
    public void testGherkinTestCreatableWithScenarioWithComment() throws Exception {
        TestStructure testStructure = new TestStructure();
        MockRun run = new MockRun();
        String GOOD_URI = "file://toSimpleSchema";
        run.setGherkin(GOOD_URI);

        MockGherkinFileSystem gherkinReader = new MockGherkinFileSystem();

        String gherkinFeatureText = new StringBuilder()
            .append("Feature: Browse the catalog and order\n")
            .append("\n")
            .append("# comment 1 <<< this is a comment \n")
            .append("Scenario: Scenario 1\n")
            .append("\n")
            .append("# comment 2 <<< this is a comment \n")
            .append("Given a terminal\n")
            .append("Then wait for \"me\" in any terminal field\n")
            .append("And type \"me\" on terminal\n")
            .append("And wait for terminal keyboard\n")
            .append("And type \"hello\" on terminal\n")
            .append("\n")
            .append("And press terminal key BACKTAB\n")
            .append("\n")
            .append("And wait for terminal keyboard\n")
            .append("Then check \"Sign-on is complete\" appears only once on terminal\n")
            .append("# comment 3 <<< this is a comment \n")

            .toString();

        gherkinReader.setFeatureText(gherkinFeatureText);

        GherkinTest test = new GherkinTest(run,testStructure,gherkinReader);
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo("Browse the catalog and order");
        assertThat(test.getMethods()).hasSize(1);
        assertThat(test.getMethods().get(0).getName()).isEqualTo("Scenario 1");
    }

   
}
