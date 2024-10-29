package dev.galasa.framework.spi.language.gherkin.parser;

import java.io.PrintStream;

import dev.galasa.framework.spi.language.gherkin.xform.ParseTreeVisitorPrinter;

public class ParseTreePrinter {

    public void print(ParseToken rootToken, PrintStream out) throws Exception {
        ParseTreeVisitorPrinter visitor = new ParseTreeVisitorPrinter();
        visitor.visit(rootToken);
        String result = visitor.getResults();
        out.println(result);
    }
    
    public String getPrintOutput(ParseToken rootToken) throws Exception {
        ParseTreeVisitorPrinter visitor = new ParseTreeVisitorPrinter();
        visitor.visit(rootToken);
        String result = visitor.getResults();
        return result;
    }
}
