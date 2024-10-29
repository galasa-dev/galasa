/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.rps;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dev.galasa.framework.internal.rps.CharacterConstant;
import dev.galasa.framework.internal.rps.tree.ASTConstant;
import dev.galasa.framework.internal.rps.tree.ASTStart;
import dev.galasa.framework.internal.rps.tree.ParseException;
import dev.galasa.framework.internal.rps.tree.RpsResolver;
import dev.galasa.framework.internal.rps.tree.SimpleNode;

import java.io.ByteArrayInputStream;

/**
 * This test class ensures that a constant character has constant behaviour,
 * always returning the same chracter.
 * 
 *  
 */
public class CharacterConstantTest {

    /**
     * This test class ensures the same character is always returned from all
     * methods.
     * 
     * @throws ParseException
     */
    @Test
    public void testAllMethodsReturnConstant() throws ParseException {
        final String testString = "GAL";
        ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());

        RpsResolver resolve = new RpsResolver(bais);

        ASTStart node = resolve.Start();

        node.dump("   ");

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);

            if (child instanceof ASTConstant) {
                CharacterConstant consti = new CharacterConstant(child);
                assertEquals("Constant character not returned", testString.charAt(i), consti.getRandomChar());
                assertEquals("Constant character not returned", testString.charAt(i), consti.getNextChar());
                assertEquals("Constant character not returned", testString.charAt(i), consti.getFirstChar());
                assertEquals("Constant character not returned", testString.charAt(i), consti.firstChar());
                assertEquals("Constant character not returned", testString.charAt(i), consti.getChar());
                assertEquals("Constant character not returned", 1, consti.numberOfCombinations());
            } else {
                throw new UnsupportedOperationException("Unrecognised node " + child.getClass().getName());
            }
        }
    }
}
