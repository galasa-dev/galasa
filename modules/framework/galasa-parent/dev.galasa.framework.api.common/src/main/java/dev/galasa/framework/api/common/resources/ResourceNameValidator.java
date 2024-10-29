/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

/**
 * A class which can validate whether a resources use valid characters or not.
 */
public class ResourceNameValidator {

    static final String lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
    static final String capitalLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String digits = "0123456789";

    static final String namespaceValidFirstCharacters = lowercaseLetters ;
    static final String namespaceValidFollowingCharacters = namespaceValidFirstCharacters + digits ;

    static final String propertyValidFirstCharacters = lowercaseLetters + capitalLetters ;
    static final String propertySeparators = ".-_";
    static final String propertyValidFollowingCharacters = propertyValidFirstCharacters
            + digits + propertySeparators ;

    public void assertNamespaceCharPatternIsValid(String possibleNamespaceName) throws InternalServletException {


        // Guard against null or empty namespace name.
        if (possibleNamespaceName == null || possibleNamespaceName.isEmpty() ) {
            ServletError errorDetails =  new ServletError(GAL5031_EMPTY_NAMESPACE);
            throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
        }

        for(int charIndex = 0 ; charIndex < possibleNamespaceName.length() ; charIndex +=1 ) {
            int c = possibleNamespaceName.charAt(charIndex);
            if (charIndex==0) {
                // Check the first character.
                if (namespaceValidFirstCharacters.indexOf(c) < 0) {
                    ServletError errorDetails =  new ServletError(GAL5032_INVALID_FIRST_CHARACTER_NAMESPACE, possibleNamespaceName, Character.toString(c));
                    throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                // Check a following character.
                if(namespaceValidFollowingCharacters.indexOf(c) < 0 ) {
                    ServletError errorDetails = new ServletError(GAL5033_INVALID_NAMESPACE_INVALID_MIDDLE_CHAR, possibleNamespaceName, Character.toString(c));
                    throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }

    /**
     * Validate the prefix of a cps property.
     * @param prefix The prefix to validate.
     * @throws FrameworkException If the property prefix is invalid.
     */
    public void assertPropertyCharPatternPrefixIsValid(String prefix) throws InternalServletException {

        // Guard against null or empty prefix
        if (prefix == null || prefix.isEmpty() ) {
            ServletError errorDetails = new ServletError(GAL5034_INVALID_PREFIX_MISSING_OR_EMPTY);
            throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
        }

        for(int charIndex = 0 ; charIndex < prefix.length() ; charIndex +=1 ) {
            int c = prefix.charAt(charIndex);
            if (charIndex==0) {
                // Check the first character.
                if (propertyValidFirstCharacters.indexOf(c) < 0) {
                    ServletError errorDetails = new ServletError(GAL5035_INVALID_FIRST_CHAR_PROPERTY_NAME_PREFIX, prefix, Character.toString(c));
                    throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                // Check a following character.
                if(propertyValidFollowingCharacters.indexOf(c) < 0 ) {
                    ServletError errorDetails = new ServletError(GAL5036_INVALID_PROPERTY_NAME_PREFIX_INVALID_CHAR, prefix, Character.toString(c));
                    throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }

    public void assertPropertyCharPatternSuffixIsValid(String suffix) throws InternalServletException {
        // Guard against null or empty prefix
        if (suffix == null || suffix.isEmpty() ) {
            ServletError errorDetails = new ServletError(GAL5037_INVALID_PROPERTY_NAME_SUFFIX_EMPTY );
            throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
        }

        for(int charIndex = 0 ; charIndex < suffix.length() ; charIndex +=1 ) {
            int c = suffix.charAt(charIndex);
            if (charIndex==0) {
                // Check the first character.
                if (propertyValidFirstCharacters.indexOf(c) < 0) {
                    ServletError errorDetails = new ServletError(GAL5038_INVALID_PROPERTY_NAME_SUFFIX_FIRST_CHAR, suffix,  Character.toString(c));
                    throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                // Check a following character.
                if(propertyValidFollowingCharacters.indexOf(c) < 0 ) {
                    ServletError errorDetails = new ServletError(GAL5039_INVALID_PROPERTY_NAME_SUFFIX_INVALID_CHAR, suffix,  Character.toString(c));
                    throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }

    public void assertPropertyNameCharPatternIsValid(String propertyName) throws InternalServletException {
        // Guard against null or empty prefix
        if (propertyName == null || propertyName.isEmpty() ) {
            ServletError errorDetails = new ServletError(GAL5040_INVALID_PROPERTY_NAME_EMPTY);
            throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
        }

        //Make sure property name has at least one dot separator
        if (!propertyName.contains(".")){
            ServletError errorDetails = new ServletError(GAL5043_INVALID_PROPERTY_NAME_NO_DOT_SEPARATOR, propertyName);
            throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
        }

        //Make sure the property name does not end in a dot
        if (propertyName.endsWith(".")){
            ServletError error = new ServletError(GAL5044_INVALID_PROPERTY_NAME_TRAILING_DOT, propertyName);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        for(int charIndex = 0 ; charIndex < propertyName.length() ; charIndex +=1 ) {
            int c = propertyName.charAt(charIndex);
            if (charIndex==0) {
                // Check the first character.
                if (propertyValidFirstCharacters.indexOf(c) < 0) {
                    ServletError errorDetails = new ServletError(GAL5041_INVALID_PROPERTY_NAME_FIRST_CHAR, propertyName, Character.toString(c));
                    throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                // Check a following character.
                if(propertyValidFollowingCharacters.indexOf(c) < 0 ) {
                    ServletError errorDetails = new ServletError(GAL5042_INVALID_PROPERTY_NAME_INVALID_CHAR, propertyName, Character.toString(c));
                    throw new InternalServletException(errorDetails, HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
    }
}