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

// To validate the string as a valid java package name before we start to use it.
func TestValidateJavaPackageNameWellFormed(t *testing.T) {
	err := ValidateJavaPackageName("well.formed")
	assert.Nil(t, err, "Validation reported a problem when the package was valid.")
}

func TestValidateJavaPackageNameBadMiddle(t *testing.T) {
	err := ValidateJavaPackageName("badly.formed.=in}.the.middle")
	assert.NotNil(t, err, "Validation reported OK when it should be invalid.")
	assert.Contains(t, err.Error(), "GAL1037E:", "Wrong error message reported.")
	assert.Contains(t, err.Error(), "=", "Wrong character being reported as the problem")
}

func TestValidateJavaPackageNameBadFirstChar(t *testing.T) {
	err := ValidateJavaPackageName(".badly.formed.first.char")
	assert.NotNil(t, err, "Validation reported OK when it should be invalid.")
	assert.Contains(t, err.Error(), "GAL1038E:", "Wrong error message reported.")
}

func TestValidateJavaPackageNameBadLastChar(t *testing.T) {
	err := ValidateJavaPackageName("badly.formed.last.char.")
	assert.NotNil(t, err, "Validation reported OK when it should be invalid.")
	assert.Contains(t, err.Error(), "GAL1039E:", "Wrong error message reported.")
}

func TestValidateJavaPackageNameBlank(t *testing.T) {
	err := ValidateJavaPackageName("")
	assert.NotNil(t, err, "Validation reported OK when it should be invalid.")
	assert.Contains(t, err.Error(), "GAL1040E:", "Wrong error message reported.")
}

func TestValidateJavaPackageNameReservedJavaKeyword(t *testing.T) {
	err := ValidateJavaPackageName("bad.because.it.contains.this.java.keyword")
	assert.NotNil(t, err, "Validation reported OK when it should be invalid.")
	assert.Contains(t, err.Error(), "GAL1044E:", "Wrong error message reported.")
}

func TestUpperCaseFirstLetterNormalWorks(t *testing.T) {
	assert.Equal(t, "Lower", UppercaseFirstLetter("lower"), "failed to uppercase the first letter")
}

func TestUpperCaseFirstLetterSingleCharWorks(t *testing.T) {
	assert.Equal(t, "L", UppercaseFirstLetter("l"), "failed to uppercase the first letter")
}
