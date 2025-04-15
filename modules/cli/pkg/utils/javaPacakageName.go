/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"strings"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

const (
	LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"
	DIGITS             = "0123456789"
	SEPARATOR          = "."
)

var (
	validStartingCharacters = LOWER_CASE_LETTERS + DIGITS
	validMiddleCharacters   = LOWER_CASE_LETTERS + DIGITS + SEPARATOR
	validLastCharacters     = LOWER_CASE_LETTERS + DIGITS

	javaReservedWords = " abstract assert boolean break byte case " +
		"catch char class continue const default do double else enum exports externds " +
		"final finally float for goto if implements import instanceof int interface long module native new " +
		"package private protected public requires return short static strictfp super switch synchronized " +
		"this throw throws transient try var void volatile while true false null "
)

func isJavaReservedWord(stringToCheck string) bool {
	wordToLookFor := " " + stringToCheck + " "
	isReserved := strings.Contains(javaReservedWords, wordToLookFor)
	return isReserved
}

// To validate the string as a valid java package name before we start to use it.
func ValidateJavaPackageName(javaPackageName string) error {
	var err error

	if javaPackageName == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_PACKAGE_NAME_BLANK)
	}

	if err == nil {
		// Check all the middle characters
		for _, c := range javaPackageName {
			charToLookFor := string(c)
			if !strings.Contains(validMiddleCharacters, charToLookFor) {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_CHAR_IN_PACKAGE_NAME, javaPackageName, charToLookFor)
				break
			}
		}
	}

	if err == nil {
		// Check the first character.
		firstChar := string(javaPackageName[0])
		if !strings.Contains(validStartingCharacters, firstChar) {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_FIRST_CHAR_IN_PKG_NAME, javaPackageName, firstChar)
		}
	}

	if err == nil {
		// Check the last character.
		length := len(javaPackageName)
		lastChar := string(javaPackageName[length-1])
		if !strings.Contains(validLastCharacters, lastChar) {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_LAST_CHAR_IN_PKG_NAME, javaPackageName, lastChar)
		}
	}

	if err == nil {
		// Check if any of the parts of the package are reserved java keywords
		for _, part := range strings.Split(javaPackageName, ".") {
			if isJavaReservedWord(part) {
				err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_INVALID_PKG_RESERVED_WORD, javaPackageName, part)
			}
		}
	}
	return err
}

// UppercaseFirstLetter - takes a string and returns the same string, but with the first letter
// turned into an uppercase letter.
func UppercaseFirstLetter(s string) string {
	firstLetter := string(s[0])
	upperCaseFirstLetter := strings.ToUpper(firstLetter)
	result := upperCaseFirstLetter + s[1:]
	return result
}
