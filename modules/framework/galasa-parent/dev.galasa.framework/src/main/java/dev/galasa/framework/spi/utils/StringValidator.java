/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

public class StringValidator {

    public boolean isAlphanumericWithDashesUnderscoresAndDots(String strToValidate) {
        boolean isValid = (strToValidate != null && !strToValidate.isBlank());

        if (isValid) {
            for (char charToCheck : strToValidate.toCharArray()) {
                if (!Character.isLetterOrDigit(charToCheck)
                    && charToCheck != '.'
                    && charToCheck != '-'
                    && charToCheck != '_'
                ) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    /**
     * Checks whether a given string is in valid Latin-1 format (e.g. characters in the range 0 - 255)
     * 
     * @param str the string to validate
     * @return true if the string is in valid Latin-1 format, or false otherwise
     */
    public boolean isLatin1(String str) {
        boolean isValidLatin1 = true;
        for (char i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 255) {
                isValidLatin1 = false;
                break;
            }
        }
        return isValidLatin1;
    }
}
