/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.streams;

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
}
