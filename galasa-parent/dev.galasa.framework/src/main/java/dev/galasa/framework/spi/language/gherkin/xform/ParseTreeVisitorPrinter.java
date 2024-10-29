/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.xform;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.parser.ParseToken;

/**
 * A visitor of the parse tree which can turn an entire parse tree into a single string, 
 * for easy output or comparing against test data.
 * 
 * Each nested level of children is indented by a few spaces so it looks like an acual 
 * hierarchy of some sort.
 * 
 * For example: This gherkin feature:
 * 
 * 
 <code>
 Feature: GherkinLog
  Scenario Outline: Log Example Statement
    
    THEN Write to log "This is a log the item: <Item>"
    THEN Write to log "Log the value: <Cost>"    

    Examples:
    | Item      | Cost |
    | apple     |   89 |
    | pineapple |  339 |
</code>
* Will render into something like this:
<code>
 {token:<feature>, line:1, text:GherkinLog}
  {token:Feature:, line:1, text:GherkinLog}
  {token:<scenarioPartList>, line:2, text:Log Example Statement}
    {token:<scenarioPart>, line:2, text:Log Example Statement}
      {token:<scenarioOutline>, line:2, text:Log Example Statement}
        {token:Scenario Outline:, line:2, text:Log Example Statement}
        {token:<stepList>, line:4, text:THEN Write to log "This is a log the item: <Item>"}
          {token:step, line:4, text:THEN Write to log "This is a log the item: <Item>"}
          {token:<stepList>, line:5, text:THEN Write to log "Log the value: <Cost>"}
            {token:step, line:5, text:THEN Write to log "Log the value: <Cost>"}
            {token:<stepList>, line:7, text:}
        {token:Examples:, line:7, text:}
        {token:<dataTable>, line:8, text:| Item      | Cost |}
          {token:<dataTableHeader>, line:8, text:| Item      | Cost |}
            {token:data line, line:8, text:| Item      | Cost |}
          {token:<dataTableLineList>, line:9, text:| apple     |   89 |}
            {token:data line, line:9, text:| apple     |   89 |}
            {token:<dataTableLineList>, line:10, text:| pineapple |  339 |}
              {token:data line, line:10, text:| pineapple |  339 |}
              {token:<dataTableLineList>, line:0, text:}
    {token:<scenarioPartList>, line:11, text:}
</code>
 */
public class ParseTreeVisitorPrinter extends ParseTreeVisitorBase {

    private int indentCount = 0 ;
    private String indent = "  ";
    StringBuffer buff ;

    public ParseTreeVisitorPrinter() {
        buff = new StringBuffer();
    }

    public String getResults() {
        return buff.toString();
    }

    @Override
    public void visit(ParseToken token) throws TestRunException {
        ParseTreeNavigator navigator = new ParseTreeNavigator(this);
        navigator.visit(token);
    }

    @Override
    public void visitFeature(ParseToken token) throws TestRunException {
    }

    public void postVisit(ParseToken token) {
        unindent();
    }
    public void preVisit(ParseToken token) {
        display(token);
        indent();
    }

    private void indent() {
        this.indentCount +=1 ;
    }

    private void unindent() {
        this.indentCount -=1;
    }

    private void display(ParseToken token) {
        int indentCountDown = this.indentCount;
        while( indentCountDown > 0 ) {
            buff.append(indent);
            indentCountDown -=1;
        }
        buff.append(token);
        buff.append("\n");
    }
    
}
