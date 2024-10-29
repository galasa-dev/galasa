/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import dev.galasa.framework.internal.rps.tree.ASTConstant;
import dev.galasa.framework.internal.rps.tree.ASTMultiRange;
import dev.galasa.framework.internal.rps.tree.ASTSingleRange;
import dev.galasa.framework.internal.rps.tree.ASTVariable;
import dev.galasa.framework.internal.rps.tree.SimpleNode;

/**
 * This class implements the ICharacter interface and retruns the appropiate
 * responses for a variable character.
 * 
 *  
 */
public class CharacterVariable implements ICharacter {
    private char                 character;
    private Random               random    = new Random();

    public static final String   UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String   LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    public static final String   NUMBERS   = "0123456789";

    private ArrayList<Character> chars     = new ArrayList<>();

    /**
     * This constructor takes in a node which contains the deifnition of the
     * variable character. The called method generates the acceptable chars that
     * satisfy the defintion an store them in the chars arrayList.
     * 
     * @param child - a node containing the chacacter defintion.
     */
    public CharacterVariable(SimpleNode child) {
        generateAcceptableChars((ASTVariable) child);
    }

    /**
     * This method randomly chooses a char within the chars array list and returns
     * it.
     * 
     * @return - char, randomly selected.
     */
    public char getRandomChar() {
        character = chars.get(random.nextInt(chars.size()));
        return character;
    }

    /**
     * This method returns the character in the array list which is next.
     * 
     * @return - char, next in order defined by the defintion.
     */
    public char getNextChar() {
        int charIndex = chars.lastIndexOf(character);
        if (charIndex == (chars.size() - 1)) {
            character = chars.get(0);
            return character;
        }
        character = chars.get(charIndex + 1);
        return character;
    }

    /**
     * This method returns the current value for the character.
     * 
     * @return - char, current chracter.
     */
    public char getChar() {
        return character;
    }

    /**
     * This method sets the chracter to the first in the array list chars.
     * 
     * @return - char, first chracter.
     */
    public char getFirstChar() {
        character = chars.get(0);
        return character;
    }

    /**
     * This method returns the chracter first in the array list chars.
     * 
     * @return - char, first chracter.
     */
    public char firstChar() {
        return chars.get(0);
    }

    /**
     * This method returns the number of possible responses that can be achieved
     * from the getRadomChar().
     * 
     * @return int - number of combinations.
     */
    public int numberOfCombinations() {
        return chars.size();
    }

    /**
     * This method, called by the constructor, generates the acceptable chars
     * defined in the defintion passed in the node.
     * 
     * @param condition - a string which can be simple (9) or complex ((a-z)(0-9))
     * @return - a array list of all the possible chars for this char in the
     *         original string.
     */
    private void generateAcceptableChars(ASTVariable variable) {
        for (int i = 0; i < variable.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) variable.jjtGetChild(i);

            if (child instanceof ASTSingleRange) {
                String constant = (String) ((ASTConstant) child.jjtGetChild(0)).jjtGetValue();
                String range = getAppropriateRange(constant);
                String firstCharacter = range.substring(0, 1);
                addCharacters(range, firstCharacter, constant);
            } else if (child instanceof ASTMultiRange) {
                String constantFrom = (String) ((ASTConstant) child.jjtGetChild(0)).jjtGetValue();
                String constantTo = (String) ((ASTConstant) child.jjtGetChild(1)).jjtGetValue();
                String range = getAppropriateRange(constantFrom);
                addCharacters(range, constantFrom, constantTo);
            } else {
                throw new UnsupportedOperationException("Unrecognised node " + child.getClass().getName());
            }
        }

        Collections.sort(chars);
    }

    /**
     * This method adds characters to the chars array list. It defines the range,
     * start and end point for the character deifntion.
     * 
     * @param range         - what range we are looking at, (0-9)(a-z)(A-Z).
     * @param characterFrom - which character to start at.
     * @param characterTo   - which chracter to go up too.
     */
    private void addCharacters(String range, String characterFrom, String characterTo) {

        int fromPos = range.indexOf(characterFrom);
        int toPos = range.indexOf(characterTo);

        if (toPos < 0) {
            throw new UnsupportedOperationException(
                    "To character not from the same range as the from character " + characterFrom + "-" + characterTo);
        }

        if (toPos < fromPos) {
            throw new UnsupportedOperationException(
                    "To character in range is before from character " + characterFrom + "-" + characterTo);
        }

        for (int i = fromPos; i <= toPos; i++) {
            char c = range.charAt(i);
            if (!chars.contains(c)) {
                chars.add(c);
            }
        }
    }

    /**
     * This method deterines which range of chracters relates to the passed
     * character.
     * 
     * @param character - number or letter(case sensitive.)
     * @return - the appropiate range.
     */
    private static String getAppropriateRange(String character) {
        if (UPPERCASE.contains(character)) {
            return UPPERCASE;
        } else if (LOWERCASE.contains(character)) {
            return LOWERCASE;
        } else if (NUMBERS.contains(character)) {
            return NUMBERS;
        }
        throw new UnsupportedOperationException("Unknown character - '" + character + "'");
    }
}
