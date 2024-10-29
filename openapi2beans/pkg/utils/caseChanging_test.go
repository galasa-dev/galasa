package utils

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestPascalToCamel(t *testing.T) {
	pascalString := "PascalString"
	camelString := pascalToCamel(pascalString)
	assert.Equal(t, "pascalString", camelString)

	pascalString = "PascalString123"
	camelString = pascalToCamel(pascalString)
	assert.Equal(t, "pascalString123", camelString)

	pascalString = "P"
	camelString = pascalToCamel(pascalString)
	assert.Equal(t, "p", camelString)

	pascalString = ""
	camelString = pascalToCamel(pascalString)
	assert.Equal(t, "", camelString)

	pascalString = "9123"
	camelString = pascalToCamel(pascalString)
	assert.Equal(t, "9123", camelString)
}

func TestSnakeVariantsToCamel(t *testing.T) {
	snakeString := "snake_string"
	camelString := snakeVariantsToCamel(snakeString)
	assert.Equal(t, "snakeString", camelString)

	snakeString = "SNAKE_STRING"
	camelString = snakeVariantsToCamel(snakeString)
	assert.Equal(t, "snakeString", camelString)

	snakeString = "snake_string_123"
	camelString = snakeVariantsToCamel(snakeString)
	assert.Equal(t, "snakeString123", camelString)

	snakeString = "p"
	camelString = snakeVariantsToCamel(snakeString)
	assert.Equal(t, "p", camelString)

	snakeString = ""
	camelString = snakeVariantsToCamel(snakeString)
	assert.Equal(t, "", camelString)

	snakeString = "1234"
	camelString = snakeVariantsToCamel(snakeString)
	assert.Equal(t, "1234", camelString)
}

func TestCamelToPascal(t *testing.T) {
	camelString := "camelString"
	pascalString := camelToPascal(camelString)
	assert.Equal(t, "CamelString", pascalString)

	camelString = "camelString123"
	pascalString = camelToPascal(camelString)
	assert.Equal(t, "CamelString123", pascalString)

	camelString = "c"
	pascalString = camelToPascal(camelString)
	assert.Equal(t, "C", pascalString)

	camelString = ""
	pascalString = camelToPascal(camelString)
	assert.Equal(t, "", pascalString)

	camelString = "1234"
	pascalString = camelToPascal(camelString)
	assert.Equal(t, "1234", pascalString)
}

func TestSnakeVariantsToPascal(t *testing.T) {
	snakeString := "snake_string"
	pascalString := snakeVariantsToPascal(snakeString)
	assert.Equal(t, "SnakeString", pascalString)

	snakeString = "SNAKE_STRING"
	pascalString = snakeVariantsToPascal(snakeString)
	assert.Equal(t, "SnakeString", pascalString)

	snakeString = "snake_string_123"
	pascalString = snakeVariantsToPascal(snakeString)
	assert.Equal(t, "SnakeString123", pascalString)

	snakeString = "p"
	pascalString = snakeVariantsToPascal(snakeString)
	assert.Equal(t, "P", pascalString)

	snakeString = ""
	pascalString = snakeVariantsToPascal(snakeString)
	assert.Equal(t, "", pascalString)

	snakeString = "1234"
	pascalString = snakeVariantsToPascal(snakeString)
	assert.Equal(t, "1234", pascalString)
}

func TestCamelVariantsToSnake(t *testing.T) {
	camelString := "camelString"
	snakeString := camelVariantsToSnake(camelString)
	assert.Equal(t, "camel_string", snakeString)

	camelString = "PascalString"
	snakeString = camelVariantsToSnake(camelString)
	assert.Equal(t, "pascal_string", snakeString)

	camelString = "camelString123"
	snakeString = camelVariantsToSnake(camelString)
	assert.Equal(t, "camel_string_123", snakeString)

	camelString = "c"
	snakeString = camelVariantsToSnake(camelString)
	assert.Equal(t, "c", snakeString)

	camelString = ""
	snakeString = camelVariantsToSnake(camelString)
	assert.Equal(t, "", snakeString)

	camelString = "1234"
	snakeString = camelVariantsToSnake(camelString)
	assert.Equal(t, "1234", snakeString)
}

func TestSnakeVariantsToSnake(t *testing.T) {
	snakeString := "snake_string"
	resultingSnake := snakeVariantsToSnake(snakeString)
	assert.Equal(t, "snake_string", resultingSnake)

	snakeString = "sNaKe_StRiNg"
	resultingSnake = snakeVariantsToSnake(snakeString)
	assert.Equal(t, "snake_string", resultingSnake)

	snakeString = "SNAKE_STRING"
	resultingSnake = snakeVariantsToSnake(snakeString)
	assert.Equal(t, "snake_string", resultingSnake)

	snakeString = "p"
	resultingSnake = snakeVariantsToSnake(snakeString)
	assert.Equal(t, "p", resultingSnake)

	snakeString = ""
	resultingSnake = snakeVariantsToSnake(snakeString)
	assert.Equal(t, "", resultingSnake)

	snakeString = "1234"
	resultingSnake = snakeVariantsToSnake(snakeString)
	assert.Equal(t, "1234", resultingSnake)
}

