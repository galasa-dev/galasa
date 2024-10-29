/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

public enum Colour {
    
    DEFAULT  ((byte)0x0, 'd'),
    BLUE     ((byte)0xf1, 'b'),
    RED      ((byte)0xf2, 'r'),
    PINK     ((byte)0xf3, 'p'),
    GREEN    ((byte)0xf4, 'g'),
    TURQUOISE((byte)0xf5, 't'),
    YELLOW   ((byte)0xf6, 'y'),
    NEUTRAL  ((byte)0xf7, 'n');
    
    private final byte code;
    private final char letter;
    
    Colour(byte code, char letter) {
        this.code   = code;
        this.letter = letter;
    }
    
    public byte getCode() {
        return this.code;
    }
    
    public char getLetter() {
        return this.letter;
    }
    
    public static Colour getColour(byte code) throws DatastreamException {
        for(Colour colour : Colour.values()) {
            if (colour.code == code) {
                return colour;
            }
        }
        
        throw new DatastreamException("Unrecognised colour code - " + code);
    }

}
