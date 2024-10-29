/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import java.util.ArrayList;
import java.util.List;

public class ParseToken {
    private TokenType type ;
    private String text;
    private int lineNumber ;

    List<ParseToken> children = new ArrayList<ParseToken>();

    public ParseToken(TokenType type, String text) {
        this(type, text, 0);
    }

    public ParseToken(TokenType type, String text, int lineNumber) {
        this.type = type ;
        this.text = text ;
        this.lineNumber = lineNumber;
    }

    public TokenType getType() {
        return this.type;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append("{token:");
        buff.append(this.type.getReadableName());
        buff.append(", line:");
        buff.append(this.lineNumber);
        buff.append(", text:");
        buff.append(this.text);
        buff.append("}");

        return buff.toString();
    }

    public String getText() {
        return this.text;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public List<ParseToken> getChildren() {
        return this.children;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isSame = false ;
        if (obj != null) {
            if (ParseToken.class.isAssignableFrom(obj.getClass())) {
                ParseToken other = (ParseToken)obj;
                isSame = other.type.equals(type) 
                        && other.text.equals(text)
                        && other.lineNumber == lineNumber
                        ;
            }
        }
        return isSame ;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        for(ParseToken child : this.children) {
            result = prime * result + child.hashCode();
        }
        result = prime * result + lineNumber;
        return result;
    }

    public void addChildren(List<ParseToken> children) {
        this.children = children;
    }

    public void setLineNumber(int newLineNumber) {
        this.lineNumber = newLineNumber;
    }

    public void setText(String text) {
        this.text = text ;
    }

}
