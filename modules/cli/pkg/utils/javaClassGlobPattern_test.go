/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
    "testing"

    "github.com/stretchr/testify/assert"
)

func TestValidateValidJavaClassGlobPatterns(t *testing.T) {
    // Given...
    patterns := []string{"dev.galasa.*", "*", "*HelloWorld"}

    // When...
    err := ValidateJavaClassGlobPatterns(patterns)

    // Then...
    assert.Nil(t, err, "Validation reported a problem when the patterns were valid.")
}

func TestValidateInvalidJavaClassGlobPatternsThrowsError(t *testing.T) {
    // Given...
    patterns := []string{"this.is.fine", "invalid pattern with spaces", "specialCharacters@$%^&"}

    // When...
    err := ValidateJavaClassGlobPatterns(patterns)

    // Then...
    assert.NotNil(t, err, "Validation didn't report a problem when the patterns were invalid.")
    assert.ErrorContains(t, err, "Unsupported glob pattern character provided")
}

