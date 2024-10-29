package utils

import (
	"strings"
	"unicode"
)


func GetCase(inputString string) string {
	var outputCase string
	if IsCamelCase(inputString) {
		outputCase = CAMEL
	} else if IsPascalCase(inputString) {
		outputCase = PASCAL
	} else if IsSnakeVariantCase(inputString) {
		outputCase = SNAKE_VARIANT
	}
	return outputCase
}

func IsSnakeVariantCase(inputString string) bool {
	var isSnakeCase bool
	wordArray := strings.Split(inputString, "_")
	isSnakeCase = len(wordArray) > 1
	return isSnakeCase
}

func IsCamelCase(inputString string) bool {
	isCamelCase := isCamelVariant(inputString)
	if isCamelCase {
		if unicode.IsUpper(rune(inputString[0])) {
			isCamelCase = false
		}
	}
	return isCamelCase
}

func IsPascalCase(inputString string) bool {
	isPascalCase := isCamelVariant(inputString)
	if isPascalCase {
		if unicode.IsLower(rune(inputString[0])) {
			isPascalCase = false
		}
	}
	return isPascalCase
}

func isCamelVariant(inputString string) (bool) {
	isCamelVariant := false
	if !strings.ContainsAny(inputString, " ,\n_.'\"!@#£$%^&*()_-=+[]{}:;\\|`~/?<>") {
		for i, char := range inputString[1:] {
			if unicode.IsUpper(char) {
				if i == len(inputString)-2 {
					isCamelVariant = false
				} else {
					isCamelVariant = true
				}
			} else if unicode.IsNumber(char) {
				if i == len(inputString)-2 {
					isCamelVariant = true
				} else {
					isCamelVariant = false
				}
			}
		}
	}
	return isCamelVariant
}