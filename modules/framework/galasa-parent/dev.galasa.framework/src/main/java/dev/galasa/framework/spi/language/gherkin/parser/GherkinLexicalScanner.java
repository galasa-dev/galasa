/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GherkinLexicalScanner implements LexicalScanner {
    private List<String> lines;
    private Iterator<String> linesWalker ;
    private int lineNumber ;
    private final static Log logger = LogFactory.getLog(GherkinLexicalScanner.class);

    private List<TokenMatcher> tokenMatchers = List.of(
        new FeatureTokenMatcher(),
        new ScenarioTokenMatcher(),
        new ScenarioOutlineTokenMatcher(),
        new ExamplesTokenMatcher(),
        new DataLineMatcher(),
        new StepLineMatcher()
    );
    private Stack<ParseToken> tokenStack  = new Stack<ParseToken>();

    public GherkinLexicalScanner(List<String> lines) {
        if (lines == null) {
            this.lines = new ArrayList<String>();
        } else {
            this.lines = lines ;
        }
        this.linesWalker = this.lines.iterator();
        this.lineNumber = 0;
    }

    public ParseToken getNextToken() {
        ParseToken token = null;

        if (!tokenStack.empty()) {
            token = tokenStack.pop();
        } else {
            token = getNextTokenFromLines();
        }

        // If we have got to the end of the file, just repeat
        // the end of file token.
        if (token.getType() == TokenType.END_OF_FILE) {
            tokenStack.push(token);
        }

        logger.debug("Lex: GetToken: Returning "+token.toString());
        return token ;
    }

    private ParseToken getNextTokenFromLines() {
        ParseToken token = null;

        while (token == null) {
            String line = getNextLine();
            if (line == null) {
                token = new ParseToken(TokenType.END_OF_FILE,"");
            } else {
                
                line = line.trim();
                if(line.isEmpty()) {
                    // Ignore blank lines.
                } else if (line.startsWith("#")) {
                    // Ignore comment lines.
                } else {

                    for ( TokenMatcher tokenMatcher : tokenMatchers) {
                        token = tokenMatcher.matches(line);
                        if (token != null) {
                            break;
                        }
                    }

                }
            }
        }
        token.setLineNumber(this.lineNumber);
        return token;
    }

    private String getNextLine() {
        String line = null ;
        this.lineNumber +=1 ;
        if (!this.linesWalker.hasNext()) {
            // There is no more data to process. 
            // So return a null string.
        } else {
            line = this.linesWalker.next();
        }
        return line ;
    }

    public void pushBackToken(ParseToken token) {
        logger.debug("Lex: pushBackToken: Pushing back token "+token.toString());
        tokenStack.push(token);
    }
}
