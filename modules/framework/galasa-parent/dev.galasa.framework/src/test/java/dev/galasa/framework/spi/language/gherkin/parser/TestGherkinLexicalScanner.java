/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import org.junit.Test;

public class TestGherkinLexicalScanner {
    @Test
    public void testCanInstantiateScannerImpl() throws Exception {
        List<String> lines = List.of("First line only");
        new GherkinLexicalScanner(lines);
    }
    
    @Test
    public void testCanScanNullReturnsEndOfFile() throws Exception {
        GherkinLexicalScanner scanner = new GherkinLexicalScanner(null);
        ParseToken token1 = scanner.getNextToken();
        assertThat(token1.getLineNumber()).isEqualTo(1);
        assertThat(token1.getText()).isBlank();
        assertThat(token1.getType()).isEqualTo(TokenType.END_OF_FILE);
    }

    @Test
    public void testCanScanCanParseFeatureThenEOF() throws Exception {
        List<String> lines = List.of("Feature: My first feature");

        List<Object> expectedTokens = List.of(
            new ParseToken(TokenType.FEATURE_START,"My first feature", 1),
            new ParseToken(TokenType.END_OF_FILE,"",2)
        );
        
        checkScannerResults(lines,expectedTokens);
    }

    @Test
    public void testCanScanCanParseFeatureThenEOFThenEOF() throws Exception {
        List<String> lines = List.of("Feature: My first feature");

        List<Object> expectedTokens = List.of(
            new ParseToken(TokenType.FEATURE_START,"My first feature", 1),
            new ParseToken(TokenType.END_OF_FILE,"",2),
            new ParseToken(TokenType.END_OF_FILE,"",2)
        );

        checkScannerResults(lines,expectedTokens);
    }

    @Test
    public void testCanScanCanPushBackAToken() throws Exception {
        List<String> lines = List.of("Feature: My first feature");
        GherkinLexicalScanner scanner = new GherkinLexicalScanner(lines);

        ParseToken token1 = scanner.getNextToken();
        assertThat(token1.getLineNumber()).isEqualTo(1);
        assertThat(token1.getText()).isEqualTo("My first feature");
        assertThat(token1.getType()).isEqualTo(TokenType.FEATURE_START);

        scanner.pushBackToken(token1);

        token1 = scanner.getNextToken();
        assertThat(token1.getLineNumber()).isEqualTo(1);
        assertThat(token1.getText()).isEqualTo("My first feature");
        assertThat(token1.getType()).isEqualTo(TokenType.FEATURE_START);

        ParseToken token2 = scanner.getNextToken();
        assertThat(token2.getLineNumber()).isEqualTo(2);
        assertThat(token2.getText()).isEqualTo("");
        assertThat(token2.getType()).isEqualTo(TokenType.END_OF_FILE);
    }

    @Test
    public void testCanGetSequenceOfTokens() throws Exception {

        List<String> lines = List.of(
            "Feature: Browse the catalog and order\n",
            "\n",
            "Scenario Outline: Browse a Product from catalog and place an order\n",
            "\n",
            "Given a terminal\n",
            "Then wait for \"myString\" in any terminal field\n",
            "And type \"myString\" on terminal\n",
            "Then check \"Sign-on is complete\" appears only once on terminal\n",
            "\n",
            "Examples:\n",
            "| username | password |\n",
            "| X123455  | YXXXXXXX |\n",
            "\n"
        );

        List<Object> expectedTokens = List.of(
            new ParseToken(TokenType.FEATURE_START,"Browse the catalog and order", 1),
            new ParseToken(TokenType.SCENARIO_OUTLINE_START, "Browse a Product from catalog and place an order",3),
            new ParseToken(TokenType.STEP,"Given a terminal",5),
            new ParseToken(TokenType.STEP,"Then wait for \"myString\" in any terminal field",6),
            new ParseToken(TokenType.STEP,"And type \"myString\" on terminal",7),
            new ParseToken(TokenType.STEP,"Then check \"Sign-on is complete\" appears only once on terminal",8),
            new ParseToken(TokenType.EXAMPLES_START,"",10),
            new ParseToken(TokenType.DATA_LINE,"| username | password |",11),
            new ParseToken(TokenType.DATA_LINE,"| X123455  | YXXXXXXX |",12),
            new ParseToken(TokenType.END_OF_FILE,"",14)
        );

        checkScannerResults(lines,expectedTokens);
    }

    @Test
    public void testCanGetScenarioTokens() throws Exception {

        List<String> lines = List.of(
            "Scenario: Browse the catalog and order\n"
        );

        List<Object> expectedTokens = List.of(
            new ParseToken(TokenType.SCENARIO_START,"Browse the catalog and order",1),
            new ParseToken(TokenType.END_OF_FILE,"",2)
        );

        checkScannerResults(lines,expectedTokens);
    }

    @Test
    public void testCommentsGetIgnored() throws Exception {

        List<String> lines = List.of(
            "# This is a comment"
        );

        List<Object> expectedTokens = List.of(
            new ParseToken(TokenType.END_OF_FILE,"",2)
        );

        checkScannerResults(lines,expectedTokens);
    }

    private void checkScannerResults(List<String>lines, List<Object> expectedTokens) throws Exception {
        GherkinLexicalScanner scanner = new GherkinLexicalScanner(lines);
        
        for( Object expectedObject : expectedTokens){
            ParseToken expectedToken = (ParseToken)expectedObject;
            ParseToken token = scanner.getNextToken();
            assertThat(token).isEqualToComparingFieldByField(expectedToken);
        }

        ParseToken token = scanner.getNextToken();
        assertThat(token.getType()).isEqualTo(TokenType.END_OF_FILE);
    }
}
