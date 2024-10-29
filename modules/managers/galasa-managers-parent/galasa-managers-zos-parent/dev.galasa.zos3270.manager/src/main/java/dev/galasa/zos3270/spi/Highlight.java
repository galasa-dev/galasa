/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

public enum Highlight {
    DEFAULT((byte)0x00, 'd'),
    NORMAL((byte)0xf0, 'n'),
    BLINK((byte)0xf1, 'b'),
    REVERSE((byte)0xf2, 'r'),
    UNDERSCORE((byte)0xf4, 'u');
    
    private final byte code;
    private final char letter;
    
    Highlight(byte code, char letter) {
        this.code   = code;
        this.letter = letter;
    }
    
    public byte getCode() {
        return this.code;
    }
    
    public char getLetter() {
        return this.letter;
    }
    
    public static Highlight getHighlight(byte code) throws DatastreamException {
        for(Highlight highlight : Highlight.values()) {
            if (highlight.code == code) {
                return highlight;
            }
        }
        
        throw new DatastreamException("Unrecognised highlight code - " + code);
    }

}
