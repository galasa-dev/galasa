/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package properties

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestValidNamespaceFormatReturnsOk(t *testing.T) {
	//Given
	validNamespaceFormat := "framework"

	//When
	err := validateNamespaceFormat(validNamespaceFormat)

	//Then
	assert.Nil(t, err)
}

func TestValidNamespaceFormatWithNumbersReturnsOk(t *testing.T) {
	//Given
	validNamespaceFormat := "fra4mework5"

	//When
	err := validateNamespaceFormat(validNamespaceFormat)

	//Then
	assert.Nil(t, err)
}

func TestInvalidNamespaceFormatStartingWithNumberReturnsError(t *testing.T) {
	//Given
	invalidNamespaceFormat := "1framework"

	//When
	err := validateNamespaceFormat(invalidNamespaceFormat)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1140E")
}

func TestInvalidNamespaceFormatSingleCharReturnsError(t *testing.T) {
	//Given
	invalidNamespaceFormat := "f"

	//When
	err := validateNamespaceFormat(invalidNamespaceFormat)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1140E")
}

func TestInvalidNamespaceFormatWithSpecialCharacterReturnsError(t *testing.T) {
	//Given
	invalidNamespaceFormat := "frame-work"

	//When
	err := validateNamespaceFormat(invalidNamespaceFormat)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1140E")
}

func TestInvalidNamespaceFormatWithNumbersAndSpecialCharacterReturnsError(t *testing.T) {
	//Given
	invalidNamespaceFormat := "fr8amework-"

	//When
	err := validateNamespaceFormat(invalidNamespaceFormat)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1140E")
}

func TestInvalidNamespaceFormatWithStartingCapitalLetterReturnsError(t *testing.T) {
	//Given
	invalidNamespaceFormat := "Framework"

	//When
	err := validateNamespaceFormat(invalidNamespaceFormat)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1140E")
}

func TestInvalidNamespaceFormatWithCapitalLetterReturnsError(t *testing.T) {
	//Given
	invalidNamespaceFormat := "frameWork"

	//When
	err := validateNamespaceFormat(invalidNamespaceFormat)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1140E")
}

func TestValidPropertyFieldFormatReturnsOk(t *testing.T) {
	//Given
	fieldKey := "name"
	validPropertyFieldFormat := "test.name"

	//When
	err := validatePropertyFieldFormat(validPropertyFieldFormat, fieldKey)

	//Then
	assert.Nil(t, err)
}

func TestValidPropertyFieldFormatWithNumbersReturnsOk(t *testing.T) {
	//Given
	fieldKey := "name"
	validPropertyFieldFormat := "test.2.times"

	//When
	err := validatePropertyFieldFormat(validPropertyFieldFormat, fieldKey)

	//Then
	assert.Nil(t, err)
}

func TestValidPropertyFieldFormatWithSpecialCharacterReturnsOk(t *testing.T) {
	//Given
	fieldKey := "prefix"
	validPropertyFieldFormat := "prop-test"

	//When
	err := validatePropertyFieldFormat(validPropertyFieldFormat, fieldKey)

	//Then
	assert.Nil(t, err)
}

func TestInvalidPropertyFieldFormatStartingWithNumberReturnsError(t *testing.T) {
	//Given
	fieldKey := "prefix"
	validPropertyFieldFormat := "1.time"

	//When
	err := validatePropertyFieldFormat(validPropertyFieldFormat, fieldKey)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1142E")
}

func TestValidPropertyFieldFormatWithNumbersAndSpecialCharacterReturnsError(t *testing.T) {
	//Given
	fieldKey := "infix"
	validPropertyFieldFormat := "fr8amework-"

	//When
	err := validatePropertyFieldFormat(validPropertyFieldFormat, fieldKey)

	//Then
	assert.Nil(t, err)
}

func TestValidPropertyFieldFormatWithStartingCapitalLetterReturnsError(t *testing.T) {
	//Given
	fieldKey := "infix"
	validPropertyFieldFormat := "Framework"

	//When
	err := validatePropertyFieldFormat(validPropertyFieldFormat, fieldKey)

	//Then
	assert.Nil(t, err)
}

func TestValidPropertyFieldFormatWithCapitalLetterReturnsError(t *testing.T) {
	//Given
	fieldKey := "suffix"
	validPropertyFieldFormat := "frameWork"

	//When
	err := validatePropertyFieldFormat(validPropertyFieldFormat, fieldKey)

	//Then
	assert.Nil(t, err)
}
func TestInvalidPropertyFieldFormatSingleCharacterReturnsError(t *testing.T) {
	//Given
	fieldKey := "prefix"
	validPropertyFieldFormat := "t"

	//When
	err := validatePropertyFieldFormat(validPropertyFieldFormat, fieldKey)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1142E")
}
