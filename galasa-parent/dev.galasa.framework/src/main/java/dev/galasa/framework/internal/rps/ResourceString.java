/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rps;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.internal.rps.tree.ASTConstant;
import dev.galasa.framework.internal.rps.tree.ASTStart;
import dev.galasa.framework.internal.rps.tree.ASTVariable;
import dev.galasa.framework.internal.rps.tree.ParseException;
import dev.galasa.framework.internal.rps.tree.RpsResolver;
import dev.galasa.framework.internal.rps.tree.SimpleNode;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourcePoolingServiceException;

/**
 * This class creates the resource string defintion build up of ICharacters.
 * 
 *  
 */
public class ResourceString {
    private List<ICharacter> string = new ArrayList<>();

    /**
     * This method takes the input string which defines a range of strings and
     * passes it though the JJTree parser we've created which determines if a
     * character is variable or constant.
     * 
     * @param input - string, used to define a range of strings. E.g APPLID{9}{9}{9}
     * @throws ResourcePoolingServiceException
     */
    public ResourceString(String input) throws ResourcePoolingServiceException {
        ByteArrayInputStream stream = new ByteArrayInputStream(input.getBytes());
        RpsResolver reslove = new RpsResolver(stream);
        ASTStart node;
        try {
            node = reslove.Start();
        } catch (ParseException e) {
            throw new ResourcePoolingServiceException("Problem Parsing String", e);
        }

//        node.dump("   ");

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            if (child instanceof ASTConstant) {
                string.add(new CharacterConstant(child));
            } else if (child instanceof ASTVariable) {
                string.add(new CharacterVariable(child));
            }
        }

    }

    /**
     * This method returns the first possible resource name defined in the resource
     * string defintion.
     * 
     * @return - string, for the definition: APPLID{9}{9}{9} the fist would be
     *         APPLID000
     */
    public String getFirstResource() {
        StringBuilder resource = new StringBuilder();
        for (ICharacter character : string) {
            resource.append(character.getFirstChar());
        }
        return resource.toString();
    }

    /**
     * This method returns a random resource name in the range defined.
     * 
     * @return string, for the definition: APPLID{9}{9}{9} a random resource could
     *         be APPLID359.
     */
    public String getRandomResource() {
        StringBuilder resource = new StringBuilder();
        for (ICharacter character : string) {
            resource.append(character.getRandomChar());
        }
        return resource.toString();
    }

    /**
     * This method returns the next logical string from a defintion. It will
     * increment both numbers and strings based on the defintion of the chracters in
     * the resource definition string.
     * 
     * @return string, for the previous: APPLID15z, the next resource would be
     *         APPLID16a.
     */
    public String getNextResource() throws InsufficientResourcesAvailableException {
        StringBuilder builder = new StringBuilder();
        boolean stillIncrementing = true;
        boolean overflow = false;

        for (int i = string.size() - 1; i >= 0; i--) {
            ICharacter character = string.get(i);

            if (character instanceof CharacterVariable && stillIncrementing) {
                char nextChar = character.getNextChar();
                if (nextChar != character.firstChar()) {
                    stillIncrementing = false;
                    builder.append(nextChar);
                    overflow = false;
                } else {
                    if (i != 0) {
                        overflow = true;
                    }
                    builder.append(nextChar);
                }
            } else {
                builder.append(character.getChar());
            }
        }
        builder.reverse();
        if (overflow) {
            throw new InsufficientResourcesAvailableException("Not enough resources available, hit overflow.");
        }
        return builder.toString();
    }

    /**
     * This method calculates and returns the number of combinations that a resource
     * string definition could provide.
     * 
     * @return - int, number of combinations.
     */
    public int getNumberOfCombinations() {
        int combintations = 1;
        for (ICharacter c : string) {
            if (c instanceof CharacterVariable) {
                combintations *= c.numberOfCombinations();
            }
        }
        return combintations;
    }
}