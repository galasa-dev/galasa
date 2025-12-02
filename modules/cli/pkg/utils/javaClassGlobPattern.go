/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

// Checks if the given patterns are all valid glob patterns that are supported by Galasa.
func ValidateJavaClassGlobPatterns(patternsToValidate []string) error {
	var err error

	for _, pattern := range patternsToValidate {
		err = validateJavaClassGlobPattern(pattern)
		if err != nil {
			break
		}
	}
	return err
}

// Checks if an individual glob pattern is a valid glob pattern supported by Galasa.
//
// The following characters are currently supported:
// (a-z, A-Z, 0-9) alphanumeric characters
// '*' (wildcard) expands to zero or more characters
// '?' corresponds to exactly one character
// '.' corresponds to an actual '.' character
func validateJavaClassGlobPattern(patternToValidate string) error {
	var err error

	for _, char := range patternToValidate {
		if !IsCharacterAlphanumeric(char) &&
				char != '*' &&
				char != '?' &&
				char != '.' {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_GLOB_PATTERN_PROVIDED)
			break
		}
	}

	return err
}