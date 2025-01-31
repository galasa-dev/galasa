/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.xform;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import dev.galasa.framework.spi.language.gherkin.parser.*;

public class TestParseTreePrinter {
    
    @Test
    public void testComplexParseTreePrintsOut() throws Exception {
        // Given...
        List<String> lines = List.of(
            "Feature: my feature",
            "Scenario: scenario1",
            " Given a",
            " When x",
            " Then y"
        );
        ParseToken rootToken = getParseTreeRootToken(lines);

        ParseTreePrinter astPrinter = new ParseTreePrinter();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream outputPrinter = new PrintStream(output);
        astPrinter.print(rootToken,outputPrinter);


        String expectedOutput = 
        "{token:<feature>, line:1, text:my feature}\n"+
        "  {token:Feature:, line:1, text:my feature}\n"+
        "  {token:<scenarioPartList>, line:2, text:scenario1}\n"+
        "    {token:<scenario>, line:2, text:scenario1}\n"+
        "      {token:Scenario:, line:2, text:scenario1}\n"+
        "      {token:<stepList>, line:3, text:Given a}\n"+
        "        {token:step, line:3, text:Given a}\n"+
        "        {token:<stepList>, line:4, text:When x}\n"+
        "          {token:step, line:4, text:When x}\n"+
        "          {token:<stepList>, line:5, text:Then y}\n"+
        "            {token:step, line:5, text:Then y}\n"+
        "            {token:<stepList>, line:6, text:}\n"+
        "    {token:<scenarioPartList>, line:6, text:}\n"+
        "\n"
        ;
        assertThat(output.toString()).as("Parse tree print output is bad.").isEqualTo(expectedOutput);
    }

    @Test
    public void testComplexParseTreePrintsOutWithCapturedOutput() throws Exception {
        // Given...
        List<String> lines = List.of(
            "Feature: my feature",
            "Scenario: scenario1",
            " Given a",
            " When x",
            " Then y"
        );
        ParseToken rootToken = getParseTreeRootToken(lines);

        ParseTreePrinter astPrinter = new ParseTreePrinter();

        String output = astPrinter.getPrintOutput(rootToken);

        String expectedOutput = 
            "{token:<feature>, line:1, text:my feature}\n"+
            "  {token:Feature:, line:1, text:my feature}\n"+
            "  {token:<scenarioPartList>, line:2, text:scenario1}\n"+
            "    {token:<scenario>, line:2, text:scenario1}\n"+
            "      {token:Scenario:, line:2, text:scenario1}\n"+
            "      {token:<stepList>, line:3, text:Given a}\n"+
            "        {token:step, line:3, text:Given a}\n"+
            "        {token:<stepList>, line:4, text:When x}\n"+
            "          {token:step, line:4, text:When x}\n"+
            "          {token:<stepList>, line:5, text:Then y}\n"+
            "            {token:step, line:5, text:Then y}\n"+
            "            {token:<stepList>, line:6, text:}\n"+
            "    {token:<scenarioPartList>, line:6, text:}\n"
            ;
        assertThat(output).as("Parse tree print output is bad.").isEqualTo(expectedOutput);
    }


    private ParseToken getParseTreeRootToken( List<String> lines) throws Exception {
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        dev.galasa.framework.spi.language.gherkin.parser.GherkinParser parser = new dev.galasa.framework.spi.language.gherkin.parser.GherkinParser(lexer);
        ParseToken rootToken = parser.Parse();
        return rootToken ;
    }

}
