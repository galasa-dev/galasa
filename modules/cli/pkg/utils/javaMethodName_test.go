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

// Test valid Java method names
func TestValidateJavaMethodNameWellFormed(t *testing.T) {
	err := ValidateJavaMethodName("testMethod")
	assert.Nil(t, err)
}

func TestValidateJavaMethodNameWithDigits(t *testing.T) {
	err := ValidateJavaMethodName("testMethod123")
	assert.Nil(t, err)
}

func TestValidateJavaMethodNameWithUnderscore(t *testing.T) {
	err := ValidateJavaMethodName("test_method")
	assert.Nil(t, err)
}

func TestValidateJavaMethodNameWithDollarSign(t *testing.T) {
	err := ValidateJavaMethodName("test$method")
	assert.Nil(t, err)
}

func TestValidateJavaMethodNameCamelCase(t *testing.T) {
	err := ValidateJavaMethodName("testMethodName")
	assert.Nil(t, err)
}

func TestValidateJavaMethodNameSingleLetter(t *testing.T) {
	err := ValidateJavaMethodName("a")
	assert.Nil(t, err)
}

func TestValidateJavaMethodNameUpperCase(t *testing.T) {
	err := ValidateJavaMethodName("TestMethod")
	assert.Nil(t, err)
}

// Test invalid Java method names - blank
func TestValidateJavaMethodNameBlank(t *testing.T) {
	err := ValidateJavaMethodName("")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1274E")
	assert.Contains(t, err.Error(), "should not be blank")
}

// Test invalid Java method names - bad first character
func TestValidateJavaMethodNameStartsWithDigit(t *testing.T) {
	err := ValidateJavaMethodName("1testMethod")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1275E")
	assert.Contains(t, err.Error(), "should start with a letter")
}

func TestValidateJavaMethodNameStartsWithUnderscore(t *testing.T) {
	err := ValidateJavaMethodName("_testMethod")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1275E")
	assert.Contains(t, err.Error(), "should start with a letter")
}

func TestValidateJavaMethodNameStartsWithDollarSign(t *testing.T) {
	err := ValidateJavaMethodName("$testMethod")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1275E")
	assert.Contains(t, err.Error(), "should start with a letter")
}

// Test invalid Java method names - bad characters
func TestValidateJavaMethodNameWithSpace(t *testing.T) {
	err := ValidateJavaMethodName("test method")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1276E")
	assert.Contains(t, err.Error(), "should not contain")
}

func TestValidateJavaMethodNameWithDash(t *testing.T) {
	err := ValidateJavaMethodName("test-method")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1276E")
	assert.Contains(t, err.Error(), "should not contain")
}

func TestValidateJavaMethodNameWithDot(t *testing.T) {
	err := ValidateJavaMethodName("test.method")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1276E")
	assert.Contains(t, err.Error(), "should not contain")
}

func TestValidateJavaMethodNameWithSpecialChars(t *testing.T) {
	err := ValidateJavaMethodName("test@method")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1276E")
	assert.Contains(t, err.Error(), "should not contain")
}

func TestValidateJavaMethodNameWithParentheses(t *testing.T) {
	err := ValidateJavaMethodName("testMethod()")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1276E")
	assert.Contains(t, err.Error(), "should not contain")
}

// Test invalid Java method names - reserved keywords
func TestValidateJavaMethodNameReservedWordClass(t *testing.T) {
	err := ValidateJavaMethodName("class")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1277E")
	assert.Contains(t, err.Error(), "reserved Java keyword")
}

func TestValidateJavaMethodNameReservedWordPublic(t *testing.T) {
	err := ValidateJavaMethodName("public")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1277E")
	assert.Contains(t, err.Error(), "reserved Java keyword")
}

func TestValidateJavaMethodNameReservedWordStatic(t *testing.T) {
	err := ValidateJavaMethodName("static")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1277E")
	assert.Contains(t, err.Error(), "reserved Java keyword")
}

func TestValidateJavaMethodNameReservedWordVoid(t *testing.T) {
	err := ValidateJavaMethodName("void")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1277E")
	assert.Contains(t, err.Error(), "reserved Java keyword")
}

func TestValidateJavaMethodNameReservedWordReturn(t *testing.T) {
	err := ValidateJavaMethodName("return")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1277E")
	assert.Contains(t, err.Error(), "reserved Java keyword")
}

func TestValidateJavaMethodNameReservedWordIf(t *testing.T) {
	err := ValidateJavaMethodName("if")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1277E")
	assert.Contains(t, err.Error(), "reserved Java keyword")
}

// Test edge cases - reserved words as part of method name (should be valid)
func TestValidateJavaMethodNameContainsReservedWord(t *testing.T) {
	err := ValidateJavaMethodName("testClassMethod")
	assert.Nil(t, err, "Method name containing 'class' as substring should be valid")
}

func TestValidateJavaMethodNameEndsWithReservedWord(t *testing.T) {
	err := ValidateJavaMethodName("getClass")
	assert.Nil(t, err, "Method name ending with 'class' should be valid")
}

