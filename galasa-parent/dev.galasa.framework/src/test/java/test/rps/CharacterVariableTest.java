/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.rps;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import dev.galasa.framework.internal.rps.CharacterConstant;
import dev.galasa.framework.internal.rps.CharacterVariable;
import dev.galasa.framework.internal.rps.tree.ASTConstant;
import dev.galasa.framework.internal.rps.tree.ASTStart;
import dev.galasa.framework.internal.rps.tree.ASTVariable;
import dev.galasa.framework.internal.rps.tree.ParseException;
import dev.galasa.framework.internal.rps.tree.RpsResolver;
import dev.galasa.framework.internal.rps.tree.SimpleNode;

/**
 * This test class ensures the correct behaviour is shown from a variable
 * character for the given methods in ICharacter.
 * 
 *  
 */
public class CharacterVariableTest {

    public static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    public static final String NUMBERS   = "0123456789";

    /**
     * This test method checks the most simple range {9} = 0-9
     * 
     * @throws ParseException
     */
    @Test
    public void testSimpleRange() throws ParseException {
        final String testString = "{9}";
        ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());

        RpsResolver resolve = new RpsResolver(bais);

        ASTStart node = resolve.Start();

        node.dump("   ");

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);

            if (child instanceof ASTVariable) {
                CharacterVariable vari = new CharacterVariable(child);
                char value = vari.getRandomChar();
                assertTrue("Number not found in expected range", (NUMBERS.indexOf(value) != -1));
            } else {
                throw new UnsupportedOperationException("Unrecognised node " + child.getClass().getName());
            }
        }
    }

    /**
     * This test method uses two digits, both variable.
     * 
     * @throws ParseException
     */
    @Test
    public void testSimpleRangeWithTwoDigits() throws ParseException {
        final String testString = "{9}{9}";

        ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());
        StringBuilder builder = new StringBuilder();

        RpsResolver resolve = new RpsResolver(bais);

        ASTStart node = resolve.Start();

        node.dump("   ");

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);

            if (child instanceof ASTVariable) {
                CharacterVariable vari = new CharacterVariable(child);
                char value = vari.getRandomChar();
                builder.append(value);
                assertTrue("Unexpectedd number found", (NUMBERS.indexOf(value) != -1));
            } else {
                throw new UnsupportedOperationException("Unrecognised node " + child.getClass().getName());
            }
        }
        assertTrue("Too many strings found.", Integer.parseInt(builder.toString()) < 100);
    }

    /**
     * This test method does a simple letter check, a-d. It generates a sample
     * resources and checks its the correct length.
     * 
     * @throws ParseException
     */
    @Test
    public void testStringWithLetters() throws ParseException {
        final String testString = "GAL{d}";

        ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());

        RpsResolver resolve = new RpsResolver(bais);

        ASTStart node = resolve.Start();

        node.dump("   ");

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);

            if (child instanceof ASTConstant) {
                CharacterConstant consti = new CharacterConstant(child);
                char value = consti.getRandomChar();
                builder.append(value);
            } else if (child instanceof ASTVariable) {
                CharacterVariable vari = new CharacterVariable(child);
                char value = vari.getRandomChar();
                assertEquals("Incorrect char found", 'a', vari.firstChar());
                builder.append(value);
            } else {
                throw new UnsupportedOperationException("Unrecognised node " + child.getClass().getName());
            }
        }
        assertEquals("Incorrect length string built.", 4, builder.length());
    }

    /**
     * This test method checks that a range can be correctly parsed.
     * 
     * @throws ParseException
     */
    @Test
    public void testStringWithMultiRangeLetters() throws ParseException {
        final String testString = "GAL{a-d}";

        ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());

        RpsResolver resolve = new RpsResolver(bais);

        ASTStart node = resolve.Start();

        node.dump("   ");

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);

            if (child instanceof ASTConstant) {
                CharacterConstant consti = new CharacterConstant(child);
                char value = consti.getRandomChar();
                builder.append(value);
            } else if (child instanceof ASTVariable) {
                CharacterVariable vari = new CharacterVariable(child);
                char value = vari.getRandomChar();
                builder.append(value);
            } else {
                throw new UnsupportedOperationException("Unrecognised node " + child.getClass().getName());
            }
        }
        assertEquals("Incorrect length string built.", 4, builder.length());
    }

    /**
     * This test method makes sure even complex strings behave as expected.
     * 
     * @throws ParseException
     */
    @Test
    public void testStringComplexPattern() throws ParseException {
        final String testString = "GAL{a-d}{a-zG-R}{9}{H}{0-48-9a-hS-Z}{2}";

        ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());

        RpsResolver resolve = new RpsResolver(bais);

        ASTStart node = resolve.Start();

        node.dump("   ");

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);

            if (child instanceof ASTConstant) {
                CharacterConstant consti = new CharacterConstant(child);
                char value = consti.getRandomChar();
                builder.append(value);
            } else if (child instanceof ASTVariable) {
                CharacterVariable vari = new CharacterVariable(child);
                char value = vari.getRandomChar();
                builder.append(value);
            } else {
                throw new UnsupportedOperationException("Unrecognised node " + child.getClass().getName());
            }
        }
        assertEquals("Incorrect length string built.", 9, builder.length());
    }

    /**
     * This test method checks the getNextChar method works with a simple number
     * ranges.
     * 
     * @throws ParseException
     */
    @Test
    public void testGetNextChar() throws ParseException {

        final String testString = "{9}";
        ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());

        RpsResolver resolve = new RpsResolver(bais);

        ASTStart node = resolve.Start();

        node.dump("   ");

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);

            if (child instanceof ASTVariable) {
                CharacterVariable vari = new CharacterVariable(child);
                vari.getRandomChar();
                for (int k = 1; k < 10; k++) {
                    char nextChar = vari.getNextChar();
                    assertEquals("Next char incorrectly found.", NUMBERS.charAt(NUMBERS.indexOf(nextChar)), nextChar);
                }
                // assertEquals(Integer.parseUnsignedInt(Character.toString(value))+1,
                // Integer.parseUnsignedInt(Character.toString(vari.getNextChar())));
            } else {
                throw new UnsupportedOperationException("Unrecognised node " + child.getClass().getName());
            }
        }
    }

    /**
     * This test method makes sure the getNextChar method works across a more
     * complex range.
     * 
     * @throws ParseException
     */
    @Test
    public void testGetNextCharIncludinLetters() throws ParseException {
        final String acceptedCharsInOrder = "abcdefghijklmnopqrstuvwxyz0123456789";

        final String testString = "{a-z0-9}";
        ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());

        RpsResolver resolve = new RpsResolver(bais);

        ASTStart node = resolve.Start();

        node.dump("   ");

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);

            if (child instanceof ASTVariable) {
                CharacterVariable vari = new CharacterVariable(child);
                char value = vari.getRandomChar();
                int index = acceptedCharsInOrder.indexOf(value) + 1;
                for (int k = 0; k < 400; k++) {
                    if (index == acceptedCharsInOrder.length()) {
                        index = 0;
                    }
                    char nextChar = vari.getNextChar();
                    assertEquals("Incorrect char letter found", acceptedCharsInOrder.charAt(index), nextChar);
                    index++;
                }
            } else {
                throw new UnsupportedOperationException("Unrecognised node " + child.getClass().getName());
            }
        }
    }
}