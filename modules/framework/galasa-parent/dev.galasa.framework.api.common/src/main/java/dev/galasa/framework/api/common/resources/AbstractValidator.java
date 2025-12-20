/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import dev.galasa.framework.spi.utils.StringValidator;

/**
 * A base validator class that contains commonly-used validation methods
 */
public abstract class AbstractValidator {

    private StringValidator stringValidator = new StringValidator();

    /**
     * Checks whether a given string is in valid Latin-1 format (e.g. characters in the range 0 - 255)
     * 
     * @param str the string to validate
     * @return true if the string is in valid Latin-1 format, or false otherwise
     */
    public boolean isLatin1(String str) {
        return stringValidator.isLatin1(str);
    }

    /**
     * Checks whether a given string contains only alphanumeric characters, '-', and '_'
     * 
     * @param str the string to validate
     * @return true if the string contains only alphanumeric characters, '-', and '_', or false otherwise
     */
    public boolean isAlphanumWithDashes(String str) {
        return stringValidator.isAlphanumWithDashes(str);
    }
}
