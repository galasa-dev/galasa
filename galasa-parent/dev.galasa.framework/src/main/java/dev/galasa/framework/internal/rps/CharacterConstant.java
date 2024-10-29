/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rps;

import dev.galasa.framework.internal.rps.tree.SimpleNode;

/**
 * This class implements the IChacter interface and retruns the appropiate
 * responses for a constant character.
 * 
 *  
 */

public class CharacterConstant implements ICharacter {
    private char character;

    /**
     * This method takes in a node from the JJTree, and sets the values of that node
     * as the constant charcter.
     * 
     * @param child - contains the constant character we want to represent in this
     *              object.
     */
    public CharacterConstant(SimpleNode child) {
        this.character = ((String) child.jjtGetValue()).charAt(0);
    }

    /**
     * @return - returns the constant chacacter.
     */
    public char getRandomChar() {
        return character;
    }

    /**
     * @return - returns the constant chacacter.
     */
    public char getNextChar() { // NOSONAR
        return character;
    }

    /**
     * @return - returns the constant chacacter.
     */
    public char getChar() { // NOSONAR
        return character;
    }

    /**
     * @return - returns the constant chacacter.
     */
    public char getFirstChar() { // NOSONAR
        return character;
    }

    /**
     * @return - returns the constant chacacter.
     */
    public char firstChar() { // NOSONAR
        return character;
    }

    /**
     * @return - returns the constant chacacter.
     */
    public int numberOfCombinations() {
        return 1;
    }
}