func TestCamelVariantsToScreamingSnake(t *testing.T) {
	camelString := "camelString"
	snakeString := camelVariantsToScreamingSnake(camelString)
	assert.Equal(t, "CAMEL_STRING", snakeString)

	camelString = "PascalString"
	snakeString = camelVariantsToScreamingSnake(camelString)
	assert.Equal(t, "PASCAL_STRING", snakeString)

	camelString = "camelString123"
	snakeString = camelVariantsToScreamingSnake(camelString)
	assert.Equal(t, "CAMEL_STRING_123", snakeString)

	camelString = "c"
	snakeString = camelVariantsToScreamingSnake(camelString)
	assert.Equal(t, "C", snakeString)

	camelString = ""
	snakeString = camelVariantsToScreamingSnake(camelString)
	assert.Equal(t, "", snakeString)

	camelString = "1234"
	snakeString = camelVariantsToScreamingSnake(camelString)
	assert.Equal(t, "1234", snakeString)
}

func TestSnakeVariantsToScreamingSnake(t *testing.T) {
	snakeString := "snake_string"
	resultingSnake := snakeVariantsToScreamingSnake(snakeString)
	assert.Equal(t, "SNAKE_STRING", resultingSnake)

	snakeString = "sNaKe_StRiNg"
	resultingSnake = snakeVariantsToScreamingSnake(snakeString)
	assert.Equal(t, "SNAKE_STRING", resultingSnake)

	snakeString = "SNAKE_STRING"
	resultingSnake = snakeVariantsToScreamingSnake(snakeString)
	assert.Equal(t, "SNAKE_STRING", resultingSnake)

	snakeString = "p"
	resultingSnake = snakeVariantsToScreamingSnake(snakeString)
	assert.Equal(t, "P", resultingSnake)

	snakeString = ""
	resultingSnake = snakeVariantsToScreamingSnake(snakeString)
	assert.Equal(t, "", resultingSnake)

	snakeString = "1234"
	resultingSnake = snakeVariantsToScreamingSnake(snakeString)
	assert.Equal(t, "1234", resultingSnake)
}


func TestStringToCamel(t *testing.T) {
	// with camel
	inputCamel := "camelCaseString"
	camelString := StringToCamel(inputCamel)
	assert.Equal(t, inputCamel, camelString)

	// with pascal
	pascalString := "PascalCaseString"
	camelString = StringToCamel(pascalString)
	assert.Equal(t, "pascalCaseString", camelString)

	// with snake
	snakeString := "snake_case_string"
	camelString = StringToCamel(snakeString)
	assert.Equal(t, "snakeCaseString", camelString)

	// with screaming snake
	snakeString = "SNAKE_CASE_STRING"
	camelString = StringToCamel(snakeString)
	assert.Equal(t, "snakeCaseString", camelString)

	randomString := "random string with no case >:D"
	camelString = StringToCamel(randomString)
	assert.Equal(t, randomString, camelString)
}

func TestStringToPascal(t *testing.T) {
	camelString := "thisIsCamel"
	pascalString := StringToPascal(camelString)
	assert.Equal(t, "ThisIsCamel", pascalString)

	inputPascal := "ThisIsPascal"
	pascalString = StringToPascal(inputPascal)
	assert.Equal(t, inputPascal, pascalString)

	snakeString := "this_is_snake"
	pascalString = StringToPascal(snakeString)
	assert.Equal(t, "ThisIsSnake", pascalString)

	snakeString = "THIS_IS_SCREAMING_SNAKE"
	pascalString = StringToPascal(snakeString)
	assert.Equal(t, "ThisIsScreamingSnake", pascalString)

	randomString := "random string with no case >:D"
	pascalString = StringToPascal(randomString)
	assert.Equal(t, randomString, pascalString)
}

func TestStringToSnake(t *testing.T) {
	camelString := "thisIsCamel"
	snakeString := StringToSnake(camelString)
	assert.Equal(t, "this_is_camel", snakeString)

	pascalString := "ThisIsPascal"
	snakeString = StringToSnake(pascalString)
	assert.Equal(t, "this_is_pascal", snakeString)

	inputSnake := "this_is_snake"
	snakeString = StringToSnake(inputSnake)
	assert.Equal(t, inputSnake, snakeString)

	snakeString = "THIS_IS_SCREAMING_SNAKE"
	snakeString = StringToSnake(snakeString)
	assert.Equal(t, "this_is_screaming_snake", snakeString)

	randomString := "random string with no case >:D"
	snakeString = StringToSnake(randomString)
	assert.Equal(t, randomString, snakeString)
}

func TestStringToScreamingSnake(t *testing.T) {
	camelString := "thisIsCamel"
	screamingSnakeString := StringToScreamingSnake(camelString)
	assert.Equal(t, "THIS_IS_CAMEL", screamingSnakeString)

	pascalString := "ThisIsPascal"
	screamingSnakeString = StringToScreamingSnake(pascalString)
	assert.Equal(t, "THIS_IS_PASCAL", screamingSnakeString)

	snakeString := "this_is_snake"
	screamingSnakeString = StringToScreamingSnake(snakeString)
	assert.Equal(t, "THIS_IS_SNAKE", screamingSnakeString)

	inputScreamingSnake := "THIS_IS_SCREAMING_SNAKE"
	screamingSnakeString = StringToScreamingSnake(inputScreamingSnake)
	assert.Equal(t, inputScreamingSnake, screamingSnakeString)

	randomString := "random string with no case >:D"
	screamingSnakeString = StringToScreamingSnake(randomString)
	assert.Equal(t, randomString, screamingSnakeString)
}