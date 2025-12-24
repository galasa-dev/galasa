/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"unicode"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

// ValidateJavaMethodName validates that a string is a valid Java method name.
// Java method names must:
// - Start with a letter (a-z, A-Z)
// - Contain only letters, digits, underscores, or dollar signs
// - Not be a Java reserved keyword
// - Not be empty
func ValidateJavaMethodName(methodName string) error {
	var err error

	if methodName == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_METHOD_NAME_BLANK)
	}

	if err == nil {
		// Check first character - must be a letter only (not underscore or dollar sign)
		firstChar := rune(methodName[0])
		if !unicode.IsLetter(firstChar) {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_FIRST_CHAR_IN_METHOD_NAME, methodName, string(firstChar))
		}
	}

	if err == nil {
		// Check remaining characters - can include letters, digits, underscore, dollar sign
		for _, c := range methodName {
			if !unicode.IsLetter(c) && !unicode.IsDigit(c) && c != '_' && c != '$' {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_CHAR_IN_METHOD_NAME, methodName, string(c))
				break
			}
		}
	}

	if err == nil {
		// Check if the method name is a reserved Java keyword
		if isJavaReservedWord(methodName) {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_METHOD_RESERVED_WORD, methodName)
		}
	}

	return err
}
