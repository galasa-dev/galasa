/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.common.screens;

import javax.validation.constraints.NotNull;

/**
 * Pojo to contain the onscreen contents. May contain nulls.
 * 
 *  
 *
 */
public class FieldContents {

    private final Character[] chars;
    private final String      text;

    /**
     * Constructor
     * 
     * @param chars the chars in the relevant places, may contain nulls
     */
    public FieldContents(@NotNull Character[] chars) {
        boolean containsNulls = false;
        for (Character c : chars) {
            if (c == null) {
                containsNulls = true;
                break;
            }
        }

        if (containsNulls) {
            this.chars = chars;
            this.text = null;
        } else {
            this.chars = null;

            char[] convChars = new char[chars.length];
            for (int i = 0; i < chars.length; i++) {
                convChars[i] = chars[i];
            }

            this.text = new String(convChars);
        }
    }

    /**
     * Get the field characters
     * 
     * @return the chars
     */
    public Character[] getChars() {
        if (chars == null && text != null) {
            char[] tc = text.toCharArray();
            Character[] ca = new Character[tc.length];
            for (int i = 0; i < tc.length; i++) {
                ca[i] = tc[i];
            }
            return ca;
        }

        return this.chars;
    }

}
