/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rps;

/**
 * This interface is used for the creation and parsing of a resource string. A
 * set of complex resources strings can be defined in the notation:
 * GAL{9}{z}{2-6}{a-z0-9} with examples of the set: - GAL0a67 - GAL4h2e -
 * GAL3z44 ... with: {9} representing 0-9. Any solo number can be passed to
 * repsent 0-X. {z} representing a-z. This is case sensitive. {2-6} representing
 * the range of ints from 2-6. {a-z0-9} A example complex defintion representing
 * a-z OR 0-9. As many OR statments can be passed for a single character.
 * 
 * The character class is going to be either constant or variable. In the
 * example above the J A T P characters being constant and the other four
 * characters being variable.
 * 
 *  
 */
public interface ICharacter {

    /**
     * This method returns a random character from the range represnted in the
     * defining resoruce string. E.g. {9} could return any int from 0-9.
     * 
     * In the object the character selected is stored.
     * 
     * @return - a char, randomised.
     */
    char getRandomChar();

    /**
     * This method uses the stored character, and returns the next one along. E.g.
     * If currently 'D' this would return 'E'.
     * 
     * This new character is stored.
     * 
     * @return - next character in the order defined.
     */
    char getNextChar();

    /**
     * This method gets the stored char.
     * 
     * @return - character currently stored.
     */
    char getChar();

    /**
     * This method gets the first char possible in the definition, and stores the
     * character in the character object.
     * 
     * @return - first character in the definition. E.g {a-z0-9} would return a.
     */
    char getFirstChar();

    /**
     * This method does the same as getFirstChar, without storing the character in
     * the object.
     * 
     * @return - the first defined chracter.
     */
    char firstChar();

    /**
     * This method returns the number of combinations a character can return.
     * 
     * @return - int.
     */
    int numberOfCombinations();
}
