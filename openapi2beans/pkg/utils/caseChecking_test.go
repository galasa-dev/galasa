package utils

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestIsSnakeVariant(t *testing.T) {
	// blank string
	snakeString := ""
	isSnake := IsSnakeVariantCase(snakeString)
	assert.False(t, isSnake)

	// regular snake
	snakeString = "this_is_snake"
	isSnake = IsSnakeVariantCase(snakeString)
	assert.True(t, isSnake)

	// screaming snake
	snakeString = "THIS_IS_SNAKE"
	isSnake = IsSnakeVariantCase(snakeString)
	assert.True(t, isSnake)

	// regular sentence
	snakeString = "this is a regular sentence"
	isSnake = IsSnakeVariantCase(snakeString)
	assert.False(t, isSnake)

	// camel case
	snakeString = "thisIsCamel"
	isSnake = IsSnakeVariantCase(snakeString)
	assert.False(t, isSnake)

	// pascal case
	snakeString = "ThisIsPascal"
	isSnake = IsSnakeVariantCase(snakeString)
	assert.False(t, isSnake)

	// with a number
	snakeString = "this_is_snake_1"
	isSnake = IsSnakeVariantCase(snakeString)
	assert.True(t, isSnake)

	// single word
	snakeString = "snake"
	isSnake = IsSnakeVariantCase(snakeString)
	assert.False(t, isSnake)

	// 2 part regular snake
	snakeString = "snake_case"
	isSnake = IsSnakeVariantCase(snakeString)
	assert.True(t, isSnake)
}

//
// IsCamelVariant tests
//

func TestIsCamelVariant(t *testing.T) {
	// regular camel case
	camelString := "camelString"
	result := isCamelVariant(camelString)
	assert.True(t, result)

	// regular pascal case
	camelString = "CamelString"
	result = isCamelVariant(camelString)
	assert.True(t, result)

	// spaced out camel
	camelString = "camel String"
	result = isCamelVariant(camelString)
	assert.False(t, result)

	// comma seperated
	camelString = "camel,String"
	result = isCamelVariant(camelString)
	assert.False(t, result)

	// full stop seperated
	camelString = "Camel.String"
	result = isCamelVariant(camelString)
	assert.False(t, result)

	// snake
	camelString = "Camel_String"
	result = isCamelVariant(camelString)
	assert.False(t, result)

	// line seperated
	camelString = `Camel
String`
	result = isCamelVariant(camelString)
	assert.False(t, result)

	// Apostrophe
	camelString = "Camel'String"
	result = isCamelVariant(camelString)
	assert.False(t, result)

	// Quotation Mark
	camelString = "Camel\"String"
	result = isCamelVariant(camelString)
	assert.False(t, result)
	// safe to say the contains any section on line 48 is working
	// so not going to test every character

	// single word
	camelString = "Camel"
	result = isCamelVariant(camelString)
	assert.False(t, result)

	// Ending in capital letter
	camelString = "CamelStrinG"
	result = isCamelVariant(camelString)
	assert.False(t, result)

	// min possible letters whilst succeeding
	camelString = "cAm"
	result = isCamelVariant(camelString)
	assert.True(t, result)

	// 1 letter
	camelString = "c"
	result = isCamelVariant(camelString)
	assert.False(t, result)
}

//
// IsCamel tests
//
func TestIsCamelCase(t *testing.T) {
	// regular camel
	camelString := "thisIsCamel"
	isCamel := IsCamelCase(camelString)
	assert.True(t, isCamel)

	// pascal
	camelString = "PascalCase"
	isCamel = IsCamelCase(camelString)
	assert.False(t, isCamel)
	
	// 2 word camel
	camelString = "camelCase"
	isCamel = IsCamelCase(camelString)
	assert.True(t, isCamel)

	// with numbers
	camelString = "camelCase101"
	isCamel = IsCamelCase(camelString)
	assert.True(t, isCamel)

	// with numbers at the start
	camelString = "101camelCase"
	isCamel = IsCamelCase(camelString)
	assert.True(t, isCamel)

	camelString = "camel1"
	isCamel = IsCamelCase(camelString)
	assert.True(t, isCamel)
}

// IsPascalTests
func TestIsPascalCase(t *testing.T) {
	// regular pascal
	pascalString := "ThisIsPascal"
	IsPascal := IsPascalCase(pascalString)
	assert.True(t, IsPascal)

	// camel
	pascalString = "camelCase"
	IsPascal = IsPascalCase(pascalString)
	assert.False(t, IsPascal)
	
	// 2 word pascal
	pascalString = "PascalCase"
	IsPascal = IsPascalCase(pascalString)
	assert.True(t, IsPascal)

	// with numbers
	pascalString = "PascalCase101"
	IsPascal = IsPascalCase(pascalString)
	assert.True(t, IsPascal)

	// with numbers at start
	pascalString = "101PascalCase"
	IsPascal = IsPascalCase(pascalString)
	assert.True(t, IsPascal)
}

func TestGetCase(t *testing.T) {
	testString := "thisIsCamel"
	stringCase := GetCase(testString)
	assert.Equal(t, CAMEL, stringCase)

	testString = "ThisIsPascal"
	stringCase = GetCase(testString)
	assert.Equal(t, PASCAL, stringCase)

	testString = "this_is_snake"
	stringCase = GetCase(testString)
	assert.Equal(t, SNAKE_VARIANT, stringCase)

	testString = "THIS_IS_SCREAMING_SNAKE"
	stringCase = GetCase(testString)
	assert.Equal(t, SNAKE_VARIANT, stringCase)
}