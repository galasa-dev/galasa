/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

// Checks if a given string contains only characters in the Latin-1 character set (codepoints 0-255),
// returning true if so, and false otherwise
func IsLatin1(str string) bool {
    isValidLatin1 := true
    for _, character := range str {
        if character > 255 {
            isValidLatin1 = false
            break
        }
    }
    return isValidLatin1
}

// IsNameValid - Checks if a given string used as a name for a structure (e.g. role, secret, group, etc.)
// only contains the following characters:
// - Alphanumeric characters (a-z, A-Z, 0-9)
// - Dashes (-)
// - Underscores (_)
func IsNameValid(name string) bool {
    isValid := true
    for _, character := range name {
        if !(IsCharacterAlphanumeric(character) ||
            (character == '-') ||
            (character == '_')) {
            isValid = false
            break
        }
    }
    return isValid
}

// IsAlphanumeric - Checks if a given string contains only alphanumeric characters
func IsAlphanumeric(str string) bool {
    isValid := true
    for _, character := range str {
        if !IsCharacterAlphanumeric(character) {
            isValid = false
            break
        }
    }
    return isValid
}

func IsCharacterAlphanumeric(character rune) bool {
    return (character >= 'a' && character <= 'z') ||
    (character >= 'A' && character <= 'Z') ||
    (character >= '0' && character <= '9')
}